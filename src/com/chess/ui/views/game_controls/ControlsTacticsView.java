package com.chess.ui.views.game_controls;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.widgets.RoboButton;
import com.chess.ui.interfaces.boards.BoardViewTacticsFace;
import com.chess.utilities.FontsHelper;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsTacticsView extends ControlsBaseView {

	private BoardViewTacticsFace boardViewFace;
	protected State state;

	public ControlsTacticsView(Context context) {
		super(context);
	}

	public ControlsTacticsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addStartButton();
		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(EXIT, styles[LEFT]); // TODO remove
		addControlButton(HINT, styles[MIDDLE]);
		addControlButton(SOLUTION, styles[MIDDLE]);
		addControlButton(ANALYSIS, styles[MIDDLE]);
		addNextWhiteButton(styles[MIDDLE], NEXT);
		addControlButton(RESTORE, styles[MIDDLE]);
		addControlButton(COMP, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

		addNextButton(styles[ORANGE], SKIP);
		addWrongButton();

		showStart();
	}

	protected void addStartButton() {
		RoboButton button = new RoboButton(getContext());
		button.setOnClickListener(this);
		button.setFont(FontsHelper.BOLD_FONT);
		button.setText(R.string.start);
		button.setTextSize(controlTextSize);
		button.setTextColor(Color.WHITE);
		button.setShadowLayer(0, 0, 0, 0x00000000);
		button.setDrawableStyle(styles[ORANGE]);
		button.setId(getButtonId(START));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}

	protected void addNextButton(int styleId, ButtonIds id) {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_arrow_right);
		button.setDrawableStyle(styleId);
		button.setId(getButtonId(id));
		button.setVisibility(GONE);
		button.setTextColor(Color.WHITE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		addView(button, params);
	}

	protected void addNextWhiteButton(int styleId, ButtonIds id) {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_arrow_right);
		button.setDrawableStyle(styleId);
		button.setId(getButtonId(id));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}

	protected void addWrongButton() {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_restore);
		button.setDrawableStyle(styles[RED]);
		button.setId(getButtonId(RESTART));
		button.setVisibility(GONE);
		button.setTextColor(Color.WHITE);

		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}

	public void setBoardViewFace(BoardViewTacticsFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (blocked)
			return;

		if (view.getId() == getButtonId(START)) {
			boardViewFace.onStart();
		} else if (view.getId() == getButtonId(RESTART) || view.getId() == getButtonId(RESTORE)) {
			boardViewFace.restart();
		} else if (view.getId() == getButtonId(ANALYSIS)) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(COMP)) {
			boardViewFace.computer();
		} else if (view.getId() == getButtonId(EXIT)) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == getButtonId(SOLUTION)) {
			boardViewFace.showSolution();
		} else if (view.getId() == getButtonId(NEXT) || view.getId() == getButtonId(SKIP)) {
			boardViewFace.newGame();
		}
	}

	public void showStart() {
		state = State.START;

		showGameButton(START, true);

		showGameButton(OPTIONS, false);
		showGameButton(RESTART, false);
		showGameButton(ANALYSIS, false);
		showGameButton(SOLUTION, false);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, false);
		showGameButton(RESTORE, false);
		showGameButton(EXIT, false);
		showGameButton(COMP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void showDefault() {
		state = State.DEFAULT;

		showGameButton(START, false);
		showGameButton(OPTIONS, true);
		showGameButton(RESTART, false);
		showGameButton(ANALYSIS, false);
		showGameButton(SOLUTION, false);
		showGameButton(HINT, true);
		showGameButton(NEXT, false);
		showGameButton(SKIP, false);
		showGameButton(RESTORE, false);
		showGameButton(EXIT, false);
		showGameButton(COMP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void showWrong() {
		state = State.WRONG;

		showGameButton(START, false);
		showGameButton(OPTIONS, true);
		showGameButton(HINT, false);
		showGameButton(SOLUTION, true);
		showGameButton(ANALYSIS, false);
		showGameButton(NEXT, true);
		showGameButton(SKIP, false);
		showGameButton(RESTART, true);
		showGameButton(RESTORE, false);
		showGameButton(EXIT, false);
		showGameButton(COMP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void showCorrect() {
		state = State.CORRECT;

		showGameButton(START, false);
		showGameButton(OPTIONS, true);
		showGameButton(HINT, false);
		showGameButton(SOLUTION, false);
		showGameButton(ANALYSIS, true);
		showGameButton(NEXT, false);
		showGameButton(SKIP, true);
		showGameButton(RESTART, false);
		showGameButton(RESTORE, false);
		showGameButton(EXIT, false);
		showGameButton(COMP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void showPractice() {
		state = State.PRACTICE;

		showGameButton(START, false);
		showGameButton(OPTIONS, true);
		showGameButton(RESTART, false);
		showGameButton(HINT, true);
		showGameButton(ANALYSIS, true);
		showGameButton(SOLUTION, false);
		showGameButton(NEXT, true);
		showGameButton(SKIP, false);
		showGameButton(RESTORE, false);
		showGameButton(EXIT, false);
		showGameButton(COMP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void showAnalysis() {
		state = State.ANALYSIS;

		showGameButton(START, false);
		showGameButton(OPTIONS, false);
		showGameButton(RESTART, false);
		showGameButton(ANALYSIS, false);
		showGameButton(SOLUTION, false);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, false);
		showGameButton(RESTORE, true);
		showGameButton(EXIT, true);
		showGameButton(COMP, true);
		showGameButton(BACK, true);
		showGameButton(FORWARD, true);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HINT, enable);
	}

	public State getState() {
		return state;
	}

	public enum State {
		START,
		DEFAULT,
		WRONG,
		CORRECT,
		PRACTICE,
		ANALYSIS
	}
}