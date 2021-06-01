package com.github.tix320.kiwi.observable;

import java.util.Optional;

import com.github.tix320.kiwi.publisher.Signal;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Subscription {

	/**
	 * Check, if this subscription completed.
	 *
	 * @return true, if completed, false otherwise.
	 */
	// boolean isCompleted();

	void freeze();

	void unfreeze();

	/**
	 * Unsubscribe from observable passing {@link Unsubscription} instance.
	 */
	void unsubscribe(Unsubscription unsubscription);

	/**
	 * Immediately unsubscribe from observable passing {@link Unsubscription} instance.
	 */
	void unsubscribeImmediately(Unsubscription unsubscription);

	/**
	 * Unsubscribe from observable.
	 */
	default void unsubscribe() {
		unsubscribe(Unsubscription.DEFAULT);
	}

	/**
	 * Immediately unsubscribe from observable.
	 */
	default void unsubscribeImmediately() {
		unsubscribeImmediately(Unsubscription.DEFAULT);
	}
}
