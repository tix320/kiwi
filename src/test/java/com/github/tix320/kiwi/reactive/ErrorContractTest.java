package com.github.tix320.kiwi.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import com.github.tix320.kiwi.observable.signal.CancelSignal;
import com.github.tix320.kiwi.observable.signal.ErrorSignal;
import com.github.tix320.kiwi.observable.signal.SignalSynchronizer.Token;
import com.github.tix320.kiwi.observable.transform.multiple.internal.FirstOfAllObservable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(KiwiSchedulerRefreshExtension.class)
public class ErrorContractTest {

	@Test
	public void publisherAbortTerminatesSubscriberWithOriginalError() throws Exception {
		Publisher<Integer> publisher = Publisher.simple();
		IllegalStateException expected = new IllegalStateException("source failed");
		AtomicReference<Throwable> actual = new AtomicReference<>();
		AtomicInteger completions = new AtomicInteger();

		publisher.asObservable().subscribe(new FlexibleSubscriber<>() {
			@Override
			protected void onError(Throwable error) {
				actual.set(error);
			}

			@Override
			public void onComplete(Completion completion) {
				completions.incrementAndGet();
			}
		});

		publisher.abort(expected);
		SchedulerUtils.awaitTermination();

		assertSame(expected, actual.get());
		assertEquals(0, completions.get());
	}

	@Test
	public void lateSubscriberReceivesPublisherError() throws Exception {
		Publisher<Object> publisher = Publisher.simple();
		IllegalArgumentException expected = new IllegalArgumentException("already failed");
		AtomicReference<Throwable> actual = new AtomicReference<>();
		publisher.abort(expected);

		publisher.asObservable().subscribe(new FlexibleSubscriber<>() {
			@Override
			protected void onError(Throwable error) {
				actual.set(error);
			}
		});

		SchedulerUtils.awaitTermination();
		assertSame(expected, actual.get());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("callbackOperators")
	public void operatorCallbackFailureIsReportedAndLaterItemsContinue(String name,
																 CallbackOperatorFactory factory) {
		IllegalStateException expected = new IllegalStateException("mapper failed");
		CapturingScheduler scheduler = new CapturingScheduler();
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			List<Integer> items = new ArrayList<>();
			AtomicInteger errors = new AtomicInteger();
			AtomicInteger completions = new AtomicInteger();

			factory.create(Observable.of(1, 2), expected).subscribe(new Subscriber<>() {
				@Override
				protected void onSubscribe(Subscription subscription) {
					subscription.request(1);
				}

				@Override
				protected void onNext(Integer item) {
					items.add(item);
				}

				@Override
				protected void onError(Throwable error) {
					errors.incrementAndGet();
				}

				@Override
				protected void onComplete(Completion completion) {
					completions.incrementAndGet();
				}
			});

			assertEquals(List.of(2), items);
			assertEquals(0, errors.get());
			assertEquals(1, completions.get());
			assertEquals(1, scheduler.failures.size());
			assertSame(expected, scheduler.failures.getFirst().getCause());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	private static Stream<Arguments> callbackOperators() {
		return Stream.of(
			Arguments.of("map", (CallbackOperatorFactory) (source, failure) -> source.map(item -> {
				if (item == 1) {
					throw failure;
				}
				return item;
			})),
			Arguments.of("filter", (CallbackOperatorFactory) (source, failure) -> source.filter(item -> {
				if (item == 1) {
					throw failure;
				}
				return true;
			})),
			Arguments.of("peek", (CallbackOperatorFactory) (source, failure) -> source.peek(item -> {
				if (item == 1) {
					throw failure;
				}
			})),
			Arguments.of("takeWhile", (CallbackOperatorFactory) (source, failure) -> source.takeWhile(item -> {
				if (item == 1) {
					throw failure;
				}
				return true;
			}))
		);
	}

	@Test
	public void collectorCallbackFailureStillCompletesDownstream() {
		IllegalStateException expected = new IllegalStateException("collector failed");
		CapturingScheduler scheduler = new CapturingScheduler();
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicInteger items = new AtomicInteger();
			AtomicInteger errors = new AtomicInteger();
			AtomicInteger completions = new AtomicInteger();

			Observable.of(1).join(item -> {
				throw expected;
			}, ",").subscribe(new FlexibleSubscriber<>() {
				@Override
				public void onNext(String item) {
					items.incrementAndGet();
				}

				@Override
				protected void onError(Throwable error) {
					errors.incrementAndGet();
				}

				@Override
				public void onComplete(Completion completion) {
					completions.incrementAndGet();
				}
			});

			assertEquals(0, items.get());
			assertEquals(0, errors.get());
			assertEquals(1, completions.get());
			assertEquals(1, scheduler.failures.size());
			assertSame(expected, scheduler.failures.getFirst().getCause());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	@Test
	public void zipCallbackFailureStillCompletesAfterItsLastTuple() {
		IllegalStateException expected = new IllegalStateException("zip callback failed");
		CapturingScheduler scheduler = new CapturingScheduler();
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicInteger completions = new AtomicInteger();
			Observable.zip(Observable.of(1), Observable.of(2)).subscribe(new FlexibleSubscriber<>() {
				@Override
				public void onNext(com.github.tix320.skimp.collection.Tuple<Integer, Integer> item) {
					throw expected;
				}

				@Override
				public void onComplete(Completion completion) {
					completions.incrementAndGet();
				}
			});

			assertEquals(1, completions.get());
			assertEquals(1, scheduler.failures.size());
			assertSame(expected, scheduler.failures.getFirst().getCause());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("multiSourceOperators")
	public void multiSourceErrorCancelsEverySource(String name, MultiSourceFactory factory) throws Exception {
		ControlledObservable first = new ControlledObservable();
		ControlledObservable second = new ControlledObservable();
		IllegalStateException expected = new IllegalStateException("source failed");
		AtomicReference<Throwable> actual = new AtomicReference<>();

		factory.create(first, second).subscribe(new FlexibleSubscriber<>() {
			@Override
			protected void onError(Throwable error) {
				actual.set(error);
			}
		});

		first.fail(expected);
		SchedulerUtils.awaitTermination();

		assertSame(expected, actual.get());
		assertEquals(1, first.cancellations.get());
		assertEquals(1, second.cancellations.get());
	}

	private static Stream<Arguments> multiSourceOperators() {
		return Stream.of(
			Arguments.of("merge", (MultiSourceFactory) Observable::merge),
			Arguments.of("zip", (MultiSourceFactory) (first, second) ->
				Observable.zip(first, second).map(tuple -> (Object) tuple)),
			Arguments.of("combineLatest", (MultiSourceFactory) (first, second) ->
				Observable.combineLatest(first, second).map(tuple -> (Object) tuple)),
			Arguments.of("firstOfAll", (MultiSourceFactory) (first, second) ->
				new FirstOfAllObservable<>(List.of(first, second))),
			Arguments.of("takeUntil", (MultiSourceFactory) Observable::takeUntil)
		);
	}

	@Test
	public void blockingHelperRethrowsOriginalError() {
		Publisher<Object> publisher = Publisher.simple();
		IllegalStateException expected = new IllegalStateException("source failed");
		publisher.abort(expected);

		IllegalStateException actual = assertThrows(IllegalStateException.class,
			() -> publisher.asObservable().await(Duration.ofSeconds(5)));

		assertSame(expected, actual);
	}

	private static final class ControlledObservable extends Observable<Object> {

		private final AtomicReference<Token> token = new AtomicReference<>();
		private final AtomicInteger cancellations = new AtomicInteger();

		@Override
		public void subscribe(Subscriber<? super Object> subscriber) {
			Token sourceToken = subscriber.createToken();
			token.set(sourceToken);
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
					sourceToken.addSignal(new CancelSignal(unsubscription));
				}
			});
			sourceToken.activate();
		}

		private void fail(Throwable error) {
			token.get().addSignal(new ErrorSignal(error));
		}

	}

	@FunctionalInterface
	private interface MultiSourceFactory {

		Observable<Object> create(Observable<Object> first, Observable<Object> second);

	}

	@FunctionalInterface
	private interface CallbackOperatorFactory {

		Observable<Integer> create(Observable<Integer> source, RuntimeException failure);

	}

	private static final class CapturingScheduler implements Scheduler {

		private final List<Exception> failures = new ArrayList<>();

		@Override
		public void schedule(Runnable task) {
			try {
				task.run();
			} catch (Exception exception) {
				failures.add(exception);
			}
		}

		@Override
		public void schedule(long delay, TimeUnit unit, Runnable task) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable task) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit, Runnable task) {
			throw new UnsupportedOperationException();
		}

	}

}
