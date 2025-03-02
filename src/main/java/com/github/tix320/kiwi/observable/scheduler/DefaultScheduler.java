package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.observable.scheduler.internal.InternalDefaultScheduler;

public final class DefaultScheduler {

	private static final AtomicReference<Scheduler> INSTANCE = new AtomicReference<>();

	static {
		INSTANCE.set(new InternalDefaultScheduler());
	}

	public static Scheduler get() {
		return INSTANCE.get();
	}

	public static void changeTo(Scheduler scheduler) {
		INSTANCE.set(scheduler);
	}
}
