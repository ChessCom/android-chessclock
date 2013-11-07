package com.chess.ui.views.game_controls;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboButton;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 19:22
 */
public class ControlsTacticsViewTablet extends ControlsTacticsView {

	private static final int[] styles = new int[]{
			R.style.Button_Glassy,
			R.style.Button_Glassy,
			R.style.Button_Glassy
	};

	public ControlsTacticsViewTablet(Context context) {
		super(context);
	}

	public ControlsTacticsViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void addButtons() {
		addStartButton();
		addControlButton(OPTIONS, styles[LEFT]);
		addControlButton(EXIT, styles[LEFT]);
		addControlButton(HINT, styles[MIDDLE]);
		addControlButton(SOLUTION, styles[MIDDLE]);
		addControlButton(ANALYSIS, styles[MIDDLE]);
		addNextWhiteButton(styles[MIDDLE], NEXT);
		addControlButton(RESTORE, styles[MIDDLE]);
		addControlButton(COMP, styles[MIDDLE]);
		addControlButton(BACK, styles[MIDDLE]);
		addControlButton(FORWARD, styles[RIGHT]);

		addNextButton(R.style.Button_OrangeNoBorder, SKIP);
		addWrongButton();
	}

	@Override
	protected void addStartButton() {
		RoboButton button = new RoboButton(getContext());
		button.setOnClickListener(this);
		button.setFont(FontsHelper.BOLD_FONT);
		button.setText(R.string.start);
		button.setTextSize(controlTextSize);
		button.setTextColor(Color.WHITE);
		button.setShadowLayer(0, 0, 0, 0x00000000);
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
