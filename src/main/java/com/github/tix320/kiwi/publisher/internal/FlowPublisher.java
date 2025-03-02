package com.github.tix320.kiwi.publisher.internal;

import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.publisher.internal.util.SlidingWindowLinkedList;
import com.github.tix320.kiwi.publisher.internal.util.SlidingWindowLinkedList.Slider;
import java.util.List;

public abstract class FlowPublisher<T> extends BasePublisher<T> {

	private final SlidingWindowLinkedList<PublishSignal<T>> queue;

	protected FlowPublisher(int windowSize) {
		super();
		this.queue = new SlidingWindowLinkedList<>(windowSize);
	}

	@Override
	protected final PublisherCursor createCursor() {
		return new PublisherCursor() {

			private final Slider<PublishSignal<T>> slider = queue.slider();

			@Override
			public boolean hasNext() {
				return slider.hasNext();
			}

			@Override
			public PublishSignal<T> next() {
				return slider.next();
			}
		};
	}

	@Override
	protected final void onPublish(T item) {
		PublishSignal<T> publishSignal = new PublishSignal<>(item);
		queue.append(publishSignal);
	}

	protected List<PublishSignal<T>> getSnapshot() {
		return queue.getSnapshot();
	}

}
