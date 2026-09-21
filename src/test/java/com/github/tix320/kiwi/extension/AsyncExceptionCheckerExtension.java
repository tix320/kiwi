package com.github.tix320.kiwi.extension;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Tigran Sargsyan on 05.03.25
 */
public class AsyncExceptionCheckerExtension implements BeforeEachCallback, AfterEachCallback {

	private List<Throwable> exceptions;
	private Thread.UncaughtExceptionHandler previousHandler;

	@Override
	public void beforeEach(ExtensionContext extensionContext) {
		exceptions = new CopyOnWriteArrayList<>();
		previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> exceptions.add(e));
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) {
		try {
			assertThat(exceptions).isEmpty();
		} finally {
			Thread.setDefaultUncaughtExceptionHandler(previousHandler);
		}
	}

}
