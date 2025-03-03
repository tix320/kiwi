package com.github.tix320.kiwi.observable.signal;

import java.util.concurrent.atomic.AtomicLong;

public abstract sealed class Signal permits PublishSignal, CancelSignal, CompleteSignal, ErrorSignal {

	private static final AtomicLong SEQ_NUMBER_GENERATOR = new AtomicLong(Long.MIN_VALUE);

	private final long seqNumber;

	public Signal() {
		this.seqNumber = SEQ_NUMBER_GENERATOR.getAndIncrement();
	}

	public final long seqNumber() {
		return seqNumber;
	}

	public abstract int priority();

}
