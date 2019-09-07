package com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends DecoratorObservable<T> {

    private final Observable<T>[] observables;

    public ConcatObservable(Observable<T>[] observables) {
        this.observables = observables;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        Subscription[] subscriptions = new Subscription[observables.length];
        for (int i = 0; i < observables.length; i++) {
            Subscription subscription = observables[i].subscribeAndHandle(consumer);
            subscriptions[i] = subscription;
        }
        return () -> {
            for (Subscription subscription : subscriptions) {
                subscription.unsubscribe();
            }
        };
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Arrays.asList(observables);
    }
}
