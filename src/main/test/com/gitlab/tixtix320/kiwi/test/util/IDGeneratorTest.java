package com.gitlab.tixtix320.kiwi.test.util;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.gitlab.tixtix320.kiwi.util.IDGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
class IDGeneratorTest {

	@Test
	void simpleTest() {
		IDGenerator generator = new IDGenerator();

		assertEquals(Long.MIN_VALUE, generator.next());

		assertEquals(Long.MIN_VALUE + 1, generator.next());
		assertEquals(Long.MIN_VALUE + 2, generator.next());

		generator.release(Long.MIN_VALUE + 1);

		assertEquals(Long.MIN_VALUE + 1, generator.next());
		assertEquals(Long.MIN_VALUE + 3, generator.next());
	}

	@Test
	void exceptionTest() {
		IDGenerator generator = new IDGenerator();

		assertEquals(Long.MIN_VALUE, generator.next());

		assertThrows(IllegalArgumentException.class, () -> generator.release(5));
	}

	@Test
	void concurrentTest() {
		IDGenerator generator = new IDGenerator();

		int count = 500000;

		Set<Long> generated = new ConcurrentSkipListSet<>();

		Stream.generate(() -> null).limit(count).parallel().forEach(o -> generated.add(generator.next()));

		assertEquals(count, generated.size());
		Set<Long> expected = LongStream.range(Long.MIN_VALUE, Long.MIN_VALUE + count)
				.boxed()
				.collect(Collectors.toSet());
		assertEquals(expected, generated);
	}
}
