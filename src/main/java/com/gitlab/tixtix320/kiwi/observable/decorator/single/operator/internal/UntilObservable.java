package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.Result;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends OperatorObservable<T, T> {

	private final Observable<?> until;

	public UntilObservable(BaseObservable<T> observable, Observable<?> until) {
		super(observable);
		this.until = until;
	}

	@Override
	protected Function<T, Result<T>> operator() {
		AtomicBoolean unsubscribe = new AtomicBoolean();
		until.onComplete(() -> unsubscribe.set(true));
		return object -> unsubscribe.get() ? Result.unsubscribe() : Result.of(object);
	}
}
