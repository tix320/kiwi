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

	@Override
	public void beforeEach(ExtensionContext extensionContext) {
		exceptions = new CopyOnWriteArrayList<>();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> exceptions.add(e));
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) {
		assertThat(exceptions).isEmpty();
	}

}
