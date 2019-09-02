package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.CompletableFuture;

public class BlockObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    private final Object waitObject;

    public BlockObservable(BaseObservable<T> observable) {
        this.observable = observable;
        waitObject = new Object();
        observable.onComplete(number -> this.complete());
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
        CompletableFuture.runAsync(() -> observable.subscribe(consumer::consume));

        observable.onComplete(number -> {
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
    public int getAvailableObjectsCount() {
        return observable.getAvailableObjectsCount();
    }
}