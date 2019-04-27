package com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 22-Feb-19
 */
public final class OnceObservable<T> extends TransformObservable<T, T> {

	public OnceObservable(BaseObservable<T> observable) {
		super(observable);
	}

	@Override
	protected BiFunction<Subscription, T, Optional<T>> transformer() {
		AtomicBoolean need = new AtomicBoolean(true);
		return (subscription, object) -> {
			if (need.getAndSet(false)) {
				return Optional.of(object);
			}
			else {
				subscription.unsubscribe();
				return Optional.empty();
			}
		};
	}


}
