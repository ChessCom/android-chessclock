package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.ui.views.drawables.smart_button.RectButtonBadgeDrawable;
import com.chess.widgets.RoboButton;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;

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
	void init(Context context, AttributeSet attrs) {
		super.init(context, attrs);

		addButtons();

		showDefault();
	}

	protected void addButtons() {
		addControlButton(OPTIONS, R.style.Rect_Bottom_Left);
		addControlButton(HOME, R.style.Rect_Bottom_Left);
		addControlButton(CHAT, R.style.Rect_Bottom_Middle);
		addControlButton(BACK, R.style.Rect_Bottom_Middle);
		addControlButton(FORWARD, R.style.Rect_Bottom_Right);

		addActionButton(CLOSE, R.string.ic_close, R.style.Rect_Bottom_Left);
		addActionButton(MAKE_MOVE, R.string.ic_check, R.style.Rect_Bottom_Right_Orange);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		} else if (view.getId() == getButtonId(HOME)) {
			boardViewFace.goHome();
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
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	public void haveNewMessage(boolean newMessage) {
		RoboButton chatButton = (RoboButton) findViewById(getButtonId(ButtonIds.CHAT));

		if (newMessage) {
			ButtonDrawableBuilder.setBackgroundToView(chatButton, R.style.Rect_Bottom_Middle_Badge);
			RectButtonBadgeDrawable background = (RectButtonBadgeDrawable) chatButton.getBackground();
			if (background != null) {
				background.setBadgeValue(NEW_MESSAGE_MARK);
			}
		} else {
			ButtonDrawableBuilder.setBackgroundToView(chatButton, R.style.Rect_Bottom_Middle);
		}
		invalidate();
	}

	public void enableAnalysisMode(boolean enable) {
//		enableGameButton(FORWARD, enable);
//		enableGameButton(BACK, enable);
	}

	public void enableControlButtons(boolean enable) {
//		enableGameButton(FORWARD, enable);
//		enableGameButton(BACK, enable);
	}

//	public void enableGameControls(boolean enable) { // not used
//		enableGameButton(OPTIONS, enable);
//		enableGameButton(HOME, enable);
//		enableGameButton(CHAT, enable);
//		enableGameButton(FORWARD, enable);
//		enableGameButton(BACK, enable);
//	}

	public void showSubmitButtons(boolean show) {
		showGameButton(OPTIONS, !show);
		showGameButton(HOME, !show);
		showGameButton(CHAT, !show);
		showGameButton(FORWARD, !show);
		showGameButton(BACK, !show);

		showGameButton(CLOSE, show);
		showGameButton(MAKE_MOVE, show);

		if (!show) {
			handler.removeCallbacks(blinkSubmitButton);
		}
	}

	public void showDefault() {
		showGameButton(OPTIONS, true);
		showGameButton(HOME, false);
		showGameButton(CHAT, true);
		showGameButton(FORWARD, true);
		showGameButton(BACK, true);

		showGameButton(CLOSE, false);
		showGameButton(MAKE_MOVE, false);
	}

	public void showAfterMatch() {
		showGameButton(OPTIONS, false);
		showGameButton(HOME, true);
		showGameButton(CHAT, true);
		showGameButton(FORWARD, true);
		showGameButton(BACK, true);

		showGameButton(CLOSE, false);
		showGameButton(MAKE_MOVE, false);
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(R.style.Button_Grey2Solid_NoBorder_Light);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(R.style.Button_OrangeNoBorder);

			blinkSubmitBtn();
		}
	};


}