package com.github.tix320.kiwi.api.proxy;

public interface ProxyCreator<T> {

	T create(T target);
}
