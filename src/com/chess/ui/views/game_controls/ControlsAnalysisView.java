package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.ui.interfaces.boards.BoardViewAnalysisFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsAnalysisView extends ControlsBaseView {

	private BoardViewAnalysisFace boardViewFace;
	private boolean showComp;

	public ControlsAnalysisView(Context context) {
		super(context);
	}

	public ControlsAnalysisView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
//		addControlButton(EXIT, styles[LEFT]);
		addControlButton(RESTART, styles[LEFT]);
		addControlButton(NOTES, styles[MIDDLE]);
		addControlButton(BOOK, styles[MIDDLE]);
		addControlButton(COMP, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked)
			return;

		if (view.getId() == getButtonId(EXIT)) {
			boardViewFace.closeBoard();
		} else if (view.getId() == getButtonId(NOTES)) {
			boardViewFace.openNotes();
		} else if (view.getId() == getButtonId(COMP)) {
			boardViewFace.vsComputer();
		} else if (view.getId() == getButtonId(BOOK)) {
			boardViewFace.showExplorer();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		}
	}

	public void setBoardViewFace(BoardViewAnalysisFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	public void enableGameControls(boolean enable) {
//		enableGameButton(EXIT, enable);
		enableGameButton(RESTART, enable);
		enableGameButton(NOTES, enable);
		enableGameButton(BOOK, enable);
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
}