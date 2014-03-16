package com.chess.utilities;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;
import com.chess.statics.StaticData;

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
		return "V3" + simpleDateFormat.format(new Date()) + " " + StaticData.USERNAME;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void start() {
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}

	@Override
	public boolean isStarted() {
		return started;
	}
}