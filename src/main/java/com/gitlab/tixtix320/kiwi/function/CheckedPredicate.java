package com.gitlab.tixtix320.kiwi.function;

public interface CheckedPredicate<T> {

	boolean test(T t) throws Exception;
}
