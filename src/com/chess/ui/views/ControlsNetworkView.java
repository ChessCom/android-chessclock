package com.chess.ui.views;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.interfaces.BoardViewNetworkFace;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsNetworkView extends ControlsBaseView {

	public static final int B_OPTIONS_ID = 0;
	public static final int B_ANALYSIS_ID = 1;
	public static final int B_CHAT_ID = 2;
	public static final int B_BACK_ID = 3;
	public static final int B_FORWARD_ID = 4;
	public static final int B_CANCEL_ID = 5;
	public static final int B_PLAY_ID = 6;

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;

	protected int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_ctrl_options,
			R.drawable.ic_ctrl_analysis,
			R.drawable.ic_ctrl_chat,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_fwd
	};

	private int CONTROL_BUTTON_HEIGHT = 37;
	private int ACTION_BUTTON_MARGIN = 6;

	private BoardViewNetworkFace boardViewFace;
	private Handler handler;

	public ControlsNetworkView(Context context) {
		super(context);
		onCreate();
	}

	public ControlsNetworkView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}


	public void onCreate() {
		setOrientation(VERTICAL);

		handler = new Handler();

		float density = getContext().getResources().getDisplayMetrics().density;
		CONTROL_BUTTON_HEIGHT *= density;
		ACTION_BUTTON_MARGIN *= density;

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
		addControlButton(B_ANALYSIS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_CHAT_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_BACK_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FORWARD_ID, R.drawable.button_emboss_right_selector);

		addActionButton(B_CANCEL_ID, R.string.cancel, R.drawable.button_grey_light_selector);
		addActionButton(B_PLAY_ID, R.string.play_move, R.drawable.button_orange_selector);

		addView(controlsLayout);
	}

	@Override
	protected int getButtonDrawablesId(int buttonId) {
		return buttonsDrawableIds[buttonId];
	}

	@Override
	public void toggleControlButton(int buttonId, boolean checked) {
	}

	private void addActionButton(int buttonId, int labelId, int backId) {
		RoboButton button = new RoboButton(getContext(), null, R.attr.orangeButton);
		button.setBackgroundResource(backId);
		button.setText(labelId);
		button.setOnClickListener(this);
		button.setId(BUTTON_PREFIX + buttonId);
		button.setVisibility(GONE);
		LayoutParams buttonParams = new LayoutParams(0, CONTROL_BUTTON_HEIGHT);
		buttonParams.weight = 1;

		if (buttonId == B_CANCEL_ID) {
			buttonParams.setMargins(0, 0, ACTION_BUTTON_MARGIN, 0);
		} else {
			buttonParams.setMargins(ACTION_BUTTON_MARGIN, 0, 0, 0);
		}

		controlsLayout.addView(button, buttonParams);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == BUTTON_PREFIX + B_OPTIONS_ID) {
			boardViewFace.showOptions();
		}
		if (view.getId() == BUTTON_PREFIX + B_ANALYSIS_ID) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == BUTTON_PREFIX + B_CHAT_ID) {
			boardViewFace.showChat();
		} else if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		} else if (view.getId() == BUTTON_PREFIX + B_CANCEL_ID) {
			boardViewFace.cancelMove();
		} else if (view.getId() == BUTTON_PREFIX + B_PLAY_ID) {
			boardViewFace.playMove();
		}
	}

	public void setBoardViewFace(BoardViewNetworkFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void haveNewMessage(boolean newMessage) {
		int imgId = newMessage ? R.drawable.ic_chat_nm : R.drawable.ic_chat;

		((ImageButton) findViewById(BUTTON_PREFIX + B_CHAT_ID)).setImageResource(imgId);
		invalidate();
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

	public void enableGameControls(boolean enable) {
		enableGameButton(B_OPTIONS_ID, enable);
		enableGameButton(B_ANALYSIS_ID, enable);
		enableGameButton(B_CHAT_ID, enable);
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
	}

	public void showSubmitButtons(boolean show) {
		showGameButton(B_OPTIONS_ID, !show);
		showGameButton(B_ANALYSIS_ID, !show);
		showGameButton(B_CHAT_ID, !show);
		showGameButton(B_FORWARD_ID, !show);
		showGameButton(B_BACK_ID, !show);

		showGameButton(B_CANCEL_ID, show);
		showGameButton(B_PLAY_ID, show);

		if (!show) {
			handler.removeCallbacks(blinkSubmitButton);
		}
	}

	@Override
	public void enableForwardBtn(boolean enable) {
		enableGameButton(B_FORWARD_ID, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
		enableGameButton(B_BACK_ID, enable);
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			findViewById(BUTTON_PREFIX + B_PLAY_ID).setBackgroundResource(R.drawable.button_grey_light_selector);

			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			findViewById(BUTTON_PREFIX + B_PLAY_ID).setBackgroundResource(R.drawable.button_orange_selector);

			blinkSubmitBtn();
		}
	};


}