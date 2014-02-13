package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.ProfileImageView;
import com.chess.widgets.RoboTextView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.05.13
 * Time: 6:12
 */
public class PanelInfoWelcomeView extends PanelInfoGameView implements View.OnClickListener {

	private int side;

	public PanelInfoWelcomeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	@Override
	protected void onCreate(AttributeSet attrs) {
		boolean useSingleLine;
		Context context = getContext();
		if (isInEditMode() || context == null) {
			return;
		}
		Resources resources = context.getResources();
		float density = resources.getDisplayMetrics().density;

		handler = new Handler();
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

		int playerTextSize = (int) (resources.getDimension(R.dimen.panel_info_welcome_player_text_size) / density);
		int playerTextColor = resources.getColor(R.color.white);

		int avatarSize;
		if (useSingleLine) {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_size);
		} else {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_big_size);
		}

		boolean hasSoftKeys = AppUtils.isNexus4Kind(context);
		if (hasSoftKeys) {
			avatarSize = (int) resources.getDimension(R.dimen.panel_info_avatar_medium_size);
		}

		int capturedPiecesViewHeight = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_height);
		int capturedPiecesViewWidth = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_width);
		int avatarMarginRight = (int) resources.getDimension(R.dimen.panel_info_avatar_margin_right);

		 // set padding
		int paddingTop = (int) resources.getDimension(R.dimen.panel_info_padding_top);
		int paddingLeft = (int) (11 * density);

		if (hasSoftKeys) {
			paddingTop = (int) (3 * density);
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
			avatarImg.setVisibility(GONE);
		}

		{// add player name
			playerTxt = new RoboTextView(context);
			LayoutParams playerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			playerParams.addRule(RIGHT_OF, AVATAR_ID);
			if (API_17) {
				playerParams.addRule(END_OF, AVATAR_ID);
			}
			playerParams.addRule(ALIGN_TOP, AVATAR_ID);
			int playerTopMargin = resources.getDimensionPixelSize(R.dimen.panel_info_welcome_player_top_margin);
			playerParams.setMargins(0, -playerTopMargin, 0, 0);

			playerTxt.setTextSize(playerTextSize);
			playerTxt.setTextColor(playerTextColor);
			playerTxt.setFont(FontsHelper.BOLD_FONT);
			playerTxt.setId(PLAYER_ID);
			if (useLtr) {
				playerTxt.setPadding((int) (4 * density), 0, 0, 0);
			} else {
				playerTxt.setPadding(0, 0, (int) (4 * density), 0);
			}
			playerTxt.setMarqueeRepeatLimit(2);
			playerTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);

			addView(playerTxt, playerParams);
			playerTxt.setVisibility(GONE);
		}


		{// add captured drawable view
			capturedPiecesView = new View(context);
			LayoutParams capturedParams = new LayoutParams(capturedPiecesViewWidth, capturedPiecesViewHeight);
			if (useSingleLine) {
				capturedParams.addRule(CENTER_VERTICAL);
				capturedParams.addRule(RIGHT_OF, PLAYER_ID);
				if (API_17) {
					capturedParams.addRule(END_OF, PLAYER_ID);
				}
			} else {
				capturedParams.addRule(RIGHT_OF, AVATAR_ID);
				if (API_17) {
					capturedParams.addRule(END_OF, AVATAR_ID);
				}
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

//		{ // Thinking View
//			RoboTextView thinkingTxt = new RoboTextView(getContext());
//			thinkingTxt.setFont(FontsHelper.BOLD_FONT);
//			thinkingTxt.setTextSize(playerTextSize);
//			thinkingTxt.setText(R.string.thinking_);
//			thinkingTxt.setTextColor(Color.WHITE);
//			thinkingTxt.setBackgroundResource(R.color.glassy_button);
//			thinkingTxt.setVisibility(GONE);
//			thinkingTxt.setGravity(Gravity.CENTER);
//
//			RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//			addView(thinkingTxt, params);
//		}
	}

	@Override
	public void setSide(int side) {
		this.side = side;

		if (avatarImg.getDrawable() != null) { // change avatar border
			((BoardAvatarDrawable) avatarImg.getDrawable()).setSide(side);
		}

		// change pieces color
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).setSide(side);

		invalidate();
	}

	@Override
	public int getSide() {
		return side;
	}

	@Override
	public void onClick(View view) {  // TODO handle avatar click

	}

	@Override
	public void setPlayerName(String playerName) {
		playerTxt.setText(playerName);
	}

	@Override
	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).updateCapturedPieces(alivePiecesCountArray);
	}

	@Override
	public void resetPieces() {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).dropPieces();
	}

	@Override
	public void showThinkingView(boolean show) {
		if (show) {
			playerTxt.setText(getContext().getString(R.string.computer) + Symbol.SPACE +
					getContext().getString(R.string.thinking));
			handler.postDelayed(thinkingDotTask, THINKING_DOT_DELAY);
		} else {
			playerTxt.setText(R.string.computer);
			handler.removeCallbacks(thinkingDotTask);
		}
	}

	private int dotsAdded;
	protected Runnable thinkingDotTask = new Runnable() {
		@Override
		public void run() {
			if (dotsAdded++ < 3) {
				playerTxt.setText(playerTxt.getText() + Symbol.DOT.trim());
			} else {
				dotsAdded = 0;
				playerTxt.setText(getContext().getString(R.string.computer) + Symbol.SPACE +
						getContext().getString(R.string.thinking));
			}
			handler.postDelayed(thinkingDotTask, THINKING_DOT_DELAY);
		}
	};
}
