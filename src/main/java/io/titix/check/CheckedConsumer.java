package io.titix.check;

public interface CheckedConsumer<T> {

	void accept(T t) throws Throwable;
}
