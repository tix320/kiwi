package io.titix.kiwi.observable.internal;

import java.util.function.Consumer;

/**
 * @author tix32 on 21-Feb-19
 */
public class ContinuousObserver<T> implements Observer<T> {

	private final Consumer<T> observer;

	private boolean need;

	public ContinuousObserver(Consumer<T> observer) {
		this.observer = observer;
		need = true;
	}

	@Override
	public void accept(T object) {
		if (need) {
			observer.accept(object);
		}
	}

	@Override
	public void complete() {
		need = false;
	}
}
