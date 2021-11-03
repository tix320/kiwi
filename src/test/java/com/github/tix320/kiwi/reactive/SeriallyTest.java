package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.publisher.BufferedPublisher;
import com.github.tix320.kiwi.publisher.Publisher;
import org.junit.jupiter.api.Test;

public class SeriallyTest {

	@Test
	public void bufferedTest() {
		BufferedPublisher<Integer> simple = Publisher.buffered(1);

		simple.publish(3);
		simple.complete();
		simple.asObservable().subscribe(System.out::println);
	}
}
