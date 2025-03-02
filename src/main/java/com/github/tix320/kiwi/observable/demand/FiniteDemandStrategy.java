package com.github.tix320.kiwi.observable.demand;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public final class FiniteDemandStrategy implements DemandStrategy {

	private static final AtomicLongFieldUpdater<FiniteDemandStrategy> COUNT_HANDLE = AtomicLongFieldUpdater.newUpdater(
		FiniteDemandStrategy.class, "count");

	private volatile long count;

	public FiniteDemandStrategy(long count) {
		this.count = count;
	}

	@Override
	public boolean needMore() {
		return COUNT_HANDLE.get(this) != 0;
	}

	@Override
	public void decrement() {
		COUNT_HANDLE.decrementAndGet(this);
	}

	@Override
	public DemandStrategy addBound(long count) {
		long current = COUNT_HANDLE.get(this);
		try {
			long newVal = Math.addExact(current, count);
			return new FiniteDemandStrategy(newVal);
		} catch (ArithmeticException e) {
			return InfiniteDemandStrategy.INSTANCE;
		}
	}

}
