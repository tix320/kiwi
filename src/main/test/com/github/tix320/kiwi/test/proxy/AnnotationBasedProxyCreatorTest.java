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
		assertEquals("messageIntercepted1plainMyAnnoValue", message);
	}

	@Test
	void twoInterceptorsWithCallingAnnotatedMethodsInRotation() {
		TestClass2 testClass = TestClass2.create("message");
		String message = testClass.getMessage();
		assertEquals("messageIntercepted1plainMyAnnoValue", message);
		message = testClass.getMessageDeprecated();
		assertEquals("messageIntercepted1plainMyAnnoValueIntercepted2", message);
	}

	@Test
	void twoInterceptorsWithCallingAnnotatedMethodsInSameTime() {
		TestClass2 testClass = TestClass2.create("message");
		String message = testClass.getMessageDoubled();
		assertEquals("messageIntercepted1doubledMyAnnoValueIntercepted2", message);
	}

	public static class TestClass1 {

		private static final AnnotationBasedProxyCreator<TestClass1> PROXY_CREATOR_IMPL = new AnnotationBasedProxyCreator<>(
				TestClass1.class, new AnnotationInterceptor<TestClass1, Deprecated>() {
			@Override
			public Class<Deprecated> getAnnotationClass() {
				return Deprecated.class;
			}

			@Override
			public Object intercept(Deprecated annotation, InterceptionContext<TestClass1> context) {
				TestClass1 proxy = context.getProxy();
				if (proxy.message.equals("foo")) {
					return "boo";
				}
				return proxy.message += "Deprecated";
			}
		});

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
			List<AnnotationInterceptor<? super TestClass2, ?>> interceptors = List.of(
					new AnnotationInterceptor<TestClass2, MyAnno>() {
						@Override
						public Class<MyAnno> getAnnotationClass() {
							return MyAnno.class;
						}

						@Override
						public Object intercept(MyAnno annotation, InterceptionContext<TestClass2> context) {
							TestClass2 proxy = context.getProxy();
							proxy.message += "Intercepted1" + annotation.value();
							return None.SELF;
						}
					}, new AnnotationInterceptor<TestClass2, Deprecated>() {
						@Override
						public Class<Deprecated> getAnnotationClass() {
							return Deprecated.class;
						}

						@Override
						public Object intercept(Deprecated annotation, InterceptionContext<TestClass2> context) {
							context.getProxy().message += "Intercepted2";
							return None.SELF;
						}
					});

			PROXY_CREATOR = new AnnotationBasedProxyCreator<TestClass2>(TestClass2.class, interceptors);
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

		@MyAnno("plainMyAnnoValue")
		public String getMessage() {
			return message;
		}

		@Deprecated
		@MyAnno("doubledMyAnnoValue")
		public String getMessageDoubled() {
			return message;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface MyAnno {
		String value();
	}
}
