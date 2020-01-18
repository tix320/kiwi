package com.github.tix320.kiwi.api.function;

public interface CheckedSupplier<T> {

    T get() throws Exception;
}
