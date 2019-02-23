package io.titix.kiwi.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tix32 on 23-Feb-19
 */
class ThreadsTest {

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
		Threads.handleFutureEx(CompletableFuture.completedFuture(null));
	}

	@Test
	void handleFutureExWithHandler() {
		Threads.handleFutureEx(CompletableFuture.failedFuture(new IllegalStateException("catching")),
				throwable -> assertEquals("catching", throwable.getMessage()));
	}
}
