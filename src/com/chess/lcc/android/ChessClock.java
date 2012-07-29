package com.chess.lcc.android;

import com.chess.backend.entity.SoundPlayer;
import com.chess.lcc.android.interfaces.LccEventListener;

import java.util.TimerTask;

public class ChessClock {
	public static final int HOUR_MINUTE_DISPLAY_MODE = 0;
	public static final int MINUTE_SECOND_DISPLAY_MODE = 1;
	public static final int SECOND_TENTHS_DISPLAY_MODE = 2;
	public static final int TIME_DEPENDENT_DISPLAY_MODE = 3;
	private int time;
	private static final int SECOND_TENTHS_THRESHOLD = 10 * 1000;
	private static final int MINUTES_SECONDS_THRESHOLD = 121 * 60 * 1000;
	private long runStart = -1;
	private int displayMode = TIME_DEPENDENT_DISPLAY_MODE;
	private LccHolder lccHolder;
	private boolean isWhite;
	private java.util.Timer myTimer;
	private boolean tenSecondsPlayed;

	public ChessClock(LccHolder lccHolder, boolean isWhite, int time) {
		this.lccHolder = lccHolder;
		this.time = time;
		this.isWhite = isWhite;
		//todo: paint();
	}

	public void setTime(int time) {
		this.time = time;
		if (isRunning()) {
			runStart = System.currentTimeMillis();
		}
		paint();
	}

	public int getTime() {
		if (isRunning()) {
			return time - (int) (System.currentTimeMillis() - runStart);
		} else {
			return time;
		}
	}

	public boolean isRunning() {
		return runStart >= 0;
	}

	public void setRunning(boolean isRunning) {
		if (isRunning == isRunning()) {
			return;
		}
		if (isRunning) {
			runStart = System.currentTimeMillis();
			startTimer();
		} else {
			time = time - (int) (System.currentTimeMillis() - runStart);
			// chess.com
			time = time < 0 ? 0 : time;
			runStart = -1;
			stopTimer();
		}
	}

	public int getDisplayMode() {
		return displayMode;
	}

	protected int getActualDisplayMode() {
		int displayMode = getDisplayMode();
		if (displayMode != TIME_DEPENDENT_DISPLAY_MODE) {
			return displayMode;
		}
		int time = Math.abs(getTime());
		if (time < SECOND_TENTHS_THRESHOLD) {
			return SECOND_TENTHS_DISPLAY_MODE;
		} else if (time < MINUTES_SECONDS_THRESHOLD) {
			return MINUTE_SECOND_DISPLAY_MODE;
		} else {
			return HOUR_MINUTE_DISPLAY_MODE;
		}
	}

	public void paint() {
		LccEventListener eventListener = lccHolder.getLccEventListener();

		String timer = createTimeString(getTime());
		if (isWhite) { // if white player move
			eventListener.setWhitePlayerTimer(timer);
		} else {
			eventListener.setBlackPlayerTimer(timer);
		}
//		eventListener.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				String timer = createTimeString(getTime());
//				if (isWhite) { // if white player move
//					eventListener.setWhitePlayerTimer(timer);
//				} else {
//					eventListener.setBlackPlayerTimer(timer);
//				}
//			}
//		});
	}

	protected String createTimeString(int time) { // TODO simplify . Use Calendar & SimpleDateTime formatter methods
		//boolean isNegative = time < 0;
		time = Math.abs(time < 0 ? 0 : time);
		int hours = time / (1000 * 60 * 60);
		time -= hours * 1000 * 60 * 60;
		int minutes = time / (1000 * 60);
		time -= minutes * 1000 * 60;
		int seconds = time / 1000;
		time -= seconds * 1000;
		int tenths = time / 100;
		time -= tenths * 100;
		//String signString = isNegative ? "-" : StaticData.SYMBOL_EMPTY;
		switch (getActualDisplayMode()) {
			case HOUR_MINUTE_DISPLAY_MODE:
				String sepString = (Math.abs(tenths) > 4) || !isRunning() ? ":" : " ";
				return /*signString + */String.valueOf(hours) +
						sepString + padStart(String.valueOf(minutes), '0', 2);
			case MINUTE_SECOND_DISPLAY_MODE:
				return /*signString + */String.valueOf(60 * hours + minutes)/*padStart(String.valueOf(60 * hours + minutes), '0', 2)*/ +
						":" + padStart(String.valueOf(seconds), '0', 2);
			case SECOND_TENTHS_DISPLAY_MODE:
				return /*signString + */padStart(String.valueOf(60 * hours + minutes), '0', 2) +
						":" + padStart(String.valueOf(seconds), '0', 2) + "." +
						String.valueOf(tenths);
			default:
				throw new IllegalStateException("Bad display mode value: " + getDisplayMode());
		}
	}

	public String padStart(String s, char c, int length) {
		if (s.length() >= length) {
			return s;
		}
		StringBuilder buf = new StringBuilder(s);
		for (int i = s.length(); i < length; i++) {
			buf.insert(0, c);
		}
		return buf.toString();
	}

	private void startTimer() {
		myTimer = new java.util.Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				paint();

				if (getTime() <= SECOND_TENTHS_THRESHOLD && !tenSecondsPlayed) {
					tenSecondsPlayed = true;
					SoundPlayer.getInstance(lccHolder.getContext()).playTenSeconds();
				}

				if (getTime() < 100) {
					stopTimer();
				}
			}
		}, 0, 100);
	}

	private void stopTimer() {
		myTimer.cancel();
	}
}
