package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.internal.observer.ObserverManager;
import io.titix.kiwi.rx.internal.observer.SourceObservable;

/**
 * @author tix32 on 23-Feb-19
 */
public abstract class BaseSubject<T> implements Subject<T> {

	protected final Collection<Consumer<T>> observers;

	protected BaseSubject() {
		this.observers = container();
	}

	public final void next(T object) {
		preNext(object);
		for (Consumer<T> observer : observers) {
			observer.accept(object);
		}
		postNext(object);
	}

	@Override
	public final void next(T[] objects) {
		for (T object : objects) {
			next(object);
		}
	}

	@Override
	public final void next(Iterable<T> objects) {
		for (T object : objects) {
			next(object);
		}
	}

	protected void preNext(T object) {
		// do nothing
	}

	@SuppressWarnings("all")
	protected void postNext(T object) {
		// do nothing
	}

	@Override
	public final Observable<T> asObservable() {
		return new SourceObservable<>(manager());
	}

	protected abstract ObserverManager<T> manager();

	protected abstract Collection<Consumer<T>> container();
}
