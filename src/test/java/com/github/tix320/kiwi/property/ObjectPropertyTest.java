package com.github.tix320.kiwi.property;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.property.internal.PropertyClosedException;
import com.github.tix320.skimp.api.interval.Interval;
import com.github.tix320.skimp.api.interval.IntervalRepeater;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class ObjectPropertyTest {

	@Test
	public void simpleTest() {
		ObjectProperty<Integer> property = Property.forObject();

		assertNull(property.getValue());

		property.setValue(3);

		assertEquals(3, property.getValue());
	}

	@Test
	public void observableTest() throws InterruptedException {
		ObjectProperty<Integer> property = Property.forObject();

		List<Integer> expected = List.of(1, 2, 3);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		property.setValue(1);

		property.asObservable().subscribe(actual::add);

		property.setValue(2);
		property.setValue(3);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void closeTest() {
		ObjectProperty<Integer> property = Property.forObject();

		List<Integer> expected = List.of(1, 2);
		List<Integer> actual = new ArrayList<>();

		property.setValue(1);

		property.asObservable().subscribe(actual::add);

		property.setValue(2);

		property.close();

		assertThrows(PropertyClosedException.class, () -> property.setValue(3));

		Interval interval = Interval.every(Duration.ofMillis(200));
		IntervalRepeater<?> retryPolicy = IntervalRepeater.of(interval, () -> assertEquals(expected, actual));

		assertTrue(retryPolicy.doUntilSuccess(10).isPresent());
	}
}
