package com.chess.ui.views.game_controls;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewDiagramFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 16:44
 */
public class ControlsDiagramView extends ControlsBaseView {

	private BoardViewDiagramFace boardViewFace;
	private State state;

	public ControlsDiagramView(Context context) {
		super(context);
	}

	public ControlsDiagramView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	void init() {
		super.init();
		Resources resources = getResources();
		if (resources == null) {
			return;
		}

		// game diagram controls
		addControlButton(OPTIONS, R.style.Rect_Bottom_Left);
		addControlButton(MAKE_MOVE, R.style.Rect_Bottom_Middle);
		addControlButton(PAUSE, R.style.Rect_Bottom_Middle);
		addControlButton(BACK_END, R.style.Rect_Bottom_Middle);
		addControlButton(BACK, R.style.Rect_Bottom_Middle);
		addControlButton(FORWARD, R.style.Rect_Bottom_Middle);
		addControlButton(FWD_END, R.style.Rect_Bottom_Right);

		// puzzles controls
		addControlButton(HINT, R.style.Rect_Bottom_Middle);
		addControlButton(RESTART, R.style.Rect_Bottom_Middle);
		addControlButton(SOLUTION, R.style.Rect_Bottom_Middle);

		addView(controlsLayout);

		showGameButton(PAUSE, false);

		showDefault();
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked)
			return;

		if (view.getId() == getButtonId(MAKE_MOVE) || view.getId() == getButtonId(PAUSE)) {
			boardViewFace.onPlay();
		} else if (view.getId() == getButtonId(BACK_END)) {
			boardViewFace.onRewindBack();
		} else if (view.getId() == getButtonId(FWD_END)) {
			boardViewFace.onRewindForward();
		} else if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(SOLUTION)) {
			boardViewFace.showSolution();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		}
	}

	public void setBoardViewFace(BoardViewDiagramFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(MAKE_MOVE, enable);
		enableGameButton(PAUSE, enable);
		enableGameButton(BACK_END, enable);
		enableGameButton(BACK, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(FWD_END, enable);

		enableGameButton(HINT, enable);
		enableGameButton(RESTART, enable);
		enableGameButton(SOLUTION, enable);
	}

	public void enablePlayButton(boolean enable) {
		enableGameButton(MAKE_MOVE, enable);
	}

	public void showPlayButton(boolean show) {
		showGameButton(MAKE_MOVE, show);
		showGameButton(PAUSE, !show);
	}

	public void showDefault() {
		state = State.DEFAULT;

		showGameButton(OPTIONS, true);
		showGameButton(MAKE_MOVE, true);
		showGameButton(PAUSE, false);
		showGameButton(BACK_END, true);
		showGameButton(BACK, true);
		showGameButton(FORWARD, true);
		showGameButton(FWD_END, true);

		// Puzzles controls
		showGameButton(HINT, false);
		showGameButton(RESTART, false);
		showGameButton(SOLUTION, false);
	}

	public void showPuzzle() {
		state = State.PUZZLE;

		showGameButton(OPTIONS, true);
		showGameButton(MAKE_MOVE, false);
		showGameButton(PAUSE, false);
		showGameButton(BACK_END, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
		showGameButton(FWD_END, false);

		// Puzzles controls
		showGameButton(HINT, true);
		showGameButton(RESTART, true);
		showGameButton(SOLUTION, true);
	}

	public State getState() {
		return state;
	}

	public enum State {
		DEFAULT,
		PUZZLE
	}
}
