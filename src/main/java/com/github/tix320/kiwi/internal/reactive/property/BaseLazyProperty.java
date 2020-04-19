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
	public final void publishChanges() {
		if (!PropertyAtomicContext.checkAtomicContext(this)) {
			super.publishChanges();
		}
	}

	@Override
	public abstract ReadOnlyProperty<T> toReadOnly();
}
