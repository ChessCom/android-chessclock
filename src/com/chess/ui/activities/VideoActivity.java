package com.chess.ui.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import com.chess.R;
import com.chess.statics.AppConstants;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.14
 * Time: 11:55
 */
public class VideoActivity extends Activity implements View.OnFocusChangeListener, View.OnTouchListener {

	public static final String SEEK_POSITION = "pos";
	private VideoView videoView;
	private MediaController mediaController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.video_player_frame);

		mediaController = new MediaController(this);
		mediaController.show(1);

		videoView = (VideoView) findViewById(R.id.videoView);

		videoView.setVideoURI(Uri.parse(getIntent().getStringExtra(AppConstants.VIDEO_LINK)));
		videoView.setMediaController(mediaController);
		videoView.requestFocus();
		hideStatusBar();

		videoView.start();
		videoView.setOnFocusChangeListener(this);
		videoView.setOnTouchListener(this);

		int pos = 0;
		if (savedInstanceState != null) {
			pos = savedInstanceState.getInt(SEEK_POSITION);
		}

		playVideoFromPos(pos);
	}

	private void playVideoFromPos(int pos) {
		videoView.seekTo(pos);
		videoView.start();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (videoView.isPlaying()) {
			outState.putInt(SEEK_POSITION, videoView.getCurrentPosition());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		videoView.getCurrentPosition();

	}

	private void hideStatusBar() {
		if (AppUtils.HONEYCOMB_PLUS_API) {
			videoView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}

		if (AppUtils.ICS_PLUS_API) {
			videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}


	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.videoView) {
			if (!hasFocus) {
				hideStatusBar();
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!mediaController.isShowing()) {
			hideStatusBar();
		}
		return false;
	}
}
