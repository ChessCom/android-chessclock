package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewCompFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 10:56
 */
public class ControlsCompViewTablet extends ControlsCompView {

	private BoardViewCompFace boardViewFace;

	private static final int[] styles = new int[]{
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle
	};

	public ControlsCompViewTablet(Context context) {
		super(context);
	}

	public ControlsCompViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(HINT, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);
	}

//	@Override
//	public void onClick(View view) {  // TODO rework click handles
//		super.onClick(view);
//		if (blocked) {
//			return;
//		}
//
//		if (view.getId() == getButtonId(OPTIONS)) {
//			boardViewFace.showOptions();
//		} else if (view.getId() == getButtonId(HINT)) {
//			boardViewFace.showHint();
//		} else if (view.getId() == getButtonId(HELP)) {
//			boardViewFace.switchAnalysis();
//		}
//	}

	@Override
	public void setBoardViewFace(BoardViewCompFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HINT, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void enableHintButton(boolean enable) {
		enableGameButton(HINT, enable);
	}
}
