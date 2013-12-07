package com.chess.ui.views;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chess.utilities.FontsHelper;
import com.chess.R;
import com.chess.widgets.RoboTextView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;

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
	public static final String NO_TIME = "--:--";

	private ImageView avatarImg;

	private RoboTextView ratingTxt;

	private RoboTextView clockTxt;

	private int side;
	private RoboTextView ratingChangeTxt;
	private RoboTextView practiceTxt;
	private LinearLayout clockLayout;
	private RoboTextView clockIconTxt;
	private ColorStateList defaultColorStateList;
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
		float density = resources.getDisplayMetrics().density;

		int padding = (int) resources.getDimension(R.dimen.panel_info_tactic_padding_top);
		int paddingLeft = resources.getDimensionPixelSize(R.dimen.panel_info_tactic_padding_side);
		int paddingRight = (int) (12 * density);
		setPadding(paddingLeft, padding, paddingRight, padding);

		float infoTextSize = resources.getDimension(R.dimen.new_tactics_info_text_size) / density;
		int avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_tactic_size);
		int avatarMarginRight = (int) resources.getDimension(R.dimen.panel_info_avatar_margin_right);
		redColor = resources.getColor(R.color.red_button);
		int lightGrey = resources.getColor(R.color.new_light_grey);

		defaultColorStateList = FontsHelper.getInstance().getThemeColorStateList(context, false);

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
			ratingTxt.setTextColor(defaultColorStateList);
			ratingTxt.setId(RATING_ID);
			ratingTxt.setFont(FontsHelper.BOLD_FONT);
			ratingTxt.setGravity(Gravity.CENTER_VERTICAL);

			addView(ratingTxt, ratingParams);
		}

		{// add rating change label
			ratingChangeTxt = new RoboTextView(context);
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RIGHT_OF, RATING_ID);
			layoutParams.addRule(CENTER_VERTICAL);

			ratingChangeTxt.setTextSize(infoTextSize);
			ratingChangeTxt.setTextColor(defaultColorStateList);
			ratingChangeTxt.setId(RATING_CHANGE_ID);
			ratingChangeTxt.setGravity(Gravity.CENTER_VERTICAL);
			ratingChangeTxt.setPadding((int)(4 * density), 0, 0, 0);
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
			float clockIconSize = resources.getDimension(R.dimen.new_tactics_clock_icon_size)/ density; // 21;
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText(R.string.ic_clock);
			clockIconTxt.setTextColor(defaultColorStateList);
			int paddingIcon = (int) (7 * density);
			int paddingIconTop = (int) (3 * density);
			clockIconTxt.setPadding(0, paddingIconTop, paddingIcon, 0);

			clockLayout.addView(clockIconTxt, clockIconParams);

			clockTxt.setTextSize(infoTextSize);
			clockTxt.setTextColor(defaultColorStateList);
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
		if (avatarImg.getDrawable() != null) { // change avatar border
			((BoardAvatarDrawable) avatarImg.getDrawable()).setSide(side);
		}
		invalidate();
	}

	public int getSide() {
		return side;
	}

	public void makeTimerRed(boolean makeRed) {
		if (makeRed) {
			clockIconTxt.setTextColor(redColor);
		} else {
			clockIconTxt.setTextColor(defaultColorStateList);
		}
		if (makeRed) {
			clockTxt.setTextColor(redColor);
		} else {
			clockTxt.setTextColor(defaultColorStateList);
		}
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
		practiceTxt.setVisibility(/*show ? VISIBLE : */GONE);

		if (show) {
			clockTxt.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}
}