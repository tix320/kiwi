package com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.Collection;
import java.util.List;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends DecoratorObservable<T> {

    private final List<Observable<T>> observables;

    public ConcatObservable(List<Observable<T>> observables) {
        this.observables = observables;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        Subscription[] subscriptions = new Subscription[observables.size()];
        for (int i = 0; i < observables.size(); i++) {
            Subscription subscription = observables.get(i).subscribeAndHandle(consumer);
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
