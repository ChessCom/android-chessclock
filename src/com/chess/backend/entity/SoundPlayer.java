package com.chess.backend.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;

/**
 * SoundPlayer class
 *
 * @author alien_roger
 * @created at: 26.04.12 6:29
 */
public class SoundPlayer {
	private static SoundPlayer ourInstance;

	public static SoundPlayer getInstance(Context context) {
		if(ourInstance == null){
			ourInstance = new SoundPlayer(context);
		}
		return ourInstance;
	}

	private Context context;

	private SoundPlayer(Context context) {
		this.context = context;
	}

	public void playCapture() {
		playSound(R.raw.capture);
	}

	public void playCastle() {
		playSound(R.raw.castle);
	}

	public void playGameEnd() {
		playSound(R.raw.game_end);
	}

	public void playGameStart() {
		playSound(R.raw.game_start);
	}

	public void playMoveOpponent() {
		playSound(R.raw.move_opponent);
	}

	public void playMoveSelf() {
		playSound(R.raw.move_self);
	}

	public void playMoveOpponentCheck() {
		playSound(R.raw.move_opponent_check);
	}

	public void playMoveSelfCheck() {
		playSound(R.raw.move_self_check);
	}

	public void playNotify() {
		playSound(R.raw.notify);
	}

	public void playTenSeconds() {
		playSound(R.raw.tenseconds);
	}

	private void playSound(int soundResource) {
		final SharedPreferences sharedPreferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, 0);
		if (sharedPreferences.getBoolean(sharedPreferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY) + AppConstants.PREF_SOUNDS, true)) {
			try {
				MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResource);
				mediaPlayer.setVolume(0.1f, 0.1f);
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mediaPlayer) {
						mediaPlayer.release();
					}
				});
			} catch (Exception e) { // TODO eliminate
				e.printStackTrace();
			}
		}
	}
}
