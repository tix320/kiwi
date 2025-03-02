package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.publisher.internal.FlowPublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends FlowPublisher<T> {

	public SimplePublisher() {
		super(0);
	}

}
