package com.gitlab.tixtix320.kiwi.test.check;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gitlab.tixtix320.kiwi.check.Try;
import com.gitlab.tixtix320.kiwi.check.internal.TryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class TryTest {

	@Test
	void emptyTest() {
		assertTrue(Try.empty().isSuccess());
		assertFalse(Try.empty().isFailure());
		assertTrue(Try.empty().isUseless());
		assertFalse(Try.empty().isPresent());
	}

	@Test
	void successTest() {
		Integer value1 = Try.success(1).get();
		assertEquals(1, value1);
		Try<Integer> objectTry = Try.success((Integer) null);
		assertEquals(Try.empty(), objectTry);
	}

	@Test
	void successSupplierTest() {
		Integer value1 = Try.success(() -> 1).get();
		assertEquals(1, value1);
		Try objectTry = Try.success(() -> null);
		assertEquals(Try.empty(), objectTry);
		assertThrows(TryException.class, () -> Try.success(() -> {
			throw new IllegalCallerException();
		}));
	}

	@Test
	void failureTest() {
		assertTrue(Try.failure(new IllegalStateException()).isFailure());
	}

	@Test
	void failureSupplierTest() {
		assertTrue(Try.failure(IllegalStateException::new).isFailure());
		assertThrows(TryException.class, () -> Try.failure(() -> {
			throw new IllegalStateException();
		}));
	}

	@Test
	void runTest() {
		AtomicBoolean value = new AtomicBoolean();
		assertTrue(Try.run(() -> value.set(true)).isSuccess());
		assertTrue(value.get());

		assertTrue(Try.run(() -> {
			throw new IllegalStateException();
		}).isFailure());
	}

	@Test
	void supplyTest() {
		assertEquals(5, Try.supply(() -> 5).get());

		assertTrue(Try.supply(() -> {
			throw new IllegalStateException();
		}).isFailure());
	}


}
