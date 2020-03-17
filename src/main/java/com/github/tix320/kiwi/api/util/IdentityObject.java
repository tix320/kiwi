package com.github.tix320.kiwi.api.util;

public final class IdentityObject<T> {

	private final T object;

	public IdentityObject(T object) {
		this.object = object;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		IdentityObject<?> that = (IdentityObject<?>) o;
		return object == that.object;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(object);
	}
}
