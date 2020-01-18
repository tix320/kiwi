package com.github.tix320.kiwi.internal.observable.decorator.single.collect;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends DecoratorObservable<R> {

    private final BaseObservable<S> observable;

    private final Queue<S> objects;

    CollectorObservable(BaseObservable<S> observable) {
        this.observable = observable;
        this.objects = new ConcurrentLinkedQueue<>();
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends R>> consumer) {
        Subscription subscription = observable.subscribeAndHandle(result -> {
            objects.add(result.getValue());
            return true;
        });
        observable.onComplete(() -> {
            consumer.consume(Result.of(collect(objects.stream()), false));
            objects.clear();
        });
        return subscription;
    }

    protected abstract R collect(Stream<S> objects);

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}
