package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherCursor;
import java.util.Objects;

public final class SinglePublisher<T> extends BasePublisher<T> {

	private volatile PublishSignal<T> regularValue;
	private volatile PublishSignal<T> valueDuringFreeze;
	private volatile int freezeCounter = 0;

	public SinglePublisher() {
	}

	public SinglePublisher(T initialValue) {
		Objects.requireNonNull(initialValue);
		publish(initialValue);
	}

	@Override
	protected void onPublish(T item) {
		Objects.requireNonNull(item);

		var signal = new PublishSignal<>(item);
		if (freezeCounter > 0) {
			valueDuringFreeze = signal;
		} else {
			regularValue = signal;
		}
	}

	@Override
	protected PublisherCursor createCursor() {
		return new PublisherCursor() {

			private volatile PublishSignal<T> lastValue;

			@Override
			public boolean hasNext() {
				var value = regularValue;
				return value != null && value != lastValue;
			}

			@Override
			public PublishSignal<T> next() {
				var value = regularValue;
				if (value == null || value == lastValue) {
					return null;
				}

				lastValue = value;
				return value;
			}
		};
	}

	public void freeze() {
		synchronized (lock) {
			freezeCounter++;
		}
	}

	public void unfreeze() {
		synchronized (lock) {
			if (freezeCounter == 0) {
				return;
			}

			freezeCounter--;

			if (freezeCounter == 0) {
				PublishSignal<T> ref = valueDuringFreeze;
				valueDuringFreeze = null;
				if (ref != null) {
					publish(ref.getItem());
				}
			}
		}

	}

	public boolean CASPublish(T expected, T newValue) {
		Objects.requireNonNull(newValue);

		synchronized (lock) {
			PublishSignal<T> valueSignal = regularValue;
			T currentValue = valueSignal == null ? null : valueSignal.getItem();

			if (Objects.equals(currentValue, expected)) {
				publish(newValue);
				return true;
			} else {
				return false;
			}
		}
	}

	public T getValue() {
		var valueSignal = regularValue;
		if (valueSignal == null) {
			return null;
		}
		return valueSignal.getItem();
	}

}
