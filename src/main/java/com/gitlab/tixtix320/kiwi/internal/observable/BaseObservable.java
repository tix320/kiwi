package com.gitlab.tixtix320.kiwi.internal.observable;

import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.BlockObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator.*;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.collect.JoinObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.collect.ToListObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.collect.ToMapObservable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

    protected BaseObservable() {
    }

    @Override
    public final Subscription subscribe(Consumer<? super T> consumer) {
        return subscribeAndHandle(result -> {
            consumer.accept(result.getValue());
            return true;
        });
    }

    @Override
    public final Observable<T> block() {
        return new BlockObservable<>(this);
    }

    @Override
    public final Observable<T> take(long count) {
        return new CountingObservable<>(this, count);
    }

    @Override
    public final Observable<T> takeUntil(Observable<?> observable) {
        return new UntilObservable<>(this, observable);
    }

    @Override
    public final Observable<T> one() {
        return new OnceObservable<>(this);
    }

    @Override
    public <R1> Observable<R1> map(Function<? super T, ? extends R1> mapper) {
        return new MapperObservable<>(this, mapper);
    }

    @Override
    public final Observable<T> filter(Predicate<? super T> filter) {
        return new FilterObservable<>(this, filter);
    }

    @Override
    public final <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
                                                    Function<? super T, ? extends V> valueMapper) {
        return new ToMapObservable<>(this, keyMapper, valueMapper);
    }

    @Override
    public final Observable<List<T>> toList() {
        return new ToListObservable<>(this);
    }

    @Override
    public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter) {
        return new JoinObservable<>(this, toString, delimiter);
    }

    @Override
    public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter,
                                         String prefix, String suffix) {
        return new JoinObservable<>(this, toString, delimiter, prefix, suffix);
    }
}
