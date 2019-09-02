package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.function.Function;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends BaseObservable<R> {

    private final BaseObservable<S> observable;

    private final Function<? super S, ? extends R> mapper;

    public MapperObservable(BaseObservable<S> observable, Function<? super S, ? extends R> mapper) {
        this.observable = observable;
        this.mapper = mapper;
        observable.onComplete(number -> this.complete());
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super R> consumer) {
        Subscription subscription = observable.subscribeAndHandle(object -> {
            R mappedValue = mapper.apply(object);
            return consumer.consume(mappedValue);
        });
        addSubscription(subscription);
        return subscription;
    }

    @Override
    public int getAvailableObjectsCount() {
        return observable.getAvailableObjectsCount();
    }
}
