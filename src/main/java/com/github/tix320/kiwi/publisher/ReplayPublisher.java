package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.publisher.internal.FlowPublisher;
import java.util.List;

/**
 * Buffered publisher for publishing objects.
 * Any publishing for this publisher will be buffered according to configured size,
 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
 *
 * @param <T> type of objects.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class ReplayPublisher<T> extends FlowPublisher<T> {

	public ReplayPublisher(int bufferCapacity) {
		super(bufferCapacity);
	}

	public List<T> getBuffer() {
		return getSnapshot().stream().map(PublishSignal::getItem).toList();
	}

}
