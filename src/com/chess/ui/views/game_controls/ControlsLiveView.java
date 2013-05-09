package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.interfaces.BoardViewNetworkFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsLiveView extends ControlsBaseView {


	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;


	private BoardViewNetworkFace boardViewFace;

	public ControlsLiveView(Context context) {
		super(context);
	}

	public ControlsLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
		void init() {
			super.init();

		addControlButton(OPTIONS, R.drawable.button_emboss_left_selector);
		addControlButton(CHAT, R.drawable.button_emboss_mid_selector);
		addControlButton(BACK, R.drawable.button_emboss_mid_selector);
		addControlButton(FORWARD, R.drawable.button_emboss_right_selector);

		addActionButton(CANCEL, R.string.cancel, R.drawable.button_grey_light_selector);
		addActionButton(PLAY, R.string.play_move, R.drawable.button_orange_selector);

		addView(controlsLayout);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions(view);
		}
		if (view.getId() == getButtonId(CHAT)) {
			boardViewFace.showChat();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		} else if (view.getId() == getButtonId(CANCEL)) {
			boardViewFace.cancelMove();
		} else if (view.getId() == getButtonId(PLAY)) {
			boardViewFace.playMove();
		}
	}

	public void setBoardViewFace(BoardViewNetworkFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void haveNewMessage(boolean newMessage) {
//		int imgId = newMessage ? R.drawable.ic_chat_nm : R.drawable.ic_chat;
//
//		((ImageButton) findViewById(getButtonId(ButtonIds.CHAT)).setImageResource(imgId);
		invalidate();
	}

	public void enableAnalysisMode(boolean enable) {
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void enableControlButtons(boolean enable) {
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(CHAT, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void showSubmitButtons(boolean show) {
		showGameButton(OPTIONS, !show);
		showGameButton(CHAT, !show);
		showGameButton(FORWARD, !show);
		showGameButton(BACK, !show);

		showGameButton(CANCEL, show);
		showGameButton(PLAY, show);

		if (!show) {
			handler.removeCallbacks(blinkSubmitButton);
		}
	}

	@Override
	public void enableForwardBtn(boolean enable) {
		enableGameButton(FORWARD, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
		enableGameButton(BACK, enable);
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			findViewById(getButtonId(PLAY)).setBackgroundResource(R.drawable.button_grey_light_selector);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			findViewById(getButtonId(PLAY)).setBackgroundResource(R.drawable.button_orange_selector);

			blinkSubmitBtn();
		}
	};


}