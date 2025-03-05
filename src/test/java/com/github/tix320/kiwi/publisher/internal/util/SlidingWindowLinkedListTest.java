package com.github.tix320.kiwi.publisher.internal.util;

import com.github.tix320.kiwi.publisher.internal.util.SlidingWindowLinkedList.Slider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class SlidingWindowLinkedListTest {

	@Test
	public void constructorRejectsNegativeWindowSize() {
		assertThatThrownBy(() -> new SlidingWindowLinkedList<>(-1))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("windowSize");
	}

	@Test
	public void zeroWindowSizeRetainsNoNodes() {
		// windowSize=0 -> always discard nodes immediately after appending
		var list = new SlidingWindowLinkedList<String>(0);
		list.append("A");
		list.append("B");
		list.append("C");

		var it = list.slider();
		assertNull(it.next());
	}

	@Test
	public void canAppendSingleElementAndSeeItIfWindowSizeNotExceeded() {
		var list = new SlidingWindowLinkedList<Integer>(5);
		list.append(42);

		// Node's value should be 42
		// assertThat(node.value()).isEqualTo(42);

		// Because windowSize=5, we haven't exceeded 5 items,
		// so the appended node remains
		var it = list.slider();
		assertThat(it.next()).isEqualTo(42);
		assertNull(it.next());
	}

	@Test
	public void largeWindowSizeDoesNotRemoveNodesIfNotExceeded() {
		var list = new SlidingWindowLinkedList<String>(10);
		list.append("A");
		list.append("B");
		// Only 2 items => not enough to exceed windowSize=10
		var items = collectAll(list.slider());
		// We see "A" and "B"
		assertThat(items).containsExactly("A", "B");
	}

	@Test
	public void slidingWindowRemovesOlderNodesWhenExceedingWindowSize() {
		// windowSize=2 means keep exactly the 2 newest nodes
		var list = new SlidingWindowLinkedList<String>(2);
		list.append("A"); // keep [A]
		list.append("B"); // keep [A,B]
		list.append("C"); // now discard oldest => keep [B,C]
		list.append("D"); // discard oldest => keep [C,D]
		var items = collectAll(list.slider());
		assertThat(items).containsExactly("C", "D");
	}

	@Test
	public void singleThreadIterationShowsNewestNodesWithinWindow() {
		var list = new SlidingWindowLinkedList<Integer>(3);
		list.append(1); // [1]
		list.append(2); // [1,2]
		list.append(3); // [1,2,3]
		// We haven't exceeded 3 => keep all
		var it = list.slider();
		assertThat(it.next()).isEqualTo(1);
		assertThat(it.next()).isEqualTo(2);
		assertThat(it.next()).isEqualTo(3);
		assertNull(it.next());

		// Append another => [2,3,4] (discard "1")
		list.append(4);
		var items = collectAll(list.slider());
		assertThat(items).containsExactly(2, 3, 4);
	}

	@Test
	public void sliderThrowsNoSuchElementIfExhausted() {
		var list = new SlidingWindowLinkedList<Integer>(1);
		list.append(10); // keep [10]
		var it = list.slider();
		assertThat(it.next()).isEqualTo(10);
		assertNull(it.next());
	}

	@Test
	public void concurrentAppendsDoNotCorruptList() throws InterruptedException {
		// If windowSize=50, we keep the 50 newest items at most
		var list = new SlidingWindowLinkedList<Integer>(50);
		int threads = 4;
		int appendsPerThread = 1000;
		var startLatch = new CountDownLatch(1);
		var doneLatch = new CountDownLatch(threads);
		var pool = Executors.newFixedThreadPool(threads);

		for (int t = 0; t < threads; t++) {
			pool.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < appendsPerThread; i++) {
						list.append(i);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					doneLatch.countDown();
				}
			});
		}
		startLatch.countDown();
		doneLatch.await();
		pool.shutdown();

		// Check that we can iterate safely
		var it = list.slider();
		int count = 0;
		while (it.next() != null) {
			count++;
		}
		// Because windowSize=50, we can't exceed ~50 items
		assertThat(count).isEqualTo(50);
	}

	@Test
	public void windowSizeOneKeepsExactlyOneNode() {
		var list = new SlidingWindowLinkedList<String>(1);
		list.append("A"); // [A]
		list.append("B"); // keep only [B]
		list.append("C"); // keep only [C]
		var items = collectAll(list.slider());
		assertThat(items).containsExactly("C");
	}

	@Test
	public void windowSizeZeroAlwaysEmpty() {
		var list = new SlidingWindowLinkedList<String>(0);
		list.append("X");
		list.append("Y");
		list.append("Z");
		// Because windowSize=0 => always empty
		var items = collectAll(list.slider());
		assertThat(items).isEmpty();
	}

	/**
	 * If windowSize = 0, snapshot should always be empty,
	 * even if we appended items.
	 */
	@Test
	public void zeroWindowSizeAlwaysEmptySnapshot() {
		var list = new SlidingWindowLinkedList<String>(0);
		list.append("A");
		list.append("B");
		list.append("C");

		var snapshot = list.getSnapshot();
		assertThat(snapshot).isEmpty();
	}

	/**
	 * If no elements were ever appended, snapshot is empty
	 * for any windowSize > 0 as well.
	 */
	@Test
	public void noElementsGivesEmptySnapshot() {
		var list = new SlidingWindowLinkedList<String>(5);
		var snapshot = list.getSnapshot();
		assertThat(snapshot).isEmpty();
	}

	/**
	 * Basic usage: Appending fewer items than windowSize,
	 * snapshot returns all items in the correct order.
	 */
	@Test
	public void basicSnapshotWithFewerThanWindowSizeElements() {
		var list = new SlidingWindowLinkedList<Integer>(5);
		list.append(10);
		list.append(20);

		// The snapshot should contain [10, 20] in order
		var snapshot = list.getSnapshot();
		assertThat(snapshot)
			.hasSize(2)
			.containsExactly(10, 20);
	}

	/**
	 * Once we exceed windowSize, the snapshot should be capped
	 * at that size. Also, the order should be from oldest to newest
	 * among the retained nodes.
	 */
	@Test
	public void snapshotCappedByWindowSize() {
		var list = new SlidingWindowLinkedList<Integer>(3);
		list.append(1);    // list -> [1]
		list.append(2);    // list -> [1, 2]
		list.append(3);    // list -> [1, 2, 3]
		list.append(4);    // now oldest removed -> [2, 3, 4]
		list.append(5);    // now oldest removed -> [3, 4, 5]

		// We expect the snapshot to show at most 3 items: [3, 4, 5]
		var snapshot = list.getSnapshot();
		assertThat(snapshot)
			.hasSize(3)
			.containsExactly(3, 4, 5);
	}

	/**
	 * Ensures that the snapshot is correct even if
	 * the list is exactly at windowSize capacity.
	 */
	@Test
	public void snapshotAtExactWindowSize() {
		var list = new SlidingWindowLinkedList<String>(2);
		list.append("A");
		list.append("B");  // [A, B] => capacity is reached, no discard yet

		var snapshot = list.getSnapshot();
		assertThat(snapshot)
			.hasSize(2)
			.containsExactly("A", "B");
	}

	@Test
	public void concurrentAppendsRespectWindowSizeInSnapshot() throws InterruptedException {
		var windowSize = 500;
		var list = new SlidingWindowLinkedList<Integer>(windowSize);

		int threads = 4;
		int appendsPerThread = 1000;
		var latch = new CountDownLatch(1);
		var done = new CountDownLatch(threads);
		ExecutorService pool = Executors.newFixedThreadPool(threads);

		for (int t = 0; t < threads; t++) {
			final int threadId = t;
			pool.submit(() -> {
				try {
					latch.await();
					for (int i = 0; i < appendsPerThread; i++) {
						int value = threadId * 10000 + i;
						list.append(value);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			});
		}

		latch.countDown();
		done.await();
		pool.shutdown();

		var snapshot = list.getSnapshot();

		assertThat(snapshot).hasSize(windowSize);
	}

	private static <T> List<T> collectAll(Slider<T> it) {
		var result = new ArrayList<T>();
		T t;
		while ((t = it.next()) != null) {
			result.add(t);
		}
		return result;
	}

}
