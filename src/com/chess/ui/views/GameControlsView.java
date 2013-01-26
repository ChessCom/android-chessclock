package com.chess.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.ui.interfaces.BoardViewFace;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class GameControlsView extends LinearLayout implements View.OnClickListener {


	private LinearLayout controlsLayout;


	private int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_ctrl_restore,
			R.drawable.ic_ctrl_options,
			R.drawable.ic_ctrl_flip,
			R.drawable.ic_ctrl_analysis,
			R.drawable.ic_ctrl_chat,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_fwd,
			R.drawable.ic_ctrl_hint
	};

	public static final int B_NEW_GAME_ID = 0;
	public static final int B_OPTIONS_ID = 1;
	public static final int B_FLIP_ID = 2;
	public static final int B_ANALYSIS_ID = 3;
	public static final int B_CHAT_ID = 4;
	public static final int B_BACK_ID = 5;
	public static final int B_FORWARD_ID = 6;
	public static final int B_HINT_ID = 7;

	private BoardViewFace boardViewFace;

	//	prefixes
	public static final int BUTTON_PREFIX = 0x00002000;


	private boolean blocked;


	public GameControlsView(Context context) {
		super(context);
		onCreate();
	}

	public GameControlsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}


	public void onCreate() {
		setOrientation(VERTICAL);

		controlsLayout = new LinearLayout(getContext());
		int paddingLeft = (int) getResources().getDimension(R.dimen.game_control_padding_left);
		int paddingTop = (int) getResources().getDimension(R.dimen.game_control_padding_top);
		int paddingRight = (int) getResources().getDimension(R.dimen.game_control_padding_right);
		int paddingBottom = (int) getResources().getDimension(R.dimen.game_control_padding_bottom);

		controlsLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

		LinearLayout.LayoutParams defaultLinLayParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		controlsLayout.setLayoutParams(defaultLinLayParams);

		addControlButton(B_NEW_GAME_ID, R.drawable.button_emboss_left_selector);
		addControlButton(B_OPTIONS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FLIP_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_ANALYSIS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_CHAT_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_BACK_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FORWARD_ID, R.drawable.button_emboss_right_selector);
		addView(controlsLayout);

	}

	private void addControlButton(int buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId));
	}

	public void addControlButton(int position, int buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId), position);
	}

	private View createControlButton(int buttonId, int backId) {
		ImageButton imageButton = new ImageButton(getContext());
		imageButton.setImageResource(buttonsDrawableIds[buttonId]);
		imageButton.setBackgroundResource(backId);
		imageButton.setOnClickListener(this);
		imageButton.setId(BUTTON_PREFIX + buttonId);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		params.weight = 1;
		imageButton.setLayoutParams(params);
		return imageButton;
	}


	public void toggleControlButton(int buttonId, boolean checked) {
		if (checked) {
			findViewById(BUTTON_PREFIX + buttonId).setBackgroundResource(R.drawable.button_emboss_mid_checked);
		} else {
			findViewById(BUTTON_PREFIX + buttonId).setBackgroundResource(R.drawable.button_emboss_mid_selector);
		}
	}

	private void showGameButton(int buttonId, boolean show) {
		findViewById(BUTTON_PREFIX + buttonId).setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void enableGameButton(int buttonId, boolean enable) {
		findViewById(BUTTON_PREFIX + buttonId).setEnabled(enable);
	}

	public void changeGameButton(int buttonId, int resId) {
		((ImageButton) findViewById(BUTTON_PREFIX + buttonId)).setImageResource(resId);
	}


	public void onClick(View view) {
		if (blocked)
			return;

		if (view.getId() == BUTTON_PREFIX + B_NEW_GAME_ID) {
			boardViewFace.newGame();
		} else if (view.getId() == BUTTON_PREFIX + B_OPTIONS_ID) {
			boardViewFace.showOptions();
		} else if (view.getId() == BUTTON_PREFIX + B_HINT_ID) {
			boardViewFace.showHint();
		} else if (view.getId() == BUTTON_PREFIX + B_FLIP_ID) {
			boardViewFace.flipBoard();
		} else if (view.getId() == BUTTON_PREFIX + B_ANALYSIS_ID) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == BUTTON_PREFIX + B_CHAT_ID) {
			boardViewFace.switchChat();
		} else if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		}
	}

	public void setBoardViewFace(BoardViewFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void haveNewMessage(boolean newMessage) {
		int imgId = newMessage ? R.drawable.ic_chat_nm : R.drawable.ic_chat;

		((ImageButton) findViewById(BUTTON_PREFIX + B_CHAT_ID)).setImageResource(imgId);
		invalidate();
	}


	public void hideChatButton() {
		showGameButton(GameControlsView.B_CHAT_ID, false);
	}

	public void turnCompMode() {
		changeGameButton(GameControlsView.B_NEW_GAME_ID, R.drawable.ic_ctrl_options);
		hideChatButton();
		addControlButton(1, GameControlsView.B_HINT_ID, R.drawable.button_emboss_mid_selector); // add hint button at second position
		enableGameButton(B_FORWARD_ID, true);
		enableGameButton(B_BACK_ID, true);
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
		enableGameButton(B_CHAT_ID, enable);
		enableGameButton(B_FLIP_ID, enable);
	}

	// todo: temporary debug
	public boolean isAnalysisEnabled() {
		return findViewById(BUTTON_PREFIX + B_ANALYSIS_ID).isEnabled()
				|| findViewById(BUTTON_PREFIX + B_FORWARD_ID).isEnabled()
				|| findViewById(BUTTON_PREFIX + B_BACK_ID).isEnabled();
	}

}