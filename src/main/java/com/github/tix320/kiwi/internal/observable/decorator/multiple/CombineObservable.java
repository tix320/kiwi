package com.github.tix320.kiwi.internal.observable.decorator.multiple;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class CombineObservable<T> extends DecoratorObservable<List<T>> {

    private final List<Observable<T>> observables;

    public CombineObservable(List<Observable<T>> observables) {
        this.observables = observables;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends List<T>>> consumer) {
        Subscription[] subscriptions = new Subscription[observables.size()];
        List<Queue<T>> queues = new ArrayList<>();
        for (int i = 0; i < observables.size(); i++) {
            queues.add(new ConcurrentLinkedQueue<>());
        }
        for (int i = 0; i < observables.size(); i++) {
            Observable<T> observable = observables.get(i);
            Queue<T> queue = queues.get(i);
            Subscription subscription = observable.subscribeAndHandle(result -> {
                queue.add(result.getValue());

                for (Queue<T> q : queues) {
                    if (q.isEmpty()) {
                        return true;
                    }
                }

                List<T> objects = new ArrayList<>(queues.size());
                for (Queue<T> q : queues) {
                    objects.add(q.poll());
                }
                consumer.consume(Result.of(objects, true));
                return true;
            });

            subscriptions[i] = subscription;
        }

        return () -> {
            for (Subscription subscription : subscriptions) {
                subscription.unsubscribe();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Observable<?>> observables() {
        return (Collection) observables;
    }
}
