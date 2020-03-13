package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

public class ReadOnlyListStock<T> implements ReadOnlyStock<List<T>> {

	private final Stock<List<T>> stock;

	private ReadOnlyListStock(Stock<List<T>> stock) {
		this.stock = stock;
	}

	@SuppressWarnings("all")
	public static <T> ReadOnlyStock<List<T>> wrap(Stock<List<T>> stock) {
		if (stock instanceof ReadOnlyListStock) {
			return (ReadOnlyListStock) stock;
		}
		else {
			return new ReadOnlyListStock<>(stock);
		}
	}

	@Override
	public List<List<T>> list() {
		return stock.list().stream().map(Collections::unmodifiableList).collect(Collectors.toList());
	}

	@Override
	public Observable<List<T>> asObservable() {
		return stock.asObservable().map(Collections::unmodifiableList);
	}
}
