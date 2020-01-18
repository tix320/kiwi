package com.github.tix320.kiwi.api.function;

public interface CheckedConsumer<T> {

    void accept(T t) throws Exception;
}
