package io.titix.check;

public interface CheckedPredicate<T> {

	boolean test(T t) throws Throwable;
}
