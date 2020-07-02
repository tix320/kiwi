package com.github.tix320.kiwi.internal.reactive.publisher;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @author Tigran Sargsyan on 23-Jun-20.
 */
public class ExceptionUtils {

	public static void applyToUncaughtExceptionHandler(Throwable t) {
		Thread thread = Thread.currentThread();
		UncaughtExceptionHandler uncaughtExceptionHandler = thread.getUncaughtExceptionHandler();
		try {
			uncaughtExceptionHandler.uncaughtException(thread, t);
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
