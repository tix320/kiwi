package com.github.tix320.kiwi.publisher.internal;

import com.github.tix320.kiwi.observable.signal.PublishSignal;

public interface PublisherCursor {

	boolean hasNext();

	PublishSignal<?> next();

}
