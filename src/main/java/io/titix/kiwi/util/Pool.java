package io.titix.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * @author tix32 on 05-Jan-19
 */
public final class Pool<O> {

	private final Queue<O> pool;

	public Pool() {
		this.pool = new ConcurrentLinkedQueue<>();
	}

	public O getProxies(Supplier<O> factory) {
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
