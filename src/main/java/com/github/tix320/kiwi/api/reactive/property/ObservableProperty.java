package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.api.reactive.ObservableCandidate;

public interface ObservableProperty<T> extends ObservableCandidate<T> {

	T getValue();

	default boolean isEmpty() {
		return getValue() == null;
	}

	default boolean isPresent() {
		return getValue() != null;
	}
}
