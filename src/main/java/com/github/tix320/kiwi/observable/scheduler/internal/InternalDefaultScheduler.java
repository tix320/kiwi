package com.github.tix320.kiwi.observable.scheduler.internal;

import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import com.github.tix320.skimp.exception.ExceptionUtils;
import com.github.tix320.skimp.thread.tracer.Tracer;
import com.github.tix320.skimp.thread.tracer.TrackableExecutorService;
import com.github.tix320.skimp.thread.tracer.TrackableScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class InternalDefaultScheduler implements Scheduler {

	private final ExecutorService executorService = TrackableExecutorService.wrap(
		new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
							   Runtime.getRuntime().availableProcessors() * 4,
							   2, TimeUnit.MINUTES,
							   new LinkedBlockingQueue<>(),
							   new DefaultSchedulerThreadFactory("Kiwi-Default-Scheduler"),
							   new ThreadPoolExecutor.DiscardPolicy()));

	private final ScheduledExecutorService scheduledExecutorService = TrackableScheduledExecutorService.wrap(
		Executors.newSingleThreadScheduledExecutor(
			new DefaultSchedulerThreadFactory("Kiwi-Default-Delayed-Scheduler")));

	@Override
	public void schedule(Runnable task) {
		executorService.execute(() -> {
			try {
				task.run();
			} catch (Exception e) {
				Tracer.INSTANCE.injectFullStacktrace(e);
				ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}
		});
	}

	@Override
	public void schedule(long delay, TimeUnit unit, Runnable task) {
		scheduledExecutorService.schedule(() -> schedule(task), delay, unit);
	}

	@Override
	public void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable task) {
		scheduledExecutorService.scheduleAtFixedRate(() -> schedule(task), initialDelay, period, unit);
	}

	@Override
	public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit, Runnable task) {
		scheduledExecutorService.scheduleWithFixedDelay(() -> schedule(task), initialDelay, delay, unit);
	}

	public boolean shutdownAndAwaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		executorService.shutdown();
		scheduledExecutorService.shutdown();
		var mainTerminated = executorService.awaitTermination(timeout, unit);
		var delayedTerminated = scheduledExecutorService.awaitTermination(timeout, unit);
		return mainTerminated && delayedTerminated;
	}

}
