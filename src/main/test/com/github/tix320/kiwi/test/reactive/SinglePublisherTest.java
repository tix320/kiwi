package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SinglePublisherTest {

	@Test
	public void simpleTest() {
		List<Integer> expected = Arrays.asList(10, 15, 19, 20, 24);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = new SinglePublisher<>(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);
		publisher.publish(15);
		observable.subscribe(number -> actual.add(number + 4));
		publisher.publish(20);

		assertEquals(expected, actual);
	}

	@Test
	public void compareAndSetTest() {
		List<Integer> expected = Arrays.asList(10, 15, 20, 25);
		List<Integer> actual = new ArrayList<>();

		SinglePublisher<Integer> publisher = new SinglePublisher<>(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);
		publisher.publish(15);
		assertEquals(15, publisher.getValue());
		boolean changed = publisher.CASPublish(15, 20);
		assertEquals(20, publisher.getValue());
		assertTrue(changed);
		changed = publisher.CASPublish(20, 25);
		assertEquals(25, publisher.getValue());
		assertTrue(changed);
		changed = publisher.CASPublish(26, 30);
		assertEquals(25, publisher.getValue());
		assertFalse(changed);

		assertEquals(expected, actual);
	}

	@Test
	public void concurrentPublishTest() throws InterruptedException {
		int count = 1000000;

		Set<Integer> expected = IntStream.range(0, count).boxed().collect(Collectors.toSet());
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		SinglePublisher<Integer> publisher = new SinglePublisher<>(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);

		IntStream.range(0, count).parallel().forEach(publisher::publish);

		Thread.sleep(2000);
		assertEquals(count, actual.size());
		assertEquals(expected, actual);
	}

	@Test
	public void concurrentCompareAndSetTest() throws InterruptedException {
		int count = 1000000;

		Set<Integer> expected = Set.of(10, 72);
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		SinglePublisher<Integer> publisher = new SinglePublisher<>(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);

		Stream.generate(() -> null).limit(count).parallel().forEach(value -> publisher.CASPublish(10, 72));

		Thread.sleep(2000);
		assertEquals(2, actual.size());
		assertEquals(expected, actual);
	}
}
