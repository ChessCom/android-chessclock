package com.chess.ui.engine;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
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
	public static final String NOTIFY = "notify";
	public static final String TENSECONDS = "tenseconds";
	public static final String SOUNDS = "sounds/";
	public static final String MP3 = ".mp3";
	private final Context context;


	private boolean playSounds;
	private static boolean useThemePack;
	private static String themePath;

	public SoundPlayer(Context context) {
		playSounds = AppUtils.getSoundsPlayFlag(context);
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
			playDefaultSound(CAPTURE);
		}
	}

	public void playCastle() {
		if (useThemePack) {
			playSound(CASTLE);
		} else {
			playDefaultSound(CASTLE);
		}
	}

	public void playGameEnd() {
		if (useThemePack) {
			playSound(GAME_END);
		} else {
			playDefaultSound(GAME_END);
		}
	}

	public void playGameStart() {
		if (useThemePack) {
			playSound(GAME_START);
		} else {
			playDefaultSound(GAME_START);
		}
	}

	public void playMoveOpponent() {
		if (useThemePack) {
			playSound(MOVE_OPPONENT);
		} else {
			playDefaultSound(MOVE_OPPONENT);
		}
	}

	public void playMoveSelf() {
		if (useThemePack) {
			playSound(MOVE_SELF);
		} else {
			playDefaultSound(MOVE_SELF);
		}
	}

	public void playMoveCheck() {
		if (useThemePack) {
			playSound(MOVE_CHECK);
		} else {
			playDefaultSound(MOVE_CHECK);
		}
	}

	public void playMovePromote() {
		if (useThemePack) {
			playSound(PROMOTE);
		} else {
			playDefaultSound(PROMOTE);
		}
	}

	public void playNotify() {
		playDefaultSound(NOTIFY);
	}

	public void playTenSeconds() {
		playDefaultSound(TENSECONDS);
	}

	private void playDefaultSound(String filePath) {
		if (playSounds) {
			try {
				AssetFileDescriptor afd = context.getAssets().openFd(SOUNDS + filePath + MP3);
				MediaPlayer player = new MediaPlayer();
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				player.prepare();
				player.start();
				player.setOnCompletionListener(completionListener);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void playSound(String filePath) {
		if (playSounds) {
			try {
				MediaPlayer mediaPlayer = new MediaPlayer();

				mediaPlayer.setDataSource(AppUtils.getSoundsThemeDir(context) + "/" + filePath + MP3);
				mediaPlayer.prepare();
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(completionListener);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	};

	public void setPlaySounds(boolean playSounds) {
		this.playSounds = playSounds;
	}
}
