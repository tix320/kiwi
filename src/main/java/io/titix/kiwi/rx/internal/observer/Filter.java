package io.titix.kiwi.rx.internal.observer;

/**
 * @author tix32 on 24-Feb-19
 */
public interface Filter<T> {

	T get();

	void unsubscribe();
}
