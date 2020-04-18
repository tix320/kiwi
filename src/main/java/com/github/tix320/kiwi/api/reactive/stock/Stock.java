package com.github.tix320.kiwi.api.reactive.stock;

import com.github.tix320.kiwi.internal.reactive.stock.ObjectStock;

public interface Stock<T> extends ObservableStock<T> {

	void add(T value);

	void addAll(Iterable<T> values);

	void close();

	ReadOnlyStock<T> toReadOnly();

	// ---------- Factory methods ----------

	static <T> Stock<T> forObject() {
		return new ObjectStock<>();
	}
}
