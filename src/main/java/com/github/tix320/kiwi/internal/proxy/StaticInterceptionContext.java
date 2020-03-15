package com.github.tix320.kiwi.internal.proxy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor.InterceptionContext;

public class StaticInterceptionContext<P> implements InterceptionContext<P> {

	private final Method method;

	private final Object[] args;

	private final P proxy;

	private final Map<String, Object> data;

	public StaticInterceptionContext(Method method, Object[] args, P proxy, Map<String, Object> data) {
		this.method = method;
		this.args = args;
		this.proxy = proxy;
		this.data = data;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public Object[] getArgs() {
		return args;
	}

	@Override
	public P getProxy() {
		return proxy;
	}

	@Override
	public void putData(String key, Object value) {
		data.put(key, value);
	}

	@Override
	public Map<String, Object> getData() {
		return Collections.unmodifiableMap(data);
	}
}
