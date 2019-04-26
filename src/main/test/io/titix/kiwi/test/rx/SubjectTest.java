package com.gitlab.tixtix320.kiwi.test.rx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
class SubjectTest {

	@Test
	void iterableTest() {
		List<String> expected = Arrays.asList("2", "1", "0");
		List<String> actual = new ArrayList<>();


		Subject<String> subject = Subject.single();
		Observable<String> observable = subject.asObservable();
		observable.subscribe(actual::add);

		subject.next(() -> new Iterator<>() {
			int index = 3;

			@Override
			public boolean hasNext() {
				return index-- > 0;
			}

			@Override
			public String next() {
				return index + "";
			}
		});

		assertEquals(expected, actual);
	}

	@Test
	void completeTest() {
		Subject<Integer> subject = Subject.single();

		subject.next(1);
		subject.next(2);
		subject.next(3);
		subject.complete();
		assertThrows(IllegalStateException.class,() -> subject.next(4));
	}
}
