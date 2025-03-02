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
	public void decrement() {
	}

	@Override
	public DemandStrategy addBound(long count) {
		return new FiniteDemandStrategy(count);
	}

}
