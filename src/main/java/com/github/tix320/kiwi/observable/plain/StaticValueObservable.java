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

/**
 * @author Tigran Sargsyan on 24.02.25
 */
public class StaticValueObservable<T> extends MonoObservable<T> {

	private final T value;

	public StaticValueObservable(T value) {
		this.value = value;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		Token token = subscriber.createToken();

		subscriber.setSubscription(new Subscription() {
			@Override
			protected void onRequest(long count) {
				token.addSignal(new PublishSignal<>(value));
				token.addSignal(new CompleteSignal(SourceCompletion.DEFAULT));
			}

			@Override
			protected void onUnboundRequest() {
				token.addSignal(new PublishSignal<>(value));
				token.addSignal(new CompleteSignal(SourceCompletion.DEFAULT));
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				token.addSignal(new CancelSignal(unsubscription));
			}
		});

		token.activate();
	}

}
