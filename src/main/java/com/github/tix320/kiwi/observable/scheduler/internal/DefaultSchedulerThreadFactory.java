package com.github.tix320.kiwi.observable.scheduler.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultSchedulerThreadFactory implements ThreadFactory {

	public static final String THREAD_GROUP_NAME = "Kiwi-Scheduler";

	private static final ThreadGroup THREAD_GROUP = new ThreadGroup(THREAD_GROUP_NAME);

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	private final String prefix;

	public DefaultSchedulerThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		String name = THREAD_GROUP_NAME + "-" + prefix + "-thread-" + threadNumber.getAndIncrement();
		Thread t = new Thread(THREAD_GROUP, runnable, name);
		t.setDaemon(true);
		return t;
	}
}
