package com.gitlab.tixtix320.kiwi.observable;

public interface Observer<T> {

	void consume(T object);
}
