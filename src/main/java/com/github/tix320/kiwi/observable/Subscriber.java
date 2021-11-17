package com.github.tix320.kiwi.observable;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.github.tix320.kiwi.observable.signal.SignalManager;
import com.github.tix320.skimp.api.exception.ExceptionUtils;
import com.github.tix320.skimp.api.thread.tracer.Tracer;

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

	public final SignalManager getSignalManager() {
		return signalManager;
	}

	public final void setSubscription(Subscription subscription) throws UserCallbackException {
		changeActionInProgress(0, 1);
		boolean changed = subscriptionUpdater.compareAndSet(this, INITIAL_STATE, subscription);
		if (!changed) {
			throw new SubscriberIllegalStateException("Subscription already set");
		}

		try {
			onSubscribe(subscription);
		}
		catch (UserCallbackException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new UserCallbackException(e);
		}
		finally {
			changeActionInProgress(1, 0);
		}
	}

	public final Subscription subscription() {
		return this.subscription;
	}

	public final void publish(T item) throws UserCallbackException {
		changeActionInProgress(0, 1);
		Subscription subscription = this.subscription;
		assert subscription != INITIAL_STATE && subscription != COMPLETED_STATE;

		try {
			onNext(item);
		}
		catch (UserCallbackException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new UserCallbackException(e);
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
		catch (Throwable e) {
			ExceptionUtils.applyToUncaughtExceptionHandler(new CompleteHandlerException(e));
		}
		finally {
			changeActionInProgress(1, 0);
		}
	}

	public final void completeWithError(Throwable error) {
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

		Tracer.INSTANCE.injectFullStacktrace(error);
		try {
			onError(error);
		}
		catch (Throwable e) {
			ExceptionUtils.applyToUncaughtExceptionHandler(new ErrorHandlerException(e));
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
	 * Handle subscription error.
	 * After calling this method, no more methods will be called.
	 *
	 * @param error contains information about error.
	 */
	protected abstract void onError(Throwable error);

	/**
	 * Handle subscription completeness.
	 * After calling this method, no more methods will be called.
	 *
	 * @param completion contains information about how subscription completed.
	 * @see Completion
	 */
	protected abstract void onComplete(Completion completion);

	public static final class UserCallbackException extends RuntimeException {
		private UserCallbackException(Throwable cause) {
			super(cause);
		}
	}

	private void changeActionInProgress(int expected, int update) {
		boolean changed = actionInProgressUpdater.compareAndSet(this, expected, update);
		if (!changed) {
			throw new SubscriberIllegalStateException(
					"Not serially signal, tried change is: %s -> %s".formatted(expected, update));
		}
	}

	private static final class InitialSubscriptionImpl extends Subscription {

		@Override
		protected void onRequest(long count) {
			throw new SubscriberIllegalStateException("Not subscribed yet");
		}

		@Override
		protected void onUnboundRequest() {
			throw new SubscriberIllegalStateException("Not subscribed yet");
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {
			throw new SubscriberIllegalStateException("Not subscribed yet");
		}
	}

	private static final class CompletedSubscriptionImpl extends Subscription {


		@Override
		protected void onRequest(long count) {
			throw new SubscriberIllegalStateException("Subscription already completed");
		}

		@Override
		protected void onUnboundRequest() {
			throw new SubscriberIllegalStateException("Subscription already completed");
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {

		}
	}

	private static final class SubscriberIllegalStateException extends IllegalStateException {
		public SubscriberIllegalStateException(String s) {
			super(s);
		}
	}

	private static final class ErrorHandlerException extends RuntimeException {
		public ErrorHandlerException(Throwable cause) {
			super(cause);
		}
	}

	private static final class CompleteHandlerException extends RuntimeException {
		public CompleteHandlerException(Throwable cause) {
			super(cause);
		}
	}
}
