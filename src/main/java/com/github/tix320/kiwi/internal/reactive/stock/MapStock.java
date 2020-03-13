package com.github.tix320.kiwi.internal.reactive.stock;

import java.util.Map;

import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public class MapStock<K, V> extends BaseStock<Map<K, V>> {

	@Override
	public ReadOnlyStock<Map<K, V>> toReadOnly() {
		return ReadOnlyMapStock.wrap(this);
	}
}
