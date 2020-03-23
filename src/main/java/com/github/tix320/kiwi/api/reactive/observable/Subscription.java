package com.github.tix320.kiwi.api.reactive.observable;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Subscription {

	/**
	 * Check, if this subscription completed.
	 *
	 * @return true, if completed, false otherwise.
	 */
	boolean isCompleted();

	/**
	 * Unsubscribe from observable.
	 */
	void unsubscribe();
}
