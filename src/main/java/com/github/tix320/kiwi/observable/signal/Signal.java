package com.github.tix320.kiwi.observable.signal;

import java.util.concurrent.atomic.AtomicLong;

public abstract sealed class Signal permits NextSignal, CancelSignal, CompleteSignal, ErrorSignal {

	private static final AtomicLong orderGenerator = new AtomicLong(Long.MIN_VALUE);

	private final long order;

	public Signal() {
		this.order = orderGenerator.getAndIncrement();
	}

	public final long order() {
		return order;
	}

	public abstract int defaultPriority();

	public abstract SignalVisitor.SignalVisitResult accept(SignalVisitor  signalVisitor);
}
