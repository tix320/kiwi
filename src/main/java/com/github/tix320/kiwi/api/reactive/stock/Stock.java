package com.github.tix320.kiwi.api.reactive.stock;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.tix320.kiwi.internal.reactive.stock.CollectionStock;
import com.github.tix320.kiwi.internal.reactive.stock.ListStock;
import com.github.tix320.kiwi.internal.reactive.stock.MapStock;
import com.github.tix320.kiwi.internal.reactive.stock.ObjectStock;

public interface Stock<T> extends ObservableStock<T> {

	void add(T value);

	void add(Iterable<T> values);

	ReadOnlyStock<T> toReadOnly();

	static <T> Stock<T> forObject() {
		return new ObjectStock<>();
	}

	static <T> Stock<List<T>> forList() {
		return new ListStock<>();
	}

	static <T> Stock<Collection<T>> forCollection() {
		return new CollectionStock<>();
	}

	static <K, V> Stock<Map<K, V>> forMap() {
		return new MapStock<>();
	}
}
