package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
class PublisherTest {

	@Test
	void completeTest() {
		Publisher<Integer> publisher = Publisher.simple();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);
		publisher.complete();
		assertThrows(PublisherCompletedException.class, () -> publisher.publish(4));
	}

	@Test
	void unsubscribeOnPublishTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		List<Integer> expected = List.of(1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable = publisher.asObservable();
		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
		observable.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set).onPublish(integer -> {
			actual.add(integer);
			subscriptionHolder.get().unsubscribe();
		}));

		publisher.publish(1);

		Thread.sleep(100);

		publisher.publish(2);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	void unsubscribeFromOtherSubscriptionOnPublishTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Set<Integer> expected = Set.of(10, 20, 50);
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		Observable<Integer> observable = publisher.asObservable();
		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
		observable.subscribe(integer -> {
			actual.add(integer * 10);
			subscriptionHolder.get().unsubscribe();
		});

		observable.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set)
				.onPublish(integer -> actual.add(integer * 20)));

		publisher.publish(1);

		Thread.sleep(100);

		publisher.publish(5);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	void completeOnPublishTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		List<Integer> expected = List.of(1);
		List<Integer> actual = new ArrayList<>();

		AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(Subscriber.<Integer>builder().onPublish(integer -> {
			actual.add(integer);
			publisher.complete();
		}).onComplete(completionType -> onCompleteCalled.set(true)));

		publisher.publish(1);

		Thread.sleep(200);
		assertThrows(PublisherCompletedException.class, () -> publisher.publish(2));

		assertEquals(expected, actual);
		assertTrue(onCompleteCalled.get());
	}

	@Test
	void completeOnPublishWithTwoSubscribersTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Set<Integer> expected = Set.of(10, 20);
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(integer -> {
			actual.add(integer * 10);
			publisher.complete();
		});
		observable.subscribe(integer -> actual.add(integer * 20));

		publisher.publish(1);

		Thread.sleep(100);

		assertThrows(PublisherCompletedException.class, () -> publisher.publish(2));
		assertEquals(expected, actual);
	}
}
