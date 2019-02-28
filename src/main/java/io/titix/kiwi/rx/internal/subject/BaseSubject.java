package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 23-Feb-19
 */
public abstract class BaseSubject<T> implements Subject<T> {

	private final AtomicBoolean completed = new AtomicBoolean(false);

	private final Collection<Runnable> completedObservers;

	private final Collection<Consumer<? super T>> observers;

	protected BaseSubject() {
		this.completedObservers = new ConcurrentLinkedQueue<>();
		this.observers = new ConcurrentLinkedQueue<>();
	}

	public final void next(T object) {
		checkCompleted();
		preNext(object);
		observers.forEach(consumer -> consumer.accept(object));
	}

	@Override
	public final void next(T[] objects) {
		checkCompleted();
		for (T object : objects) {
			preNext(object);
			observers.forEach(observer -> observer.accept(object));
		}
	}

	@Override
	public final void next(Iterable<T> objects) {
		checkCompleted();
		objects.forEach(object -> {
			preNext(object);
			observers.forEach(observer -> observer.accept(object));
		});
	}

	protected void preNext(T object) {
		// do nothing
	}

	@Override
	public void complete() {
		checkCompleted();
		completed.set(true);
		completedObservers.forEach(Runnable::run);
	}

	@Override
	public final Observable<T> asObservable() {
		return new Observable<>() {
			@Override
			public Subscription subscribe(Consumer<? super T> consumer) {

			}

			@Override
			public void onComplete(Runnable runnable) {
				completedObservers.add(runnable);
			}
		};
	}

	public final Subscription onComplete(Runnable runnable) {
		if (completed.get()) {
			runnable.run();
		}
		else {
			this.completedObservers.add(runnable);
		}
		return () -> this.completedObservers.remove(runnable);
	}

	protected abstract boolean addObserver(Consumer<? super T> consumer);

	private void checkCompleted() {
		if (completed.get()) {
			throw new IllegalStateException("Subject is completed");
		}
	}
}
