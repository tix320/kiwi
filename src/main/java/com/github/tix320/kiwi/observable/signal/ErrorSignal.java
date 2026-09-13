package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.ERROR_PRIORITY;

import java.util.Objects;

public non-sealed class ErrorSignal extends Signal {

	private final Throwable error;

	public ErrorSignal(Throwable error) {
		this.error = Objects.requireNonNull(error, "Error must not be null");
	}

	public Throwable error() {
		return error;
	}

	@Override
	public int priority() {
		return ERROR_PRIORITY;
	}

}
