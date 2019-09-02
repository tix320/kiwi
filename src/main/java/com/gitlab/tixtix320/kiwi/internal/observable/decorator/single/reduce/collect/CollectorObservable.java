package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.collect;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.ReduceObservable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
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
        observable.onComplete(this::complete);
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends R>> consumer) {
        return observable.subscribeAndHandle(result -> {
            objects.add(result.getValue());
            if (!result.hasNext()) {
                consumer.consume(result.copy(collect(objects.stream()), false));
                return false;
            }
            return true;
        });
    }

    protected abstract R collect(Stream<S> objects);
}
