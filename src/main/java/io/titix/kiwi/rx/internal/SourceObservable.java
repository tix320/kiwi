package io.titix.kiwi.rx.internal;

import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.subject.BaseSubject;

/**
 * @author tix32 on 28-Feb-19
 */
public abstract class SourceObservable<T> implements Observable<T> {

	private final BaseSubject<T> source;

	protected SourceObservable(BaseSubject<T> source) {
		this.source = source;
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer) {
		boolean need = addObserver(consumer);
		if (need) {
			observers.add(consumer);
		}
		return () -> observers.remove(consumer);
	}

	@Override
	public final void onComplete(Runnable runnable) {
		source.onComplete(runnable);
	}
}
