package com.github.tix320.kiwi.observable;

public interface Subscriber<T> {

	/**
	 * This method invoked first after subscribe. Typically it will be saved for future controlling subscription.
	 * Subscription will be registered only after this method call, i.e. if it will be failed, subscription will not be registered.
	 */
	void onSubscribe(Subscription subscription);

	/**
	 * Consume regular item.
	 */
	void onPublish(T item);

	/**
	 * Handle subscription completeness.
	 * After calling this method, no more methods will be called.
	 *
	 * @param completion contains information about how subscription completed.
	 * @see Completion
	 */
	void onComplete(Completion completion);
}
