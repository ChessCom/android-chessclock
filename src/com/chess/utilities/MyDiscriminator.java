package com.chess.utilities;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDiscriminator implements Discriminator<ILoggingEvent> {

	private static final String KEY = "subject";

	private SimpleDateFormat simpleDateFormat;
	private boolean started;

	public MyDiscriminator() {
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh");
	}

	@Override
	public String getDiscriminatingValue(ILoggingEvent iLoggingEvent) {
		return simpleDateFormat.format(new Date());
	}

	@Override
	public String getKey() {
		return KEY;
	}

	public void start() {
		started = true;
	}

	public void stop() {
		started = false;
	}

	public boolean isStarted() {
		return started;
	}
}