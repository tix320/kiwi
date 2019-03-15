package io.titix.kiwi.check;

public interface CheckedPredicate<T> {

	boolean test(T t) throws Exception;
}
