package com.github.tix320.kiwi.internal.reactive.property;

import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseLazyProperty<T> extends BaseProperty<T> {

	public BaseLazyProperty() {
	}

	public BaseLazyProperty(T value) {
		super(value);
	}

	@Override
	public synchronized final void republishState() {
		if (!PropertyAtomicContext.inAtomicContext(this)) {
			super.republishState();
		}
	}

	@Override
	public abstract ReadOnlyProperty<T> toReadOnly();
}
