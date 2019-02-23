package io.titix.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

	private final AtomicLong current;

	private final Queue<Long> availableNumbers;

	public IdGenerator() {
		current = new AtomicLong(Long.MIN_VALUE);
		availableNumbers = new ConcurrentLinkedQueue<>();
	}

	public long next() {
		if (!availableNumbers.isEmpty()) {
			return availableNumbers.poll();
		}
		return current.getAndIncrement();
	}

	public void free(long id) {
		if (id >= current.get()) {
			throw new IllegalArgumentException("id " + id + " is already free");
		}
		availableNumbers.add(id);
	}
}
