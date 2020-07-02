package com.github.tix320.kiwi.test.property;

import java.util.HashMap;
import java.util.Map;

import com.github.tix320.kiwi.api.reactive.property.MapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class MapPropertyTest {

	@Test
	void simpleTest() {
		MapProperty<Integer, String> property = Property.forMap();

		assertNull(property.getValue());

		property.setValue(Map.of(3, "foo"));

		assertEquals(Map.of(3, "foo"), property.getValue());
		assertEquals(property.getValue(), Map.of(3, "foo"));
	}

	@Test
	void observableTest() {
		MapProperty<Integer, String> property = Property.forMap();

		HashMap<Integer, String> map = new HashMap<>();
		map.put(1, "foo");
		property.setValue(map);

		boolean[] tested = new boolean[]{false, false, false};
		property.asObservable().subscribe(changedMap -> {
			if (changedMap.containsKey(1)) {
				assertEquals("foo", changedMap.get(1));
				tested[0] = true;
			}
			if (changedMap.containsKey(2)) {
				assertEquals("boo", changedMap.get(2));
				tested[1] = true;
			}

			if (changedMap.containsKey(3)) {
				assertEquals("goo", changedMap.get(3));
				tested[2] = true;
			}
		});

		property.put(2, "boo");
		property.put(3, "goo");

		assertEquals(Map.of(1, "foo", 2, "boo", 3, "goo"), map);
		assertTrue(tested[0]);
		assertTrue(tested[1]);
		assertTrue(tested[2]);
	}
}
