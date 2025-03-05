package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.publisher.MonoPublisher;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class MonoPublisherTest {

	@Test
	public void simpleTest() throws InterruptedException {
		MonoPublisher<String> mono = Publisher.mono();
		MonoObservable<String> observable = mono.asObservable();
		AtomicBoolean called = new AtomicBoolean(false);
		observable.subscribe(s -> {
			assertEquals("foo", s);
			called.set(true);
		});
		mono.publish("foo");

		SchedulerUtils.awaitTermination();

		assertTrue(called.get());
	}

	@Test
	public void subscribeAfterPublish() throws InterruptedException {
		MonoPublisher<String> mono = Publisher.mono();
		MonoObservable<String> observable = mono.asObservable();
		AtomicBoolean called = new AtomicBoolean(false);
		mono.publish("foo");
		observable.subscribe(s -> {
			assertEquals("foo", s);
			called.set(true);
		});

		SchedulerUtils.awaitTermination();

		assertTrue(called.get());
	}

}
