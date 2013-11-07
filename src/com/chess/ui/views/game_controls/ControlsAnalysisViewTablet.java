package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewAnalysisFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 5:12
 */
public class ControlsAnalysisViewTablet extends ControlsAnalysisView {

	private BoardViewAnalysisFace boardViewFace;

	private static final int[] styles = new int[]{
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle
	};

	public ControlsAnalysisViewTablet(Context context) {
		super(context);
	}

	public ControlsAnalysisViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addControlButton(EXIT, styles[LEFT]);
		addControlButton(SEARCH, styles[MIDDLE]);
		addControlButton(RESTART, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);
	}

//	@Override
//	public void onClick(View view) {  // TODO rework click handles
//		super.onClick(view);
//		if (blocked)
//			return;
//
//		if (view.getId() == getButtonId(EXIT)) {
//			boardViewFace.closeBoard();
//		} else if (view.getId() == getButtonId(SEARCH)) {
//			boardViewFace.showExplorer();
//			// TODO add search ability
//		} else if (view.getId() == getButtonId(RESTART)) {
//			boardViewFace.restart();
//		}
//	}

	@Override
	public void setBoardViewFace(BoardViewAnalysisFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void enableGameControls(boolean enable) {
		enableGameButton(EXIT, enable);
		enableGameButton(SEARCH, enable);
		enableGameButton(RESTART, enable);
		enableGameButton(BACK, enable);
		enableGameButton(FORWARD, enable);
	}
}
