package com.chess.ui.fragments.daily;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.daily_games.DailyChallengeItem;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.widgets.ProfileImageView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.01.14
 * Time: 8:40
 */
public class DailyInviteFragmentTablet extends DailyInviteFragment implements ViewTreeObserver.OnGlobalLayoutListener {


	public DailyInviteFragmentTablet() { }

	public static DailyInviteFragmentTablet createInstance(DailyChallengeItem.Data challengeItem) {
		DailyInviteFragmentTablet fragment = new DailyInviteFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putParcelable(GAME_ITEM, challengeItem);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onGlobalLayout() {
		View view = getView();
		if (view == null || view.getViewTreeObserver() == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		} else {
			view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}

		Resources resources = getResources();
		{ // invite overlay setup
			View inviteOverlay = view.findViewById(R.id.inviteOverlay);

			// let's make it to match board properties
			// it should be 2 squares inset from top of border and 4 squares tall + 1 squares from sides
			View boardview = getView().findViewById(R.id.boardview);
			int boardWidth = boardview.getWidth();
			int squareSize = boardWidth / 8; // one square size
			int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
			// now we add few pixel to compensate shadow addition
			int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
			borderOffset += shadowOffset;
			int overlayHeight = squareSize * 3 + borderOffset + shadowOffset;

			int popupWidth = squareSize * 5 + shadowOffset * 2 + borderOffset;  // for tablets we need more width
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(popupWidth, overlayHeight);
			int topMargin = (int) (squareSize * 2.5f + borderOffset - shadowOffset * 2);

			params.setMargins((int) (squareSize * 1.5f - shadowOffset), topMargin, squareSize - borderOffset, 0);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);
			inviteOverlay.setLayoutParams(params);
			inviteOverlay.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void widgetsInit(View view) {
		if (inPortrait()) {
			super.widgetsInit(view);
			return;
		}
		view.getViewTreeObserver().addOnGlobalLayoutListener(this);

		inviteDetails1Txt = (TextView) view.findViewById(R.id.inviteDetails1Txt);
		inviteTitleTxt = (TextView) view.findViewById(R.id.inviteTitleTxt);

		controlsView = (ControlsDailyView) view.findViewById(R.id.controlsView);

		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		controlsView.enableChatButton(true);
		controlsView.showSubmitButtons(true);
		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsView);

		boardView.setGameFace(gameFaceHelper);
		boardView.lockBoard(true);
	}
}
