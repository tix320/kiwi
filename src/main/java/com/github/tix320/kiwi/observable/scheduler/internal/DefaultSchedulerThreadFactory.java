package com.github.tix320.kiwi.observable.scheduler.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultSchedulerThreadFactory implements ThreadFactory {

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	private final String prefix;

	public DefaultSchedulerThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		String name = prefix + "-thread-" + threadNumber.getAndIncrement();
		Thread t = new Thread(runnable, name);
		t.setDaemon(true);
		return t;
	}

}
