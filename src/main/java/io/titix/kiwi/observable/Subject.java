package io.titix.kiwi.observable;

import io.titix.kiwi.observable.internal.DefaultSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Subject<T> {

	void next(T object);

	Observable<T> asObservable();

	static <T> Subject<T> create() {
		return new DefaultSubject<>();
	}
}
