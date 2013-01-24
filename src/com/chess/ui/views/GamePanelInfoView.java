package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.statics.AppConstants;
import com.chess.ui.engine.PieceItem;
import com.chess.ui.interfaces.BoardViewFace;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class GamePanelInfoView extends RelativeLayout implements View.OnClickListener {

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int FLAG_ID = 0x00004402;
	public static final int PREMIUM_ID = 0x00004403;
	public static final int CAPTURED_ID = 0x00004404;
	public static final int TIME_LEFT_ID = 0x00004405;

	private int AVATAR_SIZE = 22;
	private int AVATAR_MARGIN = 5;

	private int FLAG_SIZE = 16;
	private int FLAG_MARGIN = 5;

	private int CAPTURED_PIECES_VIEW_HEIGHT = 22; // same as avatar
	private int CAPTURED_PIECES_VIEW_WIDTH = 110;


	private float density;

	private RoboTextView playerTxt;
	private ImageView avatarImg;
	private ImageView flagImg;
	private View capturedPiecesView;
	private RoboTextView timeLeftTxt;
	private ImageView premiumImg;

	private int side;

	public GamePanelInfoView(Context context) {
		super(context);
		onCreate();
	}

	public GamePanelInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public void onCreate() {
		density = getContext().getResources().getDisplayMetrics().density;

		CAPTURED_PIECES_VIEW_HEIGHT *= density;
		CAPTURED_PIECES_VIEW_WIDTH *= density;
		AVATAR_SIZE *= density;
		AVATAR_MARGIN *= density;
		FLAG_SIZE *= density;
		FLAG_MARGIN *= density;

		setBackgroundResource(R.color.new_main_back);

		int padding = (int) (7 * density);
		setPadding(padding, padding, padding, padding);


		{// add avatar view
			avatarImg = new ImageView(getContext());

			LayoutParams avatarParams = new LayoutParams(AVATAR_SIZE, AVATAR_SIZE);
			avatarParams.setMargins(AVATAR_MARGIN, AVATAR_MARGIN, AVATAR_MARGIN, AVATAR_MARGIN);
			avatarParams.addRule(RelativeLayout.CENTER_VERTICAL);

//			avatarImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.img_profile_picture_stub));
			avatarImg.setScaleType(ImageView.ScaleType.FIT_XY);
			avatarImg.setAdjustViewBounds(true);
			avatarImg.setId(AVATAR_ID);

			addView(avatarImg, avatarParams);
		}

		{// add player name
			playerTxt = new RoboTextView(getContext());
			RelativeLayout.LayoutParams playerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			playerParams.addRule(RelativeLayout.RIGHT_OF, AVATAR_ID);
			playerParams.addRule(RelativeLayout.CENTER_VERTICAL);

			playerTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			playerTxt.setTextColor(getContext().getResources().getColor(R.color.new_light_grey));
			playerTxt.setText("Player (9999)");
			playerTxt.setId(PLAYER_ID);

			addView(playerTxt, playerParams);
		}

		{// add player flag
			flagImg = new ImageView(getContext());

			RelativeLayout.LayoutParams flagParams = new RelativeLayout.LayoutParams(FLAG_SIZE, FLAG_SIZE);
			flagParams.setMargins(FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN);
			flagParams.addRule(RelativeLayout.RIGHT_OF, PLAYER_ID);
			flagParams.addRule(RelativeLayout.CENTER_VERTICAL);

			flagImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_united_states));
			flagImg.setScaleType(ImageView.ScaleType.FIT_XY);
			flagImg.setAdjustViewBounds(true);
			flagImg.setId(FLAG_ID);

			addView(flagImg, flagParams);
		}

		{// add player premium icon
			premiumImg = new ImageView(getContext());

			RelativeLayout.LayoutParams premiumParams = new RelativeLayout.LayoutParams(FLAG_SIZE, FLAG_SIZE);
			premiumParams.setMargins(FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN);
			premiumParams.addRule(RelativeLayout.RIGHT_OF, FLAG_ID);
			premiumParams.addRule(RelativeLayout.CENTER_VERTICAL);

			premiumImg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_nav_upgrade));
			premiumImg.setScaleType(ImageView.ScaleType.FIT_XY);
			premiumImg.setAdjustViewBounds(true);
			premiumImg.setId(PREMIUM_ID);

			addView(premiumImg, premiumParams);
		}

		{// add time left text
			timeLeftTxt = new RoboTextView(getContext());
			RelativeLayout.LayoutParams timeLeftParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					AVATAR_SIZE);
			timeLeftParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			timeLeftParams.addRule(RelativeLayout.CENTER_VERTICAL);
			timeLeftParams.setMargins((int) (7 * density), 0, 0, 0);

			timeLeftTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			timeLeftTxt.setTextColor(getContext().getResources().getColor(R.color.new_light_grey));
			timeLeftTxt.setText("2 days");
			timeLeftTxt.setBackgroundResource(R.drawable.back_grey_emboss);
			timeLeftTxt.setId(TIME_LEFT_ID);
			timeLeftTxt.setFont(RoboTextView.BOLD_FONT);

			addView(timeLeftTxt, timeLeftParams);
		}

		{// add captured drawable view
			capturedPiecesView = new View(getContext());
			RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(CAPTURED_PIECES_VIEW_WIDTH,
					CAPTURED_PIECES_VIEW_HEIGHT);
			imageParams.addRule(RelativeLayout.LEFT_OF, TIME_LEFT_ID);
			imageParams.addRule(RelativeLayout.CENTER_VERTICAL);

			CapturedPiecesDrawable capturedPiecesDrawable = new CapturedPiecesDrawable(getContext());
			capturedPiecesView.setBackgroundDrawable(capturedPiecesDrawable);
			capturedPiecesView.setId(CAPTURED_ID);

			addView(capturedPiecesView, imageParams);
		}
	}


	public void setSide(int side) {
		this.side = side;

		// change avatar border

		// change pieces color
		((CapturedPiecesDrawable)capturedPiecesView.getBackground()).setSide(side);

		// change timeLeft color and background
		if (side == AppConstants.WHITE_SIDE) {
			timeLeftTxt.setBackgroundResource(R.drawable.back_white_emboss);
			timeLeftTxt.setTextColor(getContext().getResources().getColor(R.color.new_main_back));
		} else {
			timeLeftTxt.setBackgroundResource(R.drawable.back_grey_emboss);
			timeLeftTxt.setTextColor(getContext().getResources().getColor(R.color.new_light_grey));
		}


		invalidate();
	}

	public int getSide() {
		return side;
	}

	public void onClick(View view) {  // TODO handle avatar click

	}

}