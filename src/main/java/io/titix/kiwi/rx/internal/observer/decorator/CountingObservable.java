package io.titix.kiwi.rx.internal.observer.decorator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.BaseObservable;

/**
 * @author tix32 on 22-Feb-19
 */
public final class CountingObservable<T> extends DecoratorObservable<T> {

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		super(observable);
		this.count = count < 1 ? 1 : count;
	}


	@Override
	public Subscription subscribe(Consumer<T> consumer) {
		AtomicLong limit = new AtomicLong(count);
		Consumer<T> filteredConsumer = new Consumer<>() {
			@Override
			public void accept(T object) {
				if (limit.getAndDecrement() != 0) {
					consumer.accept(object);
				}
				else {
					observers().remove(this);
				}
			}
		};
		observable.subscribe(filteredConsumer);

		return () -> observers().remove(filteredConsumer);
	}
}
