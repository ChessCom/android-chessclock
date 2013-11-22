package com.chess.ui.views.game_controls;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.ui.views.drawables.YourMoveDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboImageButton;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;
import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.RESTART;
import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.START;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 21:13
 */
public class ControlsLessonsViewTablet extends ControlsLessonsView {

	public ControlsLessonsViewTablet(Context context) {
		super(context);
	}

	public ControlsLessonsViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addControlButton(OPTIONS, styles[LEFT]);

		addNextButton(R.style.Button_Green, NEXT);
		addNextButton(R.style.Button_OrangeNoBorder, SKIP);
		addStartButton();
		addWrongButton();
		addYourMoveButton();
	}


	@Override
	protected void addNextButton(int styleId, ButtonIds id) {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_arrow_right);
		button.setDrawableStyle(styleId);
		button.setId(getButtonId(id));
		button.setVisibility(GONE);
		button.setTextColor(Color.WHITE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}

	@Override
	protected void addYourMoveButton() {
		RoboImageButton button = new RoboImageButton(getContext());
		button.setOnClickListener(this);
		button.setId(getButtonId(HINT));
		ButtonDrawableBuilder.setBackgroundToView(button, styles[RIGHT]);
		yourMoveDrawable = new YourMoveDrawable(getContext());
		button.setImageDrawable(yourMoveDrawable);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}

	@Override
	protected void addStartButton() {
		RoboButton button = new RoboButton(getContext());
		button.setOnClickListener(this);
		button.setFont(FontsHelper.BOLD_FONT);
		button.setText(R.string.start_lesson);
		button.setTextSize(controlTextSize);
		button.setTextColor(Color.WHITE);
		button.setShadowLayer(0, 0, 0, 0);
		button.setDrawableStyle(R.style.Button_OrangeNoBorder);
		button.setId(getButtonId(START));
		button.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}

	@Override
	protected void addWrongButton() {
		RoboButton button = getDefaultButton();
		button.setText(R.string.ic_restore);
		button.setDrawableStyle(R.style.Button_Red);
		button.setId(getButtonId(RESTART));
		button.setVisibility(GONE);
		button.setTextColor(Color.WHITE);
		LayoutParams params = new LayoutParams(0, controlButtonHeight);

		params.weight = 1;

		addView(button, params);
	}
}
