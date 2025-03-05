package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import com.github.tix320.skimp.object.None;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class UntilObservableTest {

	@Test
	public void publishTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();

		List<Integer> expected = List.of(1, 2);
		List<Integer> actual = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(actual::add);

		publisher.publish(1);
		publisher.publish(2);

		untilPublisher.publish(None.SELF);

		publisher.publish(3);
		publisher.publish(4);
		publisher.complete();

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void publishAndCompleteTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();

		List<Integer> expected = List.of(1, 2);
		List<Integer> actual = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(actual::add);

		publisher.publish(1);
		publisher.publish(2);

		untilPublisher.publish(None.SELF);
		untilPublisher.complete();

		publisher.publish(3);
		publisher.publish(4);
		publisher.complete();

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void completeTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();

		List<Integer> expected = List.of(1, 2);
		List<Integer> actual = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(actual::add);

		publisher.publish(1);
		publisher.publish(2);

		untilPublisher.complete();

		publisher.publish(3);
		publisher.publish(4);
		publisher.complete();

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void takeUntilAlreadyCompletedObservableTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = Collections.synchronizedList(new ArrayList<>());

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
			}

			@Override
			public void onNext(Integer item) {
				called.add("onPublish");
				subscription().cancel(Unsubscription.DEFAULT);
			}

			@Override
			public void onComplete(Completion completion) {
				called.add("onComplete");
			}
		});

		SchedulerUtils.awaitTermination();

		assertEquals(List.of("onSubscribe", "onComplete"), called);
	}

	@Test
	public void publishOnSubscribeWithTakeUntilTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = Collections.synchronizedList(new ArrayList<>());

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
			}

			@Override
			public void onNext(Integer item) {
				called.add("onPublish");
			}

			@Override
			public void onComplete(Completion completion) {
				called.add("onComplete");
			}
		});

		SchedulerUtils.awaitTermination();

		assertEquals(List.of("onSubscribe", "onComplete"), called);
	}

	@Test
	public void takeUntilObservableTest() throws InterruptedException {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
				subscription.request(1);
			}

			@Override
			public void onNext(Integer item) {
				called.add("onPublish");
				subscription().cancel(Unsubscription.DEFAULT);
			}

			@Override
			public void onComplete(Completion completion) {
				called.add("onComplete");
			}
		});

		untilPublisher.complete();

		SchedulerUtils.awaitTermination();

		assertEquals(List.of("onSubscribe", "onPublish", "onComplete"), called);
	}

}
