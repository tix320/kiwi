package com.github.tix320.kiwi.internal.reactive.property;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.property.ReadOnlyStock;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class BaseLazyStock<T> extends BaseStock<T> {

	private final List<T> notPublishedValues;

	public BaseLazyStock() {
		this.notPublishedValues = new ArrayList<>();
	}

	@Override
	public final void publishChanges() {
		checkClosed();
		List<T> values = new ArrayList<>(notPublishedValues);
		notPublishedValues.clear();
		values.forEach(super::publish);
	}

	@Override
	protected final void publish(T value) {
		if (PropertyAtomicContext.inAtomicContext(this)) {
			notPublishedValues.add(value);
		}
		else {
			super.publish(value);
		}
	}

	@Override
	public final void close() {
		super.close();
		notPublishedValues.clear();
	}

	@Override
	public abstract ReadOnlyStock<T> toReadOnly();
}
