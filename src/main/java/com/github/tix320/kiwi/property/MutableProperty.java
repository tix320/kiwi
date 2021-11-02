package com.github.tix320.kiwi.property;

public interface MutableProperty<T> extends Property<T> {

	void setValue(T value);

	boolean compareAndSetValue(T expectedValue, T value);

	void close();

	boolean isClosed();

	Property<T> toReadOnly();
}
