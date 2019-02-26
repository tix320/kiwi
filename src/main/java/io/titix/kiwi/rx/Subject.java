package io.titix.kiwi.rx;

import io.titix.kiwi.rx.internal.subject.buffer.ConcurrentBufferSubject;
import io.titix.kiwi.rx.internal.subject.buffer.SimpleBufferSubject;
import io.titix.kiwi.rx.internal.subject.single.ConcurrentSingleSubject;
import io.titix.kiwi.rx.internal.subject.single.SimpleSingleSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Subject<T> {

	void next(T object);

	void next(T[] objects);

	void next(Iterable<T> objects);

	Observable<T> asObservable();

	static <T> Subject<T> single() {
		return new SimpleSingleSubject<>();
	}

	static <T> Subject<T> concurrentSingle() {
		return new ConcurrentSingleSubject<>();
	}

	static <T> Subject<T> buffered(int bufferSize) {
		return new SimpleBufferSubject<>(bufferSize);
	}

	static <T> Subject<T> concurrentBuffered(int bufferSize) {
		return new ConcurrentBufferSubject<>(bufferSize);
	}
}
