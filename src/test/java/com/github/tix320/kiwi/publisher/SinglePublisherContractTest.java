package com.github.tix320.kiwi.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SinglePublisherContractTest {

	@Test
	public void mapperFailureDoesNotChangeCurrentValue() {
		SinglePublisher<String> publisher = new SinglePublisher<>("good", value -> {
			if (value.equals("bad")) {
				throw new IllegalArgumentException("bad value");
			}
			return value;
		});

		assertThrows(IllegalArgumentException.class, () -> publisher.publish("bad"));

		assertEquals("good", publisher.getValue());
	}

}
