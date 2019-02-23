package io.titix.kiwi.rx.internal.observer;

import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 22-Feb-19
 */
public final class OneTimeObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	public OneTimeObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public Subscription subscribe(Consumer<T> observer) {
		var consumed = new Object() {
			boolean $ = false;
		};
		return ((BaseObservable<T>) observable).subscribe(observer, () -> {
			boolean need = !consumed.$;
			consumed.$ = true;
			return need;
		});
	}
}
