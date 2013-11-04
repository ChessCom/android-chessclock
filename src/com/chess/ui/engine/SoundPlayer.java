package com.chess.ui.engine;

import android.content.Context;
import android.media.MediaPlayer;
import com.chess.R;
import com.chess.utilities.AppUtils;

import java.io.IOException;

/**
 * SoundPlayer class
 *
 * @author alien_roger
 * @created at: 26.04.12 6:29
 */
public class SoundPlayer {

	/* File names */

	public static final String CAPTURE = "capture";
	public static final String CASTLE = "castle";
	public static final String GAME_END = "game-end";
	public static final String GAME_START = "game-start";
	public static final String MOVE_CHECK = "move-check";
	public static final String MOVE_OPPONENT = "move-opponent";
	public static final String MOVE_SELF = "move-self";
	public static final String PREMOVE = "premove";
	public static final String PROMOTE = "promote";


	private static SoundPlayer ourInstance;
	private static boolean playSounds;
	private static boolean useThemePack;
	private static String themePath;

	public static SoundPlayer getInstance(Context context) {
		if (ourInstance == null) {
			ourInstance = new SoundPlayer(context);
		}

		playSounds = AppUtils.getSoundsPlayFlag(context);

		return ourInstance;
	}

	private Context context;

	private SoundPlayer(Context context) {
		this.context = context;
	}

	public static boolean isUseThemePack() {
		return useThemePack;
	}

	public static void setUseThemePack(boolean useThemePack) {
		SoundPlayer.useThemePack = useThemePack;
	}

	public static String getThemePath() {
		return themePath;
	}

	public static void setThemePath(String themePath) {
		SoundPlayer.themePath = themePath;
	}

	public void playCapture() {
		if (useThemePack) {
			playSound(CAPTURE);
		} else {
			playSound(R.raw.capture);
		}
	}

	public void playCastle() {
		if (useThemePack) {
			playSound(CASTLE);
		} else {
			playSound(R.raw.castle);
		}
	}

	public void playGameEnd() {
		if (useThemePack) {
			playSound(GAME_END);
		} else {
			playSound(R.raw.game_end);
		}
	}

	public void playGameStart() {
		if (useThemePack) {
			playSound(GAME_START);
		} else {
			playSound(R.raw.game_start);
		}
	}

	public void playMoveOpponent() {
		if (useThemePack) {
			playSound(MOVE_OPPONENT);
		} else {
			playSound(R.raw.move_opponent);
		}
	}

	public void playMoveSelf() {
		if (useThemePack) {
			playSound(MOVE_SELF);
		} else {
			playSound(R.raw.move_self);
		}
	}

	public void playMoveOpponentCheck() {
		if (useThemePack) {
			playSound(MOVE_CHECK);
		} else {
			playSound(R.raw.move_opponent_check);
		}
	}

	public void playMoveSelfCheck() {
		if (useThemePack) {
			playSound(MOVE_CHECK);
		} else {
			playSound(R.raw.move_self_check);
		}
	}

	public void playNotify() {
		playSound(R.raw.notify);
	}

	public void playTenSeconds() {
		playSound(R.raw.tenseconds);
	}

	private void playSound(int soundResource) {
		if (playSounds) {
			MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResource);

			if (mediaPlayer == null) {
				return;
			}

			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(completionListener);
		}
	}

	private void playSound(String filePath) {
		if (playSounds) {
			MediaPlayer mediaPlayer = new MediaPlayer();
			try {
				mediaPlayer.setDataSource(AppUtils.getSoundsThemeDir(context) + "/" + filePath + ".mp3");
				mediaPlayer.prepare();
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(completionListener);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	};
}
