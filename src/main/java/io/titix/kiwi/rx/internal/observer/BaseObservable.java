package io.titix.kiwi.rx.internal.observer;

import java.util.function.Consumer;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 23-Feb-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

	public abstract Subscription subscribe(Consumer<T> consumer, Predicate<Filter> filter);
}
