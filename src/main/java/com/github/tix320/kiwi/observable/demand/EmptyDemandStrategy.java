package com.github.tix320.kiwi.observable.demand;

public final class EmptyDemandStrategy implements DemandStrategy {

	public static final EmptyDemandStrategy INSTANCE = new EmptyDemandStrategy();

	private EmptyDemandStrategy() {
	}

	@Override
	public boolean needMore() {
		return false;
	}

	@Override
	public DemandStrategy applyNewValue(long count) {
		if (count == Long.MAX_VALUE) {
			return InfiniteDemandStrategy.INSTANCE;
		}

		return new FiniteDemandStrategy(count);
	}
}
