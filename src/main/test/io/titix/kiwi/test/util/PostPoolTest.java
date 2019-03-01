package io.titix.kiwi.test.util;

import java.util.ArrayList;

import io.titix.kiwi.util.PostPool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class PostPoolTest {

	@Test
	void simpleTest() {
		PostPool<ArrayList<Object>> prePool = new PostPool<>();
		ArrayList<Object> list1 = prePool.get(ArrayList::new);
		list1.add("first");

		ArrayList<Object> list2 = prePool.get(ArrayList::new);
		list2.add("second");

		assertNotEquals(list1, list2);

		prePool.release(list1);

		ArrayList<Object> list3 = prePool.get(ArrayList::new);

		assertEquals(list1, list3);

		ArrayList<Object> list4 = prePool.get(ArrayList::new);

		assertNotEquals(list4, list1);
		assertNotEquals(list4, list2);
		assertNotEquals(list4, list3);
	}
}
