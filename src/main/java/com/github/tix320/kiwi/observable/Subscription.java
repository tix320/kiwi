package com.github.tix320.kiwi.observable;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Subscription {

	/**
	 * Adds the given number {@code n} of items to the current
	 * unfulfilled demand for this subscription.
	 *
	 * @param n the increment of demand; a value of {@code
	 *          Long.MAX_VALUE} may be considered as effectively unbounded
	 * @throws IllegalArgumentException If {@code n} is less than or equal to zero
	 */
	void request(long n);

	/**
	 * Unsubscribe from observable passing {@link Unsubscription} instance.
	 */
	void cancel(Unsubscription unsubscription);

	/**
	 * Unsubscribe from observable.
	 */
	default void cancel() {
		cancel(Unsubscription.DEFAULT);
	}
}
