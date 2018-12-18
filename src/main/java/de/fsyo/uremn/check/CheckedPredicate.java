package de.fsyo.uremn.check;

public interface CheckedPredicate<T> {

	boolean test(T t) throws Throwable;
}
