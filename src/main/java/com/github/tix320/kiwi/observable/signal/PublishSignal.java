package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.DefaultPriorities.PUBLISH_PRIORITY;

public non-sealed class PublishSignal<T> extends Signal {

	private final T item;

	public PublishSignal(T item) {
		this.item = item;
	}

	public final T getItem() {
		return item;
	}

	@Override
	public int defaultPriority() {
		return PUBLISH_PRIORITY;
	}

	@Override
	public final void accept(SignalVisitor signalVisitor) {
		signalVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "PublishSignal{" + item + '}';
	}

}
