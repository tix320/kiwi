package com.github.tix320.kiwi.api.util;

import java.util.Deque;
import java.util.LinkedList;

public final class IDGenerator {

	private final long start;

	private long cursor;

	private final Deque<Long> availableNumbers;

	public IDGenerator() {
		this(Long.MIN_VALUE);
	}

	public IDGenerator(long start) {
		this.start = start;
		this.cursor = start;
		this.availableNumbers = new LinkedList<>();
		generateIds(10);
	}

	public synchronized long next() {
		if (availableNumbers.isEmpty()) {
			long generatedCount = this.cursor - this.start;
			long needToGenerate = generatedCount / 2;
			generateIds(needToGenerate);
		}

		return availableNumbers.removeFirst();
	}

	public synchronized void release(long id) {
		if (id >= cursor) {
			throw new IllegalArgumentException("id " + id + " is already free");
		}
		availableNumbers.addFirst(id);
	}

	private void generateIds(long count) {
		long end = cursor + count;
		for (long i = cursor; i < end; i++) {
			availableNumbers.addLast(i);
		}
		this.cursor = end;
	}
}
