package io.titix.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * @author tix32 on 24-Feb-19
 */
public final class PostPool<O> {

	private final Queue<O> pool;

	public PostPool() {
		this.pool = new ConcurrentLinkedQueue<>();
	}

	public O get(Supplier<O> factory) {
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
