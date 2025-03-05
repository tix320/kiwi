package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.SinglePublisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class SinglePublisherTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<Integer> expected1 = List.of(10, 15, 20);
		List<Integer> expected2 = List.of(19, 24);

		List<Integer> actual1 = new CopyOnWriteArrayList<>();
		List<Integer> actual2 = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = new SinglePublisher<>(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual1::add);
		publisher.publish(15);
		observable.subscribe(number -> actual2.add(number + 4));
		publisher.publish(20);

		SchedulerUtils.awaitTermination();

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
	}

	@Test
	public void compareAndSetTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(10, 15, 20, 25);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

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

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void concurrentPublishTest() throws InterruptedException {
		List<Integer> actual = new CopyOnWriteArrayList<>();

		SinglePublisher<Integer> publisher = new SinglePublisher<>(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);

		IntStream.range(0, 100000).parallel().forEach(publisher::publish);
		publisher.publish(15);

		SchedulerUtils.awaitTermination();

		assertEquals(15, publisher.getValue());
		assertEquals(15, actual.get(actual.size() - 1));
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

		SchedulerUtils.awaitTermination();

		assertEquals(2, actual.size());
		assertEquals(expected, actual);
	}

	@Test
	public void subscribeAlreadyClosed() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.single();
		publisher.publish(3);
		publisher.complete();

		AtomicInteger holder = new AtomicInteger();

		publisher.asObservable().subscribe(holder::set);

		SchedulerUtils.awaitTermination();

		assertEquals(3, holder.get());
	}

}
