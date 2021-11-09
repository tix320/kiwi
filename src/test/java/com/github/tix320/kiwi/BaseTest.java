package com.github.tix320.kiwi;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.observable.scheduler.DefaultScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseTest {

	private List<Throwable> throwables;

	@BeforeEach
	public void setUp() {
		throwables = new ArrayList<>();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> throwables.add(e));
	}

	@AfterEach
	public void tearDown() {
		assertEquals(0, throwables.size());
	}
}
