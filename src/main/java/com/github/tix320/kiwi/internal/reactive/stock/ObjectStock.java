package com.github.tix320.kiwi.internal.reactive.stock;

import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public class ObjectStock<T> extends BaseStock<T> {

	@Override
	public ReadOnlyStock<T> toReadOnly() {
		return ReadOnlyObjectStock.wrap(this);
	}
}
