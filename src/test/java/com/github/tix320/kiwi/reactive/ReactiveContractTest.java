package com.github.tix320.kiwi.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
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
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.ErrorSignal;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class ReactiveContractTest {

	@Test
	public void takeZeroCompletesWithoutItem() throws Exception {
		List<Integer> items = new CopyOnWriteArrayList<>();
		List<Completion> completions = new CopyOnWriteArrayList<>();

		Observable.of(1, 2, 3).take(0).subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				items.add(item);
			}

			@Override
			public void onComplete(Completion completion) {
				completions.add(completion);
			}
		});

		SchedulerUtils.awaitTermination();
		assertEquals(List.of(), items);
		assertEquals(1, completions.size());
	}

	@Test
	public void skipReplacesDemandConsumedBySkippedItem() throws Exception {
		assertEquals(List.of(2), requestOne(Observable.of(1, 2).skip(1)));
	}

	@Test
	public void filterReplacesDemandConsumedByRejectedItem() throws Exception {
		assertEquals(List.of(2), requestOne(Observable.of(1, 2).filter(item -> item % 2 == 0)));
	}

	@Test
	public void collectorConsumesSourceAfterFirstDemand() throws Exception {
		assertEquals(List.of(List.of(1, 2, 3)), requestOne(Observable.of(1, 2, 3).toList()));
	}

	@Test
	public void unboundedRequestInvokesHookOnlyOnce() {
		AtomicInteger requests = new AtomicInteger();
		Subscription subscription = new Subscription() {
			@Override
			protected void onRequest(long count) {
			}

			@Override
			protected void onUnboundRequest() {
				requests.incrementAndGet();
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
			}
		};

		subscription.requestUnbounded();
		subscription.requestUnbounded();

		assertEquals(1, requests.get());
	}

	@Test
	public void staticMonoProducesValueOnlyOnce() throws Exception {
		List<Integer> items = new CopyOnWriteArrayList<>();
		Observable.of(1).subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
				subscription.request(1);
				subscription.request(1);
			}

			@Override
			protected void onNext(Integer item) {
				items.add(item);
			}

			@Override
			protected void onComplete(Completion completion) {
			}
		});

		SchedulerUtils.awaitTermination();
		assertEquals(List.of(1), items);
	}

	@Test
	public void getReturnsNullWhenObservableCompletesEmpty() throws Exception {
		assertNull(Observable.empty().get(Duration.ofSeconds(5)));
		SchedulerUtils.awaitTermination();
	}

	@Test
	public void closedEmptyPublisherCompletesLateSubscriberWithoutDemand() throws Exception {
		Publisher<Integer> publisher = Publisher.simple();
		publisher.complete();
		List<Completion> completions = new CopyOnWriteArrayList<>();

		publisher.asObservable().subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
			}

			@Override
			protected void onNext(Integer item) {
			}

			@Override
			protected void onComplete(Completion completion) {
				completions.add(completion);
			}
		});

		SchedulerUtils.awaitTermination();
		assertEquals(1, completions.size());
	}

	@Test
	public void nullItemsAreRejected() {
		Publisher<Object> publisher = Publisher.simple();
		assertThrows(NullPointerException.class, () -> publisher.publish(null));
		assertThrows(NullPointerException.class, () -> Observable.of((Object) null));
	}

	@Test
	public void nullControlValuesAreRejectedBeforeStateChanges() {
		Subscriber<Object> subscriber = new FlexibleSubscriber<>() {};
		assertThrows(NullPointerException.class, () -> subscriber.setSubscription(null));

		Subscription subscription = new Subscription() {
			@Override
			protected void onRequest(long count) {
			}

			@Override
			protected void onUnboundRequest() {
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
			}
		};
		subscriber.setSubscription(subscription);

		assertThrows(NullPointerException.class, () -> subscriber.complete(null));
		assertThrows(NullPointerException.class, () -> subscription.cancel(null));

		Publisher<Object> publisher = Publisher.simple();
		assertThrows(NullPointerException.class, () -> publisher.complete(null));
		assertThrows(NullPointerException.class, () -> publisher.abort(null));
		assertThrows(NullPointerException.class, () -> new CompleteSignal(null));
		assertThrows(NullPointerException.class, () -> new CancelSignal(null));
		assertThrows(NullPointerException.class, () -> new ErrorSignal(null));
		publisher.publish(new Object());
	}

	@Test
	public void nullCallbacksAreRejectedWhenConfigured() {
		Observable<Integer> source = Observable.of(1);

		assertThrows(NullPointerException.class, () -> source.map(null));
		assertThrows(NullPointerException.class, () -> source.filter(null));
		assertThrows(NullPointerException.class, () -> source.takeWhile(null));
		assertThrows(NullPointerException.class, () -> source.peek(null));
		assertThrows(NullPointerException.class,
			() -> source.toMap((Function<Integer, Integer>) null, Function.identity()));
		assertThrows(NullPointerException.class,
			() -> source.toMap(Function.identity(), (Function<Integer, Integer>) null));
		assertThrows(NullPointerException.class,
			() -> source.join((Function<Integer, String>) null, ","));
		assertThrows(NullPointerException.class, () -> source.subscribe((Consumer<Integer>) null));
		assertThrows(NullPointerException.class,
			() -> source.waitAndApply(Duration.ofSeconds(1), null));
		assertThrows(NullPointerException.class,
			() -> source.getOnTimeout(Duration.ofSeconds(1), null));
	}

	@Test
	public void competingTerminalOperationsDeliverExactlyOneTerminalCallback() throws Exception {
		AtomicInteger terminalCallbacks = new AtomicInteger();
		int iterations = 50;

		for (int i = 0; i < iterations; i++) {
			Publisher<Integer> publisher = Publisher.simple();
			publisher.asObservable().subscribe(new FlexibleSubscriber<>() {
				@Override
				public void onComplete(Completion completion) {
					terminalCallbacks.incrementAndGet();
				}

				@Override
				protected void onError(Throwable error) {
					terminalCallbacks.incrementAndGet();
				}
			});

			CountDownLatch start = new CountDownLatch(1);
			Thread complete = new Thread(() -> runTerminalAction(start, publisher::complete));
			Thread abort = new Thread(() -> runTerminalAction(start,
				() -> publisher.abort(new IllegalStateException("expected race failure"))));
			complete.start();
			abort.start();
			start.countDown();
			complete.join();
			abort.join();
		}

		SchedulerUtils.awaitTermination();
		assertEquals(iterations, terminalCallbacks.get());
	}

	@Test
	public void timeoutCancelsSourceSubscription() {
		AtomicInteger cancellations = new AtomicInteger();
		Observable<Object> observable = observableWithCancellation(cancellations, false);

		assertThrows(com.github.tix320.kiwi.observable.TimeoutException.class,
			() -> observable.get(Duration.ofMillis(1)));
		assertEquals(1, cancellations.get());
	}

	@Test
	public void callbackExceptionDoesNotStrandQueuedCompletion() {
		Scheduler previous = KiwiSchedulerHolder.get();
		CapturingScheduler scheduler = new CapturingScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicBoolean completed = new AtomicBoolean(false);
			AtomicInteger errors = new AtomicInteger();
			Observable.of(1).subscribe(new Subscriber<>() {
				@Override
				protected void onSubscribe(Subscription subscription) {
					subscription.request(1);
				}

				@Override
				protected void onNext(Integer item) {
					throw new IllegalStateException("Expected test failure");
				}

				@Override
				protected void onError(Throwable error) {
					errors.incrementAndGet();
				}

				@Override
				protected void onComplete(Completion completion) {
					completed.set(true);
				}
			});

			assertEquals(1, scheduler.failures.size());
			assertEquals(2, scheduler.scheduledTasks.get());
			assertEquals(true, completed.get());
			assertEquals(0, errors.get());
		} finally {
			KiwiSchedulerHolder.changeTo(previous);
		}
	}

	@Test
	public void takeCancelsAtLimitWhenDownstreamCallbackFails() {
		Scheduler previous = KiwiSchedulerHolder.get();
		CapturingScheduler scheduler = new CapturingScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicBoolean completed = new AtomicBoolean(false);
			Observable.of(1, 2).take(1).subscribe(new Subscriber<>() {
				@Override
				protected void onSubscribe(Subscription subscription) {
					subscription.requestUnbounded();
				}

				@Override
				protected void onNext(Integer item) {
					throw new IllegalStateException("Expected test failure");
				}

				@Override
				protected void onComplete(Completion completion) {
					completed.set(true);
				}
			});

			assertEquals(true, completed.get());
			assertEquals(1, scheduler.failures.size());
		} finally {
			KiwiSchedulerHolder.changeTo(previous);
		}
	}

	@Test
	public void terminalCallbackExceptionReschedulesWorker() {
		Scheduler previous = KiwiSchedulerHolder.get();
		CapturingScheduler scheduler = new CapturingScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicBoolean completed = new AtomicBoolean(false);
			Observable.of(1).subscribe(new Subscriber<>() {
				@Override
				protected void onSubscribe(Subscription subscription) {
					subscription.request(1);
				}

				@Override
				protected void onNext(Integer item) {
				}

				@Override
				protected void onComplete(Completion completion) {
					completed.set(true);
					throw new IllegalStateException("Expected terminal callback failure");
				}
			});

			assertEquals(true, completed.get());
			assertEquals(1, scheduler.failures.size());
			assertEquals(2, scheduler.scheduledTasks.get());
		} finally {
			KiwiSchedulerHolder.changeTo(previous);
		}
	}

	@Test
	public void aggregateCancellationAttemptsEverySource() {
		AtomicInteger firstCancellations = new AtomicInteger();
		AtomicInteger secondCancellations = new AtomicInteger();
		Observable<Object> merged = Observable.merge(
			observableWithCancellation(firstCancellations, true),
			observableWithCancellation(secondCancellations, false));
		Subscriber<Object> subscriber = new Subscriber<>() {
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
		merged.subscribe(subscriber);

		assertThrows(IllegalStateException.class, subscriber.subscription()::cancel);
		assertEquals(1, firstCancellations.get());
		assertEquals(1, secondCancellations.get());
	}

	private static <T> List<T> requestOne(Observable<T> observable) throws Exception {
		List<T> items = new CopyOnWriteArrayList<>();
		observable.subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
				subscription.request(1);
			}

			@Override
			protected void onNext(T item) {
				items.add(item);
			}

			@Override
			protected void onComplete(Completion completion) {
			}
		});

		SchedulerUtils.awaitTermination();
		return items;
	}

	private static void runTerminalAction(CountDownLatch start, Runnable action) {
		try {
			start.await();
			action.run();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(exception);
		} catch (RuntimeException ignored) {
			// The operation that loses the terminal race observes the already closed publisher.
		}
	}

	private static Observable<Object> observableWithCancellation(AtomicInteger cancellations, boolean fail) {
		return new Observable<>() {
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
						if (fail) {
							throw new IllegalStateException("Expected test failure");
						}
					}
				});
			}
		};
	}

	private static final class CapturingScheduler implements Scheduler {

		private final List<Exception> failures = new ArrayList<>();
		private final AtomicInteger scheduledTasks = new AtomicInteger();

		@Override
		public void schedule(Runnable task) {
			scheduledTasks.incrementAndGet();
			try {
				task.run();
			} catch (Exception exception) {
				failures.add(exception);
			}
		}

		@Override
		public void schedule(long delay, TimeUnit unit, Runnable task) {
			schedule(task);
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
