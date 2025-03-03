package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.skimp.collection.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class IntegrationTest {

	@Test
	public void nestedCombineLatest() throws InterruptedException {
		List<Tuple<Integer, Tuple<Integer, Integer>>> expected = List.of(new Tuple<>(10, new Tuple<>(20, 30)),
																		 new Tuple<>(10, new Tuple<>(4, 30)),
																		 new Tuple<>(50, new Tuple<>(4, 30)),
																		 new Tuple<>(50, new Tuple<>(4, 60)));
		List<Tuple<Integer, Tuple<Integer, Integer>>> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();
		Publisher<Integer> publisher3 = Publisher.simple();

		Observable.combineLatest(publisher1.asObservable(),
								 Observable.combineLatest(publisher2.asObservable(), publisher3.asObservable()))
			.subscribe(actual::add);

		publisher1.publish(10);
		publisher2.publish(20);
		publisher3.publish(30);

		publisher2.publish(4);
		publisher1.publish(50);
		publisher3.publish(60);

		Thread.sleep(100);

		assertEquals(expected, actual);

	}

	@Test
	public void nestedCombineLatest2() throws InterruptedException {
		List<Tuple<Integer, Tuple<Integer, Integer>>> expected = List.of(new Tuple<>(12, new Tuple<>(21, 30)),
																		 new Tuple<>(12, new Tuple<>(21, 31)),
																		 new Tuple<>(12, new Tuple<>(4, 31)),
																		 new Tuple<>(50, new Tuple<>(4, 31)),
																		 new Tuple<>(50, new Tuple<>(4, 60)));
		List<Tuple<Integer, Tuple<Integer, Integer>>> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();
		Publisher<Integer> publisher3 = Publisher.simple();

		Observable.combineLatest(publisher1.asObservable(),
								 Observable.combineLatest(publisher2.asObservable(), publisher3.asObservable()))
			.subscribe(actual::add);

		publisher1.publish(10);
		publisher1.publish(11);
		publisher1.publish(12);
		publisher2.publish(20);
		publisher2.publish(21);
		publisher3.publish(30);
		publisher3.publish(31);

		publisher2.publish(4);
		publisher1.publish(50);
		publisher3.publish(60);

		Thread.sleep(100);

		assertEquals(expected, actual);

	}

}
