package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.CompletableFuture;

public interface Scheduler {

	CompletableFuture<Void> schedule(Runnable runnable);
}
