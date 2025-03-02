package com.github.tix320.kiwi.observable;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public abstract class Subscription {

	private boolean cancelled;

	private final Object lock = new Object();

	/**
	 * Adds the given number {@code n} of items to the current
	 * unfulfilled demand for this subscription.
	 *
	 * @param n the increment of demand; a value of {@code
	 *          Long.MAX_VALUE} may be considered as effectively unbounded
	 *
	 * @throws IllegalArgumentException If {@code n} is less than or equal to zero
	 */
	public final void request(long n) {
		if (n <= 0) {
			throw new IllegalArgumentException(String.valueOf(n));
		}

		synchronized (lock) {
			if (cancelled) {
				throw new IllegalStateException("Subscription is already cancelled");
			}

			onRequest(n);
		}
	}

	public final void requestUnbounded() {
		synchronized (lock) {
			if (cancelled) {
				throw new IllegalStateException("Subscription is already cancelled");
			}

			onUnboundRequest();
		}
	}

	/**
	 * Unsubscribe from observable passing {@link Unsubscription} instance.
	 */
	public final void cancel(Unsubscription unsubscription) {
		synchronized (lock) {
			if (cancelled) {
				return;
			}
			cancelled = true;
			onCancel(unsubscription);
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
