package com.github.tix320.kiwi.publisher.internal;

import com.github.tix320.kiwi.observable.signal.Signal;

public interface PublisherCursor {

	Signal get();

	void moveForward();
}
