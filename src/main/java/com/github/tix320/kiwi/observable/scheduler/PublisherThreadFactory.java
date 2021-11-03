package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class PublisherThreadFactory implements ThreadFactory {

	public static final String THREAD_GROUP_NAME = "Kiwi-Reactive-Publisher";

	private static final ThreadGroup THREAD_GROUP = new ThreadGroup(THREAD_GROUP_NAME);

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	@Override
	public Thread newThread(Runnable runnable) {
		String name = THREAD_GROUP_NAME + "-thread-" + threadNumber.getAndIncrement();
		Thread t = new Thread(THREAD_GROUP, runnable, name);
		t.setDaemon(true);
		return t;
	}
}
