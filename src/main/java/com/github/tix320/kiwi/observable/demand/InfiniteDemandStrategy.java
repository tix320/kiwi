package com.github.tix320.kiwi.observable.demand;

public final class InfiniteDemandStrategy implements DemandStrategy {

	public static final InfiniteDemandStrategy INSTANCE = new InfiniteDemandStrategy();

	private InfiniteDemandStrategy() {
	}

	@Override
	public boolean needMore() {
		return true;
	}

	@Override
	public DemandStrategy applyNewValue(long count) {
		if (count == Long.MAX_VALUE) {
			return this;
		}

		return new FiniteDemandStrategy(count);
	}
}
