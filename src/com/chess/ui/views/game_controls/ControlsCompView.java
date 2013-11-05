package com.chess.ui.views.game_controls;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewCompFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsCompView extends ControlsBaseView {


	private static final int LEFT = 0;
	private static final int MIDDLE = 1;
	private static final int RIGHT = 2;

	private BoardViewCompFace boardViewFace;
	private boolean controlsRound;

	public ControlsCompView(Context context) {
		super(context);
	}

	public ControlsCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void iniStyle(Context context, AttributeSet attrs){
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ControlsCompView);
		if (array == null) {
			return;
		}
		try {
			if (array.hasValue(R.styleable.ControlsCompView_controlsRound)) {
				controlsRound = array.getBoolean(R.styleable.ControlsCompView_controlsRound, false);
			}
		} finally {
			array.recycle();
		}
	}

	private static final int[] rectStyles = new int[] {
			R.style.Rect_Bottom_Left,
			R.style.Rect_Bottom_Middle,
			R.style.Rect_Bottom_Right
	};

	private static final int[] roundStyles = new int[] {
			R.style.Button_Glassy,
			R.style.Button_Glassy,
			R.style.Button_Glassy
	};

	@Override
	void init(Context context, AttributeSet attrs) {
		super.init(context, attrs);
		iniStyle(context, attrs);

		int[] styles;
		if (controlsRound) {
			styles = roundStyles;
		} else {
			styles = rectStyles;
		}

		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(HINT, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked) {
			return;
		}

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(HELP)) {
			boardViewFace.switchAnalysis();
		}
	}

	public void setBoardViewFace(BoardViewCompFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HINT, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void enableHintButton(boolean enable) {
		enableGameButton(HINT, enable);
	}

}