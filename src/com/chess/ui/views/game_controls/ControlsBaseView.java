package com.chess.ui.views.game_controls;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.ui.interfaces.boards.BoardViewFace;
import com.chess.ui.views.drawables.smart_button.ButtonDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.ui.views.drawables.smart_button.RectButtonDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboButton;

import java.util.HashMap;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public abstract class ControlsBaseView extends LinearLayout implements View.OnClickListener,
		View.OnLongClickListener, View.OnTouchListener {

	public static final int BUTTONS_RE_ENABLE_DELAY = 400;

	public static final int BUTTON_PREFIX = 0x00002000;
	public static final String NEW_MESSAGE_MARK = "!";

	static int LEFT = 0;
	static final int MIDDLE = 1;
	static int RIGHT = 2;
	static final int ORANGE = 3;
	static final int RED = 4;
	static final int GREEN = 5;
	static final int BADGE = 6;

	static final int STYLE_DEFAULT = 0;
	static final int STYLE_ROUNDED = 1;
	static final int STYLE_RECT_DARK = 2;

	private boolean useLtr;
	int controlIconSize;
	protected ColorStateList controlIconColor;
	protected float density;
	protected int controlTextSize;
	private BoardViewFace boardViewFace;
	private boolean fastMode;
	protected int controlsStyle;
	protected int[] styles;

	protected static final int[] roundStyles = new int[]{
			R.style.Button_Glassy,
			R.style.Button_Glassy,
			R.style.Button_Glassy,
			R.style.Button_OrangeNoBorder,
			R.style.Button_Red,
			R.style.Button_Green,
			R.style.Button_Glassy_Badge
	};

	protected static final int[] rectStyles = new int[]{
			R.style.Rect_Bottom_Left,
			R.style.Rect_Bottom_Middle,
			R.style.Rect_Bottom_Right,
			R.style.Rect_Bottom_Right_Orange,
			R.style.Rect_Bottom_Right_Red,
			R.style.Rect_Bottom_Right_Green,
			R.style.Rect_Bottom_Middle_Badge
	};

	protected static final int[] rectDarkStyles = new int[]{
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Top_Middle,
			R.style.Rect_Bottom_Right_Orange,
			R.style.Rect_Bottom_Right_Red,
			R.style.Rect_Bottom_Right_Green,
			R.style.Rect_Top_Middle_Badge
	};

	enum ButtonIds {
		/* Diagram Controls */
		BACK_END,
		FWD_END,
		REWIND_FWD,
		REWIND_BACK,

		/* Default Game Controls*/
		NOTES,
		CONDITIONAL,
		COMP,
		SOLUTION,
		OPTIONS,
		PAUSE,
		HOME,
		BOOK,
		EXIT,
		ANALYSIS,
		RESTORE,
		RESTART,
		FLIP,
		CLOSE,
		CHAT,
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
			R.string.ic_back_end,
			R.string.ic_fwd_end,
			R.string.ic_rewind_fwd,
			R.string.ic_rewind_back,
			/* Default Game Controls*/
			R.string.ic_edit,
			R.string.ic_conditional,
			R.string.ic_comp_game,
			R.string.ic_round_help,
			R.string.ic_options,
			R.string.ic_pause,
			R.string.ic_home,
			R.string.ic_book,
			R.string.ic_exit,
			R.string.ic_board,
			R.string.ic_restore,
			R.string.ic_restore,
			R.string.ic_flip,
			R.string.ic_close,
			R.string.ic_chat,
			R.string.ic_help,
			R.string.ic_stats,
			R.string.ic_hint,
			R.string.ic_left,
			R.string.ic_right,
			R.string.ic_play,
			R.string.ic_check
	};

	protected LayoutParams buttonParams;

	protected boolean blocked;
	int controlButtonHeight;
	Handler handler;
	HashMap<ButtonIds, Integer> buttonGlyphsMap;


	public ControlsBaseView(Context context) {
		super(context);
	}

	public ControlsBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	void init(Context context, AttributeSet attrs) {
		setOrientation(HORIZONTAL);
		Resources resources = getResources();
		if (resources == null) {
			return;
		}

		useLtr = AppUtils.useLtr(getContext());
		if (!useLtr) { // change borders for RTL
			LEFT = 2;
			RIGHT = 0;
		}

		handler = new Handler();

		density = resources.getDisplayMetrics().density;
		controlButtonHeight = (int) resources.getDimension(R.dimen.game_controls_button_height);
		controlIconSize = (int) (resources.getDimension(R.dimen.game_controls_icon_size) / density);
		controlIconColor = resources.getColorStateList(R.color.text_controls_icons);
		controlTextSize = (int) (resources.getDimension(R.dimen.game_controls_text_size) / density);

		buttonParams = new LayoutParams(0, controlButtonHeight);
		buttonParams.weight = 1;

		LayoutParams defaultLinLayParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		setLayoutParams(defaultLinLayParams);

		buttonGlyphsMap = new HashMap<ButtonIds, Integer>();
		ButtonIds[] values = ButtonIds.values();
		for (int i = 0; i < glyphIds.length; i++) {
			buttonGlyphsMap.put(values[i], glyphIds[i]);
		}

		iniStyle(context, attrs);

		addButtons();
	}

	protected void iniStyle(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ControlsCompView);
		if (array == null) {
			return;
		}
		try {
			if (array.hasValue(R.styleable.ControlsCompView_controls_style)) {
				controlsStyle = array.getInt(R.styleable.ControlsCompView_controls_style, STYLE_DEFAULT);
			} else {
				styles = rectStyles;
			}

			switch (controlsStyle) {
				case STYLE_DEFAULT:
					styles = rectStyles;
					break;
				case STYLE_ROUNDED:
					styles = roundStyles;
					break;
				case STYLE_RECT_DARK:
					styles = rectDarkStyles;
					break;
			}
		} finally {
			array.recycle();
		}
	}

	public void setBoardViewFace(BoardViewFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	protected abstract void addButtons();

	void addControlButton(ButtonIds buttonId, int backId) {
		addView(createControlButton(buttonId, backId));
	}

	View createControlButton(ButtonIds buttonId, int styleId) {
		RoboButton button = getDefaultButton();
		if (useLtr) {
			button.setText(buttonGlyphsMap.get(buttonId));
		} else {
			if (buttonId == ButtonIds.FORWARD) {
				button.setText(buttonGlyphsMap.get(ButtonIds.BACK));
			} else if (buttonId == ButtonIds.BACK) {
				button.setText(buttonGlyphsMap.get(ButtonIds.FORWARD));
			} else {
				button.setText(buttonGlyphsMap.get(buttonId));
			}
		}

		ButtonDrawableBuilder.setBackgroundToView(button, styleId);
		button.setId(getButtonId(buttonId));

		if (buttonId == ButtonIds.FORWARD || buttonId == ButtonIds.BACK) {
			button.setOnLongClickListener(this);
			button.setOnTouchListener(this);
		}

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

		addView(button, buttonParams);
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
		getViewById(buttonId).setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == getButtonId(OPTIONS)) {
			boardViewFace.showOptions();
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.moveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.moveForward();
		}
		boardViewFace.setFastMovesMode(false);
	}

	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == getButtonId(FORWARD)) {
			boardViewFace.setFastMovesMode(true);
			boardViewFace.moveForwardFast();
			fastMode = true;
		} else if (v.getId() == getButtonId(BACK)) {
			boardViewFace.setFastMovesMode(true);
			boardViewFace.moveBackFast();
			fastMode = true;
		}
		return false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view.getId() == getButtonId(BACK)) {
			if (fastMode) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_UP: {
						boardViewFace.setFastMovesMode(false);
						fastMode = false;
					}
				}
			}
		} else if (view.getId() == getButtonId(FORWARD)) {
			if (fastMode) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_UP: {
						boardViewFace.setFastMovesMode(false);
						fastMode = false;
					}
				}
			}
		}
		return false;
	}

	public void enableGameButton(ButtonIds buttonId, boolean enable) {
		View viewById = getViewById(buttonId);
		viewById.setEnabled(enable);
		Drawable drawable = viewById.getBackground();
		if (drawable != null) {
			if (drawable instanceof RectButtonDrawable) {
				if (enable) {
					drawable.mutate().setState(ButtonDrawable.STATE_ENABLED);
				} else {
					drawable.mutate().setState(ButtonDrawable.STATE_DISABLED);
				}
			}
		}
	}

	protected View getViewById(ButtonIds buttonId) {
		return findViewById(BUTTON_PREFIX + buttonId.ordinal());
	}

	public void lock(boolean lock) {
		blocked = lock;
//		setEnabled(!lock); // don't lock controls to prevent drawable glitches(temp)
	}

}