package com.chess.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
import com.chess.ui.interfaces.BoardViewTacticsFace;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class ControlsTacticsView extends ControlsBaseView {

	public static final int B_OPTIONS_ID = 0;
	public static final int B_ANALYSIS_ID = 1;  // restart
	public static final int B_RESTART_ID = 1;  // restart
	public static final int B_HELP_ID = 2;
	public static final int B_HINT_ID = 2;
	public static final int B_STATS_ID = 2;
	public static final int B_BACK_ID = 3;
	public static final int B_FORWARD_ID = 4;
	public static final int B_NEXT_ID = 5;

	private int CONTROL_BUTTON_HEIGHT = 44;

	private int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_ctrl_options,
			R.drawable.ic_ctrl_analysis,
			R.drawable.ic_ctrl_help,
			R.drawable.ic_ctrl_back,
			R.drawable.ic_ctrl_fwd
	};

	private BoardViewTacticsFace boardViewFace;

	private State state;


	public ControlsTacticsView(Context context) {
		super(context);
	}

	public ControlsTacticsView(Context context, AttributeSet attrs) {
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

		removeAllViews();
		addControlButton(B_OPTIONS_ID, R.drawable.button_emboss_left_selector);
		addControlButton(B_ANALYSIS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_HELP_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_BACK_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FORWARD_ID, R.drawable.button_emboss_right_selector);
		addNextButton();
		addView(controlsLayout);
	}

	public void setBoardViewFace(BoardViewTacticsFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}


	private void addNextButton() {
		RoboButton nextButton = new RoboButton(getContext());
		nextButton.setText(R.string.next);
		nextButton.setBackgroundResource(R.drawable.button_orange_selector);
		nextButton.setOnClickListener(this);
		nextButton.setId(BUTTON_PREFIX + B_NEXT_ID);
		nextButton.setFont(RoboTextView.BOLD_FONT);
		nextButton.setVisibility(GONE);
		LayoutParams params = new LayoutParams(0, CONTROL_BUTTON_HEIGHT);

		params.weight = 2;

		controlsLayout.addView(nextButton, params);
	}

	@Override
	protected int getButtonDrawablesId(int buttonId) {
		return buttonsDrawableIds[buttonId];
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == BUTTON_PREFIX + B_OPTIONS_ID) {
			boardViewFace.showOptions();
		} else if (state == State.CORRECT) {  // analysis & stats
			if (view.getId() == BUTTON_PREFIX + B_ANALYSIS_ID) {
				boardViewFace.switchAnalysis();
			} else if (view.getId() == BUTTON_PREFIX + B_STATS_ID) {
				boardViewFace.showStats();
			}
		} else if (state == State.WRONG) { // restart & hint
			if (view.getId() == BUTTON_PREFIX + B_RESTART_ID) {
				boardViewFace.restart();
			} else if (view.getId() == BUTTON_PREFIX + B_HINT_ID) {
				boardViewFace.showHint();
			}
		} else if (state == State.DEFAULT) {
			if (view.getId() == BUTTON_PREFIX + B_ANALYSIS_ID) {
				boardViewFace.switchAnalysis();
			} else if (view.getId() == BUTTON_PREFIX + B_HELP_ID) {
				boardViewFace.showHelp();
			}
		}

		if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		} else if (view.getId() == BUTTON_PREFIX + B_NEXT_ID) {
			boardViewFace.newGame();
		}
	}

	public void showWrong() {
		state = State.WRONG;
		// restart
		changeGameButton(B_ANALYSIS_ID, R.drawable.ic_ctrl_restore);
		// hint
		changeGameButton(B_HELP_ID, R.drawable.ic_ctrl_hint);
		// next
		showGameButton(B_NEXT_ID, true);
		showGameButton(B_BACK_ID, false);
		showGameButton(B_FORWARD_ID, false);
	}

	public void showCorrect() {
		state = State.CORRECT;
		// hint
		changeGameButton(B_HELP_ID, R.drawable.ic_ctrl_stats);
		// next
		showGameButton(B_NEXT_ID, true);
		showGameButton(B_BACK_ID, false);
		showGameButton(B_FORWARD_ID, false);
	}

	public void showDefault() {
		state = State.DEFAULT;

		changeGameButton(B_ANALYSIS_ID, R.drawable.ic_ctrl_analysis);
		changeGameButton(B_HELP_ID, R.drawable.ic_ctrl_help);
		showGameButton(B_NEXT_ID, false);
		showGameButton(B_BACK_ID, true);
		showGameButton(B_FORWARD_ID, true);
	}

	enum State {
		CORRECT,
		WRONG,
		DEFAULT
	}
}