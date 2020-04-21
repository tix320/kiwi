package com.github.tix320.kiwi.api.reactive.property;


import com.github.tix320.kiwi.api.reactive.observable.Observable;

public final class ReadOnlyObjectProperty<T> implements ReadOnlyProperty<T> {

	private final Property<T> property;

	public ReadOnlyObjectProperty(Property<T> property) {
		this.property = property;
	}

	@Override
	public T getValue() {
		return property.getValue();
	}

	@Override
	public Observable<T> asObservable() {
		return property.asObservable();
	}

	@Override
	public int hashCode() {
		return property.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
