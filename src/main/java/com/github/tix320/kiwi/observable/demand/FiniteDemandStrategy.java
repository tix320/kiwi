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
		return countUpdater.get(this) != 0;
	}

	@Override
	public boolean next() {
		long valueBeforeUpdate = countUpdater.getAndUpdate(this, value -> {
			if (value == 0) {
				return 0;
			}
			else {
				return value - 1;
			}
		});

		return valueBeforeUpdate != 0;
	}

	@Override
	public DemandStrategy addBound(long count) {
		long current = countUpdater.get(this);
		try {
			long newVal = Math.addExact(current, count);
			return new FiniteDemandStrategy(newVal);
		}
		catch (ArithmeticException e) {
			return InfiniteDemandStrategy.INSTANCE;
		}
	}
}
