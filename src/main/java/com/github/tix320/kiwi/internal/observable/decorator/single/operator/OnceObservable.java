package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends DecoratorObservable<T> {

    private final BaseObservable<T> observable;

    public OnceObservable(BaseObservable<T> observable) {
        this.observable = observable;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        return observable.subscribeAndHandle(result -> {
            consumer.consume(Result.of(result.getValue(), false));
            return false;
        });
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}
