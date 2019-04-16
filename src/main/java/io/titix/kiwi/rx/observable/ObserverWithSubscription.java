package io.titix.kiwi.rx.observable;

public interface ObserverWithSubscription<T> {

	void consume(T object, Subscription subscription);
}
