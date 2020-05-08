package com.github.tix320.kiwi.api.reactive.publisher;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	@Override
	protected boolean onNewSubscriber(InternalSubscription subscription) {
		return true;
	}
}
