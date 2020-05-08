package com.github.tix320.kiwi.api.util.collection;

import java.util.Objects;

public final class Tuple<T1, T2> {

	private final T1 first;
	private final T2 second;

	public Tuple(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public T1 first() {
		return first;
	}

	public T2 second() {
		return second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tuple<?, ?> tuple = (Tuple<?, ?>) o;
		return first.equals(tuple.first) && second.equals(tuple.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "[" + first + "," + second + ']';
	}
}
