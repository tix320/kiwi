package io.titix.kiwi.test.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import io.titix.kiwi.util.Threads;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tix32 on 23-Feb-19
 */
class ThreadsTest {

	@Test
	void noInstance() {
		assertThrows(InvocationTargetException.class, () -> {
			Constructor<Threads> constructor = Threads.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			constructor.newInstance();
		});
	}

	@Test
	void runAsyncWithExecutor() throws InterruptedException {
		AtomicReference<String> test = new AtomicReference<>();
		Threads.runAsync(() -> test.set("aaa"), Executors.newSingleThreadExecutor());
		assertNull(test.get());

		Thread.sleep(100);
		assertEquals("aaa", test.get());
	}

	@Test
	void runDaemon() throws InterruptedException {
		AtomicReference<String> test = new AtomicReference<>();
		Threads.runDaemon(() -> test.set("aaa"));

		Thread.sleep(100);
		assertEquals("aaa", test.get());
	}

	@Test
	void daemon() throws InterruptedException { //TODO , redesign test
		AtomicReference<String> test = new AtomicReference<>();
		Thread thread = Threads.daemon(() -> test.set("aaa"));

		assertNull(test.get());
		assertTrue(thread.isDaemon());

		thread.start();

		Thread.sleep(100);
		assertEquals("aaa", test.get());
	}

	@Test
	void handleFutureEx() {
		Threads.handleFutureEx(CompletableFuture.failedFuture(new IllegalStateException("catching")));
	}

	@Test
	void handleFutureExWithHandler() {
		Threads.handleFutureEx(CompletableFuture.failedFuture(new IllegalStateException("catching")),
				throwable -> assertEquals("catching", throwable.getMessage()));
	}
}
