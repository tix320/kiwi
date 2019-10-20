package com.gitlab.tixtix320.kiwi.api.observable;

import com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple.CombineObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple.ConcatObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.subject.BufferSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Observable<T> {

    /**
     * Subscribe to observable.
     * If observable already completed, then available values will be processed immediately.
     */
    Subscription subscribe(Consumer<? super T> consumer);

    /**
     * Subscribe to observable and handle every item, consumer must return boolean value,
     * which indicates that need more elements or not.
     * If observable already completed, then available values will be processed immediately.
     */
    Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer);

    /**
     * Add handler on completion of observable.
     * If observable already completed, then handler be processed immediately.
     */
    void onComplete(Runnable runnable);

    /**
     * BLocks current thread until observable will be completed, on which called this method.
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
        List<Observable<T>> list = new ArrayList<>(Arrays.asList(observables));
        return new ConcatObservable<>(list);
    }

    static <T> Observable<T> concat(Iterable<Observable<T>> observables) {
        List<Observable<T>> list = new ArrayList<>();
        for (Observable<T> observable : observables) {
            list.add(observable);
        }
        return new ConcatObservable<>(list);
    }

    @SafeVarargs
    static <T> Observable<List<T>> combine(Observable<T>... observables) {
        List<Observable<T>> list = new ArrayList<>(Arrays.asList(observables));
        return new CombineObservable<>(list);
    }

    static <T> Observable<List<T>> combine(Iterable<Observable<T>> observables) {
        List<Observable<T>> list = new ArrayList<>();
        for (Observable<T> observable : observables) {
            list.add(observable);
        }
        return new CombineObservable<>(list);
    }
}
