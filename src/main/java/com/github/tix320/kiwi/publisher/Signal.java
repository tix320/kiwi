package com.github.tix320.kiwi.publisher;

import java.util.Objects;

import com.github.tix320.skimp.api.generator.IDGenerator;

public abstract class Signal implements Comparable<Signal> {

	private static final IDGenerator SIGNAL_ORDER_GENERATOR = new IDGenerator(0);

	private final long order;

	public Signal() {
		this.order = SIGNAL_ORDER_GENERATOR.next();
	}
	// private final Tracer  tracer;


	public final long order() {
		return order;
	}

	@Override
	public final int compareTo(Signal o) {
		return Long.compare(order, o.order);
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Signal signal = (Signal) o;
		return order == signal.order;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(order);
	}
}
