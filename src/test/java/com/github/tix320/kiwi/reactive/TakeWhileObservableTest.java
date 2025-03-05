package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class TakeWhileObservableTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<Integer> expected = List.of(3, 4, 5);
		List<Integer> actual = Observable.of(3, 4, 5, 7)
			.takeWhile(integer -> integer < 6)
			.toList()
			.get(Duration.ofSeconds(5));
		assertEquals(expected, actual);
	}

	@Test
	public void withUnsubscribeTest() throws InterruptedException {
		List<Integer> expected = List.of(3, 4);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
			}
		};

		publisher.asObservable().takeWhile(integer -> integer < 6).subscribe(subscriber);

		publisher.publish(3);
		publisher.publish(4);

		SchedulerUtils.awaitTermination();

		subscriber.subscription().cancel();

		publisher.publish(5);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

}
