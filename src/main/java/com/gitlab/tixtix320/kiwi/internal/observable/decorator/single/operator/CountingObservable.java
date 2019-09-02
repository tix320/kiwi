package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class CountingObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    private final long count;

    public CountingObservable(BaseObservable<T> observable, long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative");
        }
        this.observable = observable;
        this.count = count;
        observable.onComplete(number -> this.complete());
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
        if (count == 0) {
            return () -> {
            };
        }
        AtomicLong limit = new AtomicLong(count);
        Subscription subscription = observable.subscribeAndHandle(object -> {
            if (limit.getAndDecrement() > 0) {
                return consumer.consume(object);
            } else {
                return false;
            }
        });
        addSubscription(subscription);
        return subscription;
    }

    @Override
    public int getAvailableObjectsCount() {
        return observable.getAvailableObjectsCount();
    }
}
