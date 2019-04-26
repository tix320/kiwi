package com.gitlab.tixtix320.kiwi.observable;

public interface ObserverWithSubscription<T> {

	void consume(T object, Subscription subscription);
}
