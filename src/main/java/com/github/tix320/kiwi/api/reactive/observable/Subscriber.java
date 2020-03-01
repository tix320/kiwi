package com.github.tix320.kiwi.api.reactive.observable;

public interface Subscriber<T> {

	/**
	 * This method invoked first after subscribe. Typically it will be saved for future controlling subscription.
	 *
	 * @param subscription a new subscription
	 */
	void onSubscribe(Subscription subscription);

	/**
	 * Consume regular item.
	 *
	 * @return true if need more elements, false otherwise.
	 */
	boolean onPublish(T item);

	/**
	 * Handle published error.
	 *
	 * @return true if need more elements, false otherwise.
	 */
	boolean onError(Throwable throwable);

	/**
	 * Handle subscription completeness.
	 * After calling this method, no more methods will be called.
	 */
	void onComplete();
}
