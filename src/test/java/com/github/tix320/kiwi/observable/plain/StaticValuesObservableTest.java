package com.github.tix320.kiwi.observable.plain;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 03.03.25
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class StaticValuesObservableTest {

	@Test
	public void requestPartialTest() throws InterruptedException {
		var observable = new StaticValuesObservable<>(List.of(1, 2, 3, 4));

		var items = new CopyOnWriteArrayList<>();

		observable.subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
				subscription.request(2);
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

		assertEquals(List.of(1, 2), items);
	}

	@Test
	public void requestFullTest() throws InterruptedException {
		var observable = new StaticValuesObservable<>(List.of(1, 2, 3, 4));

		var items = new CopyOnWriteArrayList<>();

		observable.subscribe(new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription subscription) {
				subscription.request(2);
			}

			@Override
			protected void onNext(Integer item) {
				items.add(item);
				subscription().request(1);
			}

			@Override
			protected void onComplete(Completion completion) {

			}
		});

		SchedulerUtils.awaitTermination();

		assertEquals(List.of(1, 2, 3, 4), items);
	}

}
