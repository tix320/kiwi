package com.github.tix320.kiwi.api.observable.subject;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.internal.observable.subject.BufferSubject;
import com.github.tix320.kiwi.internal.observable.subject.SimpleSubject;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Subject<T> {

	/**
	 * Publish object.
	 *
	 * @param object to publish
	 */
	void next(T object);

	/**
	 * Publish array elements
	 *
	 * @param objects to publish
	 */
	void next(T[] objects);

	/**
	 * Publish all objects from iterable
	 *
	 * @param iterable to extract objects
	 */
	void next(Iterable<T> iterable);

	/**
	 * Complete this subject, after that cannot be published objects.
	 */
	void complete();

	/**
	 * Create {@link Observable observable} of this subject.
	 *
	 * @return created observable
	 */
	Observable<T> asObservable();

	/**
	 * Create simple subject for publishing objects.
	 * The subscribers will receive objects, only if they subscribed to this subject before publishing.
	 *
	 * @param <T> type of objects.
	 *
	 * @return created subject.
	 */
	static <T> Subject<T> simple() {
		return new SimpleSubject<>();
	}

	/**
	 * Create buffered subject for publishing objects.
	 * Any publishing to this subject will be buffered according to given size,
	 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
	 *
	 * @param bufferSize for subject buffer
	 * @param <T>        type of objects.
	 *
	 * @return created subject.
	 */
	static <T> Subject<T> buffered(int bufferSize) {
		return new BufferSubject<>(bufferSize);
	}
}
