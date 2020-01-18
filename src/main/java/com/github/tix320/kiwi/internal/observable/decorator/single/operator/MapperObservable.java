package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends DecoratorObservable<R> {

    private final BaseObservable<S> observable;

    private final Function<? super S, ? extends R> mapper;

    public MapperObservable(BaseObservable<S> observable, Function<? super S, ? extends R> mapper) {
        this.observable = observable;
        this.mapper = mapper;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends R>> consumer) {
        return observable.subscribeAndHandle(result -> {
            R mappedValue = mapper.apply(result.getValue());
            return consumer.consume(Result.of(mappedValue));
        });
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}
