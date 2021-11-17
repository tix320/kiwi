package com.github.tix320.kiwi.observable;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public abstract class Subscription {

	private static final AtomicIntegerFieldUpdater<Subscription> cancelStateUpdater = AtomicIntegerFieldUpdater.newUpdater(
			Subscription.class, "cancelState");

	private static final int INITIAL_STATE = 0;
	private static final int CANCELED_STATE = 1;

	private volatile int cancelState;

	private final Object requestLock = new Object();

	/**
	 * Adds the given number {@code n} of items to the current
	 * unfulfilled demand for this subscription.
	 *
	 * @param n the increment of demand; a value of {@code
	 *          Long.MAX_VALUE} may be considered as effectively unbounded
	 * @throws IllegalArgumentException If {@code n} is less than or equal to zero
	 */
	public final void request(long n) {
		if (n <= 0) {
			throw new IllegalArgumentException(String.valueOf(n));
		}

		if (n == Long.MAX_VALUE) {
			requestUnbounded();
		}
		else {
			synchronized (requestLock) {
				onRequest(n);
			}
		}
	}

	/**
	 * Unsubscribe from observable passing {@link Unsubscription} instance.
	 */
	public final void cancel(Unsubscription unsubscription) {
		boolean changed = cancelStateUpdater.compareAndSet(this, INITIAL_STATE, CANCELED_STATE);
		if (changed) {
			onCancel(unsubscription);
		}
	}

	public final void requestUnbounded() {
		synchronized (requestLock) {
			onUnboundRequest();
		}
	}

	/**
	 * Unsubscribe from observable.
	 */
	public final void cancel() {
		cancel(Unsubscription.DEFAULT);
	}

	protected abstract void onRequest(long count);

	protected abstract void onUnboundRequest();

	protected abstract void onCancel(Unsubscription unsubscription);
}
