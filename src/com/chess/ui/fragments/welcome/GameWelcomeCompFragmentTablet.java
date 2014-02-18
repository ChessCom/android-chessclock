package com.chess.ui.fragments.welcome;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.AppConstants;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.interfaces.FragmentTabsFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.widgets.MultiDirectionSlidingDrawer;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.11.13
 * Time: 10:02
 */
public class GameWelcomeCompFragmentTablet extends GameWelcomeCompFragment implements GameCompFace,
		PopupListSelectionFace, AdapterView.OnItemClickListener, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	private TextView whatIsChessComTxt;
	private View loginBtn;
	private View signUpBtn;

	public GameWelcomeCompFragmentTablet() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		setArguments(bundle);
	}

	public static GameWelcomeCompFragmentTablet createInstance(FragmentTabsFace parentFace, CompGameConfig config) {
		GameWelcomeCompFragmentTablet fragment = new GameWelcomeCompFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		fragment.setArguments(bundle);
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.game_welcome_comp_frame, container, false);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.whatIsChessComTxt) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.WELCOME_FRAGMENT);
		} else if (view.getId() == R.id.loginBtn) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.SIGN_IN_FRAGMENT);
		} else if (view.getId() == R.id.signUpBtn) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.SIGN_UP_FRAGMENT);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case WHAT_IS_CHESSCOM:
				parentFace.changeInternalFragment(WelcomeTabsFragment.WELCOME_FRAGMENT);
				break;
			case PLAY_ONLINE_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_play_online), PLAY_ONLINE_TAG);
				break;
			case CHALLENGE_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_friends, getString(R.string.challenge_friend)), CHALLENGE_TAG);
				break;
			case REMATCH_ITEM:
				int mode = compGameConfig.getMode();
				if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE) {
					compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK);
				} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK) {
					compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
				}
				getAppData().setCompGameMode(compGameConfig.getMode());
				parentFace.changeInternalFragment(WelcomeTabsFragment.GAME_FRAGMENT);
				break;
			case TACTICS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_tactics), TACTICS_TAG);
				break;
			case LESSONS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_lessons), LESSONS_TAG);
				break;
			case VIDEOS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_videos), VIDEOS_TAG);
				break;
		}
	}

	@Override
	public void onGameOver(final String title, String reason) {
		long appearDelay = inLandscape() ? 0 : END_GAME_DELAY;
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boolean userWon = !title.equals(getString(R.string.black_wins)); // how it works for Black user? and how it works for human vs. human mode?

				topPanelView.resetPieces();
				bottomPanelView.resetPieces();

				handler.postDelayed(new Runnable() { // delay to show fling animation
					@Override
					public void run() {
						slidingDrawer.animateOpen();
					}
				}, DRAWER_APPEAR_DELAY);

				slidingDrawer.setVisibility(View.VISIBLE);
				fadeDrawerAnimator.reverse();

				// hide game widgets from right side
				topPanelView.setVisibility(View.GONE);
				bottomPanelView.setVisibility(View.GONE);
				loginBtn.setVisibility(View.GONE);
				signUpBtn.setVisibility(View.GONE);
				whatIsChessComTxt.setVisibility(View.GONE);
				controlsView.setVisibility(View.GONE);
				if (inPortrait()) {
					fadeBoardAnimator.start();
				}

				if (userWon) {
					resultTxt.setText(R.string.you_won);
				} else {
					resultTxt.setText(R.string.you_lose);
				}

			}
		}, appearDelay);
	}

	@Override
	public void onDrawerOpened() {
		if (inPortrait()) {
			ChessBoardComp.resetInstance();
			getAppData().clearSavedCompGame();
			notationsView.resetNotations();
			boardView.invalidateMe();

			getView().findViewById(R.id.bottomView).setVisibility(View.GONE);
		}
	}

	@Override
	public void onDrawerClosed() {
		slidingDrawer.setVisibility(View.GONE);
		// show game widgets from right side
		topPanelView.setVisibility(View.VISIBLE);
		bottomPanelView.setVisibility(View.VISIBLE);
		loginBtn.setVisibility(View.VISIBLE);
		signUpBtn.setVisibility(View.VISIBLE);
		whatIsChessComTxt.setVisibility(View.VISIBLE);
		controlsView.setVisibility(View.VISIBLE);

		fadeDrawerAnimator.start();
		if (inPortrait()) {
			fadeBoardAnimator.reverse();
			getView().findViewById(R.id.bottomView).setVisibility(View.VISIBLE);
			whatIsChessComTxt.setVisibility(View.GONE);
		}

		startNewGame();
	}

	@Override
	protected int getStyleForResultTitle() {
		if (inLandscape()) {
			return R.style.ListItem_Tablet;
		} else {
			return R.style.ListItem;
		}
	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);

		whatIsChessComTxt = (TextView) view.findViewById(R.id.whatIsChessComTxt);
		Drawable icon = new IconDrawable(getActivity(), R.string.ic_round_right, R.color.semitransparent_white_75,
				R.dimen.glyph_icon_big);

		whatIsChessComTxt.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.glyph_icon_padding));
		whatIsChessComTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
		whatIsChessComTxt.setOnClickListener(this);

		loginBtn = view.findViewById(R.id.loginBtn);
		signUpBtn = view.findViewById(R.id.signUpBtn);
		loginBtn.setOnClickListener(this);
		signUpBtn.setOnClickListener(this);
	}

	@Override
	public void newGame() {
		startNewGame();
	}

}
