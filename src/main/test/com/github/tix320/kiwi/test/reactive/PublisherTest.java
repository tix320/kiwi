package com.github.tix320.kiwi.test.reactive;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.internal.reactive.CompletedException;
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
		assertThrows(CompletedException.class, () -> publisher.publish(4));
	}

	@Test
	void unsubscribeOnPublishTest() {
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
		publisher.publish(2);
		assertEquals(expected, actual);
	}

	@Test
	void unsubscribeFromOtherSubscriptionOnPublishTest() {
		Publisher<Integer> publisher = Publisher.simple();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr(new PrintStream(baos));

		List<Integer> expected = List.of(10, 50);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable = publisher.asObservable();
		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
		observable.subscribe(integer -> {
			actual.add(integer * 10);
			subscriptionHolder.get().unsubscribe();
		});

		observable.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set)
				.onPublish(integer -> actual.add(integer * 20)));

		publisher.publish(1);
		publisher.publish(5);

		String output = baos.toString();
		System.out.println(output);
		assertFalse(output.contains("SubscriptionIllegalStateException"));
		assertEquals(expected, actual);
	}

	@Test
	void completeOnPublishTest() {
		Publisher<Integer> publisher = Publisher.simple();

		List<Integer> expected = List.of(1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(integer -> {
			actual.add(integer);
			publisher.complete();
		});

		publisher.publish(1);
		assertThrows(CompletedException.class, () -> publisher.publish(2));
		assertEquals(expected, actual);
	}

	@Test
	void completeOnPublishWithTwoSubscribersTest() {
		Publisher<Integer> publisher = Publisher.simple();

		List<Integer> expected = List.of(10);
		List<Integer> actual = new ArrayList<>();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr(new PrintStream(baos));

		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(integer -> {
			actual.add(integer * 10);
			publisher.complete();
		});
		observable.subscribe(integer -> actual.add(integer * 20));

		publisher.publish(1);
		assertThrows(CompletedException.class, () -> publisher.publish(2));

		String output = baos.toString();
		System.out.println(output);
		assertFalse(output.contains("SubscriptionIllegalStateException"));
		assertEquals(expected, actual);
	}
}
