package com.gitlab.tixtix320.kiwi.test.observable;

import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class TransformersTest {

    @Test
    void decoratorOnCompleteTest() {
        AtomicReference<String> actual = new AtomicReference<>("");

        Observable.of(10).map(integer -> integer + 5)
                .join(integer -> integer + "", ",", "(", ")")
                .subscribe(actual::set);

        assertEquals("(15)", actual.get());
    }

    @Test
    void filterTest() {
        AtomicReference<List<Integer>> actual = new AtomicReference<>();
        Observable.of(1, 2, 3, 4, 5).filter(integer -> integer > 2).toList().subscribe(actual::set);
        assertEquals(List.of(3, 4, 5), actual.get());
    }
}
