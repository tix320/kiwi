package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.property.ObjectProperty;
import com.github.tix320.kiwi.property.Property;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
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

		SchedulerUtils.awaitTermination();

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

		SchedulerUtils.awaitTermination();

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

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void checkedExceptionIsRethrownWithoutWrapping() {
		ObjectProperty<Integer> property = Property.forObject();
		Exception expected = new Exception("Expected test failure");

		Exception actual = assertThrows(Exception.class, () -> Property.updateAtomic(property, () -> {
			throw expected;
		}));

		assertSame(expected, actual);
	}

	@Test
	public void errorPassesThroughWithoutWrapping() {
		ObjectProperty<Integer> property = Property.forObject();
		AssertionError expected = new AssertionError("Expected test failure");

		AssertionError actual = assertThrows(AssertionError.class, () -> Property.updateAtomic(property, () -> {
			throw expected;
		}));

		assertSame(expected, actual);
	}

	@Test
	public void unfreezeIsSafeAfterPropertyWasClosed() {
		ObjectProperty<Integer> property = Property.forObject(1);
		property.freeze();
		property.setValue(2);
		property.close();

		assertDoesNotThrow(property::unfreeze);
		assertDoesNotThrow(property::unfreeze);
		assertEquals(1, property.getValue());
	}

}
