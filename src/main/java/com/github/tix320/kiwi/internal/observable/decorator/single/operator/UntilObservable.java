package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends DecoratorObservable<T> {

    private final BaseObservable<T> observable;

    private final AtomicBoolean unsubscribe;

    public UntilObservable(BaseObservable<T> observable, Observable<?> until) {
        this.observable = observable;
        this.unsubscribe = new AtomicBoolean();
        until.onComplete(() -> unsubscribe.set(true));
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        if (unsubscribe.get()) {
            return () -> {
            };
        }
        return observable.subscribeAndHandle(result -> {
            if (unsubscribe.get()) {
                return false;
            } else {
                return consumer.consume(result);
            }
        });
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}
