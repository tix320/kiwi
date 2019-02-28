package io.titix.kiwi.util;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author tix32 on 23-Feb-19
 */
 class PrePoolTest {

	@Test
	void simpleTest() {
		PrePool<ArrayList<Object>> prePool = new PrePool<>(ArrayList::new);
		ArrayList<Object> list1 = prePool.get();
		list1.add("first");

		ArrayList<Object> list2 = prePool.get();
		list2.add("second");

		assertNotEquals(list1, list2);

		prePool.release(list1);

		ArrayList<Object> list3 = prePool.get();

		assertEquals(list1, list3);

		ArrayList<Object> list4 = prePool.get();

		assertNotEquals(list4, list1);
		assertNotEquals(list4, list2);
		assertNotEquals(list4, list3);
	}
}