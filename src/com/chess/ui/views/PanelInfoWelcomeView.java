package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.CapturedPiecesDrawable;
import com.chess.utilities.AppUtils;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.05.13
 * Time: 6:12
 */
public class PanelInfoWelcomeView extends PanelInfoGameView implements View.OnClickListener {

	public static final int AVATAR_ID = 0x00004400;
	public static final int PLAYER_ID = 0x00004401;
	public static final int CAPTURED_ID = 0x00004404;
	public static final int WHAT_IS_TXT_ID = 0x00004405;

	private RoboTextView playerTxt;
	private ImageView avatarImg;
	private View capturedPiecesView;

	private Handler handler;
	private int side;
	private ObjectAnimator flipFirstHalf;

	public PanelInfoWelcomeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	@Override
	public void onCreate(AttributeSet attrs) {
		boolean useSingleLine;
		if (isInEditMode()) {
			return;
		}
		handler = new Handler();
		Context context = getContext();
		Resources resources = context.getResources();
		float density = resources.getDisplayMetrics().density;

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

		int capturedPiecesViewHeight = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_height);
		int capturedPiecesViewWidth = (int) resources.getDimension(R.dimen.panel_info_captured_pieces_width);
		int whatIsTextSize =  (int) (resources.getDimension(R.dimen.panel_info_what_is_size) / density);
		int avatarMarginRight = (int) resources.getDimension(R.dimen.panel_info_avatar_margin_right);

		int padding = (int) resources.getDimension(R.dimen.panel_info_padding_top);
		int paddingRight = (int) (4 * density);
		int paddingLeft = (int) (11 * density);
		setPadding(paddingLeft, padding, paddingRight, padding);

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

			playerParams.addRule(RIGHT_OF, AVATAR_ID);
			playerParams.addRule(CENTER_VERTICAL);

			playerTxt.setTextSize(playerTextSize);
			playerTxt.setTextColor(playerTextColor);
			playerTxt.setFont(FontsHelper.BOLD_FONT);
			playerTxt.setId(PLAYER_ID);
			playerTxt.setPadding((int) (4 * density), 0, 0, 0);
			playerTxt.setMarqueeRepeatLimit(2);
			playerTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);

			addView(playerTxt, playerParams);
		}


		{// add captured drawable view
			capturedPiecesView = new View(context);
			LayoutParams capturedParams = new LayoutParams(capturedPiecesViewWidth, capturedPiecesViewHeight);
			if (useSingleLine) {
				capturedParams.addRule(LEFT_OF, WHAT_IS_TXT_ID);
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

		{// add "What is Chess.com?"
			RoboTextView whatIsTxt = new RoboTextView(context);
			LayoutParams timeLeftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			timeLeftParams.addRule(ALIGN_PARENT_RIGHT);
			timeLeftParams.addRule(CENTER_VERTICAL);
//			timeLeftParams.setMargins((int) (7 * density), 0, 0, 0);

			whatIsTxt.setTextSize(whatIsTextSize);
			whatIsTxt.setTextColor(resources.getColor(R.color.new_square_button_p));
			whatIsTxt.setText(R.string.what_is_chess_com);
			whatIsTxt.setId(WHAT_IS_TXT_ID);
			whatIsTxt.setGravity(Gravity.CENTER_VERTICAL);
			whatIsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_badge, 0);
			whatIsTxt.setCompoundDrawablePadding((int) (7 * density));
			whatIsTxt.setVisibility(GONE);
			whatIsTxt.setPadding(0,0, (int) (15 * density), 0);

			addView(whatIsTxt, timeLeftParams);

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
	public void setPlayerLabel(String playerName) {
		playerTxt.setText(playerName);
	}

	@Override
	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		((CapturedPiecesDrawable) capturedPiecesView.getBackground()).updateCapturedPieces(alivePiecesCountArray);
	}

}
