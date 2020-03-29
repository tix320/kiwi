package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class TakeWhileObservableTest {

	@Test
	public void simpleTest() {
		List<Integer> expected = List.of(3, 4, 5);
		List<Integer> actual = Observable.of(3, 4, 5, 7).takeWhile(integer -> integer < 6).toList().get();
		assertEquals(expected, actual);
	}

	@Test
	public void withUnsubscribeTest() {
		List<Integer> expected = List.of(3, 4);
		List<Integer> actual = new ArrayList<>();

		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable()
				.takeWhile(integer -> integer < 6)
				.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set).onPublish(actual::add));

		publisher.publish(3);
		publisher.publish(4);
		subscriptionHolder.get().unsubscribe();
		publisher.publish(5);

		assertEquals(expected, actual);
	}
}
