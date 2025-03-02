package com.github.tix320.kiwi.observable.demand;

public sealed interface DemandStrategy permits EmptyDemandStrategy, FiniteDemandStrategy, InfiniteDemandStrategy {

	boolean needMore();

	void decrement();

	DemandStrategy addBound(long count);

}
