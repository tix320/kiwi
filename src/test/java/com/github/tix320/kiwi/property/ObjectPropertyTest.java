package com.github.tix320.kiwi.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tix320.kiwi.property.internal.PropertyClosedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

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
	public void closeTest() throws InterruptedException {
		ObjectProperty<Integer> property = Property.forObject();

		List<Integer> expected = List.of(1, 2);
		List<Integer> actual = new ArrayList<>();

		property.setValue(1);

		property.asObservable().subscribe(actual::add);

		property.setValue(2);

		property.close();

		assertThrows(PropertyClosedException.class, () -> property.setValue(3));

		Thread.sleep(100);

		assertEquals(expected, actual);
	}
}
