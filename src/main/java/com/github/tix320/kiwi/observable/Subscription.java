package com.github.tix320.kiwi.observable;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Subscription {

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
