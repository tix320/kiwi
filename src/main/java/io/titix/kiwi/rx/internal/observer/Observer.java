package io.titix.kiwi.rx.internal.observer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author tix32 on 23-Feb-19
 */
public final class Observer<T> {

	private final Consumer<T> consumer;

	private final BooleanSupplier predicate;

	public Observer(Consumer<T> consumer, BooleanSupplier predicate) {
		this.consumer = consumer;
		this.predicate = predicate;
	}

	public boolean needMore() {
		return predicate.getAsBoolean();
	}

	public void next(T object) {
		consumer.accept(object);
	}
}
