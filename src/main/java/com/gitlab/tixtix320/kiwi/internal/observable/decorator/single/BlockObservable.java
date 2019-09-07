package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class BlockObservable<T> extends DecoratorObservable<T> {

    private final BaseObservable<T> observable;

    private final Object waitObject;

    public BlockObservable(BaseObservable<T> observable) {
        this.observable = observable;
        waitObject = new Object();
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        CompletableFuture.runAsync(() -> observable.subscribeAndHandle(consumer));

        observable.onComplete(() -> {
            synchronized (waitObject) {
                waitObject.notifyAll();
            }
        });

        synchronized (waitObject) {
            try {
                waitObject.wait();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return () -> {
        };
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}