package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.AbstractMutableStock;

public final class ObjectStock<T> extends AbstractMutableStock<T> {

	public ObjectStock(int windowSize) {
		super(windowSize);
	}

	@Override
	public String toString() {
		return "ObjectStock{" + list() + "}";
	}

}
