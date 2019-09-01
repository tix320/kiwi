package com.gitlab.tixtix320.kiwi.api.function;

public interface CheckedConsumer<T> {

	void accept(T t) throws Exception;
}
