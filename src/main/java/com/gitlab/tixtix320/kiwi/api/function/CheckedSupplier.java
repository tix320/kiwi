package com.gitlab.tixtix320.kiwi.api.function;

public interface CheckedSupplier<T> {

	T get() throws Exception;
}
