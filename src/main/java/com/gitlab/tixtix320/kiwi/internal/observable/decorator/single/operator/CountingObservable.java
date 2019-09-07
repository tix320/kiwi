package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends DecoratorObservable<T> {

    private final BaseObservable<T> observable;

    private final long count;

    public CountingObservable(BaseObservable<T> observable, long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative");
        }
        this.observable = observable;
        this.count = count;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        if (count == 0) {
            return () -> {
            };
        }
        AtomicLong limit = new AtomicLong(count);
        return observable.subscribeAndHandle(result -> {
            long remaining = limit.decrementAndGet();
            if (remaining > 0) {
                return consumer.consume(result);
            } else if (remaining == 0) {
                consumer.consume(Result.of(result.getValue(), false));
                return false;
            } else {
                return false;
            }
        });
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}
