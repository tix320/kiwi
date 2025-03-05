package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class SimplePublisherTest {

	@Test
	public void oneObservableTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(6, 7);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(3);
		publisher.publish(4);

		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
			}
		};

		observable.subscribe(subscriber);

		publisher.publish(6);
		publisher.publish(7);

		SchedulerUtils.sleepFor(Duration.ofMillis(100));

		subscriber.subscription().cancel();

		publisher.publish(8);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void anyTypeObservables() throws InterruptedException {

		List<Integer> expected1 = List.of(7, 8, 9, 10, 11, 12, 13, 14, 15);
		List<Integer> expected2 = List.of(9, 10, 11, 12, 13, 14, 15);
		List<Integer> expected3 = List.of(10, 11, 12);
		List<Integer> expected4 = List.of(14);

		List<Integer> actual1 = new CopyOnWriteArrayList<>();
		List<Integer> actual2 = new CopyOnWriteArrayList<>();
		List<Integer> actual3 = new CopyOnWriteArrayList<>();
		List<Integer> actual4 = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(4);
		publisher.publish(5);
		publisher.publish(6);

		observable.subscribe(actual1::add);

		publisher.publish(7);
		publisher.publish(8);

		observable.subscribe(actual2::add);

		publisher.publish(9);

		observable.take(3).subscribe(actual3::add);

		publisher.publish(10);
		publisher.publish(11);
		publisher.publish(12);
		publisher.publish(13);

		observable.toMono().subscribe(actual4::add);

		publisher.publish(14);
		publisher.publish(15);

		SchedulerUtils.awaitTermination();

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected4, actual4);
	}

	@Test
	public void callAsObservableMore() throws InterruptedException {
		List<Integer> expected1 = List.of(3, 4, 5);
		List<Integer> expected2 = List.of(3, 4, 5);
		List<Integer> expected3 = List.of(3, 4, 5);
		List<Integer> expected4 = List.of(3, 4, 5);

		List<Integer> actual1 = new CopyOnWriteArrayList<>();
		List<Integer> actual2 = new CopyOnWriteArrayList<>();
		List<Integer> actual3 = new CopyOnWriteArrayList<>();
		List<Integer> actual4 = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable1 = publisher.asObservable();
		Observable<Integer> observable2 = publisher.asObservable();

		observable1.subscribe(actual1::add);
		observable1.subscribe(actual2::add);
		observable2.subscribe(actual3::add);
		observable2.subscribe(actual4::add);

		publisher.publish(3);
		publisher.publish(4);
		publisher.publish(5);

		SchedulerUtils.awaitTermination();

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected4, actual4);
	}

	@Test
	public void concurrentPublishTest() throws InterruptedException {
		int count = 100000;

		Set<Integer> expected = IntStream.range(0, count).boxed().collect(Collectors.toSet());
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		var publisher = Publisher.<Integer>simple();
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);

		IntStream.range(0, count).parallel().forEach(publisher::publish);

		SchedulerUtils.awaitTermination();

		assertEquals(count, actual.size());
		assertEquals(expected, actual);
	}

}
