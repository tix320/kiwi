package com.github.tix320.kiwi.observable;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.github.tix320.kiwi.observable.signal.SignalManager;

public abstract class Subscriber<T> {

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<Subscriber, Subscription> subscriptionUpdater = AtomicReferenceFieldUpdater.newUpdater(
			Subscriber.class, Subscription.class, "subscription");

	@SuppressWarnings("rawtypes")
	private static final AtomicIntegerFieldUpdater<Subscriber> actionInProgressUpdater = AtomicIntegerFieldUpdater.newUpdater(
			Subscriber.class, "actionInProgress");

	private static final Subscription INITIAL_STATE = new InitialSubscriptionImpl();
	private static final Subscription COMPLETED_STATE = new CompletedSubscriptionImpl();

	private final SignalManager signalManager;

	private volatile Subscription subscription = INITIAL_STATE;

	/**
	 * For validation, when assertion is enabled
	 */
	private volatile int actionInProgress = 0;

	protected Subscriber(SignalManager signalManager) {
		this.signalManager = signalManager;
	}

	public SignalManager getSignalManager() {
		return signalManager;
	}

	public final void setSubscription(Subscription subscription) {
		changeActionInProgress(0, 1);
		boolean changed = subscriptionUpdater.compareAndSet(this, INITIAL_STATE, subscription);
		if (!changed) {
			throw new SubscriberIllegalStateException("Subscription already set");
		}

		try {
			onSubscribe(subscription);
		}
		finally {
			changeActionInProgress(1, 0);
		}
	}

	public final Subscription subscription() {
		return this.subscription;
	}

	public final void publish(T item) {
		changeActionInProgress(0, 1);
		Subscription subscription = this.subscription;
		assert subscription != INITIAL_STATE && subscription != COMPLETED_STATE;

		try {
			onNext(item);
		}
		finally {
			changeActionInProgress(1, 0);
		}
	}

	public final void complete(Completion completion) {
		changeActionInProgress(0, 1);
		subscriptionUpdater.updateAndGet(this, s -> {
			if (s == INITIAL_STATE) {
				throw new SubscriberIllegalStateException(
						"Cannot complete subscriber, because it does not subscribed yet.");
			}
			else if (s == COMPLETED_STATE) {
				throw new SubscriberIllegalStateException("Subscriber already completed");
			}

			return COMPLETED_STATE;
		});

		try {
			onComplete(completion);
		}
		finally {
			changeActionInProgress(1, 0);
		}
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

	private void changeActionInProgress(int expected, int update) {
		boolean changed = actionInProgressUpdater.compareAndSet(this, expected, update);
		if (!changed) {
			throw new SubscriberIllegalStateException(
					"Not serially signal, tried change is: %s -> %s".formatted(expected, update));
		}
	}

	private static final class InitialSubscriptionImpl implements Subscription {

		@Override
		public void request(long n) {
			throw new SubscriberIllegalStateException("Not subscribed yet");
		}

		@Override
		public void cancel(Unsubscription unsubscription) {
			throw new SubscriberIllegalStateException("Not subscribed yet");
		}

		@Override
		public void cancel() {
			cancel(null);
		}
	}

	private static final class CompletedSubscriptionImpl implements Subscription {

		@Override
		public void request(long n) {
			throw new SubscriberIllegalStateException("Subscription already completed");
		}

		@Override
		public void cancel(Unsubscription unsubscription) {

		}

		@Override
		public void cancel() {
		}
	}

	private static final class SubscriberIllegalStateException extends IllegalStateException {
		public SubscriberIllegalStateException(String s) {
			super(s);
		}
	}
}
