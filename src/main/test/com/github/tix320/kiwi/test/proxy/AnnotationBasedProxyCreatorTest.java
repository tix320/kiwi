package com.github.tix320.kiwi.test.proxy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import com.github.tix320.kiwi.api.proxy.AnnotationBasedProxyCreator;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.proxy.ProxyCreator;
import com.github.tix320.kiwi.api.util.None;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationBasedProxyCreatorTest {

	@Test
	void oneInterceptorWithCallingNotAnnotatedMethod() {
		TestClass1 testClass = TestClass1.create("message");
		String message = testClass.getMessage();
		assertEquals("message", message);
	}

	@Test
	void oneInterceptorWithCallingAnnotatedMethod() {
		TestClass1 testClass = TestClass1.create("message");
		String message = testClass.getMessageDeprecated();
		assertEquals("messageDeprecated", message);
	}

	@Test
	void oneInterceptorWithReturningValue() {
		TestClass1 testClass = TestClass1.create("foo");
		String message = testClass.getMessageDeprecated();
		assertEquals("boo", message);
	}

	@Test
	void twoInterceptorsWithCallingOneAnnotatedMethod() {
		TestClass2 testClass = TestClass2.create("message");
		String message = testClass.getMessage();
		assertEquals("messageIntercepted1", message);
	}

	@Test
	void twoInterceptorsWithCallingAnnotatedMethodsInRotation() {
		TestClass2 testClass = TestClass2.create("message");
		String message = testClass.getMessage();
		assertEquals("messageIntercepted1", message);
		message = testClass.getMessageDeprecated();
		assertEquals("messageIntercepted1Intercepted2", message);
	}

	@Test
	void twoInterceptorsWithCallingAnnotatedMethodsInSameTime() {
		TestClass2 testClass = TestClass2.create("message");
		String message = testClass.getMessageDoubled();
		assertEquals("messageIntercepted1Intercepted2", message);
	}

	public static class TestClass1 {

		private static final AnnotationBasedProxyCreator<TestClass1> PROXY_CREATOR_IMPL = new AnnotationBasedProxyCreator<>(
				TestClass1.class, new AnnotationInterceptor<>(Deprecated.class, (method, args, target) -> {
			if (target.message.equals("foo")) {
				return "boo";
			}
			return target.message += "Deprecated";
		}));

		String message;

		public static TestClass1 create(String s) {
			return PROXY_CREATOR_IMPL.create(s);
		}

		public TestClass1(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		@Deprecated
		public String getMessageDeprecated() {
			return message;
		}
	}

	public static class TestClass2 {

		private static final ProxyCreator<TestClass2> PROXY_CREATOR;

		static {
			List<AnnotationInterceptor<TestClass2>> interceptors = List.of(
					new AnnotationInterceptor<>(MyAnno.class, (method, args, target) -> {
						target.message += "Intercepted1";
						return None.SELF;
					}), new AnnotationInterceptor<>(Deprecated.class, (method, args, target) -> {
						target.message += "Intercepted2";
						return None.SELF;
					}));
			PROXY_CREATOR = new AnnotationBasedProxyCreator<>(TestClass2.class, interceptors);
		}

		String message;

		public static TestClass2 create(String s) {
			return PROXY_CREATOR.create(s);
		}

		public TestClass2(String message) {
			this.message = message;
		}

		@Deprecated
		public String getMessageDeprecated() {
			return message;
		}

		@MyAnno
		public String getMessage() {
			return message;
		}

		@Deprecated
		@MyAnno
		public String getMessageDoubled() {
			return message;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface MyAnno {

	}
}
