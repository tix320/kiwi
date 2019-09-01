package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.function.Predicate;

/**
 * @author Tigran Sargsyan on 02-Mar-19
 */
public final class FilterObservable<T> extends BaseObservable<T> {

    private final BaseObservable<T> observable;

    private final Predicate<? super T> filter;

    public FilterObservable(BaseObservable<T> observable, Predicate<? super T> filter) {
        this.observable = observable;
        this.filter = filter;
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
        return observable.subscribeAndHandle((object -> {
            if (filter.test(object)) {
                return consumer.consume(object);
            }
            return true;
        }));
    }
}
