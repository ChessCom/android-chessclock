package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.ProfileImageView;
import com.chess.widgets.RelLayout;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboTextView;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.07.13
 * Time: 8:01
 */
public class PanelInfoLiveView extends PanelInfoGameView {

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int RATING_ID = 0x00004402;
	public static final int FLAG_ID = 0x00004403;
	public static final int PREMIUM_ID = 0x00004404;
	public static final int CAPTURED_ID = 0x00004405;
	public static final int TIME_LEFT_ID = 0x00004406;
	public static final int DRAW_TEXT_ID = 0x00004407;
	public static final int DRAW_DECLINE_ID = 0x00004418;
	public static final int DRAW_ACCEPT_ID = 0x00004419;

	private int flagMargin = 5;

	protected ProfileImageView avatarImg;
	protected ImageView flagImg;
	protected View capturedPiecesView;
	protected ImageView premiumImg;

	private int side;
	private boolean smallScreen;
	private float density;
	private int whitePlayerTimeColor;
	private int blackPlayerTimeColor;
	private LinearLayout clockLayout;
	private RelLayout drawOfferedRelLay;
	private OnClickListener clickListener;
	private String playerName;

	/* Timer Animation */
	private static final int DURATION = 200;
	private static final float alphaPressed = 0.2f;
	private AnimatorSet bumpAnimationSet;


	public PanelInfoLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	@Override
	public void onCreate(AttributeSet attrs) {
		boolean useSingleLine;
		if (isInEditMode()) {
			return;
		}

		Context context = getContext();
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;
		int widthPixels = resources.getDisplayMetrics().widthPixels;
		boolean useLtr = AppUtils.useLtr(context);
		boolean API_17 = AppUtils.JELLYBEAN_MR1_PLUS_API;
		if (AppUtils.HONEYCOMB_PLUS_API) {
			useSingleLine = true;
		}

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PanelInfoGameView);
		try {
			useSingleLine = array.getBoolean(R.styleable.PanelInfoGameView_oneLine, false);
		} finally {
			array.recycle();
		}

		int playerTextSize = (int) (resources.getDimension(R.dimen.panel_info_player_text_size) / density);
		int playerRatingTextSize = (int) (resources.getDimension(R.dimen.panel_info_player_rating_text_size) / density);

		blackPlayerTimeColor = resources.getColor(R.color.semitransparent_white_65);
		whitePlayerTimeColor = resources.getColor(R.color.semitransparent_black_80);

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

		// set padding
		int paddingTop = (int) resources.getDimension(R.dimen.panel_info_padding_top);
		int paddingRight = (int) (8 * density);

		boolean isTablet = AppUtils.isTablet(getContext()) ;
		if (!isTablet) {
			if (useLtr) {
				setPadding(0, 0, paddingRight, 0);
			} else {
				setPadding(paddingRight, 0, 0, 0);
			}
		}

		int paddingLeft = resources.getDimensionPixelSize(R.dimen.panel_info_avatar_left_margin);

		if (nexus4Kind) {
			paddingTop = (int) (3 * density);
		}

		{// add avatar view
			avatarImg = new ProfileImageView(context);

			LayoutParams avatarParams = new LayoutParams(avatarSize, avatarSize);
			avatarParams.setMargins(paddingLeft, paddingTop, avatarMarginRight, paddingTop);
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
				if (API_17) {
					playerParams.addRule(END_OF, AVATAR_ID);
				}
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
			playerRatingTxt.setPadding((int) (4 * density), 0, 0, 0);
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
			clockLayoutParams.addRule(CENTER_VERTICAL);

			timeRemainParams.gravity = Gravity.CENTER_VERTICAL;
			clockIconParams.gravity = Gravity.CENTER_VERTICAL;

			clockLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT | Gravity.END);
			clockLayout.setMinimumWidth((int) (70 * density));
			clockLayout.setMinimumHeight((int) (35 * density));

			clockIconTxt.setFont(FontsHelper.ICON_FONT);
			float clockIconSize = resources.getDimension(R.dimen.new_tactics_clock_icon_size) / density; // 21;
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText(R.string.ic_clock);
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
				timeLeftParams.setMargins((int) (4 * density), paddingTop, paddingRight, paddingTop);
			} else {
				timeLeftParams.setMargins(paddingRight, paddingTop, (int) (4 * density), paddingTop);
			}

			timeRemainTxt.setTextSize(playerTextSize);
			timeRemainTxt.setTextColor(resources.getColor(R.color.light_grey));

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
					capturedParams.addRule(START_OF, TIME_LEFT_ID);
					capturedParams.addRule(END_OF, PREMIUM_ID);
				}
			} else {
				capturedParams.addRule(RIGHT_OF, AVATAR_ID);
				capturedParams.addRule(BELOW, PLAYER_ID);
				capturedParams.addRule(ALIGN_BOTTOM, AVATAR_ID);
				if (API_17) {
					capturedParams.addRule(END_OF, AVATAR_ID);
				}
			}

			if (AppUtils.JELLYBEAN_PLUS_API) {
				capturedPiecesView.setBackground(capturedPiecesDrawable);
			} else {
				capturedPiecesView.setBackgroundDrawable(capturedPiecesDrawable);
			}
			capturedPiecesView.setId(CAPTURED_ID);

			addView(capturedPiecesView, capturedParams);
		}

		{ // Draw offered layout
			int buttonsTextSize = 35;
			int panelButtonWidth = (int) (60 * density);
			int panelButtonHeight = (int) (50 * density);
			drawOfferedRelLay = new RelLayout(getContext());
			drawOfferedRelLay.setVisibility(GONE);

			{ // Accept Button
				RoboButton acceptDrawBtn = new RoboButton(getContext());
				acceptDrawBtn.setFont(FontsHelper.ICON_FONT);
				acceptDrawBtn.setId(DRAW_ACCEPT_ID);
				acceptDrawBtn.setText(R.string.ic_check);
				acceptDrawBtn.setTextSize(buttonsTextSize);
				acceptDrawBtn.setDrawableStyle(R.style.Button_Glassy);
				acceptDrawBtn.setMinimumWidth(panelButtonWidth);
				acceptDrawBtn.setMinimumHeight(panelButtonHeight);
				acceptDrawBtn.setMinWidth(panelButtonWidth);
				acceptDrawBtn.setMinHeight(panelButtonHeight);
				acceptDrawBtn.setOnClickListener(this);

				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				params.addRule(ALIGN_PARENT_RIGHT);
				if (API_17) {
					params.addRule(ALIGN_PARENT_END);
				}
				params.addRule(CENTER_VERTICAL);
				if (useLtr) {
					params.setMargins((int) (5 * density), 0, 0, 0);
				} else {
					params.setMargins(0, 0, (int) (5 * density), 0);
				}

				drawOfferedRelLay.addView(acceptDrawBtn, params);
			}

			{ // Decline button
				RoboButton declineDrawBtn = new RoboButton(getContext());
				declineDrawBtn.setFont(FontsHelper.ICON_FONT);
				declineDrawBtn.setId(DRAW_DECLINE_ID);
				declineDrawBtn.setText(R.string.ic_close);
				declineDrawBtn.setTextSize(buttonsTextSize);
				declineDrawBtn.setDrawableStyle(R.style.Button_Glassy);
				declineDrawBtn.setMinimumWidth(panelButtonWidth);
				declineDrawBtn.setMinimumHeight(panelButtonHeight);
				declineDrawBtn.setMinWidth(panelButtonWidth);
				declineDrawBtn.setMinHeight(panelButtonHeight);
				declineDrawBtn.setOnClickListener(this);

				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				params.addRule(CENTER_VERTICAL);
				if (API_17) {
					params.addRule(START_OF, DRAW_ACCEPT_ID);
				}
				params.addRule(LEFT_OF, DRAW_ACCEPT_ID);
				if (useLtr) {
					params.setMargins((int) (5 * density), 0, 0, 0);
				} else {
					params.setMargins(0, 0, (int) (5 * density), 0);
				}

				drawOfferedRelLay.addView(declineDrawBtn, params);
			}

			{ // Text label

				RoboButton drawOfferedTxt = new RoboButton(getContext());
				drawOfferedTxt.setFont(FontsHelper.ICON_FONT);
				drawOfferedTxt.setId(DRAW_TEXT_ID);
				drawOfferedTxt.setText(R.string.ic_handshake);
				drawOfferedTxt.setTextSize(buttonsTextSize);
				drawOfferedTxt.setMinimumWidth(panelButtonWidth);
				drawOfferedTxt.setMinimumHeight(panelButtonHeight);
				drawOfferedTxt.setMinWidth(panelButtonWidth);
				drawOfferedTxt.setMinHeight(panelButtonHeight);

				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				params.addRule(CENTER_VERTICAL);
				params.addRule(LEFT_OF, DRAW_DECLINE_ID);
				if (API_17) {
					params.addRule(START_OF, DRAW_DECLINE_ID);
				}
				if (useLtr) {
					params.setMargins((int) (5 * density), 0, 0, 0);
				} else {
					params.setMargins(0, 0, (int) (5 * density), 0);
				}

				drawOfferedRelLay.addView(drawOfferedTxt, params);
			}

			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(ALIGN_PARENT_RIGHT);
			if (API_17) {
				params.addRule(ALIGN_PARENT_END);
			}
			addView(drawOfferedRelLay, params);
		}

		initBumpAnimation();
	}

	@Override
	public void setSide(int side) {
		this.side = side;

		if (avatarImg.getDrawable() != null) { // change avatar border
			((BoardAvatarDrawable) avatarImg.getDrawable()).setSide(side);
		}

		// change pieces color
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).setSide(side);

		// change timeLeft color and background
		if (side == ChessBoard.WHITE_SIDE) {
			ButtonDrawableBuilder.setBackgroundToView(clockLayout, R.style.Button_White_50);
			timeRemainTxt.setTextColor(whitePlayerTimeColor);
			clockIconTxt.setTextColor(whitePlayerTimeColor);
		} else {
			ButtonDrawableBuilder.setBackgroundToView(clockLayout, R.style.Button_Black_30);
			timeRemainTxt.setTextColor(blackPlayerTimeColor);
			clockIconTxt.setTextColor(blackPlayerTimeColor);
		}
		setTimeRemainPadding();

		invalidate();
	}

	@Override
	public int getSide() {
		return side;
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == DRAW_DECLINE_ID) {
			clickListener.onClick(view);
		} else if (view.getId() == DRAW_ACCEPT_ID) {
			clickListener.onClick(view);
		}
	}

	@Override
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
		playerTxt.setText(playerName);
	}

	@Override
	public void setPlayerRating(String playerRating) {
		if (playerRating != null)
			playerRatingTxt.setText(Symbol.wrapInPars(playerRating));
	}

	@Override
	public void setPlayerFlag(String country) {
		if (country != null) {
			flagImg.setImageDrawable(AppUtils.getCountryFlagScaled(getContext(), country));
		} else {
			flagImg.setVisibility(GONE);
		}
	}

	@Override
	public void setPlayerPremiumIcon(int status) {
		premiumImg.setImageResource(AppUtils.getPremiumIcon(status));
	}

	@Override
	public void showFlags(boolean show) {
		flagImg.setVisibility(show ? VISIBLE : GONE);
		premiumImg.setVisibility(show ? VISIBLE : GONE);
	}

	@Override
	public void setTimeRemain(String timeRemain) {
		timeRemainTxt.setText(timeRemain);
	}

	@Override
	public void showTimeRemain(boolean show) {
		if (drawOfferedRelLay.getVisibility() == View.GONE) {
			clockLayout.setVisibility(show ? VISIBLE : GONE);
		}
	}

	@Override
	public void showTimeLeftIcon(boolean show) {
		int styleId;
		if (show) {
			styleId = side == ChessBoard.WHITE_SIDE ? R.style.Button_White_75 : R.style.Button_Black_65;
			clockIconTxt.setVisibility(View.VISIBLE);
		} else {
			styleId = side == ChessBoard.WHITE_SIDE ? R.style.Button_White_50 : R.style.Button_Black_30;
			clockIconTxt.setVisibility(View.GONE);
		}
		ButtonDrawableBuilder.setBackgroundToView(clockLayout, styleId);
		setTimeRemainPadding();
	}

	@Override
	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).updateCapturedPieces(alivePiecesCountArray);
	}

	@Override
	public void resetPieces() {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).dropPieces();
	}

	private void setTimeRemainPadding() {
		int timeLeftSmallPadding = (int) (2 * density);
		int timeLeftBigPadding = (int) (8 * density);

		if (smallScreen) {
			clockLayout.setPadding(timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding);
		} else {
			clockLayout.setPadding(timeLeftBigPadding, timeLeftSmallPadding, timeLeftBigPadding, timeLeftSmallPadding);
		}
	}

	public void showDrawOfferedView(boolean show) {
		drawOfferedRelLay.setVisibility(show ? VISIBLE : GONE);
		clockLayout.setVisibility(show ? GONE : VISIBLE);
		flagImg.setVisibility(show ? GONE : VISIBLE);
		premiumImg.setVisibility(show ? GONE : VISIBLE);
		playerRatingTxt.setVisibility(show ? GONE : VISIBLE);
		playerTxt.setVisibility(show ? GONE : VISIBLE);
	}

	/**
	 * For some unknown reason findViewById and setOnClickListener directly doesn't work. so use interceptor here
	 *
	 * @param listener that will intercept button clicks
	 */
	public void setClickHandler(OnClickListener listener) {
		this.clickListener = listener;
	}

	@Override
	public void setLabelsTextColor(int labelsTextColor) {
		this.playerTextColor = labelsTextColor;
		playerTxt.setTextColor(playerTextColor);
		playerRatingTxt.setTextColor(playerTextColor);
		invalidate();
	}

	public void setReconnecting(boolean online) {
		if (online) {
			playerTxt.setText(playerName);

			playerRatingTxt.setVisibility(VISIBLE);
			flagImg.setVisibility(VISIBLE);
			premiumImg.setVisibility(VISIBLE);
		} else {
			playerTxt.setText(getContext().getString(R.string.reconnecting_));
			playerRatingTxt.setVisibility(GONE);
			flagImg.setVisibility(GONE);
			premiumImg.setVisibility(GONE);
		}

	}

	private void initBumpAnimation() {
		bumpAnimationSet = new AnimatorSet();
		bumpAnimationSet.playTogether(
				ObjectAnimator.ofFloat(clockLayout, "scaleX", 1, 1.25f, 1),
				ObjectAnimator.ofFloat(clockLayout, "scaleY", 1, 1.25f, 1)
		);
		bumpAnimationSet.setDuration(DURATION);
	}

	public void startTimerBump() {
		bumpAnimationSet.start();
	}

	public void cancelTimerBump() {
		bumpAnimationSet.cancel();
	}
}