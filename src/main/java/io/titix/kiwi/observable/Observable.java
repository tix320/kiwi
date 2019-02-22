package io.titix.kiwi.observable;

import java.util.function.Consumer;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<T> consumer);

	default Observable<T> take(long count) {
		long real = count < 1 ? 1 : count;
		return consumer -> {
			var subscription = new Object() {
				Subscription $;
				long limit = real;
			};
			subscription.$ = this.subscribe(t -> {
				subscription.limit--;
				consumer.accept(t);
				if (subscription.limit == 0) {
					subscription.$.unsubscribe();
				}
			});

			return subscription.$;

		};
	}

	default Observable<T> one() {
		return consumer -> {
			var subscription = new Object() {
				Subscription $;
			};
			subscription.$ = this.subscribe(t -> {
				consumer.accept(t);
				subscription.$.unsubscribe();
			});
			return subscription.$;
		};
	}
}
