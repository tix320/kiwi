package io.titix.kiwi.rx.internal.observer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 22-Feb-19
 */
public final class CountingObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final long count;

	public CountingObservable(Observable<T> observable, long count) {
		this.observable = observable;
		this.count = count < 1 ? 1 : count;
	}

	@Override
	public Subscription subscribe(Consumer<T> observer) {
		AtomicLong limit = new AtomicLong(count);
		return ((BaseObservable<T>) observable).subscribe(observer, () -> limit.getAndDecrement() != 0);
	}
}
