package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;
import com.chess.widgets.RoboButton;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 19:18
 */
public class ControlsDailyViewTablet extends ControlsDailyView {

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;
	public static final String NEW_MESSAGE_MARK = "!";

	private BoardViewNetworkFace boardViewFace;

	private static final int[] styles = new int[]{
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle
	};

	public ControlsDailyViewTablet(Context context) {
		super(context);
	}

	public ControlsDailyViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {

		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(ANALYSIS, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

		addActionButton(CLOSE, R.string.ic_close, styles[LEFT]);
		addActionButton(MAKE_MOVE, R.string.ic_check, R.style.Rect_Bottom_Right_Orange);
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
		} else if (view.getId() == getButtonId(CLOSE)) {
			boardViewFace.cancelMove();
		} else if (view.getId() == getButtonId(MAKE_MOVE)) {
			boardViewFace.playMove();
		}
	}

	@Override
	public void setBoardViewFace(BoardViewNetworkFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void haveNewMessage(boolean newMessage) {

	}

	@Override
	public void enableAnalysisMode(boolean enable) {
		enableGameButton(ANALYSIS, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void enableControlButtons(boolean enable) {
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(ANALYSIS, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void showSubmitButtons(boolean show) {
		showGameButton(OPTIONS, !show);
		showGameButton(ANALYSIS, !show);
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
			((RoboButton) getViewById(MAKE_MOVE)).setDrawableStyle(styles[LEFT]);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) getViewById(MAKE_MOVE)).setDrawableStyle(R.style.Rect_Bottom_Right_Orange);

			blinkSubmitBtn();
		}
	};
}
