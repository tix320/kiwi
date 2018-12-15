package de.fsyo.uremn.check;

public interface CheckedSupplier<T> {

    T get() throws Throwable;
}
