package com.github.tix320.kiwi.observable.scheduler;

import com.github.tix320.kiwi.observable.scheduler.internal.InternalDefaultScheduler;
import java.util.concurrent.atomic.AtomicReference;

public final class KiwiSchedulerHolder {

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
