package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboTextView;

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
	public static final int RATING_CHANGE_ID = 0x00004406;
	public static final String NO_TIME = "-:--";

	private ImageView avatarImg;
	private float density;

	private RoboTextView ratingTxt;

	private RoboTextView clockTxt;

	private int side;
	private OnClickListener listener;
	private RoboTextView ratingChangeTxt;
	private RoboTextView practiceTxt;
	private LinearLayout clockLayout;
	private RoboTextView clockIconTxt;
	private int whiteColor;
	private int redColor;

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

		int padding = (int) (7 * density);
		int paddingLeft = (int) (21 * density);
		int paddingRight = (int) (12 * density);
		setPadding(paddingLeft, padding, paddingRight, padding);

		float infoTextSize = resources.getDimension(R.dimen.new_tactics_info_text_size) / density;
		int avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_big_size);
		int timeLeftSize = (int) resources.getDimension(R.dimen.panel_info_time_left_size);
		int avatarMarginRight = (int) resources.getDimension(R.dimen.panel_info_avatar_margin_right);
		whiteColor = resources.getColor(R.color.white);
		redColor = resources.getColor(R.color.red_button);
		int lightGrey = resources.getColor(R.color.new_light_grey);

		{// add avatar view
			avatarImg = new ImageView(context);

			LayoutParams avatarParams = new LayoutParams(avatarSize, avatarSize);
			avatarParams.setMargins(0, 0, avatarMarginRight, 0);
			avatarParams.addRule(CENTER_VERTICAL);

			avatarImg.setScaleType(ImageView.ScaleType.FIT_XY);
			avatarImg.setAdjustViewBounds(true);
			avatarImg.setId(AVATAR_ID);

			addView(avatarImg, avatarParams);
		}

		{// add player rating
			ratingTxt = new RoboTextView(context);
			LayoutParams ratingParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			ratingParams.addRule(CENTER_VERTICAL);
			ratingParams.addRule(RIGHT_OF, AVATAR_ID);

			ratingTxt.setTextSize(infoTextSize);
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

			ratingChangeTxt.setTextSize(infoTextSize);
			ratingChangeTxt.setTextColor(lightGrey);
			ratingChangeTxt.setId(RATING_CHANGE_ID);
			ratingChangeTxt.setGravity(Gravity.CENTER_VERTICAL);
			ratingChangeTxt.setPadding((int)(8 * density), 0, 0, 0);
			ratingChangeTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			ratingChangeTxt.setFont(FontsHelper.BOLD_FONT);

			addView(ratingChangeTxt, layoutParams);
		}

		{// add time left text
			clockLayout = new LinearLayout(context);

			clockIconTxt = new RoboTextView(context);
			clockTxt = new RoboTextView(context);

			LayoutParams clockLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams timePassedParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams clockIconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			clockLayoutParams.addRule(ALIGN_PARENT_RIGHT);
			clockLayoutParams.addRule(CENTER_VERTICAL);
			timePassedParams.gravity = CENTER_VERTICAL;
			clockIconParams.gravity = CENTER_VERTICAL;

			clockIconTxt.setFont(FontsHelper.ICON_FONT);
			float clockIconSize = resources.getDimension(R.dimen.new_tactics_clock_icon_size)/density; // 21;
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText(R.string.ic_clock);
			clockIconTxt.setTextColor(whiteColor);
			int paddingIcon = (int) (7 * density);
			int paddingIconTop = (int) (3 * density);
			clockIconTxt.setPadding(0, paddingIconTop, paddingIcon, 0);

			clockLayout.addView(clockIconTxt, clockIconParams);

			clockTxt.setTextSize(infoTextSize);
			clockTxt.setTextColor(whiteColor);
			clockTxt.setFont(FontsHelper.BOLD_FONT);
			clockTxt.setText(NO_TIME);
			clockTxt.setId(TIME_LEFT_ID);
			clockLayout.addView(clockTxt, timePassedParams);

			addView(clockLayout, clockLayoutParams);
		}

		{// add practice mode label
			practiceTxt = new RoboTextView(context);
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(ALIGN_PARENT_RIGHT);
			layoutParams.addRule(CENTER_VERTICAL);

			practiceTxt.setTextSize(infoTextSize);
			practiceTxt.setTextColor(lightGrey);
			practiceTxt.setGravity(Gravity.CENTER);
			practiceTxt.setPadding(0, 0, (int) (18 * density), 0);
			practiceTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			practiceTxt.setFont(FontsHelper.BOLD_FONT);
			practiceTxt.setVisibility(GONE);
			practiceTxt.setText(R.string.practice);

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

	public void makeTimerRed(boolean makeRed) {
		clockIconTxt.setTextColor(makeRed ? redColor : whiteColor);
		clockTxt.setTextColor(makeRed ? redColor : whiteColor);
	}

	public void setPlayerTimeLeft(String timeLeft) {
		clockTxt.setText(timeLeft);
	}

	public void setPlayerScore(int score) {
		ratingTxt.setText(String.valueOf(score));
	}

	public void showClock(boolean show) {
		clockTxt.setVisibility(show ? VISIBLE : GONE);
		clockIconTxt.setVisibility(show ? VISIBLE : GONE);
	}

	public void showDefault() {
		clockLayout.setVisibility(VISIBLE);
		practiceTxt.setVisibility(GONE);
		ratingChangeTxt.setVisibility(GONE);
	}

	public void showCorrect(boolean show, String newRatingStr) {
		if (show) {
			ratingChangeTxt.setText(newRatingStr);
			ratingChangeTxt.setVisibility(VISIBLE);
			practiceTxt.setVisibility(GONE);
		} else {
			ratingChangeTxt.setVisibility(GONE);
		}
	}

	public void showWrong(boolean show, String newRatingStr) {
		if (show) {
			ratingChangeTxt.setText(newRatingStr);
			ratingChangeTxt.setVisibility(VISIBLE);
			practiceTxt.setVisibility(GONE);
		} else {
			ratingChangeTxt.setVisibility(GONE);
		}
	}

	public void showPractice(boolean show) {
		practiceTxt.setVisibility(show ? VISIBLE : GONE);

		if (show) {
			clockTxt.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}
}