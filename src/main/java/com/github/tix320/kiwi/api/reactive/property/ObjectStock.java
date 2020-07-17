package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.internal.reactive.property.BaseStock;

public final class ObjectStock<T> extends BaseStock<T> {

	@Override
	public ReadOnlyObjectStock<T> toReadOnly() {
		return new ReadOnlyObjectStock<>(this);
	}

	@Override
	public String toString() {
		return "ObjectStock";
	}
}
