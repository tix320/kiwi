package com.github.tix320.kiwi.api.util;

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

	public static void appendAsyncStacktrace(StackTraceElement[] asyncRunnerStackTrace, Throwable throwable) {
		StackTraceElement[] realStacktrace = throwable.getStackTrace();

		StackTraceElement[] newStacktrace = new StackTraceElement[asyncRunnerStackTrace.length + realStacktrace.length];

		System.arraycopy(realStacktrace, 0, newStacktrace, 0, realStacktrace.length);
		System.arraycopy(asyncRunnerStackTrace, 0, newStacktrace, realStacktrace.length, asyncRunnerStackTrace.length);

		throwable.setStackTrace(newStacktrace);
	}
}
