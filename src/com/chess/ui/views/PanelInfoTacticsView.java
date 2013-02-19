package com.chess.ui.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;

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

	private int AVATAR_SIZE = 34;
	private int TOP_BUTTON_HEIGHT = 36;
	private int AVATAR_MARGIN = 11;


	private float density;

	private RoboTextView ratingTxt;
	private ImageView avatarImg;

	private RoboTextView clockTxt;

	private int side;
	private RoboButton topButton;
	private OnClickListener listener;
	private RoboTextView ratingChangeTxt;
	private RoboTextView practiceTxt;

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

		AVATAR_SIZE *= density;
		AVATAR_MARGIN *= density;
		TOP_BUTTON_HEIGHT *= density;

		setBackgroundResource(R.color.new_main_back);

		int padding = (int) (7 * density);
		setPadding(padding, padding, padding, padding);


		{// add avatar view
			avatarImg = new ImageView(getContext());

			LayoutParams avatarParams = new LayoutParams(AVATAR_SIZE, AVATAR_SIZE);
			avatarParams.setMargins(AVATAR_MARGIN, 0, AVATAR_MARGIN, 0);
			avatarParams.addRule(CENTER_VERTICAL);

			avatarImg.setScaleType(ImageView.ScaleType.FIT_XY);
			avatarImg.setAdjustViewBounds(true);
			avatarImg.setId(AVATAR_ID);

			// set avatars
			Bitmap src = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();
			BoardAvatarDrawable boardAvatarDrawable = new BoardAvatarDrawable(getContext(), src);
			boardAvatarDrawable.setBorderThick(4);
			avatarImg.setImageDrawable(boardAvatarDrawable);

			addView(avatarImg, avatarParams);
		}

		{// add player name
			ratingTxt = new RoboTextView(getContext());
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			playerParams.addRule(RIGHT_OF, AVATAR_ID);
			playerParams.addRule(CENTER_VERTICAL);

			ratingTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
			ratingTxt.setTextColor(Color.WHITE);
			ratingTxt.setId(PLAYER_ID);
			ratingTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			ratingTxt.setFont(RoboTextView.BOLD_FONT);

			addView(ratingTxt, playerParams);
		}

		{// add time left text
			clockTxt = new RoboTextView(getContext());
			LayoutParams timeLeftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			timeLeftParams.addRule(ALIGN_PARENT_RIGHT);
			timeLeftParams.addRule(CENTER_VERTICAL);

			clockTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			clockTxt.setTextColor(getContext().getResources().getColor(R.color.new_light_grey));
			clockTxt.setId(TIME_LEFT_ID);
			clockTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			clockTxt.setCompoundDrawablePadding((int) (7 * density));
			clockTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tactics_clock, 0, 0, 0);
			clockTxt.setGravity(Gravity.CENTER);
			clockTxt.setPadding(0, 0, (int) (20 * density), 0);

			addView(clockTxt, timeLeftParams);
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
			practiceTxt.setPadding(0, 0, (int)(18 * density), 0);
			practiceTxt.setShadowLayer(0.5f, 0, -1, Color.BLACK);
			practiceTxt.setFont(RoboTextView.BOLD_FONT);
			practiceTxt.setVisibility(GONE);
			practiceTxt.setText(R.string.practice_mode);

			addView(practiceTxt, layoutParams);
		}
	}

	public void setSide(int side) {
		this.side = side;

		// change avatar border
		((BoardAvatarDrawable)avatarImg.getDrawable()).setSide(side);

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

	public void hideCorrect(){
		showCorrect(false, null);
	}

	public void showDefault(){
		clockTxt.setVisibility(VISIBLE);
		practiceTxt.setVisibility(GONE);
		topButton.setVisibility(GONE);
		ratingChangeTxt.setVisibility(GONE);
	}

	public void showCorrect(boolean show, String newRatingStr){
		if (show) {
			topButton.setBackgroundResource(R.drawable.button_light_green_selector);
			topButton.setText(R.string.correct);
			topButton.setVisibility(VISIBLE);

			ratingChangeTxt.setText(newRatingStr);
			ratingChangeTxt.setVisibility(VISIBLE);
			clockTxt.setVisibility(GONE);
			practiceTxt.setVisibility(GONE);

		} else {
			topButton.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}

	public void hideWrong(){
		showWrong(false, null);
	}

	public void showWrong(boolean show, String newRatingStr){
		if (show) {
			topButton.setBackgroundResource(R.drawable.button_red_selector);
			topButton.setText(R.string.wrong);
			topButton.setVisibility(VISIBLE);

			ratingChangeTxt.setText(newRatingStr);
			ratingChangeTxt.setVisibility(VISIBLE);
			clockTxt.setVisibility(GONE);
			practiceTxt.setVisibility(GONE);
		} else {
			topButton.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}

	public void showPractice(boolean show){
		practiceTxt.setVisibility(show? VISIBLE: GONE);

		if (show) {
			topButton.setVisibility(GONE);
			clockTxt.setVisibility(GONE);
			ratingChangeTxt.setVisibility(GONE);
		}
	}
}