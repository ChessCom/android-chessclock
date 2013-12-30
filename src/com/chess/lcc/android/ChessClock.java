package com.chess.lcc.android;

import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.statics.Symbol;
import com.chess.ui.engine.SoundPlayer;

import java.util.Timer;
import java.util.TimerTask;

public class ChessClock {
	public static final int SECOND_MS = 1000;
	public static final int TENTH_MS = 100;
	public static final int HOUR_MINUTE_DISPLAY_MODE = 0;
	public static final int MINUTE_SECOND_DISPLAY_MODE = 1;
	public static final int SECOND_TENTHS_DISPLAY_MODE = 2;
	public static final int TIME_DEPENDENT_DISPLAY_MODE = 3;
	public static final int DISPLAY_MODE = TIME_DEPENDENT_DISPLAY_MODE;
	private static final String TAG = "LccLog-Clock";
	public static final char ZERO_CHAR = '0';
	private int time;
	private static final int SECOND_TENTHS_THRESHOLD = 20 * SECOND_MS;
	private static final int MINUTES_SECONDS_THRESHOLD = 121 * 60 * SECOND_MS;
	private LccHelper lccHelper;
	private boolean isWhite;
	private Timer timer;
	private boolean tenSecondsPlayed;
	private Game game;
	private String playerName;
	private boolean isRunning;

	public ChessClock(LccHelper lccHelper, boolean isWhite, boolean isGameOver) {
		this.lccHelper = lccHelper;
		this.isWhite = isWhite;
		game = lccHelper.getCurrentGame();
		playerName = isWhite ? game.getWhitePlayer().getUsername() : game.getBlackPlayer().getUsername();

		setRunning(!isGameOver);
	}

	public void updateTime() {
		time = game.getActualClockForPlayerMs(playerName).intValue();
		/*String timeString = createTimeString();
		Log.d(TAG, this + " getActualClockForPlayerMs " + (isWhite ? "WHITE " : "BLACK ") + "ms: " + time + " clock: " + timeString + " user: " + playerName);*/
	}

	public void setRunning(boolean isRunning) {
		if (this.isRunning == isRunning) {
			return;
		}
		this.isRunning = isRunning;
		if (isRunning) {
			startTimer();
		} else {
			stopTimer();
		}
	}

	public int getActualDisplayMode() {
//		if (DISPLAY_MODE != TIME_DEPENDENT_DISPLAY_MODE) { // alwasy false
//			return DISPLAY_MODE;
//		}
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
		if (eventListener == null) {
			return;
		}

		if (!isRunning) { // show time for finished game
			updateTime();
		}

		String timeString = createTimeString();
		if (isWhite) { // if white player move
			eventListener.setWhitePlayerTimer(timeString);
		} else {
			eventListener.setBlackPlayerTimer(timeString);
		}
//		eventListener.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				String timeString = createTimeString(getTime());
//				if (isWhite) { // if white player move
//					eventListener.setWhitePlayerTimer(timeString);
//				} else {
//					eventListener.setBlackPlayerTimer(timeString);
//				}
//			}
//		});
	}

	public String createTimeString() { // TODO simplify . Use Calendar & SimpleDateTime formatter methods
		//boolean isNegative = time < 0;
		int time = this.time;
		int hours = time / (SECOND_MS * 60 * 60);
		time -= hours * SECOND_MS * 60 * 60;
		int minutes = time / (SECOND_MS * 60);
		time -= minutes * SECOND_MS * 60;
		int seconds = time / SECOND_MS;
		time -= seconds * SECOND_MS;
		int tenths = time / TENTH_MS;
//		time -= tenths * TENTH_MS;  // useless assignment
		switch (getActualDisplayMode()) {
			case HOUR_MINUTE_DISPLAY_MODE:
				String sepString = (Math.abs(tenths) > 4) || !isRunning ? Symbol.COLON : Symbol.SPACE;
				return String.valueOf(hours) +
						sepString + padStart(String.valueOf(minutes), ZERO_CHAR, 2);
			case MINUTE_SECOND_DISPLAY_MODE:
				return String.valueOf(60 * hours + minutes) +
						Symbol.COLON + padStart(String.valueOf(seconds), ZERO_CHAR, 2);
			case SECOND_TENTHS_DISPLAY_MODE:
				return padStart(String.valueOf(60 * hours + minutes), ZERO_CHAR, 2) +
						Symbol.COLON + padStart(String.valueOf(seconds), ZERO_CHAR, 2) + Symbol.DOT +
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
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {

				updateTime();

				if (!isRunning) {
					stopTimer();
					return;
				}

				paint();

				if (time <= SECOND_TENTHS_THRESHOLD && !tenSecondsPlayed) {
					tenSecondsPlayed = true;
					SoundPlayer.getInstance(lccHelper.getContext()).playTenSeconds();
				}

				/*if (time < TENTH_MS) {
					stopTimer();
				}*/
			}
		}, 0, TENTH_MS);
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}
}
