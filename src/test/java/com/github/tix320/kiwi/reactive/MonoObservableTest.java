package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.publisher.Publisher;
import org.junit.jupiter.api.Disabled;
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

		assertEquals(expected, actual);
	}

	@Test
	public void toMonoCompletedTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(3, 7);
		List<Integer> actual = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
			}

			@Override
			public void onComplete(Completion completion) {
				actual.add(7);
			}
		});

		publisher.publish(3);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	@Disabled
	public void exceptionOnPublishTest() throws InterruptedException {
		List<Integer> expected = List.of(3, 45);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
				throw new IllegalStateException();
			}

			// @Override
			// protected void onError(Throwable error) {
			// 	actual.add(45);
			// }
		});

		publisher.publish(3);
		publisher.publish(4);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}
}
