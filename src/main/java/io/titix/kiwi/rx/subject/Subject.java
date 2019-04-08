package io.titix.kiwi.rx.subject;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.subject.internal.BufferSubject;
import io.titix.kiwi.rx.subject.internal.SingleSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Subject<T> {

	void next(T object);

	void next(T[] objects);

	void next(Iterable<T> objects);

	void complete();

	Observable<T> asObservable();

	static <T> Subject<T> single() {
		return new SingleSubject<>();
	}

	static <T> Subject<T> buffered(int bufferSize) {
		return new BufferSubject<>(bufferSize);
	}
}
