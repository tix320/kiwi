package io.titix.check;

public interface CheckedSupplier<T> {

	T get() throws Throwable;
}
