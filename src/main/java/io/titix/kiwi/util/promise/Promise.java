package io.titix.kiwi.util.promise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Tigran.Sargsyan on 20-Feb-19
 */
public final class Promise<T> {

	private volatile T value;

	private Throwable cause;

	private State state;

	private final List<Consumer<T>> acceptHandlers = new ArrayList<>();

	private final List<Consumer<Throwable>> rejectHandlers = new ArrayList<>();

	public Promise(PromiseResolver<T> resolver) {
		state = State.PENDING;

		resolver.resolve(value -> {
			changeState(State.ACCEPTED);
			this.value = value;
		}, cause -> {
			changeState(State.REJECTED);
			this.cause = cause;
		});
	}

	public void changeState(State state) {
		if (this.state == State.PENDING) {
			this.state = state;
		}
	}

	public Promise<?> then(Consumer<T> consumer) {
		return new Promise<>((accept, reject) -> acceptHandlers.add(t -> {
			consumer.accept(t);
			accept.accept(null);
		}));
	}

	public <R> Promise<R> then(Function<T, R> function) {
		return new Promise<>((accept, reject) -> acceptHandlers.add(t -> accept.accept(function.apply(t))));
	}

	public Promise<?> catcho(Consumer<Throwable> consumer) {
		return new Promise<>((accept, reject) -> rejectHandlers.add(consumer.andThen(reject::reject)));
	}

	public <R> Promise<R> catcho(Function<Throwable, R> function) {
		return new Promise<>((accept, reject) -> rejectHandlers.add(t -> accept.accept(function.apply(t))));
	}

	enum State {
		PENDING, ACCEPTED, REJECTED
	}
}
