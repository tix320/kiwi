package com.github.tix320.kiwi.api.observable;

public interface Item<T> {

	T get();

	boolean hasNext();
}
