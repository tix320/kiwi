package com.github.tix320.kiwi.api.observable;

public interface ConditionalConsumer<T> {

    boolean consume(T object);
}
