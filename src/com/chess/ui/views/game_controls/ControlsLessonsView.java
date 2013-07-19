package com.chess.ui.views.game_controls;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.interfaces.boards.BoardViewLessonsFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 12:58
 */
public class ControlsLessonsView extends ControlsBaseView {

	private BoardViewLessonsFace boardViewFace;

	public ControlsLessonsView(Context context) {
		super(context);
	}

	public ControlsLessonsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	void init() {
		super.init();

		removeAllViews();

		addControlButton(OPTIONS, R.style.Rect_Bottom_Left);
		addControlButton(HINT, R.style.Rect_Bottom_Middle);

		addNextButton(R.style.Rect_Bottom_Right_Green, NEXT);
		addNextButton(R.style.Rect_Bottom_Right_Orange, SKIP);
		addStartButton();
		addWrongButton();

		addView(controlsLayout);

		showStart();
	}

	protected void addNextButton(int styleId, ButtonIds id) {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_arrow_right);
		button.setDrawableStyle(styleId);
		button.setId(getButtonId(id));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		controlsLayout.addView(button, params);
	}

	protected void addStartButton() {
		RoboButton button = new RoboButton(getContext());
		button.setOnClickListener(this);
		button.setFont(FontsHelper.BOLD_FONT);
		button.setText(R.string.start_lesson);
		button.setTextSize(controlTextSize);
		button.setTextColor(Color.WHITE);
		button.setShadowLayer(0, 0, 0, 0x00000000);
		button.setDrawableStyle(R.style.Rect_Bottom_Right_Orange);
		button.setId(getButtonId(START));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		controlsLayout.addView(button, params);
	}

	protected void addWrongButton() {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_restore);
		button.setDrawableStyle(R.style.Rect_Bottom_Right_Red);
		button.setId(getButtonId(RESTART));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 2;

		controlsLayout.addView(button, params);
	}

	@Override
	public void enableForwardBtn(boolean enable) {

	}

	@Override
	public void enableBackBtn(boolean enable) {

	}

	public void setBoardViewFace(BoardViewLessonsFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void onClick(View view) {
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions(view);
		} else if (view.getId() == getButtonId(START)) {
			boardViewFace.start();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		} else if (view.getId() == getButtonId(HINT)) {
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(NEXT) || view.getId() == getButtonId(SKIP)) {
			boardViewFace.newGame();
		}
	}

	public void showWrong() {
		showGameButton(NEXT, false);
		showGameButton(START, false);
		showGameButton(HINT, false);
		showGameButton(SKIP, false);
		showGameButton(RESTART, true);
	}

	public void showCorrect() {
		showGameButton(RESTART, false);
		showGameButton(START, false);
		showGameButton(HINT, false);
		showGameButton(NEXT, true);
		showGameButton(SKIP, false);
	}

	public void showDefault() {
		showGameButton(OPTIONS, true);
		showGameButton(START, false);
		showGameButton(RESTART, false);
		showGameButton(HINT, true);
		showGameButton(NEXT, false);
		showGameButton(SKIP, false);
	}

	public void showStart() {
		showGameButton(OPTIONS, false);
		showGameButton(START, true);
		showGameButton(RESTART, false);
		showGameButton(HINT, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, false);
	}

	public void showAfterRetry() {
		showGameButton(HINT, false);
		showGameButton(RESTART, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, true);

	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);

	}
}
