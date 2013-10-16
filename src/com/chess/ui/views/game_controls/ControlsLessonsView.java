package com.chess.ui.views.game_controls;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import com.chess.utilities.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboImageButton;
import com.chess.ui.interfaces.boards.BoardViewLessonsFace;
import com.chess.ui.views.drawables.YourMoveDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 12:58
 */
public class ControlsLessonsView extends ControlsBaseView {

	private BoardViewLessonsFace boardViewFace;
	private int usedHints;
	private YourMoveDrawable yourMoveDrawable;

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

		addNextButton(R.style.Rect_Bottom_Right_Green, NEXT);
		addNextButton(R.style.Rect_Bottom_Right_Orange, SKIP);
		addStartButton();
		addWrongButton();
		addYourMoveButton();

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

		params.weight = 1;

		controlsLayout.addView(button, params);
	}

	protected void addYourMoveButton() {
		RoboImageButton button = new RoboImageButton(getContext());
		button.setOnClickListener(this);
		button.setId(getButtonId(HINT));
		ButtonDrawableBuilder.setBackgroundToView(button, R.style.Rect_Bottom_Right);
		yourMoveDrawable = new YourMoveDrawable(getContext());
		button.setImageDrawable(yourMoveDrawable);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

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

		params.weight = 1;

		controlsLayout.addView(button, params);
	}

	protected void addWrongButton() {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_restore);
		button.setDrawableStyle(R.style.Rect_Bottom_Right_Red);
		button.setId(getButtonId(RESTART));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		controlsLayout.addView(button, params);
	}

	public void setBoardViewFace(BoardViewLessonsFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	@Override
	public void onClick(View view) {
		if (blocked)
			return;

		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		} else if (view.getId() == getButtonId(START)) {
			boardViewFace.start();
		} else if (view.getId() == getButtonId(RESTART)) {
			boardViewFace.restart();
		} else if (view.getId() == getButtonId(HINT)) {
			yourMoveDrawable.updateUsedHints(++usedHints);
			boardViewFace.showHint();
		} else if (view.getId() == getButtonId(NEXT) ) {
			boardViewFace.nextPosition();
		} else if (view.getId() == getButtonId(SKIP)) {
			boardViewFace.newGame();
		}
	}

	public void showWrong() {
		showGameButton(OPTIONS, true);
		showGameButton(NEXT, false);
		showGameButton(START, false);
		showGameButton(HINT, false);
		showGameButton(SKIP, false);
		showGameButton(RESTART, true);
	}

	public void showCorrect() {
		showGameButton(OPTIONS, true);
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

	public void showNewGame() {
		showGameButton(OPTIONS, true);
		showGameButton(HINT, false);
		showGameButton(RESTART, false);
		showGameButton(NEXT, false);
		showGameButton(SKIP, true);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(OPTIONS, enable);
	}

	public void dropUsedHints() {
		usedHints = 0;
		yourMoveDrawable.updateUsedHints(0);
	}
}
