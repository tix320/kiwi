package com.github.tix320.kiwi.observable.transform.multiple.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> extends Observable<T> {

	private static final Unsubscription UNTIL_UNSUBSCRIPTION = new Unsubscription();

	private static final SourceCompletion SOURCE_COMPLETED_VIA_UNTIL = new SourceCompletion(
		"SOURCE_COMPLETED_VIA_UNTIL");

	private final Observable<T> mainSource;

	private final Observable<?> untilSource;

	public UntilObservable(Observable<T> mainSource, Observable<?> untilSource) {
		this.mainSource = Objects.requireNonNull(mainSource);
		this.untilSource = Objects.requireNonNull(untilSource);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		var context = new UntilContext<T>(subscriber);
		Subscriber<Object> untilSubscriber = subscriber.spawn(new UntilSubscriber<>(context));
		Subscriber<T> mainSubscriber = subscriber.spawn(new MainSubscriber<>(context));

		try {
			untilSource.subscribe(untilSubscriber);
			mainSource.subscribe(mainSubscriber);
		} catch (Exception exception) {
			SubscriptionUtils.rollback(context.subscriptions, exception);
		}
	}

	private static final class UntilContext<T> {

		private final Subscriber<? super T> downstream;
		private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();
		private volatile Subscription mainSubscription;
		private volatile Subscription untilSubscription;
		private final AtomicBoolean terminated = new AtomicBoolean(false);

		private final Subscription generalSubscription = new Subscription() {
			@Override
			protected void onRequest(long count) {
				mainSubscription.request(count);
			}

			@Override
			protected void onUnboundRequest() {
				mainSubscription.requestUnbounded();
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				SubscriptionUtils.cancelAll(subscriptions, new UserUnsubscription(unsubscription));
			}
		};

		private UntilContext(Subscriber<? super T> downstream) {
			this.downstream = downstream;
		}

		private void setUntilSubscription(Subscription subscription) {
			untilSubscription = subscription;
			subscriptions.add(subscription);
		}

		private void setMainSubscription(Subscription subscription) {
			mainSubscription = subscription;
			subscriptions.add(subscription);
		}

		private void stopFromUntil() {
			if (!terminated.compareAndSet(false, true)) {
				return;
			}

			SubscriptionUtils.cancelAllThen(subscriptions, UNTIL_UNSUBSCRIPTION,
				() -> downstream.complete(SOURCE_COMPLETED_VIA_UNTIL));
		}

		private void fail(Throwable error) {
			if (terminated.compareAndSet(false, true)) {
				SubscriptionUtils.failAll(subscriptions, downstream, error);
			}
		}

	}

	private static final class UntilSubscriber<T> extends MinorSubscriber<Object, T> {

		private final UntilContext<T> context;

		private UntilSubscriber(UntilContext<T> context) {
			this.context = context;
		}

		@Override
		protected void onSubscribe(Subscription subscription) {
			context.setUntilSubscription(subscription);
			subscription.requestUnbounded();
		}

		@Override
		protected void onNext(Object item) {
			context.stopFromUntil();
		}

		@Override
		protected void onComplete(Completion completion) {
			if (completion instanceof SourceCompletion) {
				context.stopFromUntil();
			}
		}

		@Override
		protected void onError(Throwable error) {
			context.fail(error);
		}

	}

	private static final class MainSubscriber<T> extends MinorSubscriber<T, T> {

		private final UntilContext<T> context;

		private MainSubscriber(UntilContext<T> context) {
			this.context = context;
		}

		@Override
		protected void onSubscribe(Subscription subscription) {
			context.setMainSubscription(subscription);
			context.downstream.setSubscription(context.generalSubscription);
		}

		@Override
		protected void onNext(T item) {
			if (!context.terminated.get()) {
				context.downstream.publish(item);
			}
		}

		@Override
		protected void onComplete(Completion completion) {
			if (completion == UNTIL_UNSUBSCRIPTION) {
				return;
			}
			if (!context.terminated.compareAndSet(false, true)) {
				return;
			}

			if (completion instanceof UserUnsubscription userUnsubscription) {
				context.downstream.complete(userUnsubscription.realUnsubscription());
			} else {
				SubscriptionUtils.cancelAllThen(List.of(context.untilSubscription), Unsubscription.DEFAULT,
					() -> context.downstream.complete(completion));
			}
		}

		@Override
		protected void onError(Throwable error) {
			context.fail(error);
		}

	}

	private static final class UserUnsubscription extends Unsubscription {

		private UserUnsubscription(Unsubscription unsubscription) {
			super(unsubscription);
		}

		private Unsubscription realUnsubscription() {
			return data();
		}

	}

}
