package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.api.reactive.ObservableCandidate;

public interface ObservableProperty<T> extends ObservableCandidate<T> {

	T getValue();

	default boolean isNull() {
		return getValue() == null;
	}

	default boolean isNonNull() {
		return getValue() != null;
	}
}
