package com.github.tix320.kiwi.api.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * @author Tigran Sargsyan on 05-Jan-19
 */
public final class ObjectPool<O> {

	private final Queue<O> pool;

	private final Supplier<O> factory;

	public ObjectPool(Supplier<O> factory) {
		this.pool = new LinkedList<>();
		this.factory = factory;
	}

	public synchronized O get() {
		O object = pool.poll();
		if (object == null) {
			return factory.get();
		}
		return object;
	}

	public synchronized void release(O object) {
		pool.add(object);
	}
}
