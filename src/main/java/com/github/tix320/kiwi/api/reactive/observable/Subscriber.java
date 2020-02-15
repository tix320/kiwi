package com.github.tix320.kiwi.api.reactive.observable;

public interface Subscriber<T> {

	/**
	 * Consume regular item.
	 *
	 * @return true if need more elements, false otherwise.
	 */
	boolean consume(T item);

	/**
	 * Handle published error.
	 *
	 * @return true if need more elements, false otherwise.
	 */
	boolean onError(Throwable throwable);

	/**
	 * Add handler on completion of observable.
	 * If observable already completed, then handler be processed immediately.
	 */
	void onComplete();
}
