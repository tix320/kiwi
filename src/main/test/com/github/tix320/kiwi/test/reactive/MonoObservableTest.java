package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonoObservableTest {

	@Test
	public void toMonoTest() {
		List<Integer> expected = Collections.singletonList(3);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(actual::add);

		publisher.publish(3);
		publisher.publish(4);

		assertEquals(expected, actual);
	}

	@Test
	public void toMonoCompletedTest() {
		List<Integer> expected = Arrays.asList(3, 7);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		MonoObservable<Integer> observable = publisher.asObservable().toMono();
		observable.subscribe(Subscriber.<Integer>builder().onPublish(actual::add).onComplete((completionType) -> actual.add(7)));

		publisher.publish(3);

		assertEquals(expected, actual);
	}
}
