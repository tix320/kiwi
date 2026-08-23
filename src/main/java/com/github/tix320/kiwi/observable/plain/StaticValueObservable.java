package com.github.tix320.kiwi.observable.plain;

import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.signal.CancelSignal;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.observable.signal.SignalSynchronizer.Token;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tigran Sargsyan on 24.02.25
 */
public class StaticValueObservable<T> extends MonoObservable<T> {

	private final T value;

	public StaticValueObservable(T value) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		Token token = subscriber.createToken();

		subscriber.setSubscription(new Subscription() {
			private final AtomicBoolean requested = new AtomicBoolean(false);

			@Override
			protected void onRequest(long count) {
				produceValue();
			}

			@Override
			protected void onUnboundRequest() {
				produceValue();
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				token.addSignal(new CancelSignal(unsubscription));
			}

			private void produceValue() {
				if (requested.compareAndSet(false, true)) {
					token.addSignal(new PublishSignal<>(value));
					token.addSignal(new CompleteSignal(SourceCompletion.DEFAULT));
				}
			}
		});

		token.activate();
	}

}
