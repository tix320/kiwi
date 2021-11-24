package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.publisher.ReplayPublisher;
import com.github.tix320.kiwi.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeriallyTest {

	@Test
	public void bufferedTest() {
		ReplayPublisher<Integer> simple = Publisher.buffered(1);

		simple.publish(3);
		simple.complete();
		simple.asObservable().subscribe(integer -> assertEquals(3, integer));
	}
}
