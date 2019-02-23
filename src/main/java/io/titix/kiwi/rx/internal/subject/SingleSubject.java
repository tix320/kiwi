package io.titix.kiwi.rx.internal.subject;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.BaseObservable;
import io.titix.kiwi.rx.internal.observer.Observer;

/**
 * @author tix32 on 21-Feb-19
 */
public abstract class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public Observable<T> asObservable() {
		return new BaseObservable<>() {

			@Override
			public Subscription subscribe(Consumer<T> consumer, BooleanSupplier predicate) {
				Observer<T> observer = new Observer<>(consumer, predicate);
				observers.add(observer);
				return () -> observers.remove(observer);
			}

			@Override
			public Subscription subscribe(Consumer<T> consumer) {
				Observer<T> observer = new Observer<>(consumer, () -> true);
				observers.add(observer);
				return () -> observers.remove(observer);
			}
		};
	}
}
