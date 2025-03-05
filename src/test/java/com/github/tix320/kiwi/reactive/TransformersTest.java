package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class TransformersTest {

	@Test
	public void decoratorOnCompleteTest() throws InterruptedException {
		AtomicReference<String> actual = new AtomicReference<>("");

		Observable.of(10)
			.map(integer -> integer + 5)
			.join(integer -> integer + "", ",", "(", ")")
			.subscribe(actual::set);

		SchedulerUtils.awaitTermination();

		assertEquals("(15)", actual.get());
	}

	@Test
	public void filterTest() throws InterruptedException {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4, 5).filter(integer -> integer > 2).toList().subscribe(actual::set);

		SchedulerUtils.awaitTermination();

		assertEquals(List.of(3, 4, 5), actual.get());
	}

}
