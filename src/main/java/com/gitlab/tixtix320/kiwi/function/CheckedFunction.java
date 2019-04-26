package com.gitlab.tixtix320.kiwi.function;

public interface CheckedFunction<T, R> {

	R apply(T t) throws Exception;
}
