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
		addControlButton(PLAY_FOR_ME, R.style.Rect_Bottom_Middle);
		addControlButton(ANALYSIS, R.style.Rect_Bottom_Middle);
		addControlButton(HINT, R.style.Rect_Bottom_Middle);
		addControlButton(HELP, R.style.Rect_Bottom_Middle);
		addControlButton(BACK, R.style.Rect_Bottom_Middle);
		addControlButton(FORWARD, R.style.Rect_Bottom_Right);

		addNextButton();
		addWrongButton();

		addView(controlsLayout);

		showDefault();
	}

	private void addNextButton() {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_arrow_right);
		button.setDrawableStyle(R.style.Rect_Bottom_Right_Green);
		button.setId(getButtonId(NEXT));
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
		} else if (view.getId() == getButtonId(PLAY_FOR_ME)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(HELP)) {
			boardViewFace.showHelp();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		} else if (view.getId() == getButtonId(NEXT)) {
			boardViewFace.newGame();
		}
	}

	public void showWrong() {
		showGameButton(HINT, false);
		showGameButton(HELP, false);
		showGameButton(ANALYSIS, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
		showGameButton(PLAY_FOR_ME, true);
		showGameButton(RESTART, true);
	}

	public void showCorrect() {
		showGameButton(HINT, false);
		showGameButton(HELP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
		showGameButton(ANALYSIS, true);
		showGameButton(NEXT, true);
	}

	public void showDefault() {
		showGameButton(RESTART, false);
		showGameButton(PLAY_FOR_ME, false);
		showGameButton(ANALYSIS, false);
		showGameButton(HELP, true);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HELP, enable);
//		enableGameButton(ButtonIds.FORWARD, enable);
//		enableGameButton(ButtonIds.BACK, enable);
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