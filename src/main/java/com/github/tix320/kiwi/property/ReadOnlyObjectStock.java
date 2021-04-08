package com.github.tix320.kiwi.property;

import java.util.List;

import com.github.tix320.kiwi.observable.Observable;

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
