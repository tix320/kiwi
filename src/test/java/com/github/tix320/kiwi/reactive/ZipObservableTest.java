package com.github.tix320.kiwi.reactive;

import java.util.*;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.skimp.api.collection.Tuple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipObservableTest {

	@Test
	public void zipTest() throws InterruptedException {
		Set<Tuple<Integer, Integer>> expected = Set.of(new Tuple<>(10, 20), new Tuple<>(30, 50));
		Set<Tuple<Integer, Integer>> actual = Collections.synchronizedSet(new HashSet<>());

		Observable<Integer> observable1 = Observable.of(10, 30, 40);

		Observable<Integer> observable2 = Observable.of(20, 50);

		Observable.zip(observable1, observable2).subscribe(actual::add);

		Thread.sleep(200);

		assertEquals(expected, actual); //FIXME <[[10,20], [30,50]]> but was: <[[10,20]]>
	}

	@Test
	public void zipTestWithMono() throws InterruptedException {
		List<Integer> expected = Arrays.asList(10, 50, -1);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Observable<Integer> observable1 = Observable.of(10, 30, 40);
		Observable<Integer> observable2 = Observable.of(50, 60);

		Observable.zip(observable1, observable2.toMono()).subscribe(integers -> {
			actual.add(integers.first());
			actual.add(integers.second());
			actual.add(-1);
		});

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void zipOnCompleteTest() throws InterruptedException {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(6, 4), Arrays.asList(9, 7),
				Collections.singletonList(25));
		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = new SinglePublisher<>(4);

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(Subscriber.<Tuple<Integer, Integer>>builder().onPublish(
						o -> actual.add(List.of(o.first(), o.second())))
						.onComplete((completionType) -> actual.add(Collections.singletonList(25))));

		publisher1.publish(6);
		publisher2.publish(7);

		publisher1.publish(9);

		publisher2.complete();

		publisher1.publish(10);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void zipCompleteObservableWhichHasItemInQueueTest() throws InterruptedException {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(6, 4), Arrays.asList(9, 7), Arrays.asList(10, 20),
				Collections.singletonList(25));
		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = new SinglePublisher<>(4);

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(Subscriber.<Tuple<Integer, Integer>>builder().onPublish(
						o -> actual.add(List.of(o.first(), o.second())))
						.onComplete((completionType) -> actual.add(Collections.singletonList(25))));

		publisher1.publish(6);
		publisher2.publish(7);

		publisher1.publish(9);

		publisher2.publish(20);

		publisher2.complete();

		publisher1.publish(10);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}


	@Test
	public void zipCompleteObservableComplexTest() throws InterruptedException {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(1, 3), Arrays.asList(2, 4),
				Collections.singletonList(25));

		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(Subscriber.<Tuple<Integer, Integer>>builder().onPublish(
						o -> actual.add(List.of(o.first(), o.second())))
						.onComplete((completionType) -> actual.add(Collections.singletonList(25))));

		publisher1.publish(1);
		publisher1.publish(2);

		publisher1.complete();
		publisher2.publish(3);
		publisher2.publish(4);

		publisher2.publish(20);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

}
