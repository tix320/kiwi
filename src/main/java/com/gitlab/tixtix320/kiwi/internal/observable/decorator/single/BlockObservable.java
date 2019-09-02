package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BlockObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    private final Object waitObject;

    public BlockObservable(BaseObservable<T> observable) {
        this.observable = observable;
        waitObject = new Object();
        observable.onComplete(this::complete);
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
}