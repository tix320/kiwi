package io.titix.kiwi.rx;

import java.util.function.Consumer;

import io.titix.kiwi.rx.internal.observer.CountingObservable;
import io.titix.kiwi.rx.internal.observer.OneTimeObservable;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<T> consumer);

	default Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	default Observable<T> one() {
		return new OneTimeObservable<>(this);
	}
}
