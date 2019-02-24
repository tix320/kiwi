package io.titix.kiwi.rx.internal.subject;

import io.titix.kiwi.rx.internal.observer.SourceObservable;

/**
 * @author tix32 on 21-Feb-19
 */
public abstract class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public SourceObservable<T> source() {
		return new SourceObservable<>(observers, c -> {});
	}
}
