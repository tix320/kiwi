package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class SkipObservableTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<Integer> expected = List.of(6, 7);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());
		Observable.of(4, 5, 6, 7).skip(2).subscribe(actual::add);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void completeBeforeSkipAll() throws InterruptedException {
		List<Integer> expected = List.of();
		List<Integer> actual = new ArrayList<>();
		Publisher<Integer> publisher = Publisher.simple();

		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
			}
		};

		publisher.asObservable().skip(2).subscribe(subscriber);

		publisher.publish(4);
		publisher.publish(5);

		SchedulerUtils.awaitTermination();

		subscriber.subscription().cancel();

		publisher.publish(7);

		assertEquals(expected, actual);
	}

}
