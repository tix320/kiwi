package com.github.tix320.kiwi.api.proxy;

import java.lang.reflect.Method;

public interface Interceptor<T> {

	Object intercept(Method method, Object[] args, T proxy);
}
