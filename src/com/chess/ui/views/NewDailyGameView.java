package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.13
 * Time: 7:23
 */
public class NewDailyGameView extends NewDefaultGameView {

	private static final int VS_ID = BASE_ID + 101;
	private static final int RIGHT_BUTTON_ID = BASE_ID + 102;

	public NewDailyGameView(Context context) {
		super(context);
	}

	public NewDailyGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewDailyGameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void addButtons(ConfigItem configItem, RelativeLayout compactRelLay) {
		// Left Button - "3 days Mode"
		RoboButton leftButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams leftBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		leftBtnParams.addRule(RelativeLayout.BELOW, TITLE_ID);
		leftButton.setText(configItem.getLeftButtonText());
		leftButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		leftButton.setId(LEFT_BTN_ID);
		compactRelLay.addView(leftButton, leftBtnParams);

		// vs
		RoboTextView vsText = new RoboTextView(getContext());
		RelativeLayout.LayoutParams vsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		vsTxtParams.addRule(RelativeLayout.RIGHT_OF, LEFT_BTN_ID);
		vsTxtParams.addRule(RelativeLayout.BELOW, TITLE_ID);
		vsText.setPadding((int) (8 * density), (int) (10 * density), (int) (8 * density), 0);
		vsText.setText("vs");
		vsText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
		vsText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
		vsText.setId(VS_ID);

		compactRelLay.addView(vsText, vsTxtParams);

		// Right Button - "Random"
		RoboButton rightButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams rightButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		rightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rightButtonParams.addRule(RelativeLayout.RIGHT_OF, VS_ID);
		rightButtonParams.addRule(RelativeLayout.BELOW, TITLE_ID);

		rightButton.setId(RIGHT_BUTTON_ID);
		rightButton.setText(configItem.getRightButtonText());
		rightButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);

		compactRelLay.addView(rightButton, rightButtonParams);

	}

	@Override
	protected void addCustomView(ConfigItem configItem, RelativeLayout optionsAndPlayView) {
		// Play Button
		RoboButton playButton = new RoboButton(getContext(), null, R.attr.orangeButtonSmall);
		RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		playButtonParams.addRule(RelativeLayout.BELOW, LEFT_BTN_ID);
		playButton.setText(R.string.new_play_ex);
		playButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

		optionsAndPlayView.addView(playButton, playButtonParams);
	}
}
