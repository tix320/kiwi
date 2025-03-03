package com.github.tix320.kiwi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseTest {

	private List<Throwable> throwables;

	@BeforeEach
	public void setUp() {
		throwables = new CopyOnWriteArrayList<>();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> throwables.add(e));
	}

	@AfterEach
	public void tearDown() {
		assertEquals(0, throwables.size());
	}

}
