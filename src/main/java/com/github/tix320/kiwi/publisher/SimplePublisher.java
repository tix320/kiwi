package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.publisher.internal.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	public SimplePublisher() {
		super(0, 10);
	}

	@Override
	protected final int resolveInitialCursorOnSubscribe() {
		return queueSize();
	}
}
