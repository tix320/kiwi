package com.github.tix320.kiwi.observable.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class SignalSynchronizerContractTest {

	@Test
	public void terminalCallbackFailureDoesNotStrandOtherTokens() {
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		ManualScheduler scheduler = new ManualScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			SignalSynchronizer synchronizer = new SignalSynchronizer();
			synchronizer.addNewTokenSpace();
			AtomicInteger received = new AtomicInteger();

			Subscriber<Object> failingSubscriber = subscriber(item -> {
			}, () -> {
				throw new IllegalStateException("terminal callback failed");
			});
			Subscriber<Object> receivingSubscriber = subscriber(item -> received.incrementAndGet(), () -> {
			});

			SignalSynchronizer.Token failingToken = synchronizer.createToken(failingSubscriber);
			SignalSynchronizer.Token receivingToken = synchronizer.createToken(receivingSubscriber);
			failingToken.addSignal(new CompleteSignal(SourceCompletion.DEFAULT));
			receivingToken.addSignal(new PublishSignal<>("value"));
			failingToken.activate();
			receivingToken.activate();

			assertThrows(RuntimeException.class, scheduler::runNext);
			scheduler.runNext();

			assertEquals(1, received.get());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	@Test
	public void cancellationOutranksQueuedErrorAndItems() {
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		ManualScheduler scheduler = new ManualScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			SignalSynchronizer synchronizer = new SignalSynchronizer();
			AtomicInteger items = new AtomicInteger();
			AtomicInteger errors = new AtomicInteger();
			AtomicReference<Completion> terminal = new AtomicReference<>();
			Subscriber<Object> subscriber = new Subscriber<>() {
				@Override
				protected void onSubscribe(Subscription subscription) {
				}

				@Override
				protected void onNext(Object item) {
					items.incrementAndGet();
				}

				@Override
				protected void onError(Throwable error) {
					errors.incrementAndGet();
				}

				@Override
				protected void onComplete(Completion completion) {
					terminal.set(completion);
				}
			};
			subscriber.setSubscription(new EmptySubscription());
			SignalSynchronizer.Token token = synchronizer.createToken(subscriber);
			Unsubscription cancellation = new Unsubscription("test");
			token.addSignal(new PublishSignal<>("value"));
			token.addSignal(new ErrorSignal(new IllegalStateException("failure")));
			token.addSignal(new CancelSignal(cancellation));
			token.activate();

			scheduler.runNext();

			assertSame(cancellation, terminal.get());
			assertEquals(0, items.get());
			assertEquals(0, errors.get());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	private static Subscriber<Object> subscriber(java.util.function.Consumer<Object> onNext, Runnable onComplete) {
		Subscriber<Object> subscriber = new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
			}

			@Override
			protected void onNext(Object item) {
				onNext.accept(item);
			}

			@Override
			protected void onComplete(Completion completion) {
				onComplete.run();
			}
		};
		subscriber.setSubscription(new EmptySubscription());
		return subscriber;
	}

	private static final class EmptySubscription extends Subscription {
		@Override
		protected void onRequest(long count) {
		}

		@Override
		protected void onUnboundRequest() {
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {
		}
	}

	private static final class ManualScheduler implements Scheduler {

		private final Queue<Runnable> tasks = new ArrayDeque<>();

		@Override
		public void schedule(Runnable task) {
			tasks.add(task);
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

		private void runNext() {
			tasks.remove().run();
		}
	}

}
