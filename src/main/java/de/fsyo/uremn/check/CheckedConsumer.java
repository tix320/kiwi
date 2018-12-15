package de.fsyo.uremn.check;

public interface CheckedConsumer<T> {

    void accept(T t) throws Throwable;
}
