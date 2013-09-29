package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsDailyView extends ControlsBaseView {


	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;

	private BoardViewNetworkFace boardViewFace;

	public ControlsDailyView(Context context) {
		super(context);
	}

	public ControlsDailyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	void init() {
		super.init();

		addControlButton(OPTIONS, R.style.Rect_Bottom_Left);
		addControlButton(ANALYSIS, R.style.Rect_Bottom_Middle);
		addControlButton(CHAT, R.style.Rect_Bottom_Middle);
		addControlButton(BACK, R.style.Rect_Bottom_Middle);
		addControlButton(FORWARD, R.style.Rect_Bottom_Right);

		addActionButton(CLOSE, R.string.ic_close, R.style.Rect_Bottom_Left);
		addActionButton(MAKE_MOVE, R.string.ic_check, R.style.Rect_Bottom_Right_Orange);

		addView(controlsLayout);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		}
		if (view.getId() == getButtonId(ANALYSIS)) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == getButtonId(CHAT)) {
			boardViewFace.showChat();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		} else if (view.getId() == getButtonId(CLOSE)) {
			boardViewFace.cancelMove();
		} else if (view.getId() == getButtonId(MAKE_MOVE)) {
			boardViewFace.playMove();
		}
	}

	public void setBoardViewFace(BoardViewNetworkFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void haveNewMessage(boolean newMessage) {
//		int imgId = newMessage ? R.drawable.ic_chat_nm : R.drawable.ic_chat; // TODO create custom top badge drawable

//		((ImageButton) findViewById(getButtonId(ButtonIds.CHAT)).setImageResource(imgId);
		invalidate();
	}

	public void enableAnalysisMode(boolean enable) {
		enableGameButton(ANALYSIS, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void enableControlButtons(boolean enable) {
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(ANALYSIS, enable);
		enableGameButton(CHAT, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void showSubmitButtons(boolean show) {
		showGameButton(OPTIONS, !show);
		showGameButton(ANALYSIS, !show);
		showGameButton(CHAT, !show);
		showGameButton(FORWARD, !show);
		showGameButton(BACK, !show);

		showGameButton(CLOSE, show);
		showGameButton(MAKE_MOVE, show);

		if (!show) {
			handler.removeCallbacks(blinkSubmitButton);
		}
	}

	@Override
	public void enableForwardBtn(boolean enable) {
//		enableGameButton(FORWARD, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
//		enableGameButton(BACK, enable);
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton)findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(R.style.Button_Grey2Solid_NoBorder_Light);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton)findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(R.style.Button_Orange2);

			blinkSubmitBtn();
		}
	};


}