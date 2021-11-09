package com.github.tix320.kiwi.reactive;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.property.ObjectProperty;
import com.github.tix320.kiwi.property.ObjectStock;
import com.github.tix320.kiwi.property.Property;
import com.github.tix320.kiwi.property.Stock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	public void stockTest() throws InterruptedException {
		List<IllegalStateException> exceptions = new ArrayList<>();

		Set<Integer> expected = Set.of(3, 4, 5, 6, 8);
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		ObjectStock<Integer> stock = Stock.forObject();

		stock.add(3);

		stock.asObservable().subscribe(integer -> {
			if (integer == 3 || integer == 4) {
				actual.add(integer);
			}
		});

		Property.updateAtomic(stock, () -> {
			stock.add(4);


			FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {
				@Override
				public void onNext(Integer item) {
					if (item == 4) {
						exceptions.add(new IllegalStateException());
					}
				}
			};

			stock.asObservable().subscribe(subscriber);

			Thread.sleep(200);

			subscriber.subscription().cancel();

			stock.add(5);

			FlexibleSubscriber<Integer> subscriber2 = new FlexibleSubscriber<>() {
				@Override
				public void onNext(Integer integer) {
					if (integer == 4 || integer == 5) {
						actual.add(8);
					}
				}
			};

			stock.asObservable().subscribe(subscriber2);

			stock.asObservable().subscribe(integer -> {
				if (integer == 5) {
					actual.add(5);
				}
			});

			stock.add(6);

			stock.asObservable().subscribe(integer -> {
				if (integer == 6) {
					actual.add(integer);
				}
			});
		});

		Thread.sleep(200);

		assertEquals(expected, actual);
		assertEquals(0, exceptions.size());
	}

	@Disabled("Until nested atomic context bugfix")
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

		assertEquals(expected, actual); // FIXME Expected :[3, 6, 7], Actual   :[3, 5, 6, 7]
	}
}
