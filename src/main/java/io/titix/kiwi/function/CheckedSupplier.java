package io.titix.kiwi.function;

public interface CheckedSupplier<T> {

	T get() throws Exception;
}
