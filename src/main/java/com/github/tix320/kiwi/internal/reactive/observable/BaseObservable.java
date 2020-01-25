package com.github.tix320.kiwi.internal.reactive.observable;

import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

	protected BaseObservable() {
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer) {
		return subscribeAndHandle(item -> {
			consumer.accept(item.get());
			return true;
		});
	}
}
