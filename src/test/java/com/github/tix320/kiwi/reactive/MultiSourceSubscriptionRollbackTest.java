package com.github.tix320.kiwi.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.transform.multiple.internal.FirstOfAllObservable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

public class MultiSourceSubscriptionRollbackTest {

	@Test
	public void mergeRollsBack() {
		assertRollback(Observable::merge);
	}

	@Test
	public void zipRollsBack() {
		assertRollback(Observable::zip);
	}

	@Test
	public void combineLatestRollsBack() {
		assertRollback(Observable::combineLatest);
	}

	@Test
	public void firstOfAllRollsBack() {
		assertRollback((first, second) -> new FirstOfAllObservable<>(List.of(first, second)));
	}

	@Test
	public void takeUntilRollsBack() {
		IllegalStateException subscriptionFailure = new IllegalStateException("Subscription failed");
		TrackingObservable until = new TrackingObservable(false);
		Observable<?> result = failingObservable(subscriptionFailure).takeUntil(until);

		IllegalStateException actual = assertThrows(IllegalStateException.class,
			() -> result.subscribe(inertSubscriber()));

		assertSame(subscriptionFailure, actual);
		assertEquals(1, until.cancellations.get());
	}

	@Test
	public void cancellationFailureIsSuppressedUnderSubscriptionFailure() {
		IllegalStateException subscriptionFailure = new IllegalStateException("Subscription failed");
		TrackingObservable first = new TrackingObservable(true);
		Observable<Object> merged = Observable.merge(first, failingObservable(subscriptionFailure));

		IllegalStateException actual = assertThrows(IllegalStateException.class,
			() -> merged.subscribe(inertSubscriber()));

		assertSame(subscriptionFailure, actual);
		assertEquals(1, actual.getSuppressed().length);
		assertEquals(1, first.cancellations.get());
	}

	private static void assertRollback(
		BiFunction<Observable<Object>, Observable<Object>, Observable<?>> operator) {
		IllegalStateException subscriptionFailure = new IllegalStateException("Subscription failed");
		TrackingObservable first = new TrackingObservable(false);
		Observable<?> result = operator.apply(first, failingObservable(subscriptionFailure));

		IllegalStateException actual = assertThrows(IllegalStateException.class,
			() -> result.subscribe(inertSubscriber()));

		assertSame(subscriptionFailure, actual);
		assertEquals(1, first.cancellations.get());
	}

	private static Observable<Object> failingObservable(RuntimeException failure) {
		return new Observable<>() {
			@Override
			public void subscribe(Subscriber<? super Object> subscriber) {
				throw failure;
			}
		};
	}

	private static Subscriber<Object> inertSubscriber() {
		return new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
			}

			@Override
			protected void onNext(Object item) {
			}

			@Override
			protected void onComplete(Completion completion) {
			}
		};
	}

	private static final class TrackingObservable extends Observable<Object> {

		private final AtomicInteger cancellations = new AtomicInteger();
		private final boolean cancellationFails;

		private TrackingObservable(boolean cancellationFails) {
			this.cancellationFails = cancellationFails;
		}

		@Override
		public void subscribe(Subscriber<? super Object> subscriber) {
			subscriber.setSubscription(new Subscription() {
				@Override
				protected void onRequest(long count) {
				}

				@Override
				protected void onUnboundRequest() {
				}

				@Override
				protected void onCancel(Unsubscription unsubscription) {
					cancellations.incrementAndGet();
					if (cancellationFails) {
						throw new IllegalStateException("Cancellation failed");
					}
				}
			});
		}

	}

}
