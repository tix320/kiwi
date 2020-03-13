package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

public class ReadOnlyObjectStock<T> implements ReadOnlyStock<T> {

	private final Stock<T> stock;

	private ReadOnlyObjectStock(Stock<T> stock) {
		this.stock = stock;
	}

	@SuppressWarnings("all")
	public static <T> ReadOnlyStock<T> wrap(Stock<T> stock) {
		if (stock instanceof ReadOnlyObjectStock) {
			return (ReadOnlyObjectStock) stock;
		}
		else {
			return new ReadOnlyObjectStock<>(stock);
		}
	}

	@Override
	public List<T> list() {
		return stock.list();
	}

	@Override
	public Observable<T> asObservable() {
		return stock.asObservable();
	}
}
