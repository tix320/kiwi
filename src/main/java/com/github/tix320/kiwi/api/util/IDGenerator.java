package com.github.tix320.kiwi.api.util;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public final class IDGenerator {

	private static final AtomicLongFieldUpdater<IDGenerator> FIELD_UPDATER = AtomicLongFieldUpdater.newUpdater(
			IDGenerator.class, "value");

	private volatile long value;


	public IDGenerator() {
		this(Long.MIN_VALUE);
	}

	public IDGenerator(long start) {
		value = start;
	}

	public long next() {
		return FIELD_UPDATER.getAndIncrement(this);
	}
}
