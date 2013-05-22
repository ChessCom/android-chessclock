package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class PanelInfoTacticsView extends RelativeLayout {

	public static final int AVATAR_ID = 0x00004400;
	public static final int RATING_ID = 0x00004401;
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
		Context context = getContext();
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		AVATAR_MARGIN *= density;
		TOP_BUTTON_HEIGHT *= density;

		int padding = (int) (7 * density);
		int paddingLeft = (int) (21 * density);
		int paddingRight = (int) (12 * density);
		setPadding(paddingLeft, padding, paddingRight, padding);

		{// add player rating
			ratingTxt = new RoboTextView(context);
			LayoutParams ratingParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			ratingParams.addRule(CENTER_VERTICAL);

			ratingTxt.setTextSize(resources.getDimension(R.dimen.new_tactics_rating_text_size)/density);
			ratingTxt.setTextColor(Color.WHITE);
			ratingTxt.setId(RATING_ID);
			ratingTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			ratingTxt.setFont(FontsHelper.BOLD_FONT);
			ratingTxt.setGravity(Gravity.CENTER_VERTICAL);
			ratingTxt.setPadding(0, (int) (3 * density), 0, 0);

			addView(ratingTxt, ratingParams);
		}

		{// add rating change label
			ratingChangeTxt = new RoboTextView(context);
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RIGHT_OF, RATING_ID);
			layoutParams.addRule(CENTER_VERTICAL);

			ratingChangeTxt.setTextSize(resources.getDimension(R.dimen.new_tactics_rating_change_text_size)/ density);
			ratingChangeTxt.setTextColor(resources.getColor(R.color.new_light_grey));
			ratingChangeTxt.setId(RATING_CHANGE_ID);
			ratingChangeTxt.setGravity(Gravity.CENTER_VERTICAL);
			ratingChangeTxt.setPadding((int)(8 * density), 0, 0, 0);
			ratingChangeTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			ratingChangeTxt.setFont(FontsHelper.BOLD_FONT);

			addView(ratingChangeTxt, layoutParams);
		}

		{// add time left text
			clockLayout = new LinearLayout(context);

			RoboTextView clockIconTxt = new RoboTextView(context);
			clockTxt = new RoboTextView(context);

			LayoutParams clockLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams timePassedParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams clockIconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			clockLayoutParams.addRule(CENTER_IN_PARENT);
			timePassedParams.gravity = CENTER_VERTICAL;
			clockIconParams.gravity = CENTER_VERTICAL;

			clockIconTxt.setFont(FontsHelper.ICON_FONT);
			float clockIconSize = resources.getDimension(R.dimen.new_tactics_clock_icon_size)/density; // 21;
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText(R.string.glyph_clock);
			clockIconTxt.setTextColor(resources.getColor(R.color.main_menu_back_top));
			int paddingIcon = (int) (7 * density);
			int paddingIconTop = (int) (3 * density);
			clockIconTxt.setPadding(0, paddingIconTop, paddingIcon, 0);

			clockLayout.addView(clockIconTxt, clockIconParams);

			float clockTextSize = resources.getDimension(R.dimen.new_tactics_clock_text_size)/density;//16;
			clockTxt.setTextSize(clockTextSize);
			clockTxt.setTextColor(resources.getColor(R.color.main_menu_back_top));
			clockTxt.setFont(FontsHelper.BOLD_FONT);
			clockTxt.setText("--:--");
			clockTxt.setId(TIME_LEFT_ID);
			clockLayout.addView(clockTxt, timePassedParams);

			int topPadding = (int) (12 * density);
			int sidePadding = (int) (29 * density);
			ButtonDrawableBuilder.setBackgroundToView(clockLayout,  R.style.Button_White);
			clockLayout.setPadding(sidePadding, topPadding, sidePadding, topPadding);

			addView(clockLayout, clockLayoutParams);
		}

		{// add Wrong/Correct button
			topButton = new RoboButton(context);
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, TOP_BUTTON_HEIGHT);
			params.addRule(ALIGN_PARENT_RIGHT);
			params.addRule(CENTER_VERTICAL);

			topButton.setTextSize(resources.getDimension(R.dimen.new_tactics_top_result_text_size)/ density);
			topButton.setMinimumWidth((int) (110 * density));
			topButton.setText(R.string.wrong);
			topButton.setFont(FontsHelper.BOLD_FONT);
			topButton.setDrawableStyle(R.style.Button_Red);
			topButton.setVisibility(GONE);

			addView(topButton, params);
		}

		{// add practice mode label
			practiceTxt = new RoboTextView(context);
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(ALIGN_PARENT_RIGHT);
			layoutParams.addRule(CENTER_VERTICAL);

			practiceTxt.setTextSize(resources.getDimension(R.dimen.new_tactics_clock_text_size)/density);
			practiceTxt.setTextColor(resources.getColor(R.color.new_light_grey));
			practiceTxt.setGravity(Gravity.CENTER);
			practiceTxt.setPadding(0, 0, (int) (18 * density), 0);
			practiceTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			practiceTxt.setFont(FontsHelper.BOLD_FONT);
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
			topButton.setDrawableStyle(R.style.Button_Green_Light);
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
			topButton.setDrawableStyle(R.style.Button_Red);
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