package io.titix.kiwi.rx.subject.internal;

import io.titix.kiwi.rx.observable.Observer;
import io.titix.kiwi.rx.observable.ObserverWithSubscription;
import io.titix.kiwi.rx.observable.Subscription;

/**
 * @author tix32 on 21-Feb-19
 */
public final class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public Subscription addObserver(Observer<? super T> observer) {
		observers.add(observer);
		return () -> observers.remove(observer);
	}

	@Override
	public Subscription addObserver(ObserverWithSubscription<? super T> observer) {
		Observer<T> realObserver = new Observer<>() {
			@Override
			public void consume(T object) {
				observer.consume(object, () -> observers.remove(this));
			}
		};
		observers.add(realObserver);
		return () -> observers.remove(realObserver);
	}

	@Override
	protected void preNext(T object) {
	}
}
