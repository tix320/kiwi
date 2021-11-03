package com.github.tix320.kiwi.publisher.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

final class PublisherSubscription<I> implements Subscription {

	private final BasePublisher<I> publisher;

	private final Subscriber<? super I> realSubscriber;

	private final AtomicInteger cursor;

	private final AtomicBoolean actionInProgress;

	private volatile SubscriberCompletion subscriberCompletion;

	private final AtomicLong demand = new AtomicLong(0);

	public PublisherSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber, int initialCursor) {
		this.publisher = publisher;
		this.realSubscriber = realSubscriber;
		this.cursor = new AtomicInteger(initialCursor);
		this.actionInProgress = new AtomicBoolean(true);
		this.subscriberCompletion = null;
	}

	public int cursor() {
		return cursor.get();
	}

	public void startWork() {
		this.actionInProgress.set(false);
		tryDoAction();
	}

	public void tryDoAction() {
		if (publisher.isFrozen()) {
			return;
		}

		final boolean changed = actionInProgress.compareAndSet(false, true);
		if (!changed) {
			return;
		}

		if (subscriberCompletion != null) {
			if (subscriberCompletion.performed()) {
				actionInProgress.set(false);
			}
			else {
				final int lastItemIndex = subscriberCompletion.lastItemIndex();
				if (cursor.get() > lastItemIndex) {
					doComplete(subscriberCompletion.completion()).whenComplete((unused, ex) -> {
						if (ex != null) {
							ExceptionUtils.applyToUncaughtExceptionHandler(ex);
						}

						actionInProgress.set(false);
					});
				}
				else {
					doPublish().whenComplete((published, ex) -> {
						if (ex != null) {
							ExceptionUtils.applyToUncaughtExceptionHandler(ex);
						}
						actionInProgress.set(false);

						if (published) {
							tryDoAction();
						}
					});
				}
			}
		}
		else {
			if (cursor.get() < publisher.getPublishCount()) {
				doPublish().whenComplete((published, ex) -> {
					if (ex != null) {
						ExceptionUtils.applyToUncaughtExceptionHandler(ex);
					}
					actionInProgress.set(false);

					if (published) {
						tryDoAction();
					}
				});
			}
			else {
				if (publisher.isCompleted()) {
					this.subscriberCompletion = new SubscriberCompletion(publisher.getPublishCount() - 1,
							publisher.getCompletion());
					doComplete(SourceCompletion.DEFAULT).whenComplete((unused, ex) -> {
						if (ex != null) {
							ExceptionUtils.applyToUncaughtExceptionHandler(ex);
						}

						actionInProgress.set(false);
					});
				}
				else {
					actionInProgress.set(false);
				}
			}
		}
	}

	@Override
	public void request(long n) {
		if (n <= 0) {
			throw new IllegalArgumentException(String.valueOf(n));
		}

		if (n == Long.MAX_VALUE) {
			demand.set(-1);
		}
		else {
			demand.addAndGet(n);
		}

		tryDoAction();
	}

	@Override
	public void cancel(Unsubscription unsubscription) {
		synchronized (publisher) {
			if (this.subscriberCompletion == null) {
				this.subscriberCompletion = new SubscriberCompletion(cursor.get() - 1, unsubscription);
				tryDoAction();
			}
		}
	}

	private CompletableFuture<Boolean> doPublish() {
		long dem = demand.getAndUpdate(operand -> {
			if (operand == 0 || operand == -1) {
				return operand;
			}
			else {
				return operand - 1;
			}
		});

		if (dem != 0) {
			int cursor = this.cursor.getAndIncrement();
			final I value = publisher.getNthPublish(cursor);
			return realSubscriber.getScheduler().schedule(() -> realSubscriber.publish(value)).thenApply(unused -> true);
		}
		else {
			return CompletableFuture.completedFuture(false);
		}
	}

	private CompletableFuture<Void> doComplete(Completion completion) {
		subscriberCompletion.setPerformed();

		publisher.removeSubscription(this);

		return realSubscriber.getScheduler().schedule(() -> realSubscriber.complete(completion));
	}

	private static final class SubscriberCompletion {

		private final int lastItemIndex;

		private final Completion completion;

		private volatile boolean performed;

		SubscriberCompletion(int lastItemIndex, Completion completion) {
			this.lastItemIndex = lastItemIndex;
			this.completion = completion;
			this.performed = false;
		}

		public int lastItemIndex() {
			return lastItemIndex;
		}

		public Completion completion() {
			return completion;
		}

		public boolean performed() {
			return performed;
		}

		public void setPerformed() {
			this.performed = true;
		}

		@Override
		public String toString() {
			return "SubscriberCompletion{" + "lastItemIndex=" + lastItemIndex + ", completion=" + completion + ", performed=" + performed + '}';
		}
	}

}
