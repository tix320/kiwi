package com.github.tix320.kiwi.utils;

import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.observable.scheduler.internal.InternalDefaultScheduler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tigran Sargsyan on 05.03.25
 */
public class SchedulerUtils {

	public static void sleepFor(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void awaitTermination() throws InterruptedException {
		Thread.sleep(100);
		var scheduler = (InternalDefaultScheduler) KiwiSchedulerHolder.get();
		var terminated = scheduler.shutdownAndAwaitTermination(30, TimeUnit.SECONDS);
		assertThat(terminated).isTrue().describedAs("Scheduler did not terminate after 30 seconds");
	}

}
