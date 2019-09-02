package com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
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
        observable.onComplete(() -> this.complete());
    }

    @Override
    public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
        return observable.subscribeAndHandle((result -> {
            result.process( (value,hasNext) -> {
                if (filter.test(result.getValue())) {
                    consumer.consume(result);
                } else {
                    consumer.consume(result.withoutValue());
                }
            })
            if (filter.test(result.getValue())) {
                consumer.consume(result);
            } else {
                consumer.consume(result.withoutValue());
            }
            return true;
        }));
    }
}
