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
import android.widget.LinearLayout;
import com.chess.*;
import com.chess.backend.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;

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

	private int FLAG_SIZE = 16;
	private int FLAG_MARGIN = 5;

	protected RoboTextView playerTxt;
	protected ImageView avatarImg;
	protected ImageView flagImg;
	protected View capturedPiecesView;
	protected RoboTextView timeRemainTxt;
	protected ImageView premiumImg;

	private int side;
	private boolean smallScreen;
	private float density;
	private Resources resources;
	private RoboTextView playerRatingTxt;
	private int whitePlayerTimeColor;
	private int blackPlayerTimeColor;
	private RoboTextView clockIconTxt;
	private LinearLayout clockLayout;
	private RelLayout drawOfferedRelLay;
	private OnClickListener clickListener;

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
		resources = context.getResources();
		density = resources.getDisplayMetrics().density;

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
		int playerTextColor = resources.getColor(R.color.white);

		blackPlayerTimeColor = resources.getColor(R.color.semitransparent_white_65);
		whitePlayerTimeColor = resources.getColor(R.color.semitransparent_black_80);

		int avatarSize;
		if (useSingleLine) {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_size);
		} else {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_big_size);
		}

		boolean hasSoftKeys = AppUtils.hasSoftKeys(((Activity) getContext()).getWindowManager());
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
			clockLayout = new LinearLayout(context);
			timeRemainTxt = new RoboTextView(context);
			clockIconTxt = new RoboTextView(context);

			LayoutParams clockLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams timeRemainParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams clockIconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			clockLayoutParams.addRule(ALIGN_PARENT_RIGHT);
			clockLayoutParams.addRule(CENTER_VERTICAL);

			timeRemainParams.gravity = Gravity.CENTER_VERTICAL;
			clockIconParams.gravity = Gravity.CENTER_VERTICAL;

			clockLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			clockLayout.setMinimumWidth((int) (70 * density));
			clockLayout.setMinimumHeight((int) (35 * density));

			clockIconTxt.setFont(FontsHelper.ICON_FONT);
			float clockIconSize = resources.getDimension(R.dimen.new_tactics_clock_icon_size) / density; // 21;
			clockIconTxt.setTextSize(clockIconSize);
			clockIconTxt.setText(R.string.ic_clock);
			int paddingIcon = (int) (4 * density);
			int paddingIconTop = (int) (2 * density);
			clockIconTxt.setPadding(0, paddingIconTop, paddingIcon, 0);
			clockIconTxt.setVisibility(GONE);


			clockLayout.addView(clockIconTxt, clockIconParams);

			LayoutParams timeLeftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, timeLeftSize);
			timeLeftParams.addRule(ALIGN_PARENT_RIGHT);
			timeLeftParams.addRule(CENTER_VERTICAL);
			timeLeftParams.setMargins((int) (7 * density), 0, 0, 0); // use to set space between captured pieces in single line mode

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
			LayoutParams capturedParams = new LayoutParams(capturedPiecesViewWidth, capturedPiecesViewHeight);
			if (useSingleLine) {
				capturedParams.addRule(LEFT_OF, TIME_LEFT_ID);
				capturedParams.addRule(CENTER_VERTICAL);
				capturedParams.addRule(RIGHT_OF, PLAYER_ID);
			} else {
				capturedParams.addRule(RIGHT_OF, AVATAR_ID);
				capturedParams.addRule(BELOW, PLAYER_ID);
				capturedParams.addRule(ALIGN_BOTTOM, AVATAR_ID);
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

		{ // Draw offered layout
			int buttonsTextSize = 35;
			int panelButtonWidth = (int) (60 * density);
			int panelButtonHeight = (int) (50 * density);
			drawOfferedRelLay = new RelLayout(getContext());
			drawOfferedRelLay.setVisibility(GONE);

			{ // Text label
				RoboTextView drawOfferedTxt = new RoboTextView(getContext());
				drawOfferedTxt.setFont(FontsHelper.BOLD_FONT);
				drawOfferedTxt.setId(DRAW_TEXT_ID);
				drawOfferedTxt.setText(R.string.draw);
				drawOfferedTxt.setTextColor(playerTextColor);
				drawOfferedTxt.setTextSize(playerTextSize);

				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,	ViewGroup.LayoutParams.WRAP_CONTENT);
				params.addRule(CENTER_VERTICAL);
				params.addRule(LEFT_OF, DRAW_DECLINE_ID);

				drawOfferedRelLay.addView(drawOfferedTxt, params);
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
				params.addRule(LEFT_OF, DRAW_ACCEPT_ID);
				params.setMargins((int) (5 * density), 0, 0, 0);

				drawOfferedRelLay.addView(declineDrawBtn, params);
			}

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
				params.addRule(CENTER_VERTICAL);
				params.setMargins((int) (5 * density), 0, 0, 0);

				drawOfferedRelLay.addView(acceptDrawBtn, params);
			}
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,	ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(ALIGN_PARENT_RIGHT);
			addView(drawOfferedRelLay, params);
		}

		{// Set padding
			int padding = (int) resources.getDimension(R.dimen.panel_info_padding_top);
			int paddingRight = (int) (4 * density);
			int paddingLeft = (int) (11 * density);

			if (hasSoftKeys) {
				padding = (int) (3 * density);
			}

			setPadding(paddingLeft, padding, paddingRight, padding);
		}
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
	public void onClick(View view) {  // TODO handle avatar click
		super.onClick(view);
		if (view.getId() == DRAW_DECLINE_ID) {
			clickListener.onClick(view);
		} else if (view.getId() == DRAW_ACCEPT_ID) {
			clickListener.onClick(view);
		}
	}

	@Override
	public void setPlayerName(String playerName) {
		playerTxt.setText(playerName);
	}

	@Override
	public void setPlayerRating(String playerRating) {
		if (playerRating != null)
			playerRatingTxt.setText(Symbol.wrapInPars(playerRating));
	}

	@Override
	public void setPlayerFlag(String country) {
		flagImg.setImageDrawable(AppUtils.getCountryFlagScaled(getContext(), country));
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
		int timeLeftBigPadding = (int) (12 * density);

		if (smallScreen) {
			clockLayout.setPadding(timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding, timeLeftSmallPadding);
		} else {
			clockLayout.setPadding(timeLeftBigPadding, timeLeftSmallPadding, timeLeftBigPadding, timeLeftSmallPadding);
		}
	}

	public void showDrawOfferedView(boolean show) {
		drawOfferedRelLay.setVisibility(show ? VISIBLE : GONE);
		clockLayout.setVisibility(show ? GONE : VISIBLE);
	}

	/**
	 * For some unknown reason findViewById and setOnClickListener directly doesn't work. so use interceptor here
	 * @param listener that will intercept button clicks
	 */
	public void setClickHandler(OnClickListener listener){
		this.clickListener = listener;
	}
}