package com.github.tix320.kiwi.internal.reactive.property;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.property.Stock;

public final class ReadOnlyObjectStock<T> implements ReadOnlyStock<T> {

	private final Stock<T> stock;

	public ReadOnlyObjectStock(Stock<T> stock) {
		this.stock = stock;
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