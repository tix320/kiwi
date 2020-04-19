package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.property.ObjectProperty;
import com.github.tix320.kiwi.api.reactive.property.ObjectStock;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.Stock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
public class PropertyAtomicContextTest {

	@Test
	public void onePropertyTest() {
		List<Integer> expected = Arrays.asList(3, 5, 6);
		List<Integer> actual = new ArrayList<>();

		ObjectProperty<Integer> property = Property.forObject();
		property.asObservable().subscribe(actual::add);

		property.setValue(3);

		Property.inAtomicContext(() -> {
			property.setValue(4);
			property.setValue(5);
		});

		property.setValue(6);

		assertEquals(expected, actual);
	}

	@Test
	public void doublePropertyTest() {
		List<Integer> expected = Arrays.asList(3, 4, 5, 7, 8, 9);
		List<Integer> actual = new ArrayList<>();

		ObjectProperty<Integer> property1 = Property.forObject();
		ObjectProperty<Integer> property2 = Property.forObject();
		property1.asObservable().subscribe(actual::add);
		property2.asObservable().subscribe(actual::add);

		property1.setValue(3);
		property2.setValue(4);

		Property.inAtomicContext(() -> {
			property1.setValue(4);
			property1.setValue(5);
			property2.setValue(6);
			property2.setValue(7);
		});

		property1.setValue(8);
		property2.setValue(9);

		assertEquals(expected, actual);
	}

	@Test
	void stockTest() {
		List<IllegalStateException> exceptions = new ArrayList<>();

		List<Integer> expected = Arrays.asList(3, 4, 5, 6);
		List<Integer> actual = new ArrayList<>();

		ObjectStock<Integer> stock = Stock.forObject();

		stock.add(3);

		stock.asObservable().subscribe(integer -> {
			if (integer == 3 || integer == 4) {
				actual.add(integer);
			}
		});

		Property.inAtomicContext(() -> {
			stock.add(4);

			AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
			stock.asObservable()
					.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set).onPublish(integer -> {
						if (integer == 4) {
							exceptions.add(new IllegalStateException());
						}
					}));

			subscriptionHolder.get().unsubscribe();

			stock.add(5);

			stock.asObservable()
					.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set).onPublish(integer -> {
						if (integer == 4 || integer == 5) {
							exceptions.add(new IllegalStateException());
						}
					}));

			subscriptionHolder.get().unsubscribe();
		});

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

		assertEquals(expected, actual);
		exceptions.forEach(Throwable::printStackTrace);
		assertEquals(0, exceptions.size());
	}
}
