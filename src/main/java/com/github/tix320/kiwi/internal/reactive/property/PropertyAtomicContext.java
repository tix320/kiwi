package com.github.tix320.kiwi.internal.reactive.property;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.github.tix320.kiwi.api.reactive.property.ChangeableProperty;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
public final class PropertyAtomicContext {

	private static final ThreadLocal<Set<ChangeableProperty>> atomicContext = new ThreadLocal<>();

	public static void create() {
		atomicContext.set(Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	public static void destroy() {
		atomicContext.remove();
	}

	public static void commitChangesAndDestroy() {
		Set<ChangeableProperty> properties = atomicContext.get();
		atomicContext.remove();
		for (ChangeableProperty property : properties) {
			try {
				property.publishChanges();
			}
			catch (PropertyClosedException e) {
				System.err.println("PROPERTY WARNING: Atomic Context Destroy - " + e.getMessage());
			}
		}
	}

	public static boolean inAtomicContext(ChangeableProperty property) {
		Set<ChangeableProperty> properties = atomicContext.get();
		if (properties == null) {
			return false;
		}
		else {
			properties.add(property);
			return true;
		}
	}

	public static boolean inAtomicContext() {
		return atomicContext.get() != null;
	}
}
