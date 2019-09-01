package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    private final Observable<?> until;

    public UntilObservable(BaseObservable<T> observable, Observable<?> until) {
        this.observable = observable;
        this.until = until;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
        AtomicBoolean unsubscribe = new AtomicBoolean();
        until.onComplete(() -> unsubscribe.set(true));
        Subscription subscription = observable.subscribeAndHandle(object -> {
            if (unsubscribe.get()) {
                return false;
            } else {
                return consumer.consume(object);
            }
        });
        addSubscription(subscription);
        return subscription;
    }
}
