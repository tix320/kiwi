package com.github.tix320.kiwi.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class PropertyContractTest {

	@Test
	public void listPropertyMutatesOriginalListAndPublishesUnmodifiableView() {
		List<Integer> original = new ArrayList<>(List.of(1));
		ListProperty<Integer> property = Property.forList(original);
		List<Integer> published = property.asObservable().get(Duration.ofSeconds(1));

		assertSame(original, property.getValue());
		assertThrows(UnsupportedOperationException.class, () -> published.add(2));

		property.add(2);

		assertEquals(List.of(1, 2), original);
		assertEquals(List.of(1, 2), published);
	}

	@Test
	public void setPropertyMutatesOriginalSetAndPublishesUnmodifiableView() {
		Set<Integer> original = new HashSet<>(Set.of(1));
		SetProperty<Integer> property = Property.forSet(original);
		Set<Integer> published = property.asObservable().get(Duration.ofSeconds(1));

		assertSame(original, property.getValue());
		assertThrows(UnsupportedOperationException.class, () -> published.add(2));

		property.add(2);

		assertEquals(Set.of(1, 2), original);
		assertEquals(Set.of(1, 2), published);
	}

	@Test
	public void mapPropertyMutatesOriginalMapAndPublishesUnmodifiableView() {
		Map<Integer, String> original = new HashMap<>(Map.of(1, "one"));
		MapProperty<Integer, String> property = Property.forMap(original);
		Map<Integer, String> published = property.asObservable().get(Duration.ofSeconds(1));

		assertSame(original, property.getValue());
		assertThrows(UnsupportedOperationException.class, () -> published.put(2, "two"));

		property.put(2, "two");

		assertEquals(Map.of(1, "one", 2, "two"), original);
		assertEquals(Map.of(1, "one", 2, "two"), published);
	}

}
