package com.gitlab.tixtix320.kiwi.observable.subject.internal;

import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public Subscription addObserver(Observer<? super T> observer) {
		observers.add(observer);
		return () -> observers.remove(observer);
	}

	@Override
	protected void preNext(T object) {
	}
}
