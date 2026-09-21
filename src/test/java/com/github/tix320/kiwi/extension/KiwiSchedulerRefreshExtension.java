package com.github.tix320.kiwi.extension;

import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import com.github.tix320.kiwi.utils.TestScheduler;
import java.time.Duration;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Tigran Sargsyan on 05.03.25
 */
public class KiwiSchedulerRefreshExtension implements BeforeEachCallback, AfterEachCallback {

	private Scheduler previousScheduler;

	@Override
	public void beforeEach(ExtensionContext extensionContext) {
		previousScheduler = KiwiSchedulerHolder.get();
		KiwiSchedulerHolder.changeTo(new TestScheduler());
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		Scheduler scheduler = KiwiSchedulerHolder.get();
		try {
			if (scheduler instanceof TestScheduler testScheduler && !testScheduler.shutdown(Duration.ofSeconds(30))) {
				throw new AssertionError("Test scheduler did not terminate after 30 seconds");
			}
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

}
