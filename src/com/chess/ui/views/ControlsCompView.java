package com.chess.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.ui.interfaces.BoardViewCompFace;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsCompView extends ControlsBaseView {

	public static final int B_OPTIONS_ID = 0;
	public static final int B_HINT_ID = 1;
	public static final int B_HELP_ID = 2;
	public static final int B_BACK_ID = 3;
	public static final int B_FORWARD_ID = 4;

	protected int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_ctrl_options,
			R.drawable.ic_ctrl_hint,
			R.drawable.ic_ctrl_help,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_fwd
	};

	private int CONTROL_BUTTON_HEIGHT = 37;

	private BoardViewCompFace boardViewFace;

	public ControlsCompView(Context context) {
		super(context);
		onCreate();
	}

	public ControlsCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public void onCreate() {
		setOrientation(VERTICAL);

		float density = getContext().getResources().getDisplayMetrics().density;
		CONTROL_BUTTON_HEIGHT *= density;

		controlsLayout = new LinearLayout(getContext());
		int paddingLeft = (int) getResources().getDimension(R.dimen.game_control_padding_left);
		int paddingTop = (int) getResources().getDimension(R.dimen.game_control_padding_top);
		int paddingRight = (int) getResources().getDimension(R.dimen.game_control_padding_right);
		int paddingBottom = (int) getResources().getDimension(R.dimen.game_control_padding_bottom);

		controlsLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

		LayoutParams defaultLinLayParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		buttonParams = new LayoutParams(0, CONTROL_BUTTON_HEIGHT);
		buttonParams.weight = 1;

		controlsLayout.setLayoutParams(defaultLinLayParams);

		addControlButton(B_OPTIONS_ID, R.drawable.button_emboss_left_selector);
		addControlButton(B_HINT_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_HELP_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_BACK_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FORWARD_ID, R.drawable.button_emboss_right_selector);
		addView(controlsLayout);
	}

	@Override
	protected int getButtonDrawablesId(int buttonId) {
		return buttonsDrawableIds[buttonId];
	}

	@Override
	public void toggleControlButton(int buttonId, boolean checked) {

	}

	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == BUTTON_PREFIX + B_OPTIONS_ID) {
			boardViewFace.showOptions();
		} else if (view.getId() == BUTTON_PREFIX + B_HINT_ID) {
			boardViewFace.showHint();
		} else if (view.getId() == BUTTON_PREFIX + B_HELP_ID) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		}
	}

	public void setBoardViewFace(BoardViewCompFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void enableRewindButtons(boolean enable) {
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(B_OPTIONS_ID, enable);
		enableGameButton(B_HELP_ID, enable);
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
	}
}