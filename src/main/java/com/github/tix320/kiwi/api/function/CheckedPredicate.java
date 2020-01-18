package com.github.tix320.kiwi.api.function;

public interface CheckedPredicate<T> {

    boolean test(T t) throws Exception;
}
