package com.github.tix320.kiwi.publisher.internal;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.observable.signal.Signal;

public abstract class FlowPublisher<T> extends BasePublisher<T> {

	protected final List<PublishSignal<T>> queue = new ArrayList<>();

	@Override
	protected final PublisherCursor publishIterator() {
		return new PublisherCursor() {

			volatile int cursor;

			{
				synchronized (lock) {
					cursor = initialCursor();
				}
			}

			@Override
			public Signal get() {
				synchronized (lock) {
					if (cursor < queue.size()) {
						return queue.get(cursor);
					}
					else {
						if (completion != null && cursor == queue.size()) {
							return completion;
						}
						else {
							return null;
						}

					}
				}
			}

			@Override
			public void moveForward() {
				synchronized (lock) {
					cursor++;
				}
			}
		};
	}

	protected abstract int initialCursor();
}
