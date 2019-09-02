package com.gitlab.tixtix320.kiwi.internal.observable.decorator.multiple;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends BaseObservable<T> {

    private final Observable<T>[] observables;

    public ConcatObservable(Observable<T>[] observables) {
        this.observables = observables;
        AtomicInteger completedCount = new AtomicInteger();
        for (Observable<T> observable : observables) {
            observable.onComplete(() -> {
                if (completedCount.incrementAndGet() == observables.length) {
                    this.complete();
                }
            });
        }

    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
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
