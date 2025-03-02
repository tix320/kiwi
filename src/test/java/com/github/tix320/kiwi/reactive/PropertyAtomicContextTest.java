package com.github.tix320.kiwi.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tix320.kiwi.property.ObjectProperty;
import com.github.tix320.kiwi.property.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
public class PropertyAtomicContextTest {

	@Test
	public void onePropertyTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(3, 5, 6);
		List<Integer> actual = new ArrayList<>();

		ObjectProperty<Integer> property = Property.forObject();
		property.asObservable().subscribe(actual::add);

		property.setValue(3);

		Property.updateAtomic(property, () -> {
			property.setValue(4);
			property.setValue(5);
		});

		property.setValue(6);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void doublePropertyTest() throws InterruptedException {
		Set<Integer> expected = Set.of(3, 4, 6, 8, 9, 10);
		Set<Integer> actual = new HashSet<>();

		ObjectProperty<Integer> property1 = Property.forObject();
		ObjectProperty<Integer> property2 = Property.forObject();
		property1.asObservable().subscribe(actual::add);
		property2.asObservable().subscribe(actual::add);

		property1.setValue(3);
		property2.setValue(4);

		Property.updateAtomic(property1, property2, () -> {
			property1.setValue(4);
			property1.setValue(5);
			property1.setValue(6);
			property2.setValue(7);
			property2.setValue(8);
		});

		property1.setValue(9);
		property2.setValue(10);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void nestedContextsTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(3, 6, 7);
		List<Integer> actual = new ArrayList<>();

		ObjectProperty<Integer> property = Property.forObject();
		property.asObservable().subscribe(actual::add);

		property.setValue(3);

		Property.updateAtomic(property, () -> {
			property.setValue(4);

			Property.updateAtomic(property, () -> {
				property.setValue(5);
			});

			property.setValue(6);
		});

		property.setValue(7);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

}
