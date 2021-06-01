package com.github.tix320.kiwi.observable;

public abstract class Subscriber<T> {

	private volatile Subscription subscription;

	public final void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		onSubscribe();
	}

	/**
	 * This method invoked first after subscribe. Typically it will be saved for future controlling subscription.
	 * Subscription will be registered only after this method call, i.e. if it will be failed, subscription will not be registered.
	 */
	public void onSubscribe() {
		// No-op
	}

	/**
	 * Consume regular item.
	 */
	public void onPublish(T item) {
		// No-op
	}

	/**
	 * Handle subscription completeness.
	 * After calling this method, no more methods will be called.
	 *
	 * @param completion contains information about how subscription completed.
	 *
	 * @see Completion
	 */
	public void onComplete(Completion completion) {
		// No-op
	}

	public final Subscription subscription() {
		return subscription;
	}
}
