package io.titix.kiwi.rx.internal.observer;

import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class SourceObservable<T> implements Observable<T> {

	private final Manager<T> manager;

	public SourceObservable(Manager<T> manager) {
		this.manager = manager;
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer) {
		manager.add(consumer);
		return () -> manager.remove(consumer);
	}
}
