package com.github.tix320.kiwi.api.reactive.property;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Tigran Sargsyan on 09-May-20.
 */
public final class StateProperty<T extends Enum<T>> extends ObjectProperty<T> {

	public StateProperty(T value) {
		super(value);
	}

	public void checkValue(T... expected) {
		checkValue(s -> String.format("Invalid state for this operation: %s", s), expected);
	}

	public void checkValue(String errorMessage, T... expected) {
		checkValue(t -> errorMessage, expected);
	}

	public synchronized void checkValue(Function<T, String> errorFactory, T... expected) {
		T actualState = getValue();
		if (!Arrays.asList(expected).contains(actualState)) {
			throw new IllegalStateException(errorFactory.apply(actualState));
		}
	}
}
