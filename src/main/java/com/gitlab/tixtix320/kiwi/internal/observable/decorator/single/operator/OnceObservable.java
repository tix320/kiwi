package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    public OnceObservable(BaseObservable<T> observable) {
        this.observable = observable;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
        Subscription subscription = observable.subscribeAndHandle(object -> {
            consumer.consume(object);
            return false;
        });
        addSubscription(subscription);
        return subscription;
    }
}
