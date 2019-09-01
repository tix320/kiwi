package com.gitlab.tixtix320.kiwi.test.util;

import com.gitlab.tixtix320.kiwi.api.util.ObjectPool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
class ObjectPoolTest {

	@Test
	void simpleTest() {
		ObjectPool<ArrayList<Object>> objectPool = new ObjectPool<>(ArrayList::new);
		ArrayList<Object> list1 = objectPool.get();
		list1.add("first");

		ArrayList<Object> list2 = objectPool.get();
		list2.add("second");

		assertNotEquals(list1, list2);

		objectPool.release(list1);

		ArrayList<Object> list3 = objectPool.get();

		assertEquals(list1, list3);

		ArrayList<Object> list4 = objectPool.get();

		assertNotEquals(list4, list1);
		assertNotEquals(list4, list2);
		assertNotEquals(list4, list3);
	}
}
