package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.internal.reactive.property.BaseLazyStock;

public final class ObjectStock<T> extends BaseLazyStock<T> {

	@Override
	public ReadOnlyObjectStock<T> toReadOnly() {
		return new ReadOnlyObjectStock<>(this);
	}

	@Override
	public String toString() {
		return "ObjectStock: " + list();
	}
}
