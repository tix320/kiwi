package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SkipObservableTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<Integer> expected = List.of(6, 7);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());
		Observable.of(4, 5, 6, 7).skip(2).subscribe(actual::add);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void completeBeforeSkipAll() {
		List<Integer> expected = List.of();
		List<Integer> actual = new ArrayList<>();
		Publisher<Integer> publisher = Publisher.simple();


		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
		publisher.asObservable()
				.skip(2)
				.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set).onPublish(actual::add));

		publisher.publish(4);
		publisher.publish(5);
		subscriptionHolder.get().unsubscribe();
		publisher.publish(7);

		assertEquals(expected, actual);
	}
}
