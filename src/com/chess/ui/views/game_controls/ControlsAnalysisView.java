package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.ui.interfaces.boards.BoardViewAnalysisFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;
import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.COMP;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.03.14
 * Time: 18:16
 */
public class ControlsAnalysisView extends ControlsBaseView {

	protected BoardViewAnalysisFace boardViewFace;
	protected boolean showComp;
	private boolean showDaily;

	public ControlsAnalysisView(Context context) {
		super(context);
	}

	public ControlsAnalysisView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addControlButton(RESTART, styles[LEFT]);
		addControlButton(FLIP, styles[MIDDLE]);
		addControlButton(NOTES, styles[MIDDLE]);
		addControlButton(BOOK, styles[MIDDLE]);
		addControlButton(COMP, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

		showDailyControls(false);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(EXIT)) {
			boardViewFace.closeBoard();
		} else if (view.getId() == getButtonId(FLIP)) {
			boardViewFace.flipBoard();
		} else if (view.getId() == getButtonId(COMP)) {
			boardViewFace.vsComputer();
		} else if (view.getId() == getButtonId(NOTES)) {
			boardViewFace.openNotes();
		} else if (view.getId() == getButtonId(BOOK)) {
			boardViewFace.showExplorer();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		} else {
			super.onClick(view);
		}
	}

	public void setBoardViewFace(BoardViewAnalysisFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(RESTART, enable);
		enableGameButton(FLIP, enable);
		if (showDaily) {
			enableGameButton(NOTES, enable);
			enableGameButton(BOOK, enable);
		}

		if (showComp) {
			enableGameButton(COMP, enable);
		}
		enableGameButton(BACK, enable);
		enableGameButton(FORWARD, enable);
	}

	public void showVsComp(boolean showComp) {
		this.showComp = showComp;
		showGameButton(COMP, showComp);
	}

	public void showDailyControls(boolean showDaily) {
		this.showDaily = showDaily;
		showGameButton(NOTES, showDaily);
		showGameButton(BOOK, showDaily);
	}
}