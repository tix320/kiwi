package com.github.tix320.kiwi.observable.signal;

import com.github.tix320.kiwi.observable.signal.SignalManager.SignalVisitResult;

public non-sealed class PublishSignal<T> extends Signal {

	public static final int DEFAULT_PRIORITY = 100;

	private final T item;

	public PublishSignal(T item) {this.item = item;}

	public final T getItem() {
		return item;
	}

	@Override
	public int defaultPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public final <R> R accept(SignalVisitor<R> signalVisitor) {
		return signalVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "NextSignal{" + item + '}';
	}
}
