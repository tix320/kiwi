package com.github.tix320.kiwi.publisher.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tix320.kiwi.observable.*;

final class PublisherSubscription<I> implements Subscription {

	private final BasePublisher<I> publisher;

	private final Subscriber<? super I> realSubscriber;

	private final AtomicInteger cursor;

	private final AtomicBoolean actionInProgress;

	private volatile SubscriberCompletion subscriberCompletion;

	public PublisherSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber) {
		this.publisher = publisher;
		this.realSubscriber = realSubscriber;
		this.cursor = new AtomicInteger(0);
		this.actionInProgress = new AtomicBoolean(false);
		this.subscriberCompletion = null;
	}

	public void changeCursor(int n) {
		cursor.set(n);
	}

	public int cursor() {
		return cursor.get();
	}

	public void markSourceCompleted() {
		synchronized (publisher) {
			if (this.subscriberCompletion == null) {
				this.subscriberCompletion = new SubscriberCompletion(publisher.getPublishCount() - 1,
						publisher.getCompletion());

				tryDoAction();
			}
		}

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
					subscriberCompletion.setPerformed();
					doComplete(subscriberCompletion.completion()).whenComplete((unused, unused1) -> {
						actionInProgress.set(false);
					});
				}
				else {
					final I value = publisher.getNthPublish(cursor.getAndIncrement());
					doPublish(value).whenComplete((unused, unused1) -> {
						actionInProgress.set(false);
						tryDoAction();
					});
				}
			}
		}
		else {
			if (cursor.get() < publisher.getPublishCount()) {
				final I value = publisher.getNthPublish(cursor.getAndIncrement());
				doPublish(value).whenComplete((unused, unused1) -> {
					actionInProgress.set(false);
					tryDoAction();
				});
			}
			else {
				if (publisher.isCompleted()) {
					doComplete(SourceCompleted.DEFAULT).whenComplete((unused, unused1) -> actionInProgress.set(false));
				}
				else {
					actionInProgress.set(false);
				}
			}
		}
	}

	@Override
	public void cancel(Unsubscription unsubscription) {
		synchronized (publisher) {
			if (this.subscriberCompletion == null) {
				this.subscriberCompletion = new SubscriberCompletion(publisher.getPublishCount() - 1, unsubscription);

				tryDoAction();
			}
		}
	}

	@Override
	public void cancelImmediately(Unsubscription unsubscription) {
		synchronized (publisher) {
			if (this.subscriberCompletion == null) {
				this.subscriberCompletion = new SubscriberCompletion(cursor.get() - 1, unsubscription);

				tryDoAction();
			}
		}
	}

	private CompletableFuture<Void> doPublish(I value) {
		return BasePublisher.runAsync(() -> realSubscriber.onPublish(value));
	}

	private CompletableFuture<Void> doComplete(Completion completion) {
		return BasePublisher.runAsync(() -> {
			publisher.removeSubscription(this);
			realSubscriber.onComplete(completion);
		});
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
	}

}
