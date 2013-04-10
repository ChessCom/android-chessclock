package com.chess.ui.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.statics.AppConstants;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.utilities.AppUtils;


/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class PanelInfoGameView extends RelativeLayout implements View.OnClickListener {

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int FLAG_ID = 0x00004402;
	public static final int PREMIUM_ID = 0x00004403;
	public static final int CAPTURED_ID = 0x00004404;
	public static final int TIME_LEFT_ID = 0x00004405;

	private static final int PLAYER_TEXT_SIZE = 16;

	private int AVATAR_MARGIN = 4;

	private int FLAG_SIZE = 16;
	private int FLAG_MARGIN = 5;

	private RoboTextView playerTxt;
	private ImageView avatarImg;
	private ImageView flagImg;
	private View capturedPiecesView;
	private RoboTextView timeLeftTxt;
	private ImageView premiumImg;

	private int side;
	private boolean useSingleLine;
	private LayoutParams capturedParams;
	private boolean smallScreen;
	private float density;
	private int AVATAR_SIZE;
	private int CAPTURED_PIECES_VIEW_HEIGHT;
	private int CAPTURED_PIECES_VIEW_WIDTH;
	private int TIME_LEFT_SIZE;

	public PanelInfoGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	public void onCreate(AttributeSet attrs) {
		if (AppUtils.HONEYCOMB_PLUS_API) {
			useSingleLine = true;
		}

		TypedArray array = getContext().obtainStyledAttributes(attrs,  R.styleable.PanelInfoGameView);
		try {
			useSingleLine = array.getBoolean(R.styleable.PanelInfoGameView_oneLine, false);
		} finally {
			array.recycle();
		}

		AVATAR_SIZE = (int) getContext().getResources().getDimension(R.dimen.panel_info_avatar_size);
		if (!useSingleLine) {
			AVATAR_SIZE = (int) getContext().getResources().getDimension(R.dimen.panel_info_avatar_big_size);
		}

		smallScreen = AppUtils.noNeedTitleBar(getContext());

		density = getContext().getResources().getDisplayMetrics().density;

		CAPTURED_PIECES_VIEW_HEIGHT = (int) getContext().getResources().getDimension(R.dimen.panel_info_captured_pieces_height);
		CAPTURED_PIECES_VIEW_WIDTH = (int) getContext().getResources().getDimension(R.dimen.panel_info_captured_pieces_width);
		TIME_LEFT_SIZE = (int) getContext().getResources().getDimension(R.dimen.panel_info_time_left_size);

		AVATAR_MARGIN *= density;
		FLAG_SIZE *= density;
		FLAG_MARGIN *= density;

		int padding = (int) (2 * density);
		if (smallScreen) {
			padding = 1;
		}

		int paddingSide = (int) (4 * density);
		setPadding(paddingSide, padding, paddingSide, padding);


		{// add avatar view
			avatarImg = new ImageView(getContext());

			LayoutParams avatarParams = new LayoutParams(AVATAR_SIZE, AVATAR_SIZE);
			avatarParams.setMargins(AVATAR_MARGIN, AVATAR_MARGIN, 0, AVATAR_MARGIN);
			avatarParams.addRule(CENTER_VERTICAL);

			avatarImg.setScaleType(ImageView.ScaleType.FIT_XY);
			avatarImg.setAdjustViewBounds(true);
			avatarImg.setId(AVATAR_ID);
			if (useSingleLine) {
				avatarImg.setVisibility(GONE);
			}

			addView(avatarImg, avatarParams);
		}

		{// add player name
			playerTxt = new RoboTextView(getContext());
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			if (useSingleLine) {
				playerParams.addRule(CENTER_VERTICAL);
				playerParams.addRule(ALIGN_PARENT_LEFT);
//				playerParams.addRule(LEFT_OF, CAPTURED_ID);
			} else {
				playerParams.addRule(RIGHT_OF, AVATAR_ID);
				playerParams.addRule(ALIGN_TOP, AVATAR_ID);
			}

			playerTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, PLAYER_TEXT_SIZE);
			playerTxt.setFont(RoboTextView.BOLD_FONT);
			playerTxt.setTextColor(getContext().getResources().getColor(R.color.white));
			playerTxt.setId(PLAYER_ID);
			playerTxt.setPadding((int) (4 * density), 0, 0, 0);
			playerTxt.setMarqueeRepeatLimit(2);
			playerTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);

			addView(playerTxt, playerParams);
		}

		{// add player flag
			flagImg = new ImageView(getContext());

			LayoutParams flagParams = new LayoutParams(FLAG_SIZE, FLAG_SIZE);
			flagParams.setMargins(FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN);
			flagParams.addRule(RelativeLayout.RIGHT_OF, PLAYER_ID);
			if (useSingleLine) {
				flagParams.addRule(RelativeLayout.CENTER_VERTICAL);
			} else {
				flagParams.addRule(RelativeLayout.ALIGN_TOP, AVATAR_ID);
			}

			flagImg.setImageDrawable(AppUtils.getUserFlag(getContext()));
			flagImg.setScaleType(ImageView.ScaleType.FIT_XY);
			flagImg.setAdjustViewBounds(true);
			flagImg.setId(FLAG_ID);

			addView(flagImg, flagParams);
		}

		{// add player premium icon
			premiumImg = new ImageView(getContext());

			LayoutParams premiumParams = new LayoutParams(FLAG_SIZE, FLAG_SIZE);
			premiumParams.setMargins(FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN, FLAG_MARGIN);
			premiumParams.addRule(RIGHT_OF, FLAG_ID);
			if (useSingleLine) {
				premiumParams.addRule(CENTER_VERTICAL);
			} else {
				premiumParams.addRule(ALIGN_TOP, AVATAR_ID);
			}

			premiumImg.setScaleType(ImageView.ScaleType.FIT_XY);
			premiumImg.setAdjustViewBounds(true);
			premiumImg.setId(PREMIUM_ID);

			addView(premiumImg, premiumParams);
		}

		{// add time left text
			timeLeftTxt = new RoboTextView(getContext());
			LayoutParams timeLeftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					TIME_LEFT_SIZE);
			timeLeftParams.addRule(ALIGN_PARENT_RIGHT);
			timeLeftParams.addRule(CENTER_VERTICAL);
			timeLeftParams.setMargins((int) (7 * density), 0, 0, 0);

			timeLeftTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			timeLeftTxt.setTextColor(getContext().getResources().getColor(R.color.light_grey));
			timeLeftTxt.setBackgroundResource(R.drawable.back_grey_emboss);
			timeLeftTxt.setId(TIME_LEFT_ID);
			timeLeftTxt.setFont(RoboTextView.BOLD_FONT);
			timeLeftTxt.setGravity(Gravity.CENTER_VERTICAL);
			if (smallScreen) {
				timeLeftTxt.setPadding((int)(2* density),(int)(2* density),(int)(2* density),(int)(2* density));
			} else {
				timeLeftTxt.setPadding((int)(10* density),(int)(2* density),(int)(10* density),(int)(2* density));
			}

			addView(timeLeftTxt, timeLeftParams);
		}

		{// add captured drawable view
			capturedPiecesView = new View(getContext());
			capturedParams = new LayoutParams(CAPTURED_PIECES_VIEW_WIDTH, CAPTURED_PIECES_VIEW_HEIGHT);
			if (useSingleLine) {
				capturedParams.addRule(LEFT_OF, TIME_LEFT_ID);
				capturedParams.addRule(CENTER_VERTICAL);
				capturedParams.addRule(RIGHT_OF, PLAYER_ID);
			} else {
				capturedParams.addRule(RIGHT_OF, AVATAR_ID);
				capturedParams.addRule(BELOW, PLAYER_ID);
			}

			CapturedPiecesDrawable capturedPiecesDrawable = new CapturedPiecesDrawable(getContext());
			if (AppUtils.JELLYBEAN_PLUS_API) {
				capturedPiecesView.setBackground(capturedPiecesDrawable);
			} else {
				capturedPiecesView.setBackgroundDrawable(capturedPiecesDrawable);
			}
			capturedPiecesView.setId(CAPTURED_ID);

			addView(capturedPiecesView, capturedParams);
		}
	}

	public void setSide(int side) {
		this.side = side;

		if ( avatarImg.getDrawable() != null) { // change avatar border
			((BoardAvatarDrawable) avatarImg.getDrawable()).setSide(side);
		}

		// change pieces color
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).setSide(side);

		// change timeLeft color and background
		if (side == AppConstants.WHITE_SIDE) {
			timeLeftTxt.setBackgroundResource(R.drawable.back_white_emboss);
			timeLeftTxt.setTextColor(getContext().getResources().getColor(R.color.new_main_back));
			if (smallScreen) {
				timeLeftTxt.setPadding((int)(2* density),(int)(2* density),(int)(2* density),(int)(2* density));
			} else {
				timeLeftTxt.setPadding((int)(10* density),(int)(2* density),(int)(10* density),(int)(2* density));
			}
		} else {
			timeLeftTxt.setBackgroundResource(R.drawable.back_grey_emboss);
			timeLeftTxt.setTextColor(getContext().getResources().getColor(R.color.light_grey));
			if (smallScreen) {
				timeLeftTxt.setPadding((int)(2* density),(int)(2* density),(int)(2* density),(int)(2* density));
			} else {
				timeLeftTxt.setPadding((int)(10* density),(int)(2* density),(int)(10* density),(int)(2* density));
			}
		}

		invalidate();
	}

	public int getSide() {
		return side;
	}

	@Override
	public void onClick(View view) {  // TODO handle avatar click

	}

	public void activateTimer(boolean active) {
		timeLeftTxt.setVisibility(active? VISIBLE: INVISIBLE);
	}

	public void setTimeLeft(String timeLeft) {
		timeLeftTxt.setText(timeLeft);
	}

	public void setPlayerLabel(String playerName) {
		playerTxt.setText(playerName);
	}

	public void showTimeLeft(boolean show) {
		timeLeftTxt.setVisibility(show ? VISIBLE : GONE);
	}

	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		((CapturedPiecesDrawable)capturedPiecesView.getBackground()).updateCapturedPieces(alivePiecesCountArray);
	}
}