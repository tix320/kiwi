package io.titix.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

	private final AtomicLong current;

	private final Queue<Long> availableNumbers;

	public IdGenerator() {
		current = new AtomicLong();
		availableNumbers = new ConcurrentLinkedQueue<>();
	}

	public long next() {
		if (!availableNumbers.isEmpty()) {
			return availableNumbers.poll();
		}
		return current.getAndIncrement();
	}

	public void detach(long id) {
		availableNumbers.add(id);
	}
}
