package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class UntilObservableTest {

	@Test
	void takeUntilAlreadyCompletedObservableTest() {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				return true;
			}

			@Override
			public boolean onPublish(Integer item) {
				called.add("onPublish");
				return false;
			}

			@Override
			public void onComplete(CompletionType completionType) {
				called.add("onComplete");
			}
		});


		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}

	@Test
	void publishOnSubscribeWithTakeUntilTest() {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
				return true;
			}

			@Override
			public boolean onPublish(Integer item) {
				called.add("onPublish");
				return false;
			}

			@Override
			public void onComplete(CompletionType completionType) {
				called.add("onComplete");
			}
		});


		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}

	@Test
	void takeUntilObservableTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
				return true;
			}

			@Override
			public boolean onPublish(Integer item) {
				called.add("onPublish");
				return false;
			}

			@Override
			public void onComplete(CompletionType completionType) {
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
