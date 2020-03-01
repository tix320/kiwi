package com.github.tix320.kiwi.api.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.util.None;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;

import static net.bytebuddy.matcher.ElementMatchers.any;

public final class AnnotationBasedProxyCreator<T> implements ProxyCreator<T> {

	private final Class<? extends T> targetClass;

	private final Class<? extends T> proxyClass;

	private final List<MethodHandle> constructorsMethodHandles;

	private final List<AnnotationInterceptor<T>> interceptors;

	public AnnotationBasedProxyCreator(Class<? extends T> targetClass, AnnotationInterceptor<T> interceptor) {
		this(targetClass, List.of(interceptor));
	}

	public AnnotationBasedProxyCreator(Class<? extends T> targetClass, List<AnnotationInterceptor<T>> interceptors) {
		if (Modifier.isFinal(targetClass.getModifiers())) {
			throw new IllegalArgumentException(
					String.format("Final class %s cannot be wrapped with proxy", targetClass));
		}
		if (targetClass.getConstructors().length == 0) {
			throw new IllegalArgumentException(String.format("%s does not have any public constructor", targetClass));
		}
		this.targetClass = targetClass;
		this.interceptors = interceptors;
		this.proxyClass = createProxyClass(targetClass);
		this.constructorsMethodHandles = createConstructorMethodHandles(proxyClass);
	}

	private List<MethodHandle> createConstructorMethodHandles(Class<?> proxyClass) {
		Lookup lookup = MethodHandles.publicLookup();
		return Arrays.stream(proxyClass.getConstructors())
				.map(constructor -> Try.supplyOrRethrow(() -> lookup.unreflectConstructor(constructor)))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T create(Object... args) {
		List<Class<?>> types = Arrays.stream(args).map(Object::getClass).collect(Collectors.toList());
		try {
			for (MethodHandle methodHandle : constructorsMethodHandles) {
				try {
					return (T) methodHandle.invokeWithArguments(args);
				}
				catch (WrongMethodTypeException ignored) {
				}
			}
			throw new IllegalArgumentException(
					String.format("For types %s not found matching constructor or it is not public", types));
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Throwable throwable) {
			throw new IllegalStateException(throwable);
		}
	}

	@Override
	public Class<? extends T> getTargetClass() {
		return targetClass;
	}

	@Override
	public Class<? extends T> getProxyClass() {
		return proxyClass;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends T> createProxyClass(Class<?> targetClass) {
		return (Class<? extends T>) new ByteBuddy().subclass(targetClass, Default.IMITATE_SUPER_CLASS)
				.method(any())
				.intercept(MethodDelegation.to(new ProxyInterceptor()))
				.make()
				.load(targetClass.getClassLoader())
				.getLoaded();
	}

	public class ProxyInterceptor {

		@SuppressWarnings("unchecked")
		@RuntimeType
		public Object intercept(@SuperCall Callable<?> callable, @AllArguments Object[] allArguments,
								@Origin Method method, @This Object proxy)
				throws Throwable {
			for (AnnotationInterceptor<T> annotationInterceptor : interceptors) {
				if (method.isAnnotationPresent(annotationInterceptor.getAnnotationClass())) {
					Object result = annotationInterceptor.getInterceptor().intercept(method, (T) proxy);
					if (result != None.SELF) {
						return result;
					}
				}
			}
			return callable.call();
		}
	}

}
