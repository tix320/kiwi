package io.titix.kiwi.observable.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import io.titix.kiwi.observable.Observable;
import io.titix.kiwi.observable.Subject;
import io.titix.kiwi.observable.Subscription;

/**
 * @author tix32 on 21-Feb-19
 */
public final class CountingSubject<T> implements Subject<T> {

	private final List<Consumer<T>> observers;

	public CountingSubject() {
		observers = new LinkedList<>();
	}

	@Override
	public void next(T object) {
	observers.forEach(observer -> observer.accept(object));
	}

	@Override
	public Observable<T> asObservable() {
		return null;
	}


}
