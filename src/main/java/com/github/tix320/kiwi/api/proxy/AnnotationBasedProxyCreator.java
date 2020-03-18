package com.github.tix320.kiwi.api.proxy;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.internal.proxy.StaticInterceptionContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;

import static net.bytebuddy.matcher.ElementMatchers.any;

public final class AnnotationBasedProxyCreator<T> implements ProxyCreator<T> {

	private static final Lookup LOOKUP = MethodHandles.publicLookup();

	private final Class<? extends T> targetClass;

	private final List<MethodHandle> constructorsMethodHandles;

	private final List<AnnotationInterceptor<? super T, ?>> annotationInterceptors;

	public AnnotationBasedProxyCreator(Class<? extends T> targetClass,
									   AnnotationInterceptor<? super T, ?> annotationInterceptor) {
		this(targetClass, List.of(annotationInterceptor));
	}

	public AnnotationBasedProxyCreator(Class<? extends T> targetClass,
									   List<AnnotationInterceptor<? super T, ?>> annotationInterceptors) {
		if (Modifier.isFinal(targetClass.getModifiers())) {
			throw new IllegalArgumentException(
					String.format("Final class %s cannot be wrapped with proxy", targetClass));
		}
		if (targetClass.getConstructors().length == 0) {
			throw new IllegalArgumentException(String.format("%s does not have any public constructor", targetClass));
		}
		this.targetClass = targetClass;
		Class<?> proxyClass = annotationInterceptors.isEmpty() ? targetClass : createProxyClass(targetClass);
		this.annotationInterceptors = annotationInterceptors;
		this.constructorsMethodHandles = createConstructorMethodHandles(proxyClass);
	}

	private List<MethodHandle> createConstructorMethodHandles(Class<?> proxyClass) {
		return Arrays.stream(proxyClass.getConstructors())
				.map(constructor -> Try.supply(() -> LOOKUP.unreflectConstructor(constructor))
						.getOrElseThrow(e -> new IllegalStateException(
								String.format("Cannot access to %s for creating proxy.", targetClass), e)))
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
		public <A extends Annotation> Object intercept(@SuperCall Callable<?> callable,
													   @AllArguments Object[] allArguments, @Origin Method method,
													   @This T proxy)
				throws Throwable {
			StaticInterceptionContext<T> context = new StaticInterceptionContext<>(method, allArguments, proxy,
					new HashMap<>());
			for (AnnotationInterceptor<? super T, ?> interceptor : annotationInterceptors) {
				AnnotationInterceptor<T, A> annotationInterceptor = (AnnotationInterceptor<T, A>) interceptor;
				Class<A> annotationClass = annotationInterceptor.getAnnotationClass();
				if (annotationClass == null) {
					Object result = annotationInterceptor.intercept(null, context);
					if (result != None.SELF) {
						return result;
					}
				}
				else {
					A annotation = method.getAnnotation(annotationClass);
					if (annotation != null) {
						Object result = annotationInterceptor.intercept(annotation, context);
						if (result != None.SELF) {
							return result;
						}
					}
				}
			}
			return callable.call();
		}
	}

}
