package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.ReplayPublisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class UnlimitReplayPublisherTest {

	@Test
	public void simpleTest() throws InterruptedException {
		List<Integer> expected1 = List.of(10, 20, 40);
		List<Integer> expected2 = List.of(30, 60, 120);

		List<Integer> actual1 = new CopyOnWriteArrayList<>();
		List<Integer> actual2 = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.buffered();

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(10);

		observable.subscribe(actual1::add);

		publisher.publish(20);

		observable.map(integer -> integer * 3).subscribe(actual2::add);

		publisher.publish(40);

		SchedulerUtils.awaitTermination();

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
	}

	@Test
	public void concurrentPublishFromBufferAnyPublish() throws InterruptedException {
		int chunkSize = 100000;
		List<Integer> expectedPart1 = IntStream.rangeClosed(1, chunkSize).boxed().collect(Collectors.toList());
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		ReplayPublisher<Integer> publisher = Publisher.buffered();

		Observable<Integer> observable = publisher.asObservable();

		IntStream.rangeClosed(1, chunkSize).forEach(publisher::publish);

		int lol = (int) (chunkSize * 1.5);
		Thread thread = new Thread(() -> {
			IntStream.rangeClosed(chunkSize + 1, lol).forEach(publisher::publish);
		});
		thread.start();

		Thread thread1 = new Thread(() -> {
			IntStream.rangeClosed(lol + 1, chunkSize * 2).forEach(publisher::publish);
		});
		thread1.start();

		observable.subscribe(actual::add);

		thread.join();
		thread1.join();

		SchedulerUtils.awaitTermination();

		assertEquals(chunkSize * 2, actual.size());
		assertEquals(expectedPart1, actual.subList(0, chunkSize));
	}

}
