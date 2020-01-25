package com.github.tix320.kiwi.internal.observable.decorator.single.operator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.observable.Item;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends DecoratorObservable<T> {

	private final Observable<T> observable;

	private final AtomicBoolean completed;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.completed = new AtomicBoolean();
		until.onComplete(() -> completed.set(true));
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends T>> consumer) {
		if (completed.get()) {
			return () -> {
			};
		}
		return observable.subscribeAndHandle(item -> {
			if (completed.get()) {
				return false;
			}
			else {
				return consumer.consume(item);
			}
		});
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
