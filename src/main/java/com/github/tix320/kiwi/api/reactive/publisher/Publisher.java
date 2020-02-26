package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.None;

/**
 * A producer of items (and related control messages) received by
 * Subscribers.  Each current subscriber receives the same
 * items (via method {@link #publish}).
 * Is not recommended to publish null values, because of implementation may do not support null values.
 * Use null case instance of your class, or class {@link None} if possible.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Publisher<T> {

	/**
	 * Publish object.
	 *
	 * @param object to publish
	 */
	void publish(T object);

	/**
	 * Publish array elements
	 *
	 * @param objects to publish
	 */
	void publish(T[] objects);

	/**
	 * Publish all objects from iterable
	 *
	 * @param iterable to extract objects
	 */
	void publish(Iterable<T> iterable);

	/**
	 * Publish error to subscribers.
	 *
	 * @param throwable to publish
	 *
	 * @see Observable#subscribe(Consumer, ConditionalConsumer)
	 */
	void publishError(Throwable throwable);

	/**
	 * Complete this publisher, after that cannot be published objects.
	 * Invoking this method more than one time will no effect.
	 */
	void complete();

	/**
	 * Create {@link Observable observable} to subscribe this publisher.
	 *
	 * @return created observable
	 */
	Observable<T> asObservable();

	/**
	 * Create simple publisher for publishing objects.
	 * The subscribers will receive that objects, which is published after subscription.
	 *
	 * @param <T> type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> SimplePublisher<T> simple() {
		return new SimplePublisher<>();
	}

	/**
	 * Create single publisher for publishing objects.
	 * This publisher will hold last published object.
	 * The subscribers will receive that object after subscription immediately.
	 *
	 * @param initialValue initial value of publisher
	 * @param <T>          type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> SinglePublisher<T> single(T initialValue) {
		return new SinglePublisher<>(initialValue);
	}

	/**
	 * Equivalent to {@link Publisher#single(Object)} without initial value.
	 *
	 * @param <T> type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> SinglePublisher<T> single() {
		return new SinglePublisher<>();
	}

	/**
	 * Create buffered subject for publishing objects.
	 * Any publishing for this publisher will be buffered according to given size,
	 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
	 *
	 * @param bufferSize for subject buffer
	 * @param <T>        type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> BufferPublisher<T> buffered(int bufferSize) {
		return new BufferPublisher<>(bufferSize);
	}

	/**
	 * Create cached subject for publishing objects.
	 * Any publishing for this publisher will be cached,
	 * and subscribers may subscribe and receive objects even after publishing, which will be in cache in that time.
	 *
	 * @param <T> type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> CachedPublisher<T> cached() {
		return new CachedPublisher<>();
	}
}
