package com.github.tix320.kiwi.api.util;


import java.util.concurrent.ExecutorService;

import com.github.tix320.kiwi.api.util.LoopThread.LoopAction;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public final class Threads {

	public static LoopThread createLoopThread(LoopAction loopAction) {
		return new LoopThread(action -> new Thread(action).start(), loopAction);
	}

	public static LoopThread createLoopDaemonThread(LoopAction loopAction) {
		return new LoopThread(action -> daemon(action).start(), loopAction);
	}

	public static LoopThread createLoopThread(ExecutorService executorService, LoopAction loopAction) {
		return new LoopThread(executorService::submit, loopAction);
	}

	public static Thread daemon(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}
}
