package com.github.tix320.kiwi.extension;

import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.observable.scheduler.internal.InternalDefaultScheduler;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Tigran Sargsyan on 05.03.25
 */
public class KiwiSchedulerRefreshExtension implements BeforeEachCallback {

	@Override
	public void beforeEach(ExtensionContext extensionContext) {
		KiwiSchedulerHolder.changeTo(new InternalDefaultScheduler());
	}

}
