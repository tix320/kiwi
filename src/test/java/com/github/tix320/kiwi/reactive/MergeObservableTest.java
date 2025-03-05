package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class MergeObservableTest {

	@Test
	public void mergeWithUnsubscribing() throws InterruptedException {
		Set<Integer> maybe = Set.of(3, 5, 6, 9, 8);
		Set<Integer> actual = new CopyOnWriteArraySet<>();

		Observable<Integer> observable1 = Observable.of(3);
		Observable<Integer> observable2 = Observable.of(5, 6);
		Publisher<Integer> publisher = Publisher.buffered();
		publisher.publish(9);

		Observable<Integer> observable3 = publisher.asObservable();

		Observable.merge(observable1, observable2, observable3)
			.takeWhile(integer -> actual.size() < 4)
			.subscribe(actual::add);

		publisher.publish(8);

		SchedulerUtils.awaitTermination();

		assertEquals(4, actual.size());
		for (Integer integer : actual) {
			assertTrue(maybe.contains(integer));
		}
	}

}
