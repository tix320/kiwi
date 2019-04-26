package com.gitlab.tixtix320.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class IDGenerator {

	private final AtomicLong current;

	private final Queue<Long> availableNumbers;

	public IDGenerator() {
		current = new AtomicLong(Long.MIN_VALUE);
		availableNumbers = new ConcurrentLinkedQueue<>();
	}

	public long next() {
		Long item = availableNumbers.poll();
		if (item == null) {
			return current.getAndIncrement();
		}
		return item;
	}

	public void release(long id) {
		if (id >= current.get()) {
			throw new IllegalArgumentException("id " + id + " is already free");
		}
		availableNumbers.add(id);
	}
}
