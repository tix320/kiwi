package com.github.tix320.kiwi.reactive;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

		Observable.merge(observable1, observable2, observable3).conditionalSubscribe(number -> {
			actual.add(number);
			return actual.size() < 4;
		});

		publisher.publish(8);

		Thread.sleep(100);

		assertEquals(4, actual.size()); // FIXME expected 4 , actual 5
		for (Integer integer : actual) {
			assertTrue(maybe.contains(integer));
		}
	}
}
