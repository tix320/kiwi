package com.github.tix320.kiwi.observable.signal;

public non-sealed class NextSignal<T> extends Signal {

	public static final int DEFAULT_PRIORITY = 50;

	private final T item;

	public NextSignal(T item) {this.item = item;}

	public final T getItem() {
		return item;
	}

	@Override
	public int defaultPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public final SignalVisitor.SignalVisitResult accept(SignalVisitor signalVisitor) {
		return signalVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "NextSignal{" + item + '}';
	}
}
