package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class DefaultScheduler {

	private static final AtomicReference<Scheduler> INSTANCE = new AtomicReference<>();

	static {
		INSTANCE.set(new ExecutorServiceScheduler(
				new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2, TimeUnit.MINUTES, new SynchronousQueue<>(),
						new PublisherThreadFactory())));
	}

	public static Scheduler get() {
		return INSTANCE.get();
	}

	public static void changeTo(Scheduler scheduler) {
		INSTANCE.set(scheduler);
	}
}
