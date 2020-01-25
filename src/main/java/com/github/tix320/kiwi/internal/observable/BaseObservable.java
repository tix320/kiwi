package com.github.tix320.kiwi.internal.observable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.tix320.kiwi.api.observable.MonoObservable;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.internal.observable.decorator.single.WaitCompleteObservable;
import com.github.tix320.kiwi.internal.observable.decorator.single.collect.JoinObservable;
import com.github.tix320.kiwi.internal.observable.decorator.single.collect.ToListObservable;
import com.github.tix320.kiwi.internal.observable.decorator.single.collect.ToMapObservable;
import com.github.tix320.kiwi.internal.observable.decorator.single.operator.*;

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
