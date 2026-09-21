package com.github.tix320.kiwi.utils;

import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import com.github.tix320.kiwi.observable.scheduler.internal.DefaultSchedulerThreadFactory;
import com.github.tix320.skimp.exception.ExceptionUtils;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestScheduler implements Scheduler {

	private final ExecutorService executor = Executors.newCachedThreadPool(
		new DefaultSchedulerThreadFactory("Kiwi-Test-Scheduler"));
	private final ScheduledExecutorService delayedExecutor = Executors.newSingleThreadScheduledExecutor(
		new DefaultSchedulerThreadFactory("Kiwi-Test-Delayed-Scheduler"));
	private final AtomicInteger activeTasks = new AtomicInteger();
	private final Object idleMonitor = new Object();

	@Override
	public void schedule(Runnable task) {
		activeTasks.incrementAndGet();
		try {
			executor.execute(() -> {
				try {
					task.run();
				} catch (Exception exception) {
					ExceptionUtils.applyToUncaughtExceptionHandler(exception);
				} finally {
					markTaskFinished();
				}
			});
		} catch (RuntimeException exception) {
			markTaskFinished();
			throw exception;
		}
	}

	@Override
	public void schedule(long delay, TimeUnit unit, Runnable task) {
		delayedExecutor.schedule(() -> schedule(task), delay, unit);
	}

	@Override
	public void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable task) {
		delayedExecutor.scheduleAtFixedRate(() -> schedule(task), initialDelay, period, unit);
	}

	@Override
	public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit, Runnable task) {
		delayedExecutor.scheduleWithFixedDelay(() -> schedule(task), initialDelay, delay, unit);
	}

	public boolean awaitIdle(Duration timeout) throws InterruptedException {
		long deadline = System.nanoTime() + timeout.toNanos();
		CountDownLatch delayedBarrier = new CountDownLatch(1);
		delayedExecutor.schedule(delayedBarrier::countDown, 0, TimeUnit.NANOSECONDS);
		if (!delayedBarrier.await(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
			return false;
		}

		synchronized (idleMonitor) {
			while (activeTasks.get() != 0) {
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					return false;
				}
				TimeUnit.NANOSECONDS.timedWait(idleMonitor, remaining);
			}
		}
		return true;
	}

	public boolean shutdown(Duration timeout) throws InterruptedException {
		delayedExecutor.shutdownNow();
		boolean delayedTerminated = delayedExecutor.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS);
		boolean idle = awaitExecutorIdle(timeout);
		executor.shutdown();
		return delayedTerminated && idle && executor.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS);
	}

	private void markTaskFinished() {
		if (activeTasks.decrementAndGet() == 0) {
			synchronized (idleMonitor) {
				idleMonitor.notifyAll();
			}
		}
	}

	private boolean awaitExecutorIdle(Duration timeout) throws InterruptedException {
		long deadline = System.nanoTime() + timeout.toNanos();
		synchronized (idleMonitor) {
			while (activeTasks.get() != 0) {
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					return false;
				}
				TimeUnit.NANOSECONDS.timedWait(idleMonitor, remaining);
			}
		}
		return true;
	}
}
