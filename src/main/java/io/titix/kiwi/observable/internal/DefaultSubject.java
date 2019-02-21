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
public class DefaultSubject<T> implements Subject<T>, Observable<T> {

	private final List<Consumer<T>> observers;

	public DefaultSubject() {
		observers = new LinkedList<>();
	}

	@Override
	public Subscription subscribe(Consumer<T> consumer) {
		observers.add(consumer);
		return () -> observers.remove(consumer);
	}

	@Override
	public Observable<T> take(long count) {
		return null;
	}

	@Override
	public Observable<T> one() {
		return null;
	}

	@Override
	public void next(T object) {
		observers.forEach(observer -> observer.accept(object));
	}

	@Override
	public Observable<T> asObservable() {
		return this;
	}
}
