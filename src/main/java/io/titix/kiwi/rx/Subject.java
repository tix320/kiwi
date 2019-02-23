package io.titix.kiwi.rx;

import io.titix.kiwi.rx.internal.subject.ConcurrentBufferSubject;
import io.titix.kiwi.rx.internal.subject.ConcurrentSingleSubject;
import io.titix.kiwi.rx.internal.subject.SimpleBufferSubject;
import io.titix.kiwi.rx.internal.subject.SimpleSingleSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Subject<T> {

	void next(T object);

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
