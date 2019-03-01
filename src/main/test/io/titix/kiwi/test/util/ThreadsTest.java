package io.titix.kiwi.test.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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
	void runAsync() throws InterruptedException {
		var test = new Object() {
			String $;
		};
		Threads.runAsync(() -> test.$ = "aaa");
		assertNull(test.$);

		Thread.sleep(100);
		assertEquals("aaa", test.$);
	}

	@Test
	void runAsyncWithExecutor() throws InterruptedException {
		var test = new Object() {
			String $;
		};
		Threads.runAsync(() -> test.$ = "aaa", Executors.newSingleThreadExecutor());
		assertNull(test.$);

		Thread.sleep(100);
		assertEquals("aaa", test.$);
	}

	@Test
	void runDaemon() throws InterruptedException {
		var test = new Object() {
			String $;
		};
		Threads.runDaemon(() -> test.$ = "aaa");
		assertNull(test.$);

		Thread.sleep(100);
		assertEquals("aaa", test.$);
	}

	@Test
	void daemon() throws InterruptedException {
		var test = new Object() {
			String $;
		};
		Thread thread = Threads.daemon(() -> test.$ = "aaa");

		assertNull(test.$);
		assertTrue(thread.isDaemon());

		thread.start();

		Thread.sleep(100);
		assertEquals("aaa", test.$);
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
