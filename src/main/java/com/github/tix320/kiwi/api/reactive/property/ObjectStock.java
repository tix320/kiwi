package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.internal.reactive.property.BaseLazyStock;
import com.github.tix320.kiwi.internal.reactive.property.ReadOnlyObjectStock;

public final class ObjectStock<T> extends BaseLazyStock<T> {

	@Override
	public ReadOnlyStock<T> toReadOnly() {
		return new ReadOnlyObjectStock<>(this);
	}
}
