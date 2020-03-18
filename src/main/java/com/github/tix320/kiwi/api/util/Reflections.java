package com.github.tix320.kiwi.api.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Reflections {

	public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
		Set<Class<?>> allInterfaces = new HashSet<>();

		Class<?>[] clazzInterfaces = clazz.getInterfaces();

		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			Set<Class<?>> superClassInterfaces = getAllInterfaces(superclass);
			allInterfaces.addAll(superClassInterfaces);
		}

		allInterfaces.addAll(Arrays.asList(clazzInterfaces));

		for (Class<?> clazzInterface : clazzInterfaces) {
			Set<Class<?>> interfacesOfInterface = getAllInterfaces(clazzInterface);
			allInterfaces.addAll(interfacesOfInterface);
		}

		return allInterfaces;
	}


}
