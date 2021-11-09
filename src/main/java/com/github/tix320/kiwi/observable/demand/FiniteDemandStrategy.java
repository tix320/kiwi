package com.github.tix320.kiwi.observable.demand;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public final class FiniteDemandStrategy implements DemandStrategy {

	private static final AtomicLongFieldUpdater<FiniteDemandStrategy> countUpdater = AtomicLongFieldUpdater.newUpdater(
			FiniteDemandStrategy.class, "count");

	private volatile long count;

	public FiniteDemandStrategy(long count) {
		this.count = count;
	}

	@Override
	public boolean needMore() {
		return countUpdater.getAndDecrement(this) != 0;
	}

	@Override
	public DemandStrategy applyNewValue(long count) {
		if (count == Long.MAX_VALUE) {
			return InfiniteDemandStrategy.INSTANCE;
		}

		return new FiniteDemandStrategy(countUpdater.get(this) + count);
	}
}
