package com.gitlab.tixtix320.kiwi.api.observable;

public interface ConditionalConsumer<T> {

    boolean consume(T object);
}
