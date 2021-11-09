package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.*;

import com.github.tix320.kiwi.observable.scheduler.internal.DefaultSchedulerThreadFactory;

public final class Schedulers {

	public static final Scheduler DEFAULT = new ExecutorServiceScheduler(
			new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2, TimeUnit.MINUTES, new SynchronousQueue<>(),
					new DefaultSchedulerThreadFactory("Default")));

	public static Scheduler fromExecutor(ExecutorService executorService) {
		return new ExecutorServiceScheduler(executorService);
	}

	public static Scheduler singleThreaded(String threadNamePrefix) {
		return new ExecutorServiceScheduler(
				Executors.newSingleThreadScheduledExecutor(new DefaultSchedulerThreadFactory(threadNamePrefix)));
	}
}
