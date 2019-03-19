package io.titix.kiwi.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import io.titix.kiwi.function.CheckedRunnable;

/**
 * @author Tigran.Sargsyan on 14-Dec-18
 */

public final class Threads {

	private static final ExecutorService DAEMON_EXECUTOR = Executors.newCachedThreadPool(Threads::daemon);

	private Threads() {
		throw new IllegalStateException("No no no");
	}

	public static void runAsync(CheckedRunnable runnable, ExecutorService executorService) {
		Future<?> future = executorService.submit(() -> {
			runnable.run();
			return null;
		});
		handleFutureEx(future);
	}

	public static void runDaemon(Runnable runnable) {
		Future<?> future = DAEMON_EXECUTOR.submit(runnable);
		handleFutureEx(future);
	}

	public static Thread daemon(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}

	public static void handleFutureEx(Future<?> future) {
		DAEMON_EXECUTOR.execute(() -> {
			try {
				future.get();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			catch (ExecutionException e) {
				throw (RuntimeException) e.getCause();
			}
		});
	}

	public static void handleFutureEx(Future<?> future, Consumer<Throwable> errorHandler) {
		DAEMON_EXECUTOR.execute(() -> {
			try {
				future.get();
			}
			catch (InterruptedException e) {
				errorHandler.accept(e);
			}
			catch (ExecutionException e) {
				errorHandler.accept(e.getCause());
			}
		});
	}
}
