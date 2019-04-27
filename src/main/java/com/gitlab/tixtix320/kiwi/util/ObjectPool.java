package com.gitlab.tixtix320.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * @author Tigran Sargsyan on 05-Jan-19
 */
public final class ObjectPool<O> {

	private final Queue<O> pool;

	private final Supplier<O> factory;

	public ObjectPool(Supplier<O> factory) {
		this.pool = new ConcurrentLinkedQueue<>();
		this.factory = factory;
	}

	public O get() {
		O object = pool.poll();
		if (object == null) {
			return factory.get();
		}
		return object;
	}

	public void release(O object) {
		pool.add(object);
	}
}
