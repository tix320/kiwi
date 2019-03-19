package io.titix.kiwi.function;

public interface CheckedPredicate<T> {

	boolean test(T t) throws Exception;
}
