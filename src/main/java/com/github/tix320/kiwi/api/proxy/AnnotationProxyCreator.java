package com.github.tix320.kiwi.api.proxy;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class AnnotationProxyCreator<T, A extends Annotation> implements ProxyCreator<T> {

	private final ProxyCreator<T> interfaceProxyCreator;

	public AnnotationProxyCreator(Class<T> interfacee, Class<? extends Annotation> annotationClass,
								  BiConsumer<Optional<A>, T> interceptFunction) {
		this.interfaceProxyCreator = new DefaultProxyCreator<>(interfacee, (method, target) -> {
			@SuppressWarnings("unchecked")
			A annotation = (A) method.getAnnotation(annotationClass);
			if (annotation == null) {
				interceptFunction.accept(Optional.empty(), target);
			}
			else {
				interceptFunction.accept(Optional.of(annotation), target);
			}
		});
	}

	@Override
	public T create(T target) {
		return interfaceProxyCreator.create(target);
	}
}
