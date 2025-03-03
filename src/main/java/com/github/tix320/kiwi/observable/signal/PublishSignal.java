package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.PUBLISH_PRIORITY;

public non-sealed class PublishSignal<T> extends Signal {

	private final T item;

	public PublishSignal(T item) {
		this.item = item;
	}

	public final T getItem() {
		return item;
	}

	@Override
	public int priority() {
		return PUBLISH_PRIORITY;
	}

}
