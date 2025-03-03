package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.ERROR_PRIORITY;

public non-sealed class ErrorSignal extends Signal {

	private final Throwable error;

	public ErrorSignal(Throwable error) {
		this.error = error;
	}

	public Throwable error() {
		return error;
	}

	@Override
	public int priority() {
		return ERROR_PRIORITY;
	}

}
