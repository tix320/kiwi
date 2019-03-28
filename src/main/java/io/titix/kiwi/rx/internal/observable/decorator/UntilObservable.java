package io.titix.kiwi.rx.internal.observable.decorator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends DecoratorObservable<T, T> {

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		super(observable);
		this.until = until;
	}

	@Override
	BiFunction<Subscription, T, Result<T>> transformer() {
		final AtomicBoolean need = new AtomicBoolean(true);
		until.subscribe(ignored -> need.set(false));
		return (subscription, object) -> {
			if (need.get()) {
				return Result.of(object);
			}
			else {
				subscription.unsubscribe();
				return Result.empty();
			}
		};
	}
}
