package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.internal;

import java.util.function.Function;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.internal.SingleDecoratorObservable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.operator.Result;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public abstract class OperatorObservable<S, R> extends SingleDecoratorObservable<S, R> {

	protected OperatorObservable(Observable<S> observable) {
		super(observable);
	}

	@Override
	public final Subscription subscribeAndHandle(Observer<? super R> observer) {
		Function<S, Result<R>> operator = operator();
		return observable.subscribeAndHandle(object -> {
			Result<R> result = operator.apply(object);

			boolean needMore = true;
			if (result.isPresent()) {
				needMore = observer.consume(result.getValue());
			}

			return needMore && result.isNeedMore();
		});
	}

	protected abstract Function<S, Result<R>> operator();
}
