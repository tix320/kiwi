package com.github.tix320.kiwi.internal.observable.decorator.single;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class BlockObservable<T> extends DecoratorObservable<T> {

    private final BaseObservable<T> observable;

    public BlockObservable(BaseObservable<T> observable) {
        this.observable = observable;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        Object waitObject = new Object();
        CompletableFuture.runAsync(() -> {
            observable.subscribeAndHandle(result -> {
                boolean needMore = consumer.consume(result);
                if (!result.hasNext() || !needMore) {
                    synchronized (waitObject) {
                        waitObject.notifyAll();
                    }
                }
                return needMore;
            });
        });

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
