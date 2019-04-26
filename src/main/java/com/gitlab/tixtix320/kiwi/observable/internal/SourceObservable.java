package com.gitlab.tixtix320.kiwi.observable.internal;

import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.ObserverWithSubscription;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.subject.internal.BaseSubject;

/**
 * @author tix32 on 28-Feb-19
 */
public final class SourceObservable<T> extends BaseObservable<T> {

	private final BaseSubject<T> source;

	public SourceObservable(BaseSubject<T> source) {
		this.source = source;
	}

	@Override
	public final Subscription subscribe(Observer<? super T> observer) {
		return source.addObserver(observer);
	}

	@Override
	public Subscription subscribeAndHandle(ObserverWithSubscription<? super T> observer) {
		return source.addObserver(observer);
	}

	@Override
	public final void onComplete(Runnable runnable) {
		source.onComplete(runnable);
	}
}
