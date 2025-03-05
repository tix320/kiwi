package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 08-Apr-20.
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class GetOnTimoutObservableTest {

	@Test
	public void publishBeforeTimoutTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(1);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable().getOnTimout(Duration.ofSeconds(2), () -> 5).subscribe(actual::add);

		publisher.publish(1);
		publisher.publish(2);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void publishAfterTimoutTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(5);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable().getOnTimout(Duration.ofMillis(500), () -> 5).subscribe(actual::add);

		SchedulerUtils.sleepFor(Duration.ofMillis(800));

		publisher.publish(1);
		publisher.publish(2);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

}
