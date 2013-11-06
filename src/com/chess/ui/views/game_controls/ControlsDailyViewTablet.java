package com.chess.ui.views.game_controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.widgets.RoboButton;
import com.chess.ui.interfaces.boards.BoardViewNetworkFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 19:18
 */
public class ControlsDailyViewTablet extends ControlsDailyView {

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;
	public static final String NEW_MESSAGE_MARK = "!";

	private BoardViewNetworkFace boardViewFace;
	private LinearLayout topLayout;
	private LinearLayout bottomLayout;

	private static final int[] styles = new int[]{
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle
	};

	public ControlsDailyViewTablet(Context context) {
		super(context);
	}

	public ControlsDailyViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {

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

		addControlButton(OPTIONS, styles[LEFT], bottomLayout);
		addControlButton(ANALYSIS, styles[MIDDLE], bottomLayout);
		addControlButton(BACK, styles[MIDDLE], bottomLayout);
		addControlButton(FORWARD, styles[RIGHT], bottomLayout);

		addActionButton(CLOSE, R.string.ic_close, styles[LEFT], bottomLayout);
		addActionButton(MAKE_MOVE, R.string.ic_check, R.style.Rect_Bottom_Right_Orange, bottomLayout);
	}

	void addControlButton(ButtonIds buttonId, int backId, LinearLayout linearLayout) {
		linearLayout.addView(createControlButton(buttonId, backId));
	}

	void addActionButton(ButtonIds buttonId, int labelId, int styleId, LinearLayout layout) {
		RoboButton button = getDefaultButton();
		button.setDrawableStyle(styleId);
		button.setText(labelId);
		button.setOnClickListener(this);
		button.setId(getButtonId(buttonId));
		button.setVisibility(GONE);

		layout.addView(button, buttonParams);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		}
		if (view.getId() == getButtonId(ANALYSIS)) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == getButtonId(CLOSE)) {
			boardViewFace.cancelMove();
		} else if (view.getId() == getButtonId(MAKE_MOVE)) {
			boardViewFace.playMove();
		}
	}

	@Override
	public void setBoardViewFace(BoardViewNetworkFace boardViewFace) {
		super.setBoardViewFace(boardViewFace);
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void haveNewMessage(boolean newMessage) {

	}

	@Override
	protected View getViewById(ButtonIds buttonId) {
		View viewById = topLayout.findViewById(BUTTON_PREFIX + buttonId.ordinal());
		if (viewById == null) {
			return bottomLayout.findViewById(BUTTON_PREFIX + buttonId.ordinal());
		} else {
			return viewById;
		}
	}

	@Override
	public void enableAnalysisMode(boolean enable) {
		enableGameButton(ANALYSIS, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void enableControlButtons(boolean enable) {
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(ANALYSIS, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	@Override
	public void showSubmitButtons(boolean show) {
		showGameButton(OPTIONS, !show);
		showGameButton(ANALYSIS, !show);
		showGameButton(FORWARD, !show);
		showGameButton(BACK, !show);

		showGameButton(CLOSE, show);
		showGameButton(MAKE_MOVE, show);

		if (!show) {
			handler.removeCallbacks(blinkSubmitButton);
		}
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(R.style.Button_Grey2Solid_NoBorder_Light);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			((RoboButton) findViewById(getButtonId(MAKE_MOVE))).setDrawableStyle(R.style.Button_OrangeNoBorder);

			blinkSubmitBtn();
		}
	};
}
