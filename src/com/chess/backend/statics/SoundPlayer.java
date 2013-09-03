package com.chess.backend.statics;

import android.content.Context;
import android.media.MediaPlayer;
import com.chess.R;

/**
 * SoundPlayer class
 *
 * @author alien_roger
 * @created at: 26.04.12 6:29
 */
public class SoundPlayer {
	private static SoundPlayer ourInstance;
	private static boolean playSounds;

	public static SoundPlayer getInstance(Context context) {
		if(ourInstance == null){
			ourInstance = new SoundPlayer(context);
		}
		playSounds = new AppData(context).isPlaySounds();
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
		if (playSounds) {
			MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResource);

			if (mediaPlayer == null) {
				return;
			}

			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(completionListener);
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
