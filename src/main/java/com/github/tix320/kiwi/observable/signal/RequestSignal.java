package com.github.tix320.kiwi.observable.signal;

public non-sealed class RequestSignal extends Signal {

	public static final int DEFAULT_PRIORITY = 100;

	protected final long count;

	public RequestSignal(long count) {
		this.count = count;
	}

	public final long count() {
		return count;
	}

	@Override
	public int defaultPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public final SignalVisitor.SignalVisitResult accept(SignalVisitor signalVisitor) {
		return signalVisitor.visit(this);
	}
}
