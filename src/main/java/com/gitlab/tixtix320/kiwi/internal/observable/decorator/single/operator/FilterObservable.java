package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.DecoratorObservable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends DecoratorObservable<T> {

    private final BaseObservable<T> observable;

    private final Predicate<? super T> filter;

    public FilterObservable(BaseObservable<T> observable, Predicate<? super T> filter) {
        this.observable = observable;
        this.filter = filter;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        return observable.subscribeAndHandle((result -> {
            if (filter.test(result.getValue())) {
                return consumer.consume(result);
            } else {
                return true;
            }
        }));
    }

    @Override
    protected Collection<Observable<?>> observables() {
        return Collections.singleton(observable);
    }
}
