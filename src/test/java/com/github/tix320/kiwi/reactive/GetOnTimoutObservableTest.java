package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.observable.scheduler.Scheduler;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 08-Apr-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class GetOnTimoutObservableTest {

	@Test
	public void publishBeforeTimoutTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(1);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable().getOnTimeout(Duration.ofSeconds(2), () -> 5).subscribe(actual::add);

		publisher.publish(1);
		publisher.publish(2);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void publishAfterTimoutTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(5);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable().getOnTimeout(Duration.ofMillis(500), () -> 5).subscribe(actual::add);

		SchedulerUtils.sleepFor(Duration.ofMillis(800));

		publisher.publish(1);
		publisher.publish(2);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void subscribeAfterTimeoutReceivesFallback() throws InterruptedException {
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());
		Publisher<Integer> publisher = Publisher.simple();
		var observable = publisher.asObservable().getOnTimeout(Duration.ofMillis(50), () -> 5);

		SchedulerUtils.sleepFor(Duration.ofMillis(100));
		observable.subscribe(actual::add);
		SchedulerUtils.awaitTermination();

		assertEquals(List.of(5), actual);
	}

	@Test
	public void sourceWinnerCancelsFallbackTask() {
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		ManualTimeoutScheduler scheduler = new ManualTimeoutScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicInteger fallbackCalls = new AtomicInteger();
			List<Integer> actual = new ArrayList<>();
			Publisher<Integer> publisher = Publisher.simple();

			publisher.asObservable()
				.getOnTimeout(Duration.ofSeconds(1), fallbackCalls::incrementAndGet)
				.subscribe(actual::add);
			publisher.publish(1);
			scheduler.runDelayedTask();

			assertEquals(List.of(1), actual);
			assertEquals(0, fallbackCalls.get());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	@Test
	public void sourceWinnerDoesNotCancelFallbackForLaterSubscriber() {
		Scheduler previousScheduler = KiwiSchedulerHolder.get();
		ManualTimeoutScheduler scheduler = new ManualTimeoutScheduler();
		KiwiSchedulerHolder.changeTo(scheduler);
		try {
			AtomicInteger fallbackCalls = new AtomicInteger();
			List<Integer> first = new ArrayList<>();
			List<Integer> second = new ArrayList<>();
			Publisher<Integer> publisher = Publisher.simple();
			var observable = publisher.asObservable()
				.getOnTimeout(Duration.ofSeconds(1), fallbackCalls::incrementAndGet);

			observable.subscribe(first::add);
			publisher.publish(1);
			observable.subscribe(second::add);
			scheduler.runDelayedTasks();

			assertEquals(List.of(1), first);
			assertEquals(List.of(1), second);
			assertEquals(1, fallbackCalls.get());
		} finally {
			KiwiSchedulerHolder.changeTo(previousScheduler);
		}
	}

	private static final class ManualTimeoutScheduler implements Scheduler {

		private final List<Runnable> delayedTasks = new ArrayList<>();

		@Override
		public void schedule(Runnable task) {
			task.run();
		}

		@Override
		public void schedule(long delay, TimeUnit unit, Runnable task) {
			delayedTasks.add(task);
		}

		@Override
		public void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable task) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit, Runnable task) {
			throw new UnsupportedOperationException();
		}

		private void runDelayedTask() {
			delayedTasks.getFirst().run();
		}

		private void runDelayedTasks() {
			List.copyOf(delayedTasks).forEach(Runnable::run);
		}

	}

}
