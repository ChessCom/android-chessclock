package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.ui.views.drawables.smart_button.RectButtonBadgeDrawable;
import com.chess.widgets.RoboButton;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 9:50
 */
public class ControlsLiveViewTablet extends ControlsLiveView {

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;

	private BoardViewNetworkFace boardViewFace;

	public ControlsLiveViewTablet(Context context) {
		super(context);
	}

	public ControlsLiveViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(HOME, styles[MIDDLE]);
		addControlButton(CHAT, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

		addActionButton(CLOSE, R.string.ic_close, R.style.Rect_Bottom_Left);
		addActionButton(MAKE_MOVE, R.string.ic_check, R.style.Rect_Bottom_Right_Orange);
	}

	@Override
	public void setBoardViewFace(BoardViewNetworkFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	@Override
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

	@Override
	public void enableAnalysisMode(boolean enable) {
//		enableGameButton(FORWARD, enable);
//		enableGameButton(BACK, enable);
	}

	@Override
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

	@Override
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

	@Override
	public void showDefault() {
		showGameButton(OPTIONS, true);
		showGameButton(HOME, false);
		showGameButton(CHAT, true);
		showGameButton(FORWARD, true);
		showGameButton(BACK, true);

		showGameButton(CLOSE, false);
		showGameButton(MAKE_MOVE, false);
	}

	@Override
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
