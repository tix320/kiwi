package io.titix.kiwi.rx.observable.internal;

import java.util.function.Consumer;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.subject.internal.BaseSubject;

/**
 * @author tix32 on 28-Feb-19
 */
public final class SourceObservable<T> extends BaseObservable<T> {

	private final BaseSubject<T> source;

	public SourceObservable(BaseSubject<T> source) {
		this.source = source;
	}

	@Override
	public final Subscription subscribe(Consumer<? super T> consumer) {
		return source.addObserver(consumer);
	}

	@Override
	public final void onComplete(Runnable runnable) {
		source.onComplete(runnable);
	}
}
