package com.github.tix320.kiwi.api.observable.subject;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.internal.observable.subject.BufferSubject;
import com.github.tix320.kiwi.internal.observable.subject.SingleSubject;

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
