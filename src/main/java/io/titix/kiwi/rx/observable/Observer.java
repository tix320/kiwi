package io.titix.kiwi.rx.observable;

public interface Observer<T> {

	void consume(T object);
}
