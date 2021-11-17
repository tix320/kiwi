package com.github.tix320.kiwi.observable.transform.single;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.scheduler.DefaultScheduler;
import com.github.tix320.kiwi.observable.signal.*;

public class TimerObservable<T> extends MonoObservable<T> {

	private final Duration delay;

	private final Supplier<? extends T> itemFactory;

	public TimerObservable(Duration delay, Supplier<? extends T> itemFactory) {
		if (delay.isNegative()) {
			throw new IllegalArgumentException(delay.toString());
		}

		this.delay = delay;
		this.itemFactory = itemFactory;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		subscriber.setSubscription(new SubscriptionImpl(subscriber, subscriber.getSignalManager()));
	}

	private final class SubscriptionImpl extends Subscription {

		private final Subscriber<? super T> subscriber;

		private final SignalManager.Token token;

		private final AtomicBoolean requested = new AtomicBoolean(false);

		private SubscriptionImpl(Subscriber<? super T> subscriber, SignalManager signalManager) {
			this.subscriber = subscriber;
			this.token = signalManager.createToken(new SignalVisitorImpl());
			token.start();
		}

		@Override
		protected void onRequest(long count) {
			boolean changed = requested.compareAndSet(false, true);
			if (changed) {
				// don't care about count, because this observable emits only one item

				DefaultScheduler.get().schedule(delay.toMillis(), TimeUnit.MILLISECONDS, () -> {
					T item;
					try {
						item = itemFactory.get();
						token.addSignal(new NextSignal<>(item));
						token.addSignal(new CompleteSignal(SourceCompletion.DEFAULT));
					}
					catch (Throwable e) {
						DelayObservableException delayObservableException = new DelayObservableException(
								"Exception in provided object factory", e);
						token.addSignal(new ErrorSignal(delayObservableException));
					}
					token.tryRunWorker();
				});
			}
		}

		@Override
		protected void onUnboundRequest() {
			onRequest(1);
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {
			CancelSignal cancelSignal = new CancelSignal(unsubscription);

			token.addSignal(cancelSignal);

			token.tryRunWorker();
		}

		private final class SignalVisitorImpl implements SignalVisitor {

			@Override
			public SignalVisitResult visit(NextSignal<?> nextSignal) {
				//noinspection unchecked
				T casted = (T) nextSignal.getItem();
				subscriber.publish(casted);

				return SignalVisitResult.CONTINUE;
			}

			@Override
			public SignalVisitResult visit(CancelSignal cancelSignal) {
				subscriber.complete(cancelSignal.unsubscription());

				return SignalVisitResult.COMPLETE;
			}

			@Override
			public SignalVisitResult visit(CompleteSignal completeSignal) {
				subscriber.complete(completeSignal.completion());

				return SignalVisitResult.COMPLETE;
			}

			@Override
			public SignalVisitResult visit(ErrorSignal errorSignal) {
				subscriber.completeWithError(errorSignal.error());

				return SignalVisitResult.COMPLETE;
			}
		}
	}

	private static final class DelayObservableException extends RuntimeException {
		public DelayObservableException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
