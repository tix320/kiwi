package io.titix.kiwi.observable.internal;

import java.util.function.Consumer;

/**
 * @author tix32 on 21-Feb-19
 */
public final class CountingObserver<T> implements Observer<T> {

	private final Consumer<T> observer;

	private long remaining;

	public CountingObserver(Consumer<T> observer, long remaining) {
		this.observer = observer;
		this.remaining = remaining;
	}

	public void accept(T object) {
		if (remaining > 0) {
			observer.accept(object);
			remaining--;
		}
	}

	public void complete() {
		remaining = 0;
	}
}
