package io.titix.kiwi.util;

import java.util.ArrayList;

import io.titix.kiwi.util.Pool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author tix32 on 23-Feb-19
 */
 class PoolTest {

	@Test
	void simpleTest() {
		Pool<ArrayList<Object>> pool = new Pool<>(ArrayList::new);
		ArrayList<Object> list1 = pool.get();
		list1.add("first");

		ArrayList<Object> list2 = pool.get();
		list2.add("second");

		assertNotEquals(list1, list2);

		pool.release(list1);

		ArrayList<Object> list3 = pool.get();

		assertEquals(list1, list3);

		ArrayList<Object> list4 = pool.get();

		assertNotEquals(list4, list1);
		assertNotEquals(list4, list2);
		assertNotEquals(list4, list3);
	}
}
