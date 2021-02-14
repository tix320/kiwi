package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonoObservableTest {

	@Test
	public void toMonoTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(3);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(actual::add);

		publisher.publish(3);
		publisher.publish(4);

		Thread.sleep(100);

		assertEquals(expected, actual); //FIXME expected: <[3]> but was: <[3, 4]>
	}

	@Test
	public void toMonoCompletedTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(3, 7);
		List<Integer> actual = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(
				Subscriber.<Integer>builder().onPublish(actual::add).onComplete((completionType) -> actual.add(7)));

		publisher.publish(3);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void exceptionOnPublishTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(3);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(integer -> {
			actual.add(integer);
			throw new IllegalStateException();
		});

		publisher.publish(3);
		publisher.publish(4);

		Thread.sleep(1000);

		assertEquals(expected, actual);
	}
}
