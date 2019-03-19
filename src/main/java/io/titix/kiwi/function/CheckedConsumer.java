package io.titix.kiwi.function;

public interface CheckedConsumer<T> {

	void accept(T t) throws Exception;
}
