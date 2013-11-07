package com.chess.ui.views.game_controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewCompFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.11.13
 * Time: 11:41
 */
public class ControlsCompWelcomeViewTablet extends ControlsCompView {

	private BoardViewCompFace boardViewFace;
	private boolean controlsRound;
	private LinearLayout topLayout;
	private LinearLayout bottomLayout;
	private int buttonMargin;

	private static final int[] styles = new int[]{
			R.style.Button_Glassy,
			R.style.Button_Glassy,
			R.style.Button_Glassy
	};

	public ControlsCompWelcomeViewTablet(Context context) {
		super(context);
	}

	public ControlsCompWelcomeViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void iniStyle(Context context, AttributeSet attrs) {
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



	@Override
	protected void addButtons() {
		buttonMargin = (int) (3 * density);
		buttonParams.setMargins(buttonMargin, buttonMargin, buttonMargin, buttonMargin);

		// add 2 linear layouts for 2 rows
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		topLayout = new LinearLayout(getContext());
		topLayout.setLayoutParams(params);
		topLayout.setOrientation(HORIZONTAL);

		bottomLayout = new LinearLayout(getContext());
		bottomLayout.setLayoutParams(params);
		bottomLayout.setOrientation(HORIZONTAL);

		addView(topLayout);
		addView(bottomLayout);
		setOrientation(VERTICAL);

		addControlButton(OPTIONS, styles[LEFT], topLayout);
		addControlButton(HINT, styles[MIDDLE], topLayout);
		addControlButton(BACK, styles[MIDDLE], bottomLayout);
		addControlButton(FORWARD, styles[RIGHT], bottomLayout);
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

	void addControlButton(ButtonIds buttonId, int backId, LinearLayout linearLayout) {
		linearLayout.addView(createControlButton(buttonId, backId));
	}

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
