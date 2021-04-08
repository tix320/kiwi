package com.github.tix320.kiwi.property;

public interface Stock<T> extends ObservableStock<T> {

	void add(T value);

	void close();

	ReadOnlyStock<T> toReadOnly();

	// ---------- Factory methods ----------

	static <T> ObjectStock<T> forObject() {
		return new ObjectStock<>();
	}
}
