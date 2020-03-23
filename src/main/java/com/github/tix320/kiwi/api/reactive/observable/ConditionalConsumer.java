package com.github.tix320.kiwi.api.reactive.observable;

/**
 * This interface is used in {@link Observable} for subscribing.
 * You can unsubscribe during the processing of each item.
 *
 * @param <T> type of data
 */
@FunctionalInterface
public interface ConditionalConsumer<T> {

	/**
	 * Consume published object, return {@code true} if need more elements, {@code false} otherwise.
	 *
	 * @param object for consume
	 *
	 * @return value, to indicate need more elements or not.
	 */
	boolean accept(T object);
}
