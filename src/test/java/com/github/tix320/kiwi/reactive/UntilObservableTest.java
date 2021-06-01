package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.skimp.api.object.None;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class UntilObservableTest {

	@Test
	public void takeUntilAlreadyCompletedObservableTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = Collections.synchronizedList(new ArrayList<>());

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
			}

			@Override
			public void onPublish(Integer item) {
				called.add("onPublish");
				return RegularUnsubscription.DEFAULT;
			}

			@Override
			public void onComplete(Completion completion) {
				called.add("onComplete");
			}
		});

		Thread.sleep(100);

		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}

	@Test
	public void publishOnSubscribeWithTakeUntilTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = Collections.synchronizedList(new ArrayList<>());

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
			}

			@Override
			public void onPublish(Integer item) {
				called.add("onPublish");
				return RegularUnsubscription.DEFAULT;
			}

			@Override
			public void onComplete(Completion completion) {
				called.add("onComplete");
			}
		});

		Thread.sleep(100);

		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}

	@Test
	public void takeUntilObservableTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
			}

			@Override
			public void onPublish(Integer item) {
				called.add("onPublish");
				return RegularUnsubscription.DEFAULT;
			}

			@Override
			public void onComplete(Completion completion) {
				called.add("onComplete");
			}
		});

		untilPublisher.complete();

		Thread.sleep(100);

		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}
}
