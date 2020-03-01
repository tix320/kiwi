package com.github.tix320.kiwi.api.proxy;

public interface ProxyCreator<T> {

	T create(Object... args);

	Class<? extends T> getTargetClass();

	Class<? extends T> getProxyClass();
}
