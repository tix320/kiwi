package io.titix.kiwi.check;

public interface CheckedFunction<T, R> {

	R apply(T t) throws Throwable;
}
