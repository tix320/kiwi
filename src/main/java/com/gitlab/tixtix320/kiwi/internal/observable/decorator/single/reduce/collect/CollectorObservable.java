package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.collect;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.ReduceObservable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
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
        observable.onComplete(c -> this.complete());
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super R> consumer) {
        AtomicInteger remainingObjects = new AtomicInteger(Integer.MAX_VALUE);
        observable.onComplete(count -> {
            if (count == 0) {
                consumer.consume(collect(objects.stream()));
            } else {
                remainingObjects.set(count-objects.size());
            }
        });
        return observable.subscribeAndHandle(object -> {
            int value = remainingObjects.decrementAndGet();
            if (value > 0) { // observable not completed
                objects.add(object);
                return true;
            } else {
                objects.add(object);
                consumer.consume(collect(objects.stream()));
                return false;
            }
        });
    }

    protected abstract R collect(Stream<S> objects);

    @Override
    public final int getAvailableObjectsCount() {
        return observable.getAvailableObjectsCount();
    }
}
