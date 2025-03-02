package com.github.tix320.kiwi.observable.plain;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.signal.CancelSignal;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.observable.signal.SignalSynchronizer.Token;
import com.github.tix320.kiwi.publisher.internal.util.SlidingWindowLinkedList;
import java.util.Collection;
import java.util.List;

/**
 * @author Tigran Sargsyan on 24.02.25
 */
public class StaticValuesObservable<T> extends Observable<T> {

	private final List<T> values;

	public StaticValuesObservable(Collection<T> values) {
		this.values = List.copyOf(values);
	}

	public StaticValuesObservable(T[] values) {
		this.values = List.of(values);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		var list = new SlidingWindowLinkedList<T>(values.size());
		values.forEach(list::append);
		var slider = list.slider();

		Token token = subscriber.createToken();

		subscriber.setSubscription(new Subscription() {
			@Override
			protected void onRequest(long count) {
				for (int i = 0; i < count && slider.hasNext(); i++) {
					token.addSignal(new PublishSignal<>(slider.next()));
				}
				if (!slider.hasNext()) {
					token.addSignal(new CompleteSignal(SourceCompletion.DEFAULT));
				}
			}

			@Override
			protected void onUnboundRequest() {
				while (slider.hasNext()) {
					token.addSignal(new PublishSignal<>(slider.next()));
				}
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
