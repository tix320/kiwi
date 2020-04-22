package com.github.tix320.kiwi.api.reactive.observable;

import com.github.tix320.kiwi.api.reactive.publisher.Publisher;

public interface Subscriber<T> {

	/**
	 * This method invoked first after subscribe. Typically it will be saved for future controlling subscription.
	 * Please avoid doing any operation in this method other than saving the subscription.
	 * Otherwise this can lead to an undefined behaviour in publisher and subscribers.
	 * Subscription will be registered only after this method call, i.e. if it will be failed, subscription will not be registered.
	 *
	 * @param subscription a new subscription
	 *
	 * @return true if need register, false otherwise
	 */
	boolean onSubscribe(Subscription subscription);

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
	 *
	 * @param completionType indicates, that how subscription is completed, via {@link Subscription#unsubscribe()} or {@link Publisher#complete()}.
	 */
	void onComplete(CompletionType completionType);

	static <T> SubscriberBuilder<T> builder() {
		return new SubscriberBuilder<>();
	}
}
