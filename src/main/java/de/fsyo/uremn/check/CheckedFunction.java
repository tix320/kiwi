package de.fsyo.uremn.check;

public interface CheckedFunction<T, R> {

	R apply(T t) throws Throwable;
}
