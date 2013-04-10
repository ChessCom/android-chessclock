package com.chess.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.ui.interfaces.BoardViewAnalysisFace;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsAnalysisView extends ControlsBaseView {

	public static final int B_RESTART_ID = 0;
	public static final int B_BACK_ID = 1;
	public static final int B_FLIP_ID = 2;
	public static final int B_FORWARD_ID = 3;
	public static final int B_CLOSE_ID = 4;

	protected int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_ctrl_restart,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_flip,
			R.drawable.ic_ctrl_fwd,
			R.drawable.ic_ctrl_cancel
	};

	private int CONTROL_BUTTON_HEIGHT = 37;

	private BoardViewAnalysisFace boardViewFace;

	public ControlsAnalysisView(Context context) {
		super(context);
		onCreate();
	}

	public ControlsAnalysisView(Context context, AttributeSet attrs) {
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

		addControlButton(B_RESTART_ID, R.drawable.button_emboss_left_selector);
		addControlButton(B_BACK_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FLIP_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FORWARD_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_CLOSE_ID, R.drawable.button_emboss_right_selector);

		addView(controlsLayout);
	}

	@Override
	protected int getButtonDrawablesId(int buttonId) {
		return buttonsDrawableIds[buttonId];
	}

	public void toggleControlButton(int buttonId, boolean checked) {
	}

	@Override
	public void enableForwardBtn(boolean enable) {
		enableGameButton(B_FORWARD_ID, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
		enableGameButton(B_BACK_ID, enable);
	}

	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == BUTTON_PREFIX + B_RESTART_ID) {
			boardViewFace.restart();
		} else if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} if (view.getId() == BUTTON_PREFIX + B_FLIP_ID) {
			boardViewFace.flipBoard();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		} else if (view.getId() == BUTTON_PREFIX + B_CLOSE_ID) {
			boardViewFace.closeBoard();
		}
	}

	public void setBoardViewFace(BoardViewAnalysisFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void enableControlButtons(boolean enable) {
		enableGameButton(B_FORWARD_ID, enable);
		enableGameButton(B_BACK_ID, enable);
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(B_RESTART_ID, enable);
		enableGameButton(B_BACK_ID, enable);
		enableGameButton(B_FLIP_ID, enable);
		enableGameButton(B_FORWARD_ID, enable);
	}
}