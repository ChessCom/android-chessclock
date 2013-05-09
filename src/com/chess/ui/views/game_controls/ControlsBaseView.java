package com.chess.ui.views.game_controls;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.RoboButton;

import java.util.HashMap;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.CANCEL;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public abstract class ControlsBaseView extends LinearLayout implements View.OnClickListener {

	public static final int BUTTON_PREFIX = 0x00002000;

	enum ButtonIds {
		OPTIONS,
		ANALYSIS,
		RESTART,
		FLIP,
		CLOSE,
		CHAT,
		HELP,
		STATS,
		HINT,
		BACK,
		FORWARD,
		CANCEL,
		PLAY,
		NEXT
	}

	private Integer[] drawableIds = new Integer[]{
			R.drawable.ic_ctrl_options,
			R.drawable.ic_ctrl_analysis,
			R.drawable.ic_ctrl_restart,
			R.drawable.ic_ctrl_flip,
			R.drawable.ic_ctrl_close,
			R.drawable.ic_ctrl_chat,
			R.drawable.ic_ctrl_help,
			R.drawable.ic_ctrl_stats,
			R.drawable.ic_ctrl_hint,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_fwd
	};

	protected LinearLayout controlsLayout;
	protected LayoutParams buttonParams;

	protected boolean blocked;
	private int ACTION_BUTTON_MARGIN = 6;
	int controlButtonHeight;
	Handler handler;
	HashMap<ButtonIds, Integer> buttonDrawablesMap;


	public ControlsBaseView(Context context) {
		super(context);
		init();
	}

	public ControlsBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		setOrientation(VERTICAL);

		handler = new Handler();

		float density = getContext().getResources().getDisplayMetrics().density;
		controlButtonHeight = (int) getResources().getDimension(R.dimen.game_controls_button_height);
		ACTION_BUTTON_MARGIN *= density;

		controlsLayout = new LinearLayout(getContext());
		int paddingLeft = (int) getResources().getDimension(R.dimen.game_control_padding_left);
		int paddingTop = (int) getResources().getDimension(R.dimen.game_control_padding_top); // set padding to panelInfo instead
		int paddingRight = (int) getResources().getDimension(R.dimen.game_control_padding_right);
		int paddingBottom = (int) getResources().getDimension(R.dimen.game_control_padding_bottom);

		controlsLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

		LayoutParams defaultLinLayParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		buttonParams = new LayoutParams(0, controlButtonHeight);
		buttonParams.weight = 1;

		controlsLayout.setLayoutParams(defaultLinLayParams);

		buttonDrawablesMap = new HashMap<ButtonIds, Integer>();
		ButtonIds[] values = ButtonIds.values();
		for (int i = 0; i < drawableIds.length; i++) {
			buttonDrawablesMap.put(values[i], drawableIds[i]);
		}
	}

	void addControlButton(ButtonIds buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId));
	}

	View createControlButton(ButtonIds buttonId, int backId) {
		ImageButton imageButton = new ImageButton(getContext());
		imageButton.setImageResource(buttonDrawablesMap.get(buttonId));
		imageButton.setBackgroundResource(backId);
		imageButton.setOnClickListener(this);
		imageButton.setId(getButtonId(buttonId));

		imageButton.setLayoutParams(buttonParams);
		return imageButton;
	}

	void addActionButton(ButtonIds buttonId, int labelId, int backId) {
		RoboButton button = new RoboButton(getContext(), null, R.attr.orangeButton);
		button.setBackgroundResource(backId);
		button.setText(labelId);
		button.setOnClickListener(this);
		button.setId(getButtonId(buttonId));
		button.setVisibility(GONE);
		LayoutParams buttonParams = new LayoutParams(0, controlButtonHeight);
		buttonParams.weight = 1;

		if (buttonId == CANCEL) {
			buttonParams.setMargins(0, 0, ACTION_BUTTON_MARGIN, 0);
		} else {
			buttonParams.setMargins(ACTION_BUTTON_MARGIN, 0, 0, 0);
		}

		controlsLayout.addView(button, buttonParams);
	}

	int getButtonId(ButtonIds buttonId) {
		return BUTTON_PREFIX + buttonId.ordinal();
	}

	void showGameButton(ButtonIds buttonId, boolean show) {
		findViewById(BUTTON_PREFIX + buttonId.ordinal()).setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void enableGameButton(ButtonIds buttonId, boolean enable) {
		findViewById(BUTTON_PREFIX + buttonId.ordinal()).setEnabled(enable);
	}

	public void lock(boolean lock) {
		blocked = lock;
		setEnabled(!lock);
	}

	public abstract void enableForwardBtn(boolean enable);

	public abstract void enableBackBtn(boolean enable);
}