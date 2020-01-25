package com.github.tix320.kiwi.api.reactive.common.item;

public interface Item<T> {

	T get();

	boolean hasNext();
}
