package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import com.github.tix320.skimp.collection.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class CombineLatestObservableTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<Tuple<Integer, Integer>> expected = List.of(new Tuple<>(1, 2), new Tuple<>(1, 4), new Tuple<>(3, 4));
		List<Tuple<Integer, Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();

		Observable.combineLatest(publisher1.asObservable(), publisher2.asObservable()).subscribe(actual::add);

		publisher1.publish(1);
		publisher2.publish(2);

		publisher2.publish(4);

		publisher1.publish(3);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void oneCompleteTest() throws InterruptedException {
		List<Tuple<Integer, Integer>> expected = List.of(new Tuple<>(1, 2), new Tuple<>(1, 4), new Tuple<>(3, 4));
		List<Tuple<Integer, Integer>> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();

		Observable.combineLatest(publisher1.asObservable(), publisher2.asObservable()).subscribe(actual::add);

		publisher1.publish(1);
		publisher2.publish(2);

		publisher2.publish(4);
		publisher2.complete();

		publisher1.publish(3);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

}
