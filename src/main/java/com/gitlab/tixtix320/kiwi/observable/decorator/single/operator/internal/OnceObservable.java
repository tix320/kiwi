package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.internal;

import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.Result;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends OperatorObservable<T, T> {

	public OnceObservable(BaseObservable<T> observable) {
		super(observable);
	}

	@Override
	protected Function<T, Result<T>> operator() {
		return Result::lastOne;
	}
}
