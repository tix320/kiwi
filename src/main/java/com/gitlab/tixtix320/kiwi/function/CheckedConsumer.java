package com.gitlab.tixtix320.kiwi.function;

public interface CheckedConsumer<T> {

	void accept(T t) throws Exception;
}
