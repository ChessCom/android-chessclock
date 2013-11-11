package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.R;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;
import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.PAUSE;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.11.13
 * Time: 6:16
 */
public class ControlsDiagramViewTablet extends ControlsDiagramView {

	private static final int[] styles = new int[]{
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle
	};

	public ControlsDiagramViewTablet(Context context) {
		super(context);
	}

	public ControlsDiagramViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		// game diagram controls
		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(MAKE_MOVE, styles[MIDDLE]);
		addControlButton(PAUSE, styles[MIDDLE]);
		addControlButton(BACK_END, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[MIDDLE]);
		addControlButton(FWD_END, styles[RIGHT]);

		// puzzles controls
		addControlButton(HINT, styles[MIDDLE]);
		addControlButton(RESTART, styles[MIDDLE]);
		addControlButton(SOLUTION, styles[MIDDLE]);

		showGameButton(PAUSE, false);
	}
}
