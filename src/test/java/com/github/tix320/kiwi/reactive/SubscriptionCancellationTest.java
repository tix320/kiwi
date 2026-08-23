package com.github.tix320.kiwi.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class SubscriptionCancellationTest {

	@Test
	public void cancelDoesNotWaitAndSuppressesQueuedCallbacks() throws Exception {
		Publisher<Integer> publisher = Publisher.simple();
		CountDownLatch callbackStarted = new CountDownLatch(1);
		CountDownLatch releaseCallback = new CountDownLatch(1);
		List<Integer> received = new CopyOnWriteArrayList<>();

		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				received.add(item);
				callbackStarted.countDown();
				try {
					releaseCallback.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
		};

		publisher.asObservable().subscribe(subscriber);
		publisher.publish(1);
		assertTrue(callbackStarted.await(5, TimeUnit.SECONDS));

		publisher.publish(2);
		subscriber.subscription().cancel();
		releaseCallback.countDown();

		SchedulerUtils.awaitTermination();
		assertEquals(List.of(1), received);
	}

	@Test
	public void cancelIsIdempotent() throws Exception {
		Publisher<Integer> publisher = Publisher.simple();
		AtomicInteger completions = new AtomicInteger();
		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
			@Override
			public void onComplete(Completion completion) {
				completions.incrementAndGet();
			}
		};
		publisher.asObservable().subscribe(subscriber);

		subscriber.subscription().cancel();
		subscriber.subscription().cancel();

		SchedulerUtils.awaitTermination();
		assertEquals(1, completions.get());
	}

	@Test
	public void cancellationFromOnSubscribePreventsRegistration() throws Exception {
		Publisher<Integer> publisher = Publisher.simple();
		AtomicInteger received = new AtomicInteger();

		publisher.asObservable().subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
				subscription.cancel();
			}

			@Override
			protected void onNext(Integer item) {
				received.incrementAndGet();
			}

			@Override
			protected void onComplete(Completion completion) {
			}
		});

		publisher.publish(1);
		SchedulerUtils.awaitTermination();
		assertEquals(0, received.get());
	}

}
