package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.ObservableCandidate;
import com.github.tix320.skimp.api.object.None;

/**
 * A producer of items (and related control messages) received by subscribers.
 * Each current subscriber receives the same items (via method {@link #publish}).
 * Is not recommended to publish null values, because of implementation may do not support null values.
 * Use null case instance of your class, or class {@link None} if possible.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Publisher<T> extends ObservableCandidate<T> {

	/**
	 * Publish object.
	 *
	 * @param object to publish
	 *
	 * @throws PublisherCompletedException if publisher already completed
	 */
	void publish(T object);

	/**
	 * Complete this publisher, after that cannot be published objects.
	 * Invoking this method more than one time will no effect.
	 */
	void complete();

	/**
	 * Indicated publisher completeness.
	 *
	 * @return true, if completed, false otherwise.
	 */
	boolean isCompleted();

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
	 * Create mono publisher {@link MonoPublisher} for publishing exactly one object.
	 *
	 * @param <T> type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> MonoPublisher<T> mono() {
		return new MonoPublisher<>();
	}

	/**
	 * Create buffered publisher {@link BufferedPublisher} with given limit.
	 *
	 * @param bufferSize for subject buffer
	 * @param <T>        type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> BufferedPublisher<T> buffered(int bufferSize) {
		return new BufferedPublisher<>(bufferSize);
	}

	/**
	 * Create buffered publisher {@link BufferedPublisher} without limit.
	 *
	 * @param <T> type of objects.
	 *
	 * @return created publisher.
	 */
	static <T> BufferedPublisher<T> buffered() {
		return new UnlimitBufferedPublisher<>();
	}
}
