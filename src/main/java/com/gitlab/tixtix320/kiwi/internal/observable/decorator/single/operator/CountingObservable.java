package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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
        observable.onComplete(this::complete);
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        if (count == 0) {
            return () -> {
            };
        }
        AtomicLong limit = new AtomicLong(count);
        Subscription subscription = observable.subscribeAndHandle(result -> {
            if (limit.getAndDecrement() > 0) {

                consumer.consume(result);
                return true;
            } else {
                consumer.consume(Result.empty());
                return false;
            }
        });
        addSubscription(subscription);
        return subscription;
    }
}
