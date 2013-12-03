package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.ui.views.drawables.smart_button.RectButtonBadgeDrawable;
import com.chess.widgets.RoboButton;

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
	private boolean enableChat;

	public ControlsDailyView(Context context) {
		super(context);
	}

	public ControlsDailyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(ANALYSIS, styles[MIDDLE]);
		addControlButton(CHAT, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

		addActionButton(CLOSE, R.string.ic_close, styles[LEFT]);
		addActionButton(MAKE_MOVE, R.string.ic_check, styles[ORANGE]);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		}
		if (view.getId() == getButtonId(ANALYSIS)) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == getButtonId(CHAT)) {
			boardViewFace.showChat();
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
			ButtonDrawableBuilder.setBackgroundToView(chatButton, styles[BADGE]);
			RectButtonBadgeDrawable background = (RectButtonBadgeDrawable) chatButton.getBackground();
			if (background != null) {
				background.setBadgeValue(NEW_MESSAGE_MARK);
			}
		} else {
			ButtonDrawableBuilder.setBackgroundToView(chatButton, styles[MIDDLE]);
		}

		invalidate();
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(ANALYSIS, enable);
		if (enableChat) {
			enableGameButton(CHAT, enable);
		}
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void showSubmitButtons(boolean show) {
		showGameButton(OPTIONS, !show);
		showGameButton(ANALYSIS, !show);
		if (enableChat) {
			showGameButton(CHAT, !show);
		}
		showGameButton(FORWARD, !show);
		showGameButton(BACK, !show);

		showGameButton(CLOSE, show);
		showGameButton(MAKE_MOVE, show);

		if (!show) {
			handler.removeCallbacks(blinkSubmitButton);
		}
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(styles[LEFT]);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(styles[ORANGE]);

			blinkSubmitBtn();
		}
	};

	public void enableChatButton(boolean enable) {
		enableChat = enable;
		enableGameButton(CHAT, enable);
	}
}