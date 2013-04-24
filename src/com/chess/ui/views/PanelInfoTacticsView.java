package com.chess.ui.views;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class PanelInfoTacticsView extends RelativeLayout {

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int TIME_LEFT_ID = 0x00004405;
	public static final int TOP_BUTTON_ID = 0x00004406;
	public static final int RATING_CHANGE_ID = 0x00004407;

	private int TOP_BUTTON_HEIGHT = 36;
	private int AVATAR_MARGIN = 11;


	private float density;

	private RoboTextView ratingTxt;

	private RoboTextView clockTxt;

	private int side;
	private RoboButton topButton;
	private OnClickListener listener;
	private RoboTextView ratingChangeTxt;
	private RoboTextView practiceTxt;
	private float clockTextSize = 16;
	private float clockIconSize = 21;
	private LinearLayout clockLayout;

	public PanelInfoTacticsView(Context context) {
		super(context);
		onCreate();
	}

	public PanelInfoTacticsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public void onCreate() {
		density = getContext().getResources().getDisplayMetrics().density;

		AVATAR_MARGIN *= density;
		TOP_BUTTON_HEIGHT *= density;

		setBackgroundResource(R.color.new_main_back);

		int padding = (int) (7 * density);
		int paddingLeft = (int) (21 * density);
		int paddingRight = (int) (12 * density);
		setPadding(paddingLeft, padding, paddingRight, padding);


		{// add player rating
			ratingTxt = new RoboTextView(getContext());
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			playerParams.addRule(CENTER_VERTICAL);

			ratingTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
			ratingTxt.setTextColor(Color.WHITE);
			ratingTxt.setId(PLAYER_ID);
			ratingTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			ratingTxt.setFont(RoboTextView.BOLD_FONT);

			addView(ratingTxt, playerParams);
		}

		{// add time left text
			clockLayout = new LinearLayout(getContext());

			RoboTextView clockIconTxt = new RoboTextView(getContext());
			clockTxt = new RoboTextView(getContext());

			LayoutParams clockLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams timePassedParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams clockIconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			clockLayoutParams.addRule(CENTER_IN_PARENT);
			timePassedParams.gravity = CENTER_VERTICAL;
			clockIconParams.gravity = CENTER_VERTICAL;

			clockIconTxt.setFont(RoboTextView.ICON_FONT);
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText("\'");
			clockIconTxt.setTextColor(getContext().getResources().getColor(R.color.main_menu_back_top));
			int paddingIcon = (int) (7 * density);
			int paddingIconTop = (int) (3 * density);
			clockIconTxt.setPadding(0, paddingIconTop, paddingIcon, 0);

			clockLayout.addView(clockIconTxt, clockIconParams);

			clockTxt.setTextSize(clockTextSize);
			clockTxt.setTextColor(getContext().getResources().getColor(R.color.main_menu_back_top));
			clockTxt.setFont(RoboTextView.BOLD_FONT);
			clockTxt.setText("--:--");
			clockTxt.setId(TIME_LEFT_ID);
			clockLayout.addView(clockTxt, timePassedParams);


			int topPadding = (int) (12 * density);
			int sidePadding = (int) (29 * density);
			clockLayout.setBackgroundResource(R.drawable.button_white_solid_selector);
			clockLayout.setPadding(sidePadding, topPadding, sidePadding, topPadding);

			addView(clockLayout, clockLayoutParams);
		}

		{// add rating change label
			ratingChangeTxt = new RoboTextView(getContext());
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
//			layoutParams.addRule(RIGHT_OF, PLAYER_ID);
//			layoutParams.addRule(LEFT_OF, TOP_BUTTON_ID);
			layoutParams.addRule(CENTER_IN_PARENT);
			int textColor = getResources().getColor(R.color.new_light_grey);

			ratingChangeTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			ratingChangeTxt.setTextColor(textColor);
			ratingChangeTxt.setId(RATING_CHANGE_ID);
			ratingChangeTxt.setGravity(Gravity.CENTER);
//			ratingChangeTxt.setPadding((int)(15 * density), 0, 0, 0);
			ratingChangeTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			ratingChangeTxt.setFont(RoboTextView.BOLD_FONT);

			addView(ratingChangeTxt, layoutParams);
		}

		{// add Wrong/Correct button
			topButton = new RoboButton(getContext());
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, TOP_BUTTON_HEIGHT);
			params.addRule(ALIGN_PARENT_RIGHT);
			params.addRule(CENTER_VERTICAL);

			topButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			topButton.setMinimumWidth((int) (110 * density));
			topButton.setText(R.string.wrong);
			topButton.setFont(RoboTextView.BOLD_FONT);
			topButton.setBackgroundResource(R.drawable.button_red_selector);
			topButton.setVisibility(GONE);

			addView(topButton, params);
		}

		{// add practice mode label
			practiceTxt = new RoboTextView(getContext());
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(ALIGN_PARENT_RIGHT);
			layoutParams.addRule(CENTER_VERTICAL);
			int textColor = getResources().getColor(R.color.new_light_grey);

			practiceTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			practiceTxt.setTextColor(textColor);
			practiceTxt.setGravity(Gravity.CENTER);
			practiceTxt.setPadding(0, 0, (int) (18 * density), 0);
			practiceTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			practiceTxt.setFont(RoboTextView.BOLD_FONT);
			practiceTxt.setVisibility(GONE);
			practiceTxt.setText(R.string.practice_mode);

			addView(practiceTxt, layoutParams);
		}
	}

	public void setSide(int side) {
		this.side = side;

		invalidate();
	}

	public int getSide() {
		return side;
	}


	public void setPlayerTimeLeft(String timeLeft) {
		clockTxt.setText(timeLeft);
	}

	public void setPlayerScore(int score) {
		ratingTxt.setText(String.valueOf(score));
	}

	public void showClock(boolean show) {
		clockTxt.setVisibility(show ? VISIBLE : GONE);
	}

	public void hideCorrect() {
		showCorrect(false, null);
	}

	public void showDefault() {
		clockLayout.setVisibility(VISIBLE);
		ratingTxt.setVisibility(GONE);
		practiceTxt.setVisibility(GONE);
		topButton.setVisibility(GONE);
		ratingChangeTxt.setVisibility(GONE);
	}

	public void showCorrect(boolean show, String newRatingStr) {
		if (show) {
			topButton.setBackgroundResource(R.drawable.button_light_green_selector);
			topButton.setText(R.string.correct);
			topButton.setVisibility(VISIBLE);

			ratingChangeTxt.setText(newRatingStr);
			ratingChangeTxt.setVisibility(VISIBLE);
			ratingTxt.setVisibility(VISIBLE);
			clockLayout.setVisibility(GONE);
			practiceTxt.setVisibility(GONE);

		} else {
			topButton.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}

	public void hideWrong() {
		showWrong(false, null);
	}

	public void showWrong(boolean show, String newRatingStr) {
		if (show) {
			topButton.setBackgroundResource(R.drawable.button_red_selector);
			topButton.setText(R.string.wrong);
			topButton.setVisibility(VISIBLE);

			ratingChangeTxt.setText(newRatingStr);
			ratingChangeTxt.setVisibility(VISIBLE);
			ratingTxt.setVisibility(VISIBLE);
			clockLayout.setVisibility(GONE);
			practiceTxt.setVisibility(GONE);
		} else {
			topButton.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}

	public void showPractice(boolean show) {
		practiceTxt.setVisibility(show ? VISIBLE : GONE);

		if (show) {
			topButton.setVisibility(GONE);
			ratingTxt.setVisibility(GONE);
			clockTxt.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}
}