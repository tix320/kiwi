package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherCursor;
import java.util.Objects;
import java.util.function.Function;

public final class SinglePublisher<T> extends BasePublisher<T> {

	private final Function<? super T, ? extends T> publicationMapper;

	private volatile T regularValue;
	private volatile T valueDuringFreeze;
	private volatile PublishSignal<T> regularSignal;

	private volatile int freezeCounter = 0;

	public SinglePublisher() {
		this.publicationMapper = Function.identity();
	}

	public SinglePublisher(T initialValue) {
		this(initialValue, Function.identity());
	}

	public SinglePublisher(T initialValue, Function<? super T, ? extends T> publicationMapper) {
		this.publicationMapper = Objects.requireNonNull(publicationMapper);
		publish(Objects.requireNonNull(initialValue));
	}

	@Override
	protected void onPublish(T item) {
		Objects.requireNonNull(item);

		if (freezeCounter > 0) {
			valueDuringFreeze = item;
		} else {
			PublishSignal<T> signal = new PublishSignal<>(publicationMapper.apply(item));
			regularValue = item;
			regularSignal = signal;
		}
	}

	@Override
	protected PublisherCursor createCursor() {
		return new PublisherCursor() {

			private volatile PublishSignal<T> lastValue;

			@Override
			public boolean hasNext() {
				var value = regularSignal;
				return value != null && value != lastValue;
			}

			@Override
			public PublishSignal<T> next() {
				var value = regularSignal;
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
				T ref = valueDuringFreeze;
				valueDuringFreeze = null;
				if (ref != null && !isClosed()) {
					publish(ref);
				}
			}
		}
	}

	public boolean CASPublish(T expected, T newValue) {
		Objects.requireNonNull(newValue);

		synchronized (lock) {
			if (isClosed()) {
				throw new PublisherClosedException("Publisher is already closed, items publishing is prohibited");
			}

			T currentValue = currentValue();

			if (Objects.equals(currentValue, expected)) {
				publish(newValue);
				return true;
			} else {
				return false;
			}
		}
	}

	public <R> R modifyValue(Function<T, ModifyResult<R>> function) {
		synchronized (lock) {
			if (isClosed()) {
				throw new PublisherClosedException("Publisher is already closed, items publishing is prohibited");
			}

			T currentValue = currentValue();
			if (currentValue == null) {
				return null;
			}

			var modifyResult = function.apply(currentValue);
			if (modifyResult.republish()) {
				publish(currentValue);
			}
			return modifyResult.data();
		}
	}

	private T currentValue() {
		if (freezeCounter > 0 && valueDuringFreeze != null) {
			return valueDuringFreeze;
		}
		return regularValue;
	}

	public T getValue() {
		return regularValue;
	}

	public record ModifyResult<R>(R data, boolean republish) {
	}

}
