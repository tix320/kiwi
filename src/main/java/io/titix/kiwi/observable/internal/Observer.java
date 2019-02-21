package io.titix.kiwi.observable.internal;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observer<T> {

	void accept(T object);

	void complete();
}
