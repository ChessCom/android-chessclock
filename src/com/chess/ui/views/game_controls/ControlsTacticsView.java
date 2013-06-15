package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.interfaces.BoardViewTacticsFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsTacticsView extends ControlsBaseView {

	private BoardViewTacticsFace boardViewFace;

	public ControlsTacticsView(Context context) {
		super(context);
	}

	public ControlsTacticsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	void init() {
		super.init();

		removeAllViews();

		addControlButton(OPTIONS, R.style.Rect_Bottom_Left);
		addControlButton(ANALYSIS, R.style.Rect_Bottom_Middle);
		addControlButton(HINT, R.style.Rect_Bottom_Middle);
		addControlButton(HELP, R.style.Rect_Bottom_Middle);
		addControlButton(BACK, R.style.Rect_Bottom_Middle);
		addControlButton(FORWARD, R.style.Rect_Bottom_Right);

		addNextButton(R.style.Rect_Bottom_Right_Green, NEXT);
		addNextButton(R.style.Rect_Bottom_Right_Orange, SKIP);
		addWrongButton();

		addView(controlsLayout);

		showDefault();
	}

	private void addNextButton(int styleId, ButtonIds id) {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_arrow_right);
		button.setDrawableStyle(styleId);
		button.setId(getButtonId(id));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		controlsLayout.addView(button, params);
	}

	private void addWrongButton() {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_restore);
		button.setDrawableStyle(R.style.Rect_Bottom_Right_Red);
		button.setId(getButtonId(RESTART));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		controlsLayout.addView(button, params);
	}

	public void setBoardViewFace(BoardViewTacticsFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions(view);
		} else if (view.getId() == getButtonId(STATS)) {
			boardViewFace.showStats();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(HELP)) {
			boardViewFace.showHelp();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		} else if (view.getId() == getButtonId(NEXT) || view.getId() == getButtonId(SKIP)) {
			boardViewFace.newGame();
		}
	}

	public void showWrong() {
		showGameButton(HINT, true);
		showGameButton(HELP, false);
		showGameButton(ANALYSIS, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
		showGameButton(SKIP, false);
		showGameButton(RESTART, true);
	}

	public void showCorrect() {
		showGameButton(HINT, false);
		showGameButton(HELP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
		showGameButton(ANALYSIS, true);
		showGameButton(NEXT, true);
		showGameButton(SKIP, false);
	}

	public void showDefault() {
		showGameButton(RESTART, false);
		showGameButton(ANALYSIS, false);
		showGameButton(HELP, true);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void showAfterRetry() {
		showGameButton(RESTART, false);
		showGameButton(ANALYSIS, true);
		showGameButton(HELP, false);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, true);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HELP, enable);
	}

	@Override
	public void enableForwardBtn(boolean enable) {
//		enableGameButton(FORWARD, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
//		enableGameButton(BACK, enable);
	}

}