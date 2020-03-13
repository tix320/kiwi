package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.Collection;

import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public class CollectionStock<T> extends BaseStock<Collection<T>> {

	@Override
	public ReadOnlyStock<Collection<T>> toReadOnly() {
		return ReadOnlyCollectionStock.wrap(this);
	}
}
