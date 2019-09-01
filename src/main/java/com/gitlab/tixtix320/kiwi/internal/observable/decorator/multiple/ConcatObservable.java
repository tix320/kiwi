package com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends BaseObservable<T> {

    private final Observable<T>[] observables;

    public ConcatObservable(Observable<T>[] observables) {
        this.observables = observables;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
        Subscription[] subscriptions = new Subscription[observables.length];
        for (int i = 0; i < observables.length; i++) {
            Subscription subscription = observables[i].subscribeAndHandle(consumer);
            subscriptions[i] = subscription;
            addSubscription(subscription);
        }
        return () -> {
            for (Subscription subscription : subscriptions) {
                subscription.unsubscribe();
            }
        };
    }
}
