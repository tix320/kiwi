package com.gitlab.tixtix320.kiwi.observable.subject.internal;

import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.ObserverWithSubscription;
import com.gitlab.tixtix320.kiwi.observable.Subscription;

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
