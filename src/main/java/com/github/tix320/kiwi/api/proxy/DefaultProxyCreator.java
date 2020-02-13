package com.github.tix320.kiwi.api.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class DefaultProxyCreator<T> implements ProxyCreator<T> {

	private final Class<T> interfacee;

	private final BiConsumer<Method, T> interceptFunction;

	private final Map<Method, MethodHandle> methodHandles;

	public DefaultProxyCreator(Class<T> interfacee, BiConsumer<Method, T> interceptFunction) {
		if (!interfacee.isInterface()) {
			throw new IllegalArgumentException("Interface must be provided");
		}
		this.interfacee = interfacee;
		this.interceptFunction = interceptFunction;
		this.methodHandles = createMethodHandles(interfacee);
	}

	@SuppressWarnings("unchecked")
	public T create(T target) {
		return (T) Proxy.newProxyInstance(interfacee.getClassLoader(), new Class[]{interfacee},
				(proxy, method, args) -> {
					interceptFunction.accept(method, target);
					MethodHandle methodHandle = methodHandles.get(method);
					return methodHandle.bindTo(target).invokeWithArguments(args);
				});
	}

	private Map<Method, MethodHandle> createMethodHandles(Class<?> interfacee) {
		Method[] methods = interfacee.getMethods();
		Map<Method, MethodHandle> methodHandles = new HashMap<>();
		for (Method method : methods) {
			try {
				methodHandles.put(method, MethodHandles.publicLookup().unreflect(method));
			}
			catch (IllegalAccessException e) {
				throw new IllegalStateException(String.format("Cannot access to class %s, you must open your module.",
						method.getDeclaringClass()), e);
			}
		}
		return methodHandles;
	}
}
