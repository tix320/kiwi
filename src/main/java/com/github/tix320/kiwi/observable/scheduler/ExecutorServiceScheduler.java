package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.github.tix320.skimp.api.exception.ExceptionUtils;

public class ExecutorServiceScheduler implements Scheduler {

	private final ExecutorService executorService;

	public ExecutorServiceScheduler(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public CompletableFuture<Void> schedule(Runnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			}
			catch (Throwable e) {
				ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}
		}, executorService);
	}
}
