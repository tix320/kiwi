package io.titix.kiwi.rx.observable.transform.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.transform.Result;

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
