package com.gitlab.tixtix320.kiwi.observable.subject;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.subject.internal.BufferSubject;
import com.gitlab.tixtix320.kiwi.observable.subject.internal.SingleSubject;

/**
 * @author Tigran Sargsyan on 21-Feb-19
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
