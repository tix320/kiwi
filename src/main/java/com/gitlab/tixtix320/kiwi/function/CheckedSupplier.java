package com.gitlab.tixtix320.kiwi.function;

public interface CheckedSupplier<T> {

	T get() throws Exception;
}
