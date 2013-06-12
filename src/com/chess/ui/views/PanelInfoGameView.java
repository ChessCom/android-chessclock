package com.chess.ui.views;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RelLayout;
import com.chess.RoboTextView;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.utilities.AppUtils;


/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class PanelInfoGameView extends RelLayout implements View.OnClickListener {

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int RATING_ID = 0x00004402;
	public static final int FLAG_ID = 0x00004403;
	public static final int PREMIUM_ID = 0x00004404;
	public static final int CAPTURED_ID = 0x00004405;
	public static final int TIME_LEFT_ID = 0x00004406;

	private int FLAG_SIZE = 16;
	private int FLAG_MARGIN = 5;

	private RoboTextView playerTxt;
	private ImageView avatarImg;
	private ImageView flagImg;
	private View capturedPiecesView;
	private RoboTextView timeLeftTxt;
	private ImageView premiumImg;

	private int side;
	private boolean smallScreen;
	private float density;
	private Resources resources;
	private RoboTextView playerRatingTxt;
	private boolean timeLeftHasBack;
	private int topPlayerTimeLeftColor;
	private int bottomPlayerTimeLeftColor;

	public PanelInfoGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	public void onCreate(AttributeSet attrs) {
		boolean useSingleLine;
		if (isInEditMode()) {
			return;
		}

		Context context = getContext();
		resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		if (AppUtils.HONEYCOMB_PLUS_API) {
			useSingleLine = true;
		}

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PanelInfoGameView);
		try {
			useSingleLine = array.getBoolean(R.styleable.PanelInfoGameView_oneLine, false);
			timeLeftHasBack = array.getBoolean(R.styleable.PanelInfoGameView_timeLeftHasBack, false);
		} finally {
			array.recycle();
		}

		int playerTextSize = (int) (resources.getDimension(R.dimen.panel_info_player_text_size) / density);
		int playerRatingTextSize = (int) (resources.getDimension(R.dimen.panel_info_player_rating_text_size) / density);
		int playerTextColor = resources.getColor(R.color.white);

		if (timeLeftHasBack) {
			topPlayerTimeLeftColor = resources.getColor(R.color.semitransparent_white_65);
			bottomPlayerTimeLeftColor = resources.getColor(R.color.new_author_dark_grey);
		} else {
			topPlayerTimeLeftColor = resources.getColor(R.color.semitransparent_white_65);
			bottomPlayerTimeLeftColor = resources.getColor(R.color.white);
		}

		int avatarSize;
		if (useSingleLine) {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_size);
		} else {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_big_size);
		}

		boolean hasSoftKeys = AppUtils.hasSoftKeys(((Activity)getContext()).getWindowManager());
		if (hasSoftKeys) {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_medium_size);
		}

		smallScreen = AppUtils.noNeedTitleBar(context);

		int capturedPiecesViewHeight = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_height);
		int capturedPiecesViewWidth = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_width);
		int timeLeftSize = (int) resources.getDimension(R.dimen.panel_info_time_left_size);
		int avatarMarginRight = (int) resources.getDimension(R.dimen.panel_info_avatar_margin_right);

		FLAG_SIZE *= density;
		FLAG_MARGIN *= density;

		{// add avatar view
			avatarImg = new ImageView(context);

			LayoutParams avatarParams = new LayoutParams(avatarSize, avatarSize);
			avatarParams.setMargins(0, 0, avatarMarginRight, 0);
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
			playerTxt = new RoboTextView(context);
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			if (useSingleLine) {
				playerParams.addRule(CENTER_VERTICAL);
				playerParams.addRule(ALIGN_PARENT_LEFT);
			} else {
				playerParams.addRule(RIGHT_OF, AVATAR_ID);
				playerParams.addRule(ALIGN_TOP, AVATAR_ID);
			}

			playerTxt.setTextSize(playerTextSize);
			playerTxt.setTextColor(playerTextColor);
			playerTxt.setId(PLAYER_ID);
			playerTxt.setPadding((int) (4 * density), 0, 0, 0);
			playerTxt.setMarqueeRepeatLimit(2);
			playerTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			playerTxt.setFont(FontsHelper.BOLD_FONT);

			addView(playerTxt, playerParams);
		}

		{// add player rating
			playerRatingTxt = new RoboTextView(context);
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			playerParams.addRule(RIGHT_OF, PLAYER_ID);
			playerParams.addRule(ALIGN_TOP, AVATAR_ID);

			playerRatingTxt.setTextSize(playerRatingTextSize);
			playerRatingTxt.setTextColor(playerTextColor);
			playerRatingTxt.setId(RATING_ID);
			playerRatingTxt.setPadding((int) (4 * density), (int) (3 * density), 0, 0);
			playerRatingTxt.setFont(FontsHelper.BOLD_FONT);

			addView(playerRatingTxt, playerParams);
		}

		{// add player flag
			flagImg = new ImageView(context);

			LayoutParams flagParams = new LayoutParams(FLAG_SIZE, FLAG_SIZE);
			flagParams.setMargins(FLAG_MARGIN, 0, FLAG_MARGIN, FLAG_MARGIN);
			flagParams.addRule(RIGHT_OF, RATING_ID);
			if (useSingleLine) {
				flagParams.addRule(CENTER_VERTICAL);
			} else {
				flagParams.addRule(ALIGN_TOP, AVATAR_ID);
			}

			flagImg.setScaleType(ImageView.ScaleType.FIT_XY);
			flagImg.setAdjustViewBounds(true);
			flagImg.setId(FLAG_ID);

			addView(flagImg, flagParams);
		}

		{// add player premium icon
			premiumImg = new ImageView(context);

			LayoutParams premiumParams = new LayoutParams(FLAG_SIZE, FLAG_SIZE);
			premiumParams.setMargins(FLAG_MARGIN, 0, FLAG_MARGIN, FLAG_MARGIN);
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
			timeLeftTxt = new RoboTextView(context);
			LayoutParams timeLeftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					timeLeftSize);
			timeLeftParams.addRule(ALIGN_PARENT_RIGHT);
			timeLeftParams.addRule(CENTER_VERTICAL);
			timeLeftParams.setMargins((int) (7 * density), 0, 0, 0);

			timeLeftTxt.setTextSize(playerTextSize);
			timeLeftTxt.setTextColor(resources.getColor(R.color.light_grey));

			if (timeLeftHasBack)
				timeLeftTxt.setBackgroundResource(R.drawable.back_grey_emboss);
			timeLeftTxt.setId(TIME_LEFT_ID);
			timeLeftTxt.setFont(FontsHelper.BOLD_FONT);
			timeLeftTxt.setGravity(Gravity.CENTER_VERTICAL);
			setTimeLeftPadding();

			addView(timeLeftTxt, timeLeftParams);
		}

		{// add captured drawable view
			capturedPiecesView = new View(context);
			LayoutParams capturedParams = new LayoutParams(capturedPiecesViewWidth, capturedPiecesViewHeight);
			if (useSingleLine) {
				capturedParams.addRule(LEFT_OF, TIME_LEFT_ID);
				capturedParams.addRule(CENTER_VERTICAL);
				capturedParams.addRule(RIGHT_OF, PLAYER_ID);
			} else {
				capturedParams.addRule(RIGHT_OF, AVATAR_ID);
				capturedParams.addRule(BELOW, PLAYER_ID);
			}

			CapturedPiecesDrawable capturedPiecesDrawable = new CapturedPiecesDrawable(context);
			if (AppUtils.JELLYBEAN_PLUS_API) {
				capturedPiecesView.setBackground(capturedPiecesDrawable);
			} else {
				capturedPiecesView.setBackgroundDrawable(capturedPiecesDrawable);
			}
			capturedPiecesView.setId(CAPTURED_ID);

			addView(capturedPiecesView, capturedParams);
		}

		{// Set padding
			int padding = (int) resources.getDimension(R.dimen.panel_info_padding_top);
			int paddingRight = (int) (4 * density);
			int paddingLeft = (int) (11 * density);

			if (hasSoftKeys) {
				padding = (int) (5 * density);
			}

			setPadding(paddingLeft, padding, paddingRight, padding);
		}
	}

	public void setSide(int side) {
		this.side = side;

		if (avatarImg.getDrawable() != null) { // change avatar border
			((BoardAvatarDrawable) avatarImg.getDrawable()).setSide(side);
		}

		// change pieces color
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).setSide(side);

		// change timeLeft color and background
		if (timeLeftHasBack) {
			if (side == ChessBoard.WHITE_SIDE) {
				timeLeftTxt.setBackgroundResource(R.drawable.back_white_emboss);
				timeLeftTxt.setTextColor(topPlayerTimeLeftColor);
			} else {
				timeLeftTxt.setBackgroundResource(R.drawable.back_grey_emboss);
				timeLeftTxt.setTextColor(bottomPlayerTimeLeftColor);
			}
			setTimeLeftPadding();
		} else {
			if (side == ChessBoard.WHITE_SIDE) {
				timeLeftTxt.setTextColor(topPlayerTimeLeftColor);
			} else {
				timeLeftTxt.setTextColor(bottomPlayerTimeLeftColor);
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
		timeLeftTxt.setVisibility(active ? VISIBLE : INVISIBLE);
	}

	public void setTimeLeft(String timeLeft) {
		timeLeftTxt.setText(timeLeft);
	}

	public void setPlayerName(String playerName) {
		playerTxt.setText(playerName);
	}

	public void setPlayerRating(String playerRating) {
		if (playerRating != null)
			playerRatingTxt.setText(StaticData.SYMBOL_LEFT_PAR + playerRating + StaticData.SYMBOL_RIGHT_PAR);
	}

	public void setPlayerFlag(String country) {
		flagImg.setImageDrawable(AppUtils.getCountryFlagScaled(getContext(), country));
	}

	public void showFlags(boolean show) {
		flagImg.setVisibility(show ? VISIBLE : GONE);
		premiumImg.setVisibility(show ? VISIBLE : GONE);
	}

	public void showTimeLeft(boolean show) {
		timeLeftTxt.setVisibility(show ? VISIBLE : GONE);
	}

	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).updateCapturedPieces(alivePiecesCountArray);
	}



	private void setTimeLeftPadding() {
		int timeLeftSmallPadding = (int) (2 * density);
		int timeLeftBigPadding = (int) (10 * density);

		if (smallScreen) {
			timeLeftTxt.setPadding(timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding);
		} else {
			timeLeftTxt.setPadding(timeLeftBigPadding, timeLeftSmallPadding, timeLeftBigPadding, timeLeftSmallPadding);
		}
	}

}