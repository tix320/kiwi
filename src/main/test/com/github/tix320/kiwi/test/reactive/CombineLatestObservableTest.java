package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.collection.Tuple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class CombineLatestObservableTest {

	@Test
	public void simpleTest() {
		List<Tuple<Integer, Integer>> expected = List.of(new Tuple<>(1, 2), new Tuple<>(1, 4), new Tuple<>(3, 4));
		List<Tuple<Integer, Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();


		Observable.combineLatest(publisher1.asObservable(), publisher2.asObservable()).subscribe(actual::add);

		publisher1.publish(1);
		publisher2.publish(2);

		publisher2.publish(4);
		publisher1.publish(3);

		assertEquals(expected, actual);
	}

	@Test
	public void oneCompleteTest() {
		List<Tuple<Integer, Integer>> expected = List.of(new Tuple<>(1, 2), new Tuple<>(1, 4));
		List<Tuple<Integer, Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();


		Observable.combineLatest(publisher1.asObservable(), publisher2.asObservable()).subscribe(actual::add);

		publisher1.publish(1);
		publisher2.publish(2);

		publisher2.publish(4);
		publisher2.complete();
		publisher1.publish(3);

		assertEquals(expected, actual);
	}
}
