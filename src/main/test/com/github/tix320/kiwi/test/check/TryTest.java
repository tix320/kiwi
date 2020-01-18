package com.github.tix320.kiwi.test.check;

import com.github.tix320.kiwi.api.check.Try;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class TryTest {

    @Test
    void successTest() {
        Integer value = Try.success(1).get().get();
        assertEquals(1, value);
        Try<?> failure = Try.failure(new IllegalStateException());
    }

    @Test
    void successSupplierTest() {
        Integer value = Try.success(() -> 1).get().get();
        assertEquals(1, value);
        assertThrows(IllegalStateException.class, () -> Try.success(() -> {
            throw new RuntimeException();
        }));
    }

    @Test
    void failureTest() {
        assertTrue(Try.failure(new IllegalStateException()).isFailure());
    }

    @Test
    void failureSupplierTest() {
        assertTrue(Try.failure(IllegalStateException::new).isFailure());
        assertThrows(IllegalStateException.class, () -> Try.failure(() -> {
            throw new RuntimeException();
        }));
    }

    @Test
    void runTest() {
        AtomicBoolean value = new AtomicBoolean();
        assertTrue(Try.run(() -> value.set(true)).isSuccess());
        assertTrue(value.get());

        assertTrue(Try.run(() -> {
            throw new IllegalStateException();
        }).isFailure());
    }

    @Test
    void supplyTest() {
        assertEquals(5, Try.supply(() -> 5).get().get());

        assertTrue(Try.supply(() -> {
            throw new IllegalStateException();
        }).isFailure());
    }


}
