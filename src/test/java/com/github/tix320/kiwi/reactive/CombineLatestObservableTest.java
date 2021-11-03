package com.github.tix320.kiwi.reactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.skimp.api.collection.Tuple;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class CombineLatestObservableTest {

	@Test
	@Disabled("Until signal serialization")
	public void simpleTest() throws InterruptedException {
		List<Tuple<Integer, Integer>> expected = List.of(new Tuple<>(1, 2), new Tuple<>(1, 4), new Tuple<>(3, 4));
		List<Tuple<Integer, Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();


		Observable.combineLatest(publisher1.asObservable(), publisher2.asObservable()).subscribe(actual::add);

		publisher1.publish(1);
		publisher2.publish(2);

		Thread.sleep(100);

		publisher2.publish(4);

		Thread.sleep(100);

		publisher1.publish(3);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	@Disabled("Until signal serialization")
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

		Thread.sleep(500);

		assertEquals(expected, actual);  // FIXME  expected: <[[1,2], [1,4], [3,4]]> but was: <[[1,2], [3,2], [3,4]]>
	}
}
