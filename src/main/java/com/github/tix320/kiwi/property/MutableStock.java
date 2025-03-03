package com.github.tix320.kiwi.property;

public interface MutableStock<T> extends Stock<T> {

	void add(T value);

	void close();

}
