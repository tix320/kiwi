package com.github.tix320.kiwi.observable;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class Subscriber<T> {

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<Subscriber, Subscription> subscriptionUpdater = AtomicReferenceFieldUpdater.newUpdater(
			Subscriber.class, Subscription.class, "subscription");

	private static final Subscription INITIAL_STATE = new InitialSubscriptionImpl();
	private static final Subscription COMPLETED_STATE = new CompletedSubscriptionImpl();

	private volatile Subscription subscription = INITIAL_STATE;

	public final void setSubscription(Subscription subscription) {
		boolean changed = subscriptionUpdater.compareAndSet(this, INITIAL_STATE, subscription);
		if (!changed) {
			throw new IllegalStateException("Subscription already set");
		}

		onSubscribe(subscription);
	}

	public final Subscription subscription() {
		return this.subscription;
	}

	public final void publish(T item) {
		Subscription subscription = this.subscription;
		assert subscription != INITIAL_STATE && subscription != COMPLETED_STATE;
		onNext(item);
	}

	public final void complete(Completion completion) {
		subscriptionUpdater.updateAndGet(this, s -> {
			if (s == INITIAL_STATE) {
				throw new IllegalStateException("Cannot complete subscriber, because it does not subscribed yet.");
			}
			else if (s == COMPLETED_STATE) {
				throw new IllegalStateException("Subscriber already completed");
			}

			return COMPLETED_STATE;
		});

		onComplete(completion);
	}

	/**
	 * This method invoked first after subscribe.
	 * Subscription will be registered only after this method call, i.e. if it will be failed, subscription will not be registered.
	 */
	protected abstract void onSubscribe(Subscription subscription);

	/**
	 * Consume regular item.
	 */
	protected abstract void onNext(T item);

	/**
	 * Handle subscription completeness.
	 * After calling this method, no more methods will be called.
	 *
	 * @param completion contains information about how subscription completed.
	 * @see Completion
	 */
	protected abstract void onComplete(Completion completion);

	private static final class InitialSubscriptionImpl implements Subscription {

		@Override
		public void request(long n) {
			throw new IllegalStateException("Not subscribed yet");
		}

		@Override
		public void cancel(Unsubscription unsubscription) {
			throw new IllegalStateException("Not subscribed yet");
		}

		@Override
		public void cancel() {
			cancel(null);
		}
	}

	private static final class CompletedSubscriptionImpl implements Subscription {

		@Override
		public void request(long n) {
			throw new IllegalStateException("Subscription already completed");
		}

		@Override
		public void cancel(Unsubscription unsubscription) {

		}

		@Override
		public void cancel() {
		}
	}
}
