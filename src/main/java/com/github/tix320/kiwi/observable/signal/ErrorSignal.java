package com.github.tix320.kiwi.observable.signal;

public non-sealed class ErrorSignal extends Signal {

	public static final int DEFAULT_PRIORITY = 200;

	private final Throwable error;

	public ErrorSignal(Throwable error) {
		this.error = error;
	}

	public Throwable error() {
		return error;
	}

	@Override
	public int defaultPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public final <R> R accept(SignalVisitor<R> signalVisitor) {
		return signalVisitor.visit(this);
	}
}
