package com.github.tix320.kiwi.observable.scheduler.internal;

import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import com.github.tix320.skimp.api.exception.ExceptionUtils;
import com.github.tix320.skimp.api.thread.tracer.Tracer;
import com.github.tix320.skimp.api.thread.tracer.TrackableExecutorService;
import com.github.tix320.skimp.api.thread.tracer.TrackableScheduledExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class InternalDefaultScheduler implements Scheduler {

	private final ExecutorService executorService = TrackableExecutorService.wrap(
		new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
							   Math.min(Runtime.getRuntime().availableProcessors() * 4, 100), 2, TimeUnit.MINUTES,
							   new ArrayBlockingQueue<>(1000),
							   new DefaultSchedulerThreadFactory("Kiwi-Default-Scheduler"),
							   new RejectionHandler()));

	private final ScheduledExecutorService scheduledExecutorService = TrackableScheduledExecutorService.wrap(
		Executors.newSingleThreadScheduledExecutor(
			new DefaultSchedulerThreadFactory("Kiwi-Default-Delayed-Scheduler")));

	@Override
	public void schedule(Runnable task) {
		executorService.submit(() -> {
			try {
				task.run();
			} catch (Throwable e) {
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

	private static final class RejectionHandler implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			Thread currentThread = Thread.currentThread();
			System.err.printf("WARNING: Kiwi Internal Scheduler is full. Retrying after sleep of 3 seconds[%s]%n",
							  currentThread);
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
			executor.execute(r);
		}

	}

}
