package com.chess.lcc.android;

import com.chess.backend.entity.SoundPlayer;
import com.chess.ui.activities.GameBaseActivity;

import java.util.TimerTask;

public class ChessClock {
	public static final int HOUR_MINUTE_DISPLAY_MODE = 0;
	public static final int MINUTE_SECOND_DISPLAY_MODE = 1;
	public static final int SECOND_TENTHS_DISPLAY_MODE = 2;
	public static final int TIME_DEPENDENT_DISPLAY_MODE = 3;
	private int time;
	private int secondTenthsThreshold = 10 * 1000;
	private int minutesSecondsThreshold = 121 * 60 * 1000;
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
		if (time < secondTenthsThreshold) {
			return SECOND_TENTHS_DISPLAY_MODE;
		} else if (time < minutesSecondsThreshold) {
			return MINUTE_SECOND_DISPLAY_MODE;
		} else {
			return HOUR_MINUTE_DISPLAY_MODE;
		}
	}

	public void paint() {
		final GameBaseActivity activity = lccHolder.getAndroid().getGameActivity();
		if (activity == null /*|| activity.getWhiteClockView() == null || activity.getBlackClockView() == null*/) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
                String timer = createTimeString(getTime());
				if (isWhite) { // if white player move
					activity.setWhitePlayerTimer(timer);
				} else {
                    activity.setBlackPlayerTimer(timer);
				}
			}
		});
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

	//private long mStartTime = 0L;
	private Runnable mUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			/*final long start = mStartTime;
				  long millis = SystemClock.uptimeMillis() - start;
				  int seconds = (int) (millis / 1000);
				  int minutes = seconds / 60;
				  seconds = seconds % 60;*/
			/*if (seconds < 10) {
					  mTimeLabel.setText(StaticData.SYMBOL_EMPTY + minutes + ":0" + seconds);
				  } else {
					  mTimeLabel.setText(StaticData.SYMBOL_EMPTY + minutes + ":" + seconds);
				  }*/
			paint();
			if (getTime() < 100) {
				stopTimer();
				return;
			}
			//long time = start + (((minutes * 60) + seconds) * 1000);
			lccHolder.getAndroid().getClockHandler().postDelayed(this, 100);
		}
	};

	private void startTimer() {
		/*if(mStartTime == 0L)
			{*/
		//mStartTime = System.currentTimeMillis();
		/*lccHolder.getAndroid().getClockHandler().removeCallbacks(mUpdateTimeTask);
			lccHolder.getAndroid().getClockHandler().postDelayed(mUpdateTimeTask, 0);*/
		myTimer = new java.util.Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				paint();

				if (getTime() <= secondTenthsThreshold && !tenSecondsPlayed) {
					tenSecondsPlayed = true;
					SoundPlayer.getInstance(lccHolder.getContext()).playTenSeconds();
				}

				if (getTime() < 100) {
					stopTimer();
				}
			}
		}, 0, 100);

		//}
	}

	private void stopTimer() {
		//lccHolder.getAndroid().getClockHandler().removeCallbacks(mUpdateTimeTask);
		myTimer.cancel();
	}
}
