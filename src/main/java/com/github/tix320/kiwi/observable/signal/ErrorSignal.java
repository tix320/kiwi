package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.DefaultPriorities.ERROR_PRIORITY;

public non-sealed class ErrorSignal extends Signal {

	private final Throwable error;

	public ErrorSignal(Throwable error) {
		this.error = error;
	}

	public Throwable error() {
		return error;
	}

	@Override
	public int defaultPriority() {
		return ERROR_PRIORITY;
	}

	@Override
	public final void accept(SignalVisitor signalVisitor) {
		signalVisitor.visit(this);
	}

}
