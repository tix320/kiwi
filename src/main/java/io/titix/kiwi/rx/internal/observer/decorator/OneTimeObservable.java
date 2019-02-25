package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.BaseObservable;

/**
 * @author tix32 on 22-Feb-19
 */
public final class OneTimeObservable<T> extends DecoratorObservable<T> {

	protected OneTimeObservable(Observable<T> observable) {
		super(observable);
	}


	@Override
	public Subscription subscribe(Consumer<T> consumer) {
		AtomicBoolean need = new AtomicBoolean(true);
		Consumer<T> filteredConsumer = new Consumer<>() {
			@Override
			public void accept(T object) {
				if (need.getAndSet(false)) {
					consumer.accept(object);
					observers().remove(this);
				}
			}
		};
		observable.subscribe(filteredConsumer);

		return () -> observers().remove(filteredConsumer);
	}
}
