package io.titix.kiwi.rx.internal.observer;

import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class SourceObservable<T> implements Observable<T> {

	private final ObserverManager<T> observerManager;

	public SourceObservable(ObserverManager<T> observerManager) {
		this.observerManager = observerManager;
	}

	@Override
	public final Subscription subscribe(Consumer<T> consumer) {
		observerManager.add(consumer);
		return () -> observerManager.remove(consumer);
	}
}
