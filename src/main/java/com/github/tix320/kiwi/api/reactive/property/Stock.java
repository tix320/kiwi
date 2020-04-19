package com.github.tix320.kiwi.api.reactive.property;

public interface Stock<T> extends ChangeableProperty, ObservableStock<T> {

	void add(T value);

	void addAll(Iterable<T> values);

	void close();

	ReadOnlyStock<T> toReadOnly();

	// ---------- Factory methods ----------

	static <T> ObjectStock<T> forObject() {
		return new ObjectStock<>();
	}
}
