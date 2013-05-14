package com.chess.ui.views.game_controls;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
import com.chess.ui.interfaces.BoardViewTacticsFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsTacticsView extends ControlsBaseView {

	private BoardViewTacticsFace boardViewFace;

	public ControlsTacticsView(Context context) {
		super(context);
	}

	public ControlsTacticsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	void init() {
		super.init();

		removeAllViews();
		addControlButton(OPTIONS, R.drawable.button_emboss_left_selector);
		addControlButton(RESTART, R.drawable.button_emboss_right_selector);
		addControlButton(HINT, R.drawable.button_emboss_right_selector);
		addControlButton(HELP, R.drawable.button_emboss_mid_selector);
		addControlButton(BACK, R.drawable.button_emboss_mid_selector);
		addControlButton(FORWARD, R.drawable.button_emboss_right_selector);
		addNextButton();

		addView(controlsLayout);

		showGameButton(HINT, false);
	}

	private void addNextButton() {
		RoboButton nextButton = new RoboButton(getContext());
		nextButton.setText(R.string.next);
		nextButton.setDrawableStyle(R.style.Button_Orange2);
		nextButton.setOnClickListener(this);
		nextButton.setId(getButtonId(NEXT));
		nextButton.setFont(RoboTextView.BOLD_FONT);
		nextButton.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		controlsLayout.addView(nextButton, params);
	}

	public void setBoardViewFace(BoardViewTacticsFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions(view);
		} else if (view.getId() == getButtonId(STATS)) {
			boardViewFace.showStats();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(HELP)) {
			boardViewFace.showHelp();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		} else if (view.getId() == getButtonId(NEXT)) {
			boardViewFace.newGame();
		}
	}

	public void showWrong() {
		showGameButton(RESTART, true);
		showGameButton(HINT, true);
		showGameButton(HELP, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
		showGameButton(NEXT, true);
	}

	public void showCorrect() {
		showGameButton(HINT, false);
		showGameButton(HELP, false);
		showGameButton(BACK, true);
		showGameButton(FORWARD, true);
		showGameButton(NEXT, true);
	}

	public void showDefault() {
		showGameButton(RESTART, false);
		showGameButton(HELP, true);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(BACK, false);
		showGameButton(FORWARD, false);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
		enableGameButton(HELP, enable);
//		enableGameButton(ButtonIds.FORWARD, enable);
//		enableGameButton(ButtonIds.BACK, enable);
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