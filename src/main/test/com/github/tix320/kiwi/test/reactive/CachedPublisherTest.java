package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.CachedPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CachedPublisherTest {

	@Test
	public void simpleTest() {
		List<Integer> expected = Arrays.asList(10, 20, 30, 60, 40, 120);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.cached();

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(10);

		observable.subscribe(actual::add);

		publisher.publish(20);

		observable.map(integer -> integer * 3).subscribe(actual::add);

		publisher.publish(40);

		assertEquals(expected, actual);
	}

	@Test //TODO
	public void concurrentPublishFromBufferAnyPublish() throws InterruptedException {
		int chunkSize = 100000;
		List<Integer> expectedPart1 = IntStream.rangeClosed(1, chunkSize).boxed().collect(Collectors.toList());
		List<Integer> expectedPart2 = IntStream.rangeClosed(chunkSize + 1, chunkSize * 2)
				.boxed()
				.collect(Collectors.toList());
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		CachedPublisher<Integer> publisher = new CachedPublisher<>();

		Observable<Integer> observable = publisher.asObservable();

		IntStream.rangeClosed(1, chunkSize).forEach(publisher::publish);

		int lol = (int) (chunkSize * 1.5);
		Thread thread = new Thread(() -> {
			IntStream.rangeClosed(chunkSize + 1, lol).forEach(publisher::publish);
		});
		thread.start();

		Thread thread1 = new Thread(() -> {
			IntStream.rangeClosed(lol + 1, chunkSize * 2).forEach(publisher::publish);
		});
		thread1.start();

		observable.subscribe(actual::add);

		thread.join();
		thread1.join();

		assertTrue(actual.size() >= chunkSize);
		assertEquals(expectedPart1, actual.subList(0, chunkSize));

	}
}

