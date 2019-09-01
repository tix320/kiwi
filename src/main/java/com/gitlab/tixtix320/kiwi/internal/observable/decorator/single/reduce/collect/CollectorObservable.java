package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.collect;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.ReduceObservable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends ReduceObservable<R> {

    private final BaseObservable<S> observable;

    private final Queue<S> objects;

    CollectorObservable(BaseObservable<S> observable) {
        this.observable = observable;
        this.objects = new ConcurrentLinkedQueue<>();
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super R> consumer) {
        AtomicBoolean completed = new AtomicBoolean(false);
        observable.onComplete(() -> completed.set(true));
        return observable.subscribeAndHandle(object -> {
            objects.add(object);
            if (completed.get()) {
                consumer.consume(collect(objects.stream()));
                return false;
            }
            return true;
        });
    }

    protected abstract R collect(Stream<S> objects);
}
