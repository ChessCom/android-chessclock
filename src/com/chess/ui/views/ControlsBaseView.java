package com.chess.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public abstract class ControlsBaseView extends LinearLayout implements View.OnClickListener {

	public static final int B_OPTIONS_ID = 0;
	public static final int B_FLIP_ID = 1;
	public static final int B_ANALYSIS_ID = 2;
	public static final int B_BACK_ID = 4;
	public static final int B_FORWARD_ID = 5;

	protected LinearLayout controlsLayout;
	protected LayoutParams buttonParams;

	//	prefixes
	public static final int BUTTON_PREFIX = 0x00002000;

	protected boolean blocked;


	public ControlsBaseView(Context context) {
		super(context);
	}

	public ControlsBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void addControlButton(int buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId));
	}

	public void addControlButton(int position, int buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId), position);
	}

	protected View createControlButton(int buttonId, int backId) {
		ImageButton imageButton = new ImageButton(getContext());
		imageButton.setImageResource(getButtonDrawablesId(buttonId));
		imageButton.setBackgroundResource(backId);
		imageButton.setOnClickListener(this);
		imageButton.setId(BUTTON_PREFIX + buttonId);

		imageButton.setLayoutParams(buttonParams);
		return imageButton;
	}

	protected abstract int getButtonDrawablesId(int buttonId);


	public void toggleControlButton(int buttonId, boolean checked) {
	}

	protected void showGameButton(int buttonId, boolean show) {
		findViewById(BUTTON_PREFIX + buttonId).setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void enableGameButton(int buttonId, boolean enable) {
		findViewById(BUTTON_PREFIX + buttonId).setEnabled(enable);
	}

	public void changeGameButton(int buttonId, int resId) {
		((ImageButton) findViewById(BUTTON_PREFIX + buttonId)).setImageResource(resId);
	}

	public void enableAnalysisMode(boolean enable) {
		enableGameButton(B_ANALYSIS_ID, enable);
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
	}

	public void enableControlButtons(boolean enable) {
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
	}

	public void lock(boolean lock) {
		blocked = lock;
		setEnabled(!lock);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(B_OPTIONS_ID, enable);
		enableGameButton(B_ANALYSIS_ID, enable);
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
		enableGameButton(B_FLIP_ID, enable);
	}

	// todo: temporary debug
	public boolean isAnalysisEnabled() {
		return findViewById(BUTTON_PREFIX + B_ANALYSIS_ID).isEnabled()
				|| findViewById(BUTTON_PREFIX + B_FORWARD_ID).isEnabled()
				|| findViewById(BUTTON_PREFIX + B_BACK_ID).isEnabled();
	}

}