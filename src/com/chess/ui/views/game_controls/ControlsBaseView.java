package com.chess.ui.views.game_controls;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import java.util.HashMap;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public abstract class ControlsBaseView extends LinearLayout implements View.OnClickListener {

	public static final int BUTTON_PREFIX = 0x00002000;
	int controlIconSize;
	private ColorStateList controlIconColor;
	private float density;
	protected int controlTextSize;

	enum ButtonIds {
		OPTIONS,
		SEARCH,
		EXIT,
		ANALYSIS,
		RESTORE,
		RESTART,
		FLIP,
		CLOSE,
		CHAT,
		CHAT_NM,
		HELP,
		STATS,
		HINT,
		BACK,
		FORWARD,
		MAKE_MOVE,
		NEXT,
		SKIP,
		START
	}

	private Integer[] glyphIds = new Integer[]{
			R.string.ic_options,
			R.string.ic_search,
			R.string.ic_exit,
			R.string.ic_board,
			R.string.ic_restore,
			R.string.ic_restore,
			R.string.ic_flip,
			R.string.ic_close,
			R.string.ic_chat,
			R.string.ic_chat_nm,
			R.string.ic_help,
			R.string.ic_stats,
			R.string.ic_hint,
			R.string.ic_left,
			R.string.ic_right,
			R.string.ic_play,
			R.string.ic_check
	};

	protected LinearLayout controlsLayout;
	protected LayoutParams buttonParams;

	protected boolean blocked;
	int controlButtonHeight;
	Handler handler;
	HashMap<ButtonIds, Integer> buttonGlyphsMap;


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
		Resources resources = getResources();
		if (resources == null) {
			return;
		}

		handler = new Handler();

		density = resources.getDisplayMetrics().density;
		controlButtonHeight = (int) resources.getDimension(R.dimen.game_controls_button_height);
		controlIconSize = (int) (resources.getDimension(R.dimen.game_controls_icon_size) / density);
		controlIconColor = resources.getColorStateList(R.color.text_controls_icons);
		controlTextSize = (int) (resources.getDimension(R.dimen.game_controls_text_size) / density);

//		if (AppUtils.hasSoftKeys(((Activity) getContext()).getWindowManager()) &&
//				!(AppUtils.is7InchTablet(getContext()) || AppUtils.is10InchTablet(getContext()))) {
//			controlButtonHeight = (int) resources.getDimension(R.dimen.game_controls_button_height_smaller);
//		}
		controlsLayout = new LinearLayout(getContext());

		LayoutParams defaultLinLayParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		buttonParams = new LayoutParams(0, controlButtonHeight);
		buttonParams.weight = 1;

		controlsLayout.setLayoutParams(defaultLinLayParams);

		buttonGlyphsMap = new HashMap<ButtonIds, Integer>();
		ButtonIds[] values = ButtonIds.values();
		for (int i = 0; i < glyphIds.length; i++) {
			buttonGlyphsMap.put(values[i], glyphIds[i]);
		}
	}

	void addControlButton(ButtonIds buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId));
	}

	View createControlButton(ButtonIds buttonId, int styleId) {
		RoboButton button = getDefaultButton();
		button.setText(buttonGlyphsMap.get(buttonId));
		ButtonDrawableBuilder.setBackgroundToView(button, styleId);
		button.setId(getButtonId(buttonId));

		button.setLayoutParams(buttonParams);
		return button;
	}

	void addActionButton(ButtonIds buttonId, int labelId, int styleId) {
		RoboButton button = getDefaultButton();
		button.setDrawableStyle(styleId);
		button.setText(labelId);
		button.setOnClickListener(this);
		button.setId(getButtonId(buttonId));
		button.setVisibility(GONE);
		LayoutParams buttonParams = new LayoutParams(0, controlButtonHeight);
		buttonParams.weight = 1;

		controlsLayout.addView(button, buttonParams);
	}

	RoboButton getDefaultButton() {
		RoboButton button = new RoboButton(getContext());
		button.setFont(FontsHelper.ICON_FONT);
		button.setTextSize(controlIconSize);
		button.setTextColor(controlIconColor);
		button.setOnClickListener(this);

		float shadowRadius = 2 * density + 0.5f;
		float shadowDx = 0 * density;
		float shadowDy = 0 * density;
		button.setShadowLayer(shadowRadius, shadowDx, shadowDy, 0x88000000);

		return button;
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
//		setEnabled(!lock); // don't lock controls to prevent drawable glitches(temp)
	}

	public abstract void enableForwardBtn(boolean enable);

	public abstract void enableBackBtn(boolean enable);
}