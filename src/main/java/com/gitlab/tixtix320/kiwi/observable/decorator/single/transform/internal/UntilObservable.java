package com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.Result;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends TransformObservable<T, T> {

	private final Observable<?> until;

	public UntilObservable(BaseObservable<T> observable, Observable<?> until) {
		super(observable);
		this.until = until;
	}

	@Override
	protected BiFunction<Subscription, T, Result<T>> transformer() {
		final AtomicBoolean need = new AtomicBoolean(true);
		until.subscribe(ignored -> need.set(false));
		return (subscription, object) -> {
			if (need.get()) {
				return Result.of(object);
			}
			else {
				subscription.unsubscribe();
				return Result.none();
			}
		};
	}
}
