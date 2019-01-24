package io.titix.check;

public interface CheckedFunction<T, R> {

	R apply(T t) throws Throwable;
}
