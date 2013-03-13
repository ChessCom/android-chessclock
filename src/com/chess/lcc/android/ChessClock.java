package com.chess.lcc.android;

import com.chess.backend.entity.SoundPlayer;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;

import java.util.TimerTask;

public class ChessClock {
	public static final int SECOND_MS = 1000;
	public static final int TENTH_MS = 100;
	public static final int HOUR_MINUTE_DISPLAY_MODE = 0;
	public static final int MINUTE_SECOND_DISPLAY_MODE = 1;
	public static final int SECOND_TENTHS_DISPLAY_MODE = 2;
	public static final int TIME_DEPENDENT_DISPLAY_MODE = 3;
	public static final int DISPLAY_MODE = TIME_DEPENDENT_DISPLAY_MODE;
	private int time;
	private static final int SECOND_TENTHS_THRESHOLD = 20 * SECOND_MS;
	private static final int MINUTES_SECONDS_THRESHOLD = 121 * 60 * SECOND_MS;
	private LccHelper lccHelper;
	private boolean isWhite;
	private java.util.Timer myTimer;
	private boolean tenSecondsPlayed;
	private Game game;
	private String playerName;
	private boolean isRunning;

	public ChessClock(LccHelper lccHelper, boolean isWhite) {
		this.lccHelper = lccHelper;
		this.isWhite = isWhite;
		game = lccHelper.getCurrentGame();
		playerName = isWhite ? game.getWhitePlayer().getUsername() : game.getBlackPlayer().getUsername();

		//todo: paint();
	}

	public void setTime(int time) {
		this.time = time;
		/*if (isRunning()) {
			runStart = System.currentTimeMillis();
		}*/
		paint();
	}

	public int getTime() {
		/*if (isRunning()) {
			return time - (int) (System.currentTimeMillis() - runStart);
		} else {
			return time;
		}*/
		return game.getActualClockForPlayerMs(playerName).intValue();
	}

	public void setRunning(boolean isRunning) {
		if (this.isRunning == isRunning) {
			return;
		}
		this.isRunning = isRunning;
		if (isRunning) {
			//runStart = System.currentTimeMillis();
			startTimer();
		} else {
			//time = time - (int) (System.currentTimeMillis() - runStart);
			time = time < 0 ? 0 : time;
			//runStart = -1;
			stopTimer();
		}
	}

	protected int getActualDisplayMode() {
		if (DISPLAY_MODE != TIME_DEPENDENT_DISPLAY_MODE) {
			return DISPLAY_MODE;
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
		LccEventListener eventListener = lccHelper.getLccEventListener();

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
		int hours = time / (SECOND_MS * 60 * 60);
		time -= hours * SECOND_MS * 60 * 60;
		int minutes = time / (SECOND_MS * 60);
		time -= minutes * SECOND_MS * 60;
		int seconds = time / SECOND_MS;
		time -= seconds * SECOND_MS;
		int tenths = time / TENTH_MS;
		time -= tenths * TENTH_MS;
		//String signString = isNegative ? "-" : StaticData.SYMBOL_EMPTY;
		switch (getActualDisplayMode()) {
			case HOUR_MINUTE_DISPLAY_MODE:
				String sepString = (Math.abs(tenths) > 4) || !isRunning ? ":" : " ";
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
				throw new IllegalStateException("Bad display mode value: " + DISPLAY_MODE);
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
					SoundPlayer.getInstance(lccHelper.getContext()).playTenSeconds();
				}

				if (getTime() < TENTH_MS) {
					stopTimer();
				}
			}
		}, 0, TENTH_MS);
	}

	private void stopTimer() {
		if (myTimer != null) {
			myTimer.cancel();
		}
	}
}
