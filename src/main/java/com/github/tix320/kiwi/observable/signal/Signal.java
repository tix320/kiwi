package com.github.tix320.kiwi.observable.signal;

import java.util.concurrent.atomic.AtomicLong;

public abstract sealed class Signal permits PublishSignal, CancelSignal, CompleteSignal, ErrorSignal {

	private static final AtomicLong orderGenerator = new AtomicLong(Long.MIN_VALUE);

	private final long order;

	public Signal() {
		this.order = orderGenerator.getAndIncrement();
	}

	public final long order() {
		return order;
	}

	public abstract int defaultPriority();

	public abstract <R> R accept(SignalVisitor<R> signalVisitor);
}
