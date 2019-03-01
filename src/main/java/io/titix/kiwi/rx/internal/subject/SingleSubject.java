package io.titix.kiwi.rx.internal.subject;

import java.util.function.Consumer;

import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 21-Feb-19
 */
public final class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public Subscription addObserver(Consumer<? super T> consumer) {
		observers.add(consumer);
		return () -> observers.remove(consumer);
	}
}
