package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.BaseStock;

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
