package com.github.tix320.kiwi.observable;

import com.github.tix320.kiwi.observable.signal.SignalSynchronizer;
import com.github.tix320.kiwi.observable.signal.SignalSynchronizer.Token;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class Subscriber<T> {

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<Subscriber, Subscription> SUB_HANDLE =
		AtomicReferenceFieldUpdater.newUpdater(Subscriber.class, Subscription.class, "_subscription");

	@SuppressWarnings("rawtypes")
	private static final AtomicIntegerFieldUpdater<Subscriber> CONCURRENCY_FLAG_HANDLE =
		AtomicIntegerFieldUpdater.newUpdater(Subscriber.class, "_concurrencyFlag");

	@SuppressWarnings("rawtypes")
	private static final AtomicIntegerFieldUpdater<Subscriber> TOKEN_STATE_HANDLE =
		AtomicIntegerFieldUpdater.newUpdater(Subscriber.class, "_tokenState");

	private static final Subscription INITIAL_STATE = new InitialSubscriptionImpl();
	private volatile Subscription _subscription = INITIAL_STATE;

	private static final int NEUTRAL = 0;
	private static final int TOKEN_CREATED = 1;
	private static final int FORKED = 2;
	private static final int SPLIT = 3;
	private volatile int _tokenState = NEUTRAL;

	private static final int IDLE = 0;
	private static final int SET_SUB = 1;
	private static final int PUBLISH = 2;
	private static final int COMPLETE = 3;
	private static final int ERROR = 4;
	private volatile int _concurrencyFlag = IDLE;

	private final SignalSynchronizer signalSynchronizer;

	private Subscriber(SignalSynchronizer signalSynchronizer) {
		this.signalSynchronizer = signalSynchronizer;
	}

	protected Subscriber() {
		this(new SignalSynchronizer());
	}

	public final <N> Subscriber<N> fork(MinorSubscriber<? super N, ? extends T> minorSubscriber) {
		TOKEN_STATE_HANDLE.updateAndGet(this, value -> {
			if (value == NEUTRAL) {
				return FORKED;
			} else {
				throw new IllegalStateException("Subscriber must be neutral state to be forked");
			}
		});

		return convertMinorSubscriber(minorSubscriber);
	}

	public final <N> Subscriber<N> spawn(MinorSubscriber<? super N, ? extends T> minorSubscriber) {
		var prevState = TOKEN_STATE_HANDLE.getAndUpdate(this, value -> {
			if (value == NEUTRAL || value == SPLIT) {
				return SPLIT;
			} else {
				throw new IllegalStateException("Subscriber must be neutral or split state to spawn new one");
			}
		});
		if (prevState == SPLIT) {
			signalSynchronizer.addNewTokenSpace();
		}

		return convertMinorSubscriber(minorSubscriber);
	}

	public final Token createToken() {
		TOKEN_STATE_HANDLE.updateAndGet(this, value -> {
			if (value == NEUTRAL) {
				return TOKEN_CREATED;
			} else {
				throw new IllegalStateException("Subscriber must be neutral state to create a token");
			}
		});

		return signalSynchronizer.createToken(this);
	}

	public final void setSubscription(Subscription sub) throws SubscriberCallbackException {
		setConcurrencyFlag(SET_SUB);
		try {
			boolean changed = SUB_HANDLE.compareAndSet(this, INITIAL_STATE, sub);
			if (!changed) {
				throw new SubscriberIllegalStateException("Subscription already set");
			}

			try {
				onSubscribe(_subscription);
			} catch (SubscriberCallbackException | SubscriberIllegalStateException e) {
				throw e;
			} catch (Throwable e) {
				throw new SubscriberCallbackException(e);
			}

		} finally {
			clearConcurrencyFlag();
		}
	}

	public final void publish(T item) throws SubscriberCallbackException {
		setConcurrencyFlag(PUBLISH);
		try {
			Subscription subscription = this._subscription;
			if (subscription == INITIAL_STATE || subscription instanceof CompletedSubscriptionImpl) {
				throw new SubscriberIllegalStateException("Subscription not set or already completed");
			}

			try {
				onNext(item);
			} catch (SubscriberCallbackException | SubscriberIllegalStateException e) {
				throw e;
			} catch (Throwable e) {
				throw new SubscriberCallbackException(e);
			}

		} finally {
			clearConcurrencyFlag();
		}
	}

	public final void complete(Completion completion) {
		setConcurrencyFlag(COMPLETE);
		try {
			SUB_HANDLE.updateAndGet(this, s -> {
				if (s == INITIAL_STATE) {
					throw new SubscriberIllegalStateException(
						"Cannot complete subscriber, because it does not subscribed yet.");
				} else if (s instanceof CompletedSubscriptionImpl completedSubscription) {
					throw new SubscriberIllegalStateException(completedSubscription.completion, completion);
				}

				return new CompletedSubscriptionImpl(completion);
			});

			try {
				onComplete(completion);
			} catch (SubscriberCallbackException | SubscriberIllegalStateException e) {
				throw e;
			} catch (Throwable e) {
				throw new SubscriberCallbackException(e);
			}

		} finally {
			clearConcurrencyFlag();
		}
	}

	// public final void completeWithError(Throwable error) {
	// 	setConcurrencyFlag();
	// 	try {
	// 		subscription.updateAndGet(this, s -> {
	// 			if (s == INITIAL_STATE) {
	// 				throw new SubscriberIllegalStateException(
	// 					"Cannot complete subscriber, because it does not subscribed yet.");
	// 			} else if (s == COMPLETED_STATE) {
	// 				throw new SubscriberIllegalStateException("Subscriber already completed");
	// 			}
	//
	// 			return COMPLETED_STATE;
	// 		});
	//
	// 		try {
	// 			onError(error);
	// 		} catch (Throwable e) {
	// 			ExceptionUtils.applyToUncaughtExceptionHandler(new ErrorHandlerException(e));
	// 		}
	// 	} finally {
	// 		clearConcurrencyFlag();
	// 	}
	// }

	public final Subscription subscription() {
		return this._subscription;
	}

	/**
	 * This method invoked first after subscribe.
	 * Subscription will be registered only after this method call, i.e. if it will be failed, subscription will not be
	 * registered.
	 */
	protected abstract void onSubscribe(Subscription subscription);

	/**
	 * Consume regular item.
	 */
	protected abstract void onNext(T item);

	// /**
	//  * Handle subscription error.
	//  * After calling this method, no more methods will be called.
	//  *
	//  * @param error contains information about error.
	//  */
	// protected abstract void onError(Throwable error);

	/**
	 * Handle subscription completeness.
	 * After calling this method, no more methods will be called.
	 *
	 * @param completion contains information about how subscription completed.
	 *
	 * @see Completion
	 */
	protected abstract void onComplete(Completion completion);

	private void setConcurrencyFlag(int flag) {
		int prevFlag = CONCURRENCY_FLAG_HANDLE.getAndAccumulate(
			this, flag, (currentFlag, newFlag) -> currentFlag == IDLE ? newFlag : currentFlag);

		if (prevFlag != IDLE) {
			throw new SubscriberIllegalStateException(
				"""
					Concurrent execution in Subscriber class is detected. \
					Trying to do operation [%s] when [%s] is in process. \
					Object: %s""".formatted(flag, prevFlag, this));
		}
	}

	private void clearConcurrencyFlag() {
		CONCURRENCY_FLAG_HANDLE.set(this, IDLE);
	}

	private <N> Subscriber<N> convertMinorSubscriber(MinorSubscriber<? super N, ? extends T> minorSubscriber) {
		var subscriber = new Subscriber<N>(signalSynchronizer) {
			@Override
			protected void onSubscribe(Subscription subscription) {
				minorSubscriber.onSubscribe(subscription);
			}

			@Override
			protected void onNext(N item) {
				minorSubscriber.onNext(item);
			}

			@Override
			protected void onComplete(Completion completion) {
				minorSubscriber.onComplete(completion);
			}
		};
		minorSubscriber.setDelegate(subscriber);
		minorSubscriber.setParent(this);
		return subscriber;
	}

	public static final class SubscriberCallbackException extends RuntimeException {

		private SubscriberCallbackException(Throwable cause) {
			super(cause);
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

		private final Completion completion;

		private CompletedSubscriptionImpl(Completion completion) {
			this.completion = completion;
		}

		@Override
		protected void onRequest(long count) {
			throw new SubscriberIllegalStateException(completion);
		}

		@Override
		protected void onUnboundRequest() {
			throw new SubscriberIllegalStateException(completion);
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {

		}

	}

	private static final class SubscriberIllegalStateException extends IllegalStateException {

		public SubscriberIllegalStateException(String s) {
			super(s);
		}

		public SubscriberIllegalStateException(Completion existingCompletion) {
			super("Subscriber already completed with %s. New items request is prohibited"
					  .formatted(existingCompletion));
		}

		public SubscriberIllegalStateException(Completion existingCompletion, Completion newCompletion) {
			super("Subscriber already completed with %s. New completion with %s is prohibited"
					  .formatted(existingCompletion, newCompletion));
		}

	}

}
