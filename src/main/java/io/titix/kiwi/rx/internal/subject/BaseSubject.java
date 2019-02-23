package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.Iterator;

import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.internal.observer.Observer;

/**
 * @author tix32 on 23-Feb-19
 */
 abstract class BaseSubject<T> implements Subject<T> {

	final Collection<Observer<T>> observers;

	BaseSubject() {
		this.observers = container();
	}

	void preNext(T object) {
	}

	public final void next(T object) {
		preNext(object);
		Iterator<Observer<T>> iterator = observers.iterator();
		while (iterator.hasNext()) {
			Observer<T> observer = iterator.next();
			if (observer.needMore()) {
				observer.next(object);
			}
			else {
				iterator.remove();
			}
		}
		postNext(object);
	}

	@SuppressWarnings("all")
	void postNext(T object) {
		// do nothing
	}

	abstract Collection<Observer<T>> container();
}
