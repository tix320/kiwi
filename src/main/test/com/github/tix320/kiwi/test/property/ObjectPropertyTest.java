package com.github.tix320.kiwi.test.property;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.property.ObjectProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.internal.reactive.property.PropertyClosedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class ObjectPropertyTest {

	@Test
	void simpleTest() {
		ObjectProperty<Integer> property = Property.forObject();

		assertNull(property.getValue());

		property.setValue(3);

		assertEquals(3, property.getValue());
	}

	@Test
	void observableTest() {
		ObjectProperty<Integer> property = Property.forObject();

		List<Integer> expected = List.of(1, 2, 3);
		List<Integer> actual = new ArrayList<>();

		property.setValue(1);

		property.asObservable().subscribe(actual::add);

		property.setValue(2);
		property.setValue(3);

		assertEquals(expected, actual);
	}

	@Test
	void closeTest() {
		ObjectProperty<Integer> property = Property.forObject();

		List<Integer> expected = List.of(1, 2);
		List<Integer> actual = new ArrayList<>();

		property.setValue(1);

		property.asObservable().subscribe(actual::add);

		property.setValue(2);

		property.close();

		assertThrows(PropertyClosedException.class, () -> property.setValue(3));

		assertEquals(expected, actual);
	}
}
