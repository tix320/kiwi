package com.gitlab.tixtix320.kiwi.api.observable;

import com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple.ConcatObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.subject.BufferSubject;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Observable<T> {

    Subscription subscribe(Consumer<? super T> consumer);

    Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer);

    void complete();

    void onComplete(Consumer<Integer> consumer);

    /**
     * Return number of available objects, which will be nexted after subscribe.
     * This method will be called only after observable is completed, otherwise result is undefined.
     *
     * @return count of objects
     */
    int getAvailableObjectsCount();

    /**
     * BLocks current thread until all values will be processed.
     */
    Observable<T> block();

    Observable<T> take(long count);

    Observable<T> takeUntil(Observable<?> observable);

    Observable<T> one();

    <R> Observable<R> map(Function<? super T, ? extends R> mapper);

    Observable<T> filter(Predicate<? super T> filter);

    <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
                                       Function<? super T, ? extends V> valueMapper);

    Observable<List<T>> toList();

    Observable<String> join(Function<? super T, ? extends String> toString, String delimiter);

    Observable<String> join(Function<? super T, ? extends String> toString, String delimiter, String prefix,
                            String suffix);

    static <T> Observable<T> empty() {
        BufferSubject<T> subject = new BufferSubject<>(0);
        subject.complete();
        return subject.asObservable();
    }

    static <T> Observable<T> of(T value) {
        BufferSubject<T> subject = new BufferSubject<>(1);
        subject.next(value);
        subject.complete();
        return subject.asObservable();
    }

    @SafeVarargs
    static <T> Observable<T> of(T... values) {
        BufferSubject<T> subject = new BufferSubject<>(values.length);
        subject.next(values);
        subject.complete();
        return subject.asObservable();
    }

    @SafeVarargs
    static <T> Observable<T> concat(Observable<T>... observables) {
        return new ConcatObservable<>(observables);
    }
}
