package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

public class ReadOnlyMapStock<K, V> implements ReadOnlyStock<Map<K, V>> {

	private final Stock<Map<K, V>> stock;

	private ReadOnlyMapStock(Stock<Map<K, V>> stock) {
		this.stock = stock;
	}

	@SuppressWarnings("all")
	public static <K, V> ReadOnlyStock<Map<K, V>> wrap(Stock<Map<K, V>> stock) {
		if (stock instanceof ReadOnlyMapStock) {
			return (ReadOnlyMapStock) stock;
		}
		else {
			return new ReadOnlyMapStock<>(stock);
		}
	}

	@Override
	public List<Map<K, V>> list() {
		return stock.list().stream().map(Collections::unmodifiableMap).collect(Collectors.toList());
	}

	@Override
	public Observable<Map<K, V>> asObservable() {
		return stock.asObservable().map(Collections::unmodifiableMap);
	}
}
