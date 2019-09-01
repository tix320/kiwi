package com.gitlab.tixtix320.kiwi.api.function;

public interface CheckedFunction<T, R> {

	R apply(T t) throws Exception;
}
