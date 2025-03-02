package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.ObservableCandidate;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.skimp.api.object.None;

/**
 * A producer of items (and related control messages) received by subscribers.
 * Each current subscriber receives the same items (via method {@link #publish}).
 * Is not recommended to publish null values, because of implementation may do not support null values.
 * Use null case instance of your class, or class {@link None} if possible.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public abstract class Publisher<T> implements ObservableCandidate<T> {

	/**
	 * Publish object.
	 *
	 * @param object to publish
	 * @throws PublisherClosedException if publisher already completed
	 */
	public abstract void publish(T object);

	/**
	 * Complete this publisher, after that cannot be published objects.
	 * Invoking this method more than one time will no effect.
	 */
	public abstract void complete(SourceCompletion sourceCompletion);

	public final void complete() {
		complete(SourceCompletion.DEFAULT);
	}

	/**
	 * Abort this publisher, after that cannot be published objects.
	 * Invoking this method more than one time will have no effect.
	 */
	public abstract void abort(Throwable throwable);

	/**
	 * Indicated publisher completeness.
	 *
	 * @return true, if completed, false otherwise.
	 */
	public abstract boolean isClosed();

	/**
	 * Create simple publisher for publishing objects.
	 * The subscribers will receive that objects, which is published after subscription.
	 *
	 * @param <T> type of objects.
	 * @return created publisher.
	 */
	public static <T> SimplePublisher<T> simple() {
		return new SimplePublisher<>();
	}

	/**
	 * Create single publisher for publishing objects.
	 * This publisher will hold last published object.
	 * The subscribers will receive that object after subscription immediately.
	 *
	 * @param initialValue initial value of publisher
	 * @param <T>          type of objects.
	 * @return created publisher.
	 */
	public static <T> SinglePublisher<T> single(T initialValue) {
		return new SinglePublisher<>(initialValue);
	}

	/**
	 * Equivalent to {@link Publisher#single(Object)} without initial value.
	 *
	 * @param <T> type of objects.
	 * @return created publisher.
	 */
	public static <T> SinglePublisher<T> single() {
		return new SinglePublisher<>();
	}

	/**
	 * Create mono publisher {@link MonoPublisher} for publishing exactly one object.
	 *
	 * @param <T> type of objects.
	 * @return created publisher.
	 */
	public static <T> MonoPublisher<T> mono() {
		return new MonoPublisher<>();
	}

	/**
	 * Create buffered publisher {@link ReplayPublisher} with given limit.
	 *
	 * @param bufferSize for subject buffer
	 * @param <T>        type of objects.
	 * @return created publisher.
	 */
	public static <T> ReplayPublisher<T> buffered(int bufferSize) {
		return new ReplayPublisher<>(bufferSize);
	}

	/**
	 * Create buffered publisher {@link ReplayPublisher} without limit.
	 *
	 * @param <T> type of objects.
	 * @return created publisher.
	 */
	public static <T> ReplayPublisher<T> buffered() {
		return new ReplayPublisher<>(Integer.MAX_VALUE);
	}
}
