package io.titix.kiwi.check;

public interface CheckedSupplier<T> {

	T get() throws Exception;
}
