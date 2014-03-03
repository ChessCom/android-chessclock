package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.ProfileImageView;
import com.chess.widgets.RelLayout;
import com.chess.widgets.RoboTextView;


/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class PanelInfoGameView extends RelLayout implements View.OnClickListener {

	public static final long THINKING_DOT_DELAY = 500;

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int RATING_ID = 0x00004402;
	public static final int FLAG_ID = 0x00004403;
	public static final int PREMIUM_ID = 0x00004404;
	public static final int CAPTURED_ID = 0x00004405;
	public static final int TIME_LEFT_ID = 0x00004406;

	private int flagMargin = 5;

	protected Handler handler;

	protected RoboTextView playerTxt;
	protected ProfileImageView avatarImg;
	protected ImageView flagImg;
	protected View capturedPiecesView;
	protected RoboTextView timeRemainTxt;
	protected ImageView premiumImg;

	private int side;
	private boolean smallScreen;
	private float density;
	protected RoboTextView playerRatingTxt;
	private boolean timeLeftHasBack;
	private int topPlayerTimeLeftColor;
	private int bottomPlayerTimeLeftColor;
	protected RoboTextView clockIconTxt;
	private LinearLayout clockLayout;
	private int paddingTop;
	private int paddingRight;
	private int paddingLeft;
	protected int playerTextColor;
	private int dotsAdded;

	public PanelInfoGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	protected void onCreate(AttributeSet attrs) {
		boolean useSingleLine;
		if (isInEditMode()) {
			return;
		}

		Context context = getContext();
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;
		int widthPixels = resources.getDisplayMetrics().widthPixels;

		handler = new Handler();

		boolean useLtr = AppUtils.useLtr(context);
		boolean API_17 = AppUtils.JELLYBEAN_MR1_PLUS_API;

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
		playerTextColor = resources.getColor(R.color.white);

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

		boolean nexus4Kind = AppUtils.isNexus4Kind(getContext());
		if (nexus4Kind) {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_medium_size);
		}

		smallScreen = AppUtils.isSmallScreen(context);

		int capturedPiecesViewHeight = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_height);
		int capturedPiecesViewWidth = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_width);
		int timeLeftSize = (int) resources.getDimension(R.dimen.panel_info_time_left_size);
		int avatarMarginRight = (int) resources.getDimension(R.dimen.panel_info_avatar_margin_right);

		int flagSize = (int) resources.getDimension(R.dimen.panel_info_flag_size);
		flagMargin *= density;

		{ // set padding
			paddingTop = (int) resources.getDimension(R.dimen.panel_info_padding_top);
			paddingRight = (int) (4 * density);

			paddingLeft = resources.getDimensionPixelSize(R.dimen.panel_info_avatar_left_margin);

			if (nexus4Kind) {
				paddingTop = (int) (3 * density);
			}
		}

		{// add avatar view
			avatarImg = new ProfileImageView(context);

			LayoutParams avatarParams = new LayoutParams(avatarSize, avatarSize);
			if (useLtr) {
				avatarParams.setMargins(paddingLeft, paddingTop, avatarMarginRight, paddingTop);
			} else {
				avatarParams.setMargins(avatarMarginRight, paddingTop, paddingLeft, paddingTop);
			}

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
			int playerNameMargin = resources.getDimensionPixelSize(R.dimen.player_name_margin_top);
			playerParams.setMargins(0, -playerNameMargin, 0, 0);
			if (useSingleLine) {
				playerParams.addRule(CENTER_VERTICAL);
				playerParams.addRule(ALIGN_PARENT_LEFT);
				if (API_17) {
					playerParams.addRule(ALIGN_PARENT_START);
				}
			} else {
				playerParams.addRule(ALIGN_TOP, AVATAR_ID);
				playerParams.addRule(RIGHT_OF, AVATAR_ID);
				if (API_17) {
					playerParams.addRule(END_OF, AVATAR_ID);
				}
			}

			playerTxt.setTextSize(playerTextSize);
			playerTxt.setTextColor(playerTextColor);
			playerTxt.setId(PLAYER_ID);
			playerTxt.setPadding((int) (4 * density), 0, 0, 0);
			playerTxt.setMarqueeRepeatLimit(2);
			playerTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			playerTxt.setFont(FontsHelper.BOLD_FONT);

			if (useSingleLine && smallScreen) {
				playerTxt.setMaxWidth(widthPixels / 3);
			}

			addView(playerTxt, playerParams);
		}

		{// add player rating
			playerRatingTxt = new RoboTextView(context);
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			int marginTop = resources.getDimensionPixelSize(R.dimen.panel_info_rating_margin_top);
			playerParams.setMargins(0, -marginTop, 0, 0);
			playerParams.addRule(RIGHT_OF, PLAYER_ID);
			if (API_17) {
				playerParams.addRule(END_OF, PLAYER_ID);
			}
			playerParams.addRule(ALIGN_TOP, AVATAR_ID);

			playerRatingTxt.setTextSize(playerRatingTextSize);
			playerRatingTxt.setTextColor(playerTextColor);
			playerRatingTxt.setId(RATING_ID);
			if (useLtr) {
				playerRatingTxt.setPadding((int) (4 * density), 0, 0, 0);
			} else {
				playerRatingTxt.setPadding(0, 0, (int) (4 * density), 0);
			}

			playerRatingTxt.setFont(FontsHelper.BOLD_FONT);

			if (useSingleLine && smallScreen) {
				playerRatingTxt.setVisibility(GONE);
			}

			addView(playerRatingTxt, playerParams);
		}

		{// add player flag
			flagImg = new ImageView(context);

			LayoutParams flagParams = new LayoutParams(flagSize, flagSize);
			int marginTop = resources.getDimensionPixelSize(R.dimen.panel_info_flag_margin_top);
			flagParams.setMargins(flagMargin, -marginTop, flagMargin, flagMargin);
			flagParams.addRule(RIGHT_OF, RATING_ID);
			if (API_17) {
				flagParams.addRule(END_OF, RATING_ID);
			}
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
			int marginTop = resources.getDimensionPixelSize(R.dimen.panel_info_flag_margin_top);

			LayoutParams premiumParams = new LayoutParams(flagSize, flagSize);
			premiumParams.setMargins(flagMargin, -marginTop, flagMargin, flagMargin);
			premiumParams.addRule(RIGHT_OF, FLAG_ID);
			if (API_17) {
				premiumParams.addRule(END_OF, FLAG_ID);
			}
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
			clockLayout = new LinearLayout(context);
			timeRemainTxt = new RoboTextView(context);
			clockIconTxt = new RoboTextView(context);

			LayoutParams clockLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams timeRemainParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams clockIconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			clockLayoutParams.addRule(ALIGN_PARENT_RIGHT);
			if (API_17) {
				clockLayoutParams.addRule(ALIGN_PARENT_END);
			}
			clockLayoutParams.addRule(ALIGN_TOP, AVATAR_ID);
			clockLayoutParams.setMargins(0, (int) (-5 * density), 0, 0);

			timeRemainParams.gravity = CENTER_VERTICAL;
			clockIconParams.gravity = CENTER_VERTICAL;

			clockIconTxt.setFont(FontsHelper.ICON_FONT);
			float clockIconSize = resources.getDimension(R.dimen.new_tactics_clock_icon_size) / density; // 21;
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText(R.string.ic_clock);
			clockIconTxt.setTextColor(playerTextColor);
			int paddingIcon = resources.getDimensionPixelSize(R.dimen.new_tactics_clock_icon_padding);
			int paddingIconTop = resources.getDimensionPixelSize(R.dimen.new_tactics_clock_icon_padding_top);
			if (useLtr) {
				clockIconTxt.setPadding(0, paddingIconTop, paddingIcon, 0);
			} else {
				clockIconTxt.setPadding(paddingIcon, paddingIconTop, 0, 0);
			}

			clockIconTxt.setVisibility(GONE);

			clockLayout.addView(clockIconTxt, clockIconParams);

			LayoutParams timeLeftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, timeLeftSize);
			timeLeftParams.addRule(ALIGN_PARENT_RIGHT);
			if (API_17) {
				timeLeftParams.addRule(ALIGN_PARENT_END);
			}
			timeLeftParams.addRule(CENTER_VERTICAL);
			if (useLtr) {
				timeLeftParams.setMargins((int) (7 * density), paddingTop, paddingRight, paddingTop); // use to set space between captured pieces in single line mode
			} else {
				timeLeftParams.setMargins(paddingRight, paddingTop, (int) (7 * density), paddingTop); // use to set space between captured pieces in single line mode
			}

			timeRemainTxt.setTextSize(playerTextSize);
			timeRemainTxt.setTextColor(playerTextColor);

			if (timeLeftHasBack) {
				clockLayout.setBackgroundResource(R.drawable.back_glassy_rounded);
			}
			timeRemainTxt.setId(TIME_LEFT_ID);
			timeRemainTxt.setFont(FontsHelper.BOLD_FONT);
			timeRemainTxt.setGravity(Gravity.CENTER_VERTICAL);
			setTimeRemainPadding();

			clockLayout.addView(timeRemainTxt, timeRemainParams);

			addView(clockLayout, clockLayoutParams);
		}

		{// add captured drawable view
			capturedPiecesView = new View(context);
			CapturedPiecesDrawable capturedPiecesDrawable = new CapturedPiecesDrawable(context);
			LayoutParams capturedParams = new LayoutParams(capturedPiecesViewWidth, capturedPiecesViewHeight);
			capturedParams.setMargins(0, 0, 0, (int) (-4 * density));
			if (useSingleLine) {
				capturedParams.addRule(LEFT_OF, TIME_LEFT_ID);
				capturedParams.addRule(CENTER_VERTICAL);
				capturedParams.addRule(RIGHT_OF, PREMIUM_ID);
				if (API_17) {
					capturedParams.addRule(END_OF, PREMIUM_ID);
				}
			} else {
				capturedParams.addRule(RIGHT_OF, AVATAR_ID);
				if (API_17) {
					capturedParams.addRule(END_OF, AVATAR_ID);
				}
				capturedParams.addRule(BELOW, PLAYER_ID);
				capturedParams.addRule(ALIGN_BOTTOM, AVATAR_ID);
			}

			if (AppUtils.JELLYBEAN_PLUS_API) {
				capturedPiecesView.setBackground(capturedPiecesDrawable);
			} else {
				capturedPiecesView.setBackgroundDrawable(capturedPiecesDrawable);
			}
			capturedPiecesView.setId(CAPTURED_ID);

			addView(capturedPiecesView, capturedParams);
		}

		{ // Thinking View
			RoboTextView thinkingTxt = new RoboTextView(getContext());
			thinkingTxt.setFont(FontsHelper.BOLD_FONT);
			thinkingTxt.setTextSize(playerTextSize);
			thinkingTxt.setText(R.string.thinking_);
			thinkingTxt.setTextColor(Color.WHITE);
			thinkingTxt.setBackgroundResource(R.color.glassy_button);
			thinkingTxt.setVisibility(GONE);
			thinkingTxt.setGravity(Gravity.CENTER);

			RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

			addView(thinkingTxt, params);

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
				clockLayout.setBackgroundResource(R.drawable.back_white_emboss);
				timeRemainTxt.setTextColor(topPlayerTimeLeftColor);
			} else {
				clockLayout.setBackgroundResource(R.drawable.back_glassy_rounded);
				timeRemainTxt.setTextColor(bottomPlayerTimeLeftColor);
			}
			setTimeRemainPadding();
		} else {
			if (side == ChessBoard.WHITE_SIDE) {
				timeRemainTxt.setTextColor(topPlayerTimeLeftColor);
			} else {
				timeRemainTxt.setTextColor(bottomPlayerTimeLeftColor);
			}
		}

		invalidate(0, 0, getWidth(), getHeight());
	}

	public int getSide() {
		return side;
	}

	@Override
	public void onClick(View view) {  // TODO handle avatar click

	}


	public void setPlayerName(String playerName) {
		playerTxt.setText(playerName);
	}

	public void setPlayerRating(String playerRating) {
		if (playerRating != null)
			playerRatingTxt.setText(Symbol.wrapInPars(playerRating));
	}

	public void setPlayerFlag(String country) {
		if (country != null) {
			flagImg.setVisibility(VISIBLE);
			flagImg.setImageDrawable(AppUtils.getCountryFlagScaled(getContext(), country));
		} else {
			flagImg.setVisibility(GONE);
		}
	}

	public void setPlayerPremiumIcon(int status) {
		premiumImg.setImageResource(AppUtils.getPremiumIcon(status));
	}

	public void showFlags(boolean show) {
		flagImg.setVisibility(show ? VISIBLE : GONE);
		premiumImg.setVisibility(show ? VISIBLE : GONE);
	}

	public void setTimeRemain(String timeRemain) {
		if (timeRemain.equals(getContext().getString(R.string.vacation_on))) {
			clockIconTxt.setText(R.string.ic_pause);
			timeRemainTxt.setText(R.string.vacation_on);
		} else {
			timeRemainTxt.setText(timeRemain);
			clockIconTxt.setVisibility(timeRemain.length() > 0 ? View.VISIBLE : View.GONE);
			clockIconTxt.setText(R.string.ic_clock);
		}
	}

	public void showTimeRemain(boolean show) {
		clockLayout.setVisibility(show ? VISIBLE : GONE);
	}

	public void showTimeLeftIcon(boolean show) {
		clockIconTxt.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).updateCapturedPieces(alivePiecesCountArray);
	}

	public void resetPieces() {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).dropPieces();
	}

	private void setTimeRemainPadding() {
		int timeLeftSmallPadding = (int) (2 * density);
		int timeLeftBigPadding = (int) (10 * density);

		if (smallScreen) {
			clockLayout.setPadding(timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding);
		} else {
			clockLayout.setPadding(timeLeftBigPadding, timeLeftSmallPadding, timeLeftBigPadding, timeLeftSmallPadding);
		}
	}

	public void showThinkingView(boolean show) {
		if (show) {
			playerTxt.setText(getContext().getString(R.string.computer) + Symbol.SPACE +
					getContext().getString(R.string.thinking_));
			handler.postDelayed(thinkingDotTask, THINKING_DOT_DELAY);
		} else {
			playerTxt.setText(R.string.computer);
			handler.removeCallbacks(thinkingDotTask);
		}
	}

	protected Runnable thinkingDotTask = new Runnable() {
		@Override
		public void run() {
			if (dotsAdded++ < 3) {
				playerTxt.setText(playerTxt.getText() + Symbol.DOT.trim());
			} else {
				dotsAdded = 0;
				playerTxt.setText(getContext().getString(R.string.computer) + Symbol.SPACE +
						getContext().getString(R.string.thinking_));
			}
			handler.postDelayed(thinkingDotTask, THINKING_DOT_DELAY);
		}
	};

	public void setLabelsTextColor(int labelsTextColor) {
		this.playerTextColor = labelsTextColor;
		playerTxt.setTextColor(playerTextColor);
		timeRemainTxt.setTextColor(playerTextColor);
		playerRatingTxt.setTextColor(playerTextColor);
		clockIconTxt.setTextColor(playerTextColor);
		invalidate();
	}

	public void invalidateMe() { // TODO update only needed part of view, like avatarView
		invalidate(0, 0, getWidth(), getHeight());
	}
}