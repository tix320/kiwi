package com.github.tix320.kiwi.api.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

public interface AnnotationInterceptor<P, A extends Annotation> {

	Class<A> getAnnotationClass();

	Object intercept(A annotation, InterceptionContext<P> context);

	interface InterceptionContext<P> {
		Method getMethod();

		Object[] getArgs();

		P getProxy();

		void putData(String key, Object value);

		Map<String, Object> getData();
	}
}
