package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
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
			public void onPublish(Integer item) {
				actual.add(item);
			}
		};

		observable.subscribe(subscriber);

		publisher.publish(6);
		publisher.publish(7);

		subscriber.subscription().cancel();

		publisher.publish(8);

		Thread.sleep(100);

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

		Thread.sleep(100);

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

		Thread.sleep(100);

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected4, actual4);
	}
}
