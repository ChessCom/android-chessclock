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

	public static final int B_NEW_GAME_ID = 0;
	public static final int B_OPTIONS_ID = 1;
	public static final int B_FLIP_ID = 2;
	public static final int B_ANALYSIS_ID = 3;
	public static final int B_BACK_ID = 5;
	public static final int B_FORWARD_ID = 6;
	public static final int B_HINT_ID = 7;

	protected int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_ctrl_restore,
			R.drawable.ic_ctrl_options,
			R.drawable.ic_ctrl_flip,
			R.drawable.ic_ctrl_analysis,
			R.drawable.ic_ctrl_chat,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_fwd,
			R.drawable.ic_ctrl_hint
	};

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

		controlsLayout = new LinearLayout(getContext());
		int paddingLeft = (int) getResources().getDimension(R.dimen.game_control_padding_left);
		int paddingTop = (int) getResources().getDimension(R.dimen.game_control_padding_top);
		int paddingRight = (int) getResources().getDimension(R.dimen.game_control_padding_right);
		int paddingBottom = (int) getResources().getDimension(R.dimen.game_control_padding_bottom);

		controlsLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

		LayoutParams defaultLinLayParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		buttonParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		buttonParams.weight = 1;

		controlsLayout.setLayoutParams(defaultLinLayParams);

		addControlButton(B_NEW_GAME_ID, R.drawable.button_emboss_left_selector);
		addControlButton(B_OPTIONS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FLIP_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_ANALYSIS_ID, R.drawable.button_emboss_mid_selector);
//		addControlButton(B_CHAT_ID, R.drawable.button_emboss_mid_selector);
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
//		} else if (view.getId() == BUTTON_PREFIX + B_CHAT_ID) {
//			boardViewFace.showChat();
		} else if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		}
	}

	public void setBoardViewFace(BoardViewCompFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void turnCompMode() {
//		changeGameButton(ControlsBaseView.B_NEW_GAME_ID, R.drawable.ic_ctrl_options);
		changeGameButton(ControlsCompView.B_FLIP_ID, R.drawable.ic_ctrl_hint);
		changeGameButton(ControlsCompView.B_ANALYSIS_ID, R.drawable.ic_ctrl_help);
//		hideChatButton();
//		addControlButton(1, ControlsBaseView.B_HINT_ID, R.drawable.button_emboss_mid_selector); // add hint button at second position
		showGameButton(ControlsCompView.B_NEW_GAME_ID, false);
		enableGameButton(B_FORWARD_ID, true);
		enableGameButton(B_BACK_ID, true);
	}

}