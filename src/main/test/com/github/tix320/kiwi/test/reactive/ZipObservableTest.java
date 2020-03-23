package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.collection.Tuple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipObservableTest {

	@Test
	void zipTest() {
		List<Integer> expected = Arrays.asList(10, 20, -1, 30, 50, -1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10, 30, 40);

		Observable<Integer> observable2 = Observable.of(20, 50);

		Observable.zip(observable1, observable2).subscribe(integers -> {
			actual.add(integers.first());
			actual.add(integers.second());
			actual.add(-1);
		});

		assertEquals(expected, actual);
	}

	@Test
	void zipTestWithMono() {
		List<Integer> expected = Arrays.asList(10, 50, -1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10, 30, 40);
		Observable<Integer> observable2 = Observable.of(50, 60);

		Observable.zip(observable1, observable2.toMono()).subscribe(integers -> {
			actual.add(integers.first());
			actual.add(integers.second());
			actual.add(-1);
		});

		assertEquals(expected, actual);
	}

	@Test
	void zipOnCompleteTest() {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(6, 4), Arrays.asList(9, 7),
				Collections.singletonList(25));
		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.single(4);

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(Subscriber.<Tuple<Integer, Integer>>builder().onPublish(
						o -> actual.add(List.of(o.first(), o.second())))
						.onComplete(() -> actual.add(Collections.singletonList(25))));

		publisher1.publish(6);
		publisher2.publish(7);

		publisher1.publish(9);

		publisher2.complete();

		publisher1.publish(10);

		assertEquals(expected, actual);
	}

	@Test
	void zipCompleteObservableWhichHasItemInQueueTest() {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(6, 4), Arrays.asList(9, 7), Arrays.asList(10, 20),
				Collections.singletonList(25));
		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.single(4);

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(Subscriber.<Tuple<Integer, Integer>>builder().onPublish(
						o -> actual.add(List.of(o.first(), o.second())))
						.onComplete(() -> actual.add(Collections.singletonList(25))));

		publisher1.publish(6);
		publisher2.publish(7);

		publisher1.publish(9);

		publisher2.publish(20);

		publisher2.complete();

		publisher1.publish(10);

		assertEquals(expected, actual);
	}


	@Test
	void zipCompleteObservableComplexTest() {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(1, 3), Arrays.asList(2, 4),
				Collections.singletonList(25));

		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(Subscriber.<Tuple<Integer, Integer>>builder().onPublish(
						o -> actual.add(List.of(o.first(), o.second())))
						.onComplete(() -> actual.add(Collections.singletonList(25))));

		publisher1.publish(1);
		publisher1.publish(2);

		publisher1.complete();
		publisher2.publish(3);
		publisher2.publish(4);

		publisher2.publish(20);

		assertEquals(expected, actual);
	}

}
