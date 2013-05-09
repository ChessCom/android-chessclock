package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.ui.interfaces.BoardViewCompFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsCompView extends ControlsBaseView {


	private BoardViewCompFace boardViewFace;

	public ControlsCompView(Context context) {
		super(context);
	}

	public ControlsCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	void init() {
		super.init();

		addControlButton(OPTIONS, R.drawable.button_emboss_left_selector);
		addControlButton(HINT, R.drawable.button_emboss_mid_selector);
		addControlButton(HELP, R.drawable.button_emboss_mid_selector);
		addControlButton(BACK, R.drawable.button_emboss_mid_selector);
		addControlButton(FORWARD, R.drawable.button_emboss_right_selector);
		addView(controlsLayout);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions(view);
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(HELP)) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		}
	}

	public void setBoardViewFace(BoardViewCompFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HELP, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(BACK, enable);
	}

	public void enableHintButton(boolean enable) {
		enableGameButton(HINT, enable);
	}

	@Override
	public void enableForwardBtn(boolean enable) {
		enableGameButton(FORWARD, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
		enableGameButton(BACK, enable);
	}

}