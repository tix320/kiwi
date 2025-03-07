package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class ReplayPublisherTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<String> expected1 = List.of("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10", "a11", "a12");
		List<String> expected2 = List.of("b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "b10", "b11", "b12");
		List<String> expected3 = List.of("c2", "c3", "c4");
		List<String> expected4 = List.of("d6");

		List<String> actual1 = new CopyOnWriteArrayList<>();
		List<String> actual2 = new CopyOnWriteArrayList<>();
		List<String> actual3 = new CopyOnWriteArrayList<>();
		List<String> actual4 = new CopyOnWriteArrayList<>();

		Publisher<String> publisher = Publisher.buffered(5);
		Observable<String> observable = publisher.asObservable();

		observable.subscribe(s -> actual1.add("a" + s));
		publisher.publish("1");
		publisher.publish("2");
		publisher.publish("3");
		publisher.publish("4");
		publisher.publish("5");

		observable.subscribe(s -> actual2.add("b" + s));

		publisher.publish("6");

		observable.take(3).subscribe(s -> actual3.add("c" + s));

		publisher.publish("7");
		publisher.publish("8");
		publisher.publish("9");
		publisher.publish("10");

		observable.toMono().subscribe(s -> actual4.add("d" + s));

		publisher.publish("11");
		publisher.publish("12");

		SchedulerUtils.awaitTermination();

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected4, actual4);
	}

	@Test
	public void subscribeAfterPublishTest1() throws InterruptedException {
		List<Integer> expected = List.of(1, 2);

		List<Integer> actual = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.buffered(5);
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(1);
		publisher.publish(2);

		observable.subscribe(actual::add);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void subscribeAfterPublishTest2() throws InterruptedException {
		List<Integer> expected = List.of(2, 3);

		List<Integer> actual = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.buffered(2);
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);

		observable.subscribe(actual::add);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void concurrentTest() throws InterruptedException {
		Publisher<String> publisher = Publisher.buffered(15);
		Observable<String> observable = publisher.asObservable();

		int limit = 500;

		List<CopyOnWriteArrayList<String>> results = new ArrayList<>(limit);

		for (int i = 0; i < limit; i++) {
			results.add(new CopyOnWriteArrayList<>());
		}

		IntStream.range(0, 500).parallel().unordered().forEach(index -> {
			observable.subscribe(s -> results.get(index).add(index + ": " + s));
			publisher.publish("value" + index);
		});

		SchedulerUtils.awaitTermination();
	}

	@Test
	public void subscribeAlreadyClosed() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.buffered(5);
		publisher.publish(1);
		publisher.publish(2);
		publisher.complete();

		List<Integer> actual = new CopyOnWriteArrayList<>();

		publisher.asObservable().subscribe(actual::add);

		SchedulerUtils.awaitTermination();

		assertEquals(List.of(1, 2), actual);
	}

}
