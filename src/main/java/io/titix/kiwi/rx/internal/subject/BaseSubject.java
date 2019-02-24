package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.internal.observer.SourceObservable;

/**
 * @author tix32 on 23-Feb-19
 */
abstract class BaseSubject<T> implements Subject<T> {

	final Collection<Consumer<T>> observers;

	BaseSubject() {
		this.observers = container();
	}

	void preNext(T object) {
	}

	public final void next(T object) {
		preNext(object);
		for (Consumer<T> observer : observers) {
			observer.accept(object);
		}
		postNext(object);
	}

	@SuppressWarnings("all")
	void postNext(T object) {
		// do nothing
	}

	@Override
	public final Observable<T> asObservable() {
		return source();
	}

	abstract SourceObservable<T> source();

	abstract Collection<Consumer<T>> container();
}
