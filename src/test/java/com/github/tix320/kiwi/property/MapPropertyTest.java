package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class MapPropertyTest {

	@Test
	public void simpleTest() {
		MapProperty<Integer, String> property = Property.forMap(new ConcurrentHashMap<>());

		assertEquals(new ConcurrentHashMap<>(), property.getValue());

		property.setValue(Map.of(3, "foo"));

		assertEquals(Map.of(3, "foo"), property.getValue());
		assertEquals(property.getValue(), Map.of(3, "foo"));
	}

	@Test
	public void observableTest() throws InterruptedException {
		MapProperty<Integer, String> mapProperty = Property.forMap(new ConcurrentHashMap<>());

		ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();
		map.put(1, "foo");
		mapProperty.setValue(map);

		boolean[] tested = new boolean[]{false, false, false};
		mapProperty.asObservable().subscribe(changedMap -> {
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

		mapProperty.put(2, "boo");
		mapProperty.put(3, "goo");

		SchedulerUtils.awaitTermination();

		assertEquals(Map.of(1, "foo", 2, "boo", 3, "goo"), map);
		assertEquals(Map.of(1, "foo", 2, "boo", 3, "goo"), mapProperty.getValue());
		assertTrue(tested[0]);
		assertTrue(tested[1]);
		assertTrue(tested[2]);
	}

}
