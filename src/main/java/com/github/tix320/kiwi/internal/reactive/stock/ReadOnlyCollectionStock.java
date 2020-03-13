package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

public class ReadOnlyCollectionStock<T> implements ReadOnlyStock<Collection<T>> {

	private final Stock<Collection<T>> stock;

	private ReadOnlyCollectionStock(Stock<Collection<T>> stock) {
		this.stock = stock;
	}

	@SuppressWarnings("all")
	public static <T> ReadOnlyStock<Collection<T>> wrap(Stock<Collection<T>> stock) {
		if (stock instanceof ReadOnlyCollectionStock) {
			return (ReadOnlyCollectionStock) stock;
		}
		else {
			return new ReadOnlyCollectionStock<>(stock);
		}
	}

	@Override
	public List<Collection<T>> list() {
		return stock.list().stream().map(Collections::unmodifiableCollection).collect(Collectors.toList());
	}

	@Override
	public Observable<Collection<T>> asObservable() {
		return stock.asObservable().map(Collections::unmodifiableCollection);
	}
}
