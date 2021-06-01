package com.github.tix320.kiwi.publisher.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.observable.*;

final class SimpleSubscription<I> implements Subscription {

	private final BasePublisher<I> publisher;

	private final Subscriber<? super I> realSubscriber;

	private final AtomicInteger cursor;

	private final AtomicBoolean actionInProgress;

	private final AtomicBoolean frozen;

	private volatile SubscriberUnsubscription subscriberUnsubscription;

	SimpleSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber) {
		this(publisher, realSubscriber, 0);
	}

	private SimpleSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber, int initialCursor) {
		if (initialCursor < 0) {
			throw new IllegalStateException();
		}
		this.publisher = publisher;
		this.realSubscriber = realSubscriber;
		this.cursor = new AtomicInteger(initialCursor); // TODO wtf initial cursor
		this.actionInProgress = new AtomicBoolean(false);
		this.frozen = new AtomicBoolean(false);
		this.subscriberUnsubscription = null;
	}

	public int cursor() {
		return cursor;
	}

	public CompletableFuture<Boolean> tryDoAction() {
		if (publisher.isFrozen()) {
			return;
		}

		sharedLock

		final boolean changed = actionInProgress.compareAndSet(false, true);
		if (!changed) {
			return CompletableFuture.completedFuture(false);
		}


		if (subscriberUnsubscription != null) {
			if (subscriberUnsubscription.performed()) {
				return;
			}

			final int lastItemIndex = subscriberUnsubscription.lastItemIndex();
			if (cursor > lastItemIndex) {
				actionInProgress = true;
				subscriberUnsubscription.setPerformed();
				doComplete(subscriberUnsubscription.unsubscription()).thenRunAsync(() -> {
					actionInProgress = false;
				});
				return;
			}
		}


		if (cursor == publisher.queueSize()) {
			if (publisher.isCompleted()) {
				actionInProgress = true;
				doComplete(SourceCompleted.DEFAULT).thenRunAsync(() -> actionInProgress = false);
			}
		} else {
			actionInProgress = true;
			final I value = publisher.getNthPublish(cursor++).value();
			doPublish(value).thenRunAsync(() -> {
				actionInProgress = false;
				tryDoAction();
			});
		}

	}


	// @Override
	// public boolean isCompleted() {
	// 	SubscriberCompletion subscriberCompletion = this.subscriberCompletion;
	// 	return subscriberCompletion != null && subscriberCompletion.isDone();
	// }

	public void freeze() {
		frozen.set(true);
	}

	public void unfreeze() {
		frozen.set(false);
	}

	@Override
	public void unsubscribe(Unsubscription unsubscription) {
		synchronized (publisher) {
			if (this.subscriberUnsubscription == null) {
				this.subscriberUnsubscription = new SubscriberUnsubscription(publisher.queueSize() - 1, unsubscription);

				tryDoAction();
			}
		}
	}

	@Override
	public void unsubscribeImmediately(Unsubscription unsubscription) {
		synchronized (publisher) {
			if (this.subscriberUnsubscription == null) {
				this.subscriberUnsubscription = new SubscriberUnsubscription(cursor - 1, unsubscription);

				tryDoAction();
			}
		}
	}

	private CompletableFuture<Void> doPublish(I value) {
		return BasePublisher.runAsync(() -> {
			try {
				realSubscriber.onPublish(value);
			} catch (Throwable e) {
				if (BasePublisher.ENABLE_TRACER) {
					// ExceptionUtils.appendStacktraceToThrowable(e, item.getPublisherStackTrace());
				}
				// ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}
		});
	}

	private CompletableFuture<Void> doComplete(Completion completion) {
		return BasePublisher.runAsync(() -> {
			try {
				publisher.removeSubscription(this);
				realSubscriber.onComplete(completion);
			} catch (Throwable e) {
				if (BasePublisher.ENABLE_TRACER) {
					// ExceptionUtils.appendStacktraceToThrowable(e, subscriberCompletion.getStacktrace());
				}
				// ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}
		});
	}
}
