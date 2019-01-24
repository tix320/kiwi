package io.titix.kiwi.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Tigran.Sargsyan on 14-Dec-18
 */
public final class Threads {

	private Threads() {
		throw new IllegalStateException("No no no");
	}

	public static void runAsync(Runnable runnable) {
		new Thread(runnable).start();
	}

	public static void runAsync(Runnable runnable, ExecutorService executorService) {
		Future<?> future = executorService.submit(runnable);
		handleFutureEx(future);
	}

	public static void runDaemon(Runnable runnable) {
		daemon(runnable).start();
	}

	public static Thread daemon(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}

	public static void handleFutureEx(Future<?> future) {
		runDaemon(() -> {
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
}
