package com.gitlab.tixtix320.kiwi.observable;

public interface Observer<T> {

	boolean consume(T object);
}
