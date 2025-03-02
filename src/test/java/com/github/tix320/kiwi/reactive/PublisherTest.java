package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.PublisherClosedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public class PublisherTest {

	@Test
	public void completeTest() {
		Publisher<Integer> publisher = Publisher.simple();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);
		publisher.complete();
		assertThrows(PublisherClosedException.class, () -> publisher.publish(4));
	}

	@Test
	public void unsubscribeOnPublishTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		List<Integer> expected = List.of(1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable = publisher.asObservable();

		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
				subscription().cancel();
			}
		};

		observable.subscribe(subscriber);

		publisher.publish(1);

		Thread.sleep(100);

		publisher.publish(2);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void completeOnPublishTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		List<Integer> expected = List.of(1);
		List<Integer> actual = new ArrayList<>();

		AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
				publisher.complete();
			}

			@Override
			public void onComplete(Completion completion) {
				onCompleteCalled.set(true);
			}
		});

		publisher.publish(1);

		Thread.sleep(100);
		assertThrows(PublisherClosedException.class, () -> publisher.publish(2));

		assertEquals(expected, actual);
		assertTrue(onCompleteCalled.get());
	}

	@Test
	public void completeOnPublishWithTwoSubscribersTest() throws InterruptedException {
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

		assertThrows(PublisherClosedException.class, () -> publisher.publish(2));
		assertEquals(expected, actual);
	}
}
