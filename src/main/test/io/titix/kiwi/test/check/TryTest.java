package io.titix.kiwi.test.check;

import java.util.concurrent.atomic.AtomicBoolean;

import io.titix.kiwi.check.Try;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class TryTest {

	@Test
	void runTest() {
		AtomicBoolean value = new AtomicBoolean();
		Try.run(() -> value.set(true));
		assertTrue(value.get());
	}

	@Test
	void successTest() {
		Boolean value = Try.success(() -> true).get();
		assertTrue(value);
	}

	@Test
	void failureTest() {
		Object o = Try.failure(IllegalStateException::new).get();
		assertTrue(o instanceof);
	}
}
