package com.github.tix320.kiwi.api.proxy;

import java.lang.annotation.Annotation;

public final class AnnotationInterceptor<T> {

	private final Class<? extends Annotation> annotationClass;

	private final Interceptor<T> interceptor;

	public AnnotationInterceptor(Class<? extends Annotation> annotationClass, Interceptor<T> interceptor) {
		this.annotationClass = annotationClass;
		this.interceptor = interceptor;
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public Interceptor<T> getInterceptor() {
		return interceptor;
	}
}
