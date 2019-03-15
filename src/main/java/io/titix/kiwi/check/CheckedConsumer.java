package io.titix.kiwi.check;

public interface CheckedConsumer<T> {

	void accept(T t) throws Exception;
}
