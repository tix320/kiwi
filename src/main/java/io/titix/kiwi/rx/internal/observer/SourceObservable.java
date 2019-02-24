package io.titix.kiwi.rx.internal.observer;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class SourceObservable<T> extends BaseObservable<T> {

	private final Collection<Consumer<T>> observers;

	private final Consumer<Consumer<T>> interim;

	public SourceObservable(Collection<Consumer<T>> observers, Consumer<Consumer<T>> interim) {
		this.observers = observers;
		this.interim = interim;
	}

	@Override
	public final Subscription subscribe(Consumer<T> consumer, Predicate<Subscription> filter) {
		Consumer<T> filteredConsumer = new Consumer<>() {
			@Override
			public void accept(T object) {
				if (filter.test(() -> observers.remove(this))) {
					consumer.accept(object);
				}
			}
		};
		observers.add(filteredConsumer);
		interim.accept(filteredConsumer);
		return () -> observers.remove(filteredConsumer);
	}

	@Override
	public final Subscription subscribe(Consumer<T> consumer) {
		observers.add(consumer);
		interim.accept(consumer);
		return () -> observers.remove(consumer);
	}
}
