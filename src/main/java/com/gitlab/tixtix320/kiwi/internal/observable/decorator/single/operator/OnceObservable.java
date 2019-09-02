package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.function.Consumer;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    public OnceObservable(BaseObservable<T> observable) {
        this.observable = observable;
        observable.onComplete(this::complete);
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        Subscription subscription = observable.subscribeAndHandle(result -> consumer.consume(Result.lastOne(result.getValue())));
        addSubscription(subscription);
        return subscription;
    }
}
