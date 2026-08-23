package com.github.tix320.kiwi.publisher.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class PublisherDemandConcurrencyTest {

	@Test
	public void concurrentRequestDoesNotRestoreConsumedDemand() throws Exception {
		ControllablePublisher publisher = new ControllablePublisher();
		AtomicReference<Subscription> subscription = new AtomicReference<>();
		List<Integer> items = new CopyOnWriteArrayList<>();
		publisher.asObservable().subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription value) {
				subscription.set(value);
				value.request(1);
			}

			@Override
			protected void onNext(Integer item) {
				items.add(item);
			}

			@Override
			protected void onComplete(Completion completion) {
			}
		});

		publisher.pauseNextCursorCheck();
		Thread publishing = new Thread(() -> publisher.publish(1));
		publishing.start();
		assertTrue(publisher.cursorCheckStarted.await(5, TimeUnit.SECONDS));

		subscription.get().request(1);
		publisher.continueCursorCheck.countDown();
		publishing.join(5000);

		publisher.publish(2);
		publisher.publish(3);
		SchedulerUtils.awaitTermination();

		assertEquals(List.of(1, 2), items);
	}

	private static final class ControllablePublisher extends BasePublisher<Integer> {

		private final ConcurrentLinkedQueue<PublishSignal<Integer>> signals = new ConcurrentLinkedQueue<>();
		private final AtomicBoolean pauseCursorCheck = new AtomicBoolean(false);
		private final CountDownLatch cursorCheckStarted = new CountDownLatch(1);
		private final CountDownLatch continueCursorCheck = new CountDownLatch(1);

		@Override
		protected void onPublish(Integer item) {
			signals.add(new PublishSignal<>(item));
		}

		@Override
		protected PublisherCursor createCursor() {
			return new PublisherCursor() {
				@Override
				public boolean hasNext() {
					if (pauseCursorCheck.compareAndSet(true, false)) {
						cursorCheckStarted.countDown();
						try {
							continueCursorCheck.await();
						} catch (InterruptedException exception) {
							Thread.currentThread().interrupt();
							throw new IllegalStateException(exception);
						}
					}
					return !signals.isEmpty();
				}

				@Override
				public PublishSignal<Integer> next() {
					return signals.poll();
				}
			};
		}

		private void pauseNextCursorCheck() {
			pauseCursorCheck.set(true);
		}
	}

}
