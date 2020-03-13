package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public class ListStock<T> extends BaseStock<List<T>> {

	@Override
	public ReadOnlyStock<List<T>> toReadOnly() {
		return ReadOnlyListStock.wrap(this);
	}
}
