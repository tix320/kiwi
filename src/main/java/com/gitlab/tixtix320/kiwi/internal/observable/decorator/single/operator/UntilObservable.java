package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    private final AtomicBoolean unsubscribe;

    public UntilObservable(BaseObservable<T> observable, Observable<?> until) {
        this.observable = observable;
        this.unsubscribe = new AtomicBoolean();
        observable.onComplete(this::complete);
        until.onComplete(() -> unsubscribe.set(true));
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        if (unsubscribe.get()) {
            return () -> {
            };
        }
        Subscription subscription = observable.subscribeAndHandle(result -> {
            if (unsubscribe.get()) {
                return false;
            } else {
                return consumer.consume(result);
            }
        });
        addSubscription(subscription);
        return subscription;
    }
}
