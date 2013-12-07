package com.chess.ui.views.game_controls;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

	public ControlsCompWelcomeViewTablet(Context context) {
		super(context);
	}

	public ControlsCompWelcomeViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		if (getResources() == null) {
			return;
		}

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			int buttonMargin = (int) (3 * density);
			buttonParams.setMargins(buttonMargin, buttonMargin, buttonMargin, buttonMargin);

			// add 2 linear layouts for 2 rows
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout topLayout = new LinearLayout(getContext());
			topLayout.setLayoutParams(params);
			topLayout.setOrientation(HORIZONTAL);

			LinearLayout bottomLayout = new LinearLayout(getContext());
			bottomLayout.setLayoutParams(params);
			bottomLayout.setOrientation(HORIZONTAL);

			addView(topLayout);
			addView(bottomLayout);
			setOrientation(VERTICAL);

			addControlButton(OPTIONS, styles[LEFT], topLayout);
			addControlButton(HINT, styles[MIDDLE], topLayout);
			addControlButton(BACK, styles[MIDDLE], bottomLayout);
			addControlButton(FORWARD, styles[RIGHT], bottomLayout);
		} else {
			setOrientation(HORIZONTAL);

			addControlButton(OPTIONS, styles[LEFT]);
			addControlButton(HINT, styles[MIDDLE]);
			addControlButton(BACK, styles[MIDDLE]);
			addControlButton(FORWARD, styles[RIGHT]);
		}
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		super.onClick(view);
		if (blocked) {
			return;
		}

		if (view.getId() == getButtonId(HINT)) {
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
