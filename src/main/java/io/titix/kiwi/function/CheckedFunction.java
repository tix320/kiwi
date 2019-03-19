package io.titix.kiwi.function;

public interface CheckedFunction<T, R> {

	R apply(T t) throws Exception;
}
