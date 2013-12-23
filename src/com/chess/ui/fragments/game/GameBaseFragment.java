package com.chess.ui.fragments.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.model.BaseGameItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.popup_fragments.BasePopupDialogFragment;
import com.chess.ui.interfaces.game_ui.GameFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;
import com.slidingmenu.lib.SlidingMenu;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.13
 * Time: 13:46
 */
public abstract class GameBaseFragment extends LiveBaseFragment implements GameFace, SlidingMenu.OnClosedListener {

	protected static final String GAME_GOES = "*";
	protected static final String WHITE_WINS = "1-0";
	protected static final String BLACK_WINS = "0-1";
	private static final long TOUCH_MODE_RECONFIRM_DELAY = 300;
	private static final long SLIDE_TOUCH_DISABLE_DELAY = 500;
	protected int AVATAR_SIZE = 48;
	public static final int NOTATION_REWIND_DELAY = 400;

	protected static final String END_GAME_TAG = "end game popup";
	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";
	protected static final String OPTION_SELECTION_TAG = "option select popup";

	protected static final String GAME_ID = "game_id";
	protected static final String USERNAME = "username";

	protected SimpleDateFormat datePgnFormat = new SimpleDateFormat("yyyy.MM.dd");

	private ChessBoardBaseView boardView;
	protected View endGamePopupView;
	protected String endGameMessage;
	protected long gameId;
	private View boardFrame;
	protected ImageView topAvatarImg;
	protected ImageView bottomAvatarImg;
	protected LabelsConfig labelsConfig;
	protected ImageDownloaderToListener imageDownloader;
	protected PanelInfoGameView topPanelView;
	protected PanelInfoGameView bottomPanelView;
	protected boolean userPlayWhite;

	protected LinearLayout mopubAdLayout;
	private MoPubView moPubBannerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (AppUtils.isNeedFullScreen(getActivity())) {
			getActivityFace().setFullScreen();
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		} else if (AppUtils.noNeedTitleBar(getActivity())) {
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		}
		super.onCreate(savedInstanceState);

		labelsConfig = new LabelsConfig();
		imageDownloader = new ImageDownloaderToListener(getActivity());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		boardFrame = view.findViewById(R.id.boardFrame);
		enableSlideMenus(false);
	}

	protected void initUpgradeAndAdWidgets(View view) {
		if (!AppUtils.isNeedToUpgrade(getActivity())) {
			view.findViewById(R.id.bannerUpgradeView).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.bannerUpgradeView).setVisibility(View.VISIBLE);

			Button upgradeBtn = (Button) view.findViewById(R.id.upgradeBtn);
			upgradeBtn.setOnClickListener(this);

			mopubAdLayout = (LinearLayout) view.findViewById(R.id.mopubAdLayout);
			moPubBannerView = MopubHelper.showBannerAd(upgradeBtn, mopubAdLayout, getActivity());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (moPubBannerView != null) {
			moPubBannerView.destroy();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			MopubHelper.createRectangleAd(getActivity());
//		}
		Log.d("LccLog-GameLiveFragment", " BASE FRAGMENT invalidateGameScreen gameId = " + gameId);

		if (gameId != 0) {
			invalidateGameScreen();
		}
	}


	protected void setBoardView(ChessBoardBaseView boardView) {
		this.boardView = boardView;
	}

	@Override
	public void onResume() {
		super.onResume();

		// update boardView if boardId has changed
		boardView.updateBoardAndPiecesImgs();
		enableScreenLockTimer();
		getActivityFace().addOnCloseMenuListener(this);

		// update player font color
		if (topPanelView != null) {
			topPanelView.setLabelsTextColor(themeFontColorStateList.getDefaultColor());
		}
		if (bottomPanelView != null) {
			bottomPanelView.setLabelsTextColor(themeFontColorStateList.getDefaultColor());
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				enableSlideMenus(false);
				updateSlidingMenuState();
			}
		}, SLIDE_TOUCH_DISABLE_DELAY);
	}

	@Override
	public void onPause() {
		super.onPause();

		boardView.releaseRunnable();
		boardView.releaseBitmaps();

		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.destroyRectangleAd();
		}

		getActivityFace().removeOnCloseMenuListener(this);
		releaseScreenLockFlag();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(GAME_ID, gameId);
	}

	@Override
	public void releaseScreenLockFlag() {

		Activity activity = getActivity();
		if (activity != null) {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public abstract String getWhitePlayerName();

	@Override
	public abstract String getBlackPlayerName();

	@Override
	public boolean isUserColorWhite() {
		return labelsConfig.userSide == ChessBoard.WHITE_SIDE;
	}

	@Override
	public boolean isObservingMode() {
		return false;
	}

	@Override
	public boolean isUserAbleToMove(int color) {
		if (!currentGameExist()) {
			return false;
		}
		boolean isUserColor;
		if (isUserColorWhite()) {
			isUserColor = color == ChessBoard.WHITE_SIDE;
		} else {
			isUserColor = color == ChessBoard.BLACK_SIDE;
		}
		return isUserColor || getBoardFace().isAnalysis();
	}

	protected void enableScreenLockTimer() {
		// set touches listener to chessboard. If user don't do any moves, screen will automatically turn off after WAKE_SCREEN_TIMEOUT time
		boardView.enableTouchTimer();
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onGameOver(String message, boolean need2Finish) {
		endGameMessage = message;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (!getBoardFace().isSubmit()) {

			if (!AppUtils.isNeedToUpgrade(getActivity())) {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
			} else {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
			}

			showGameEndPopup(endGamePopupView, endGameMessage);
			//initUpgradeAndAdWidgets(endGamePopupView); //todo: uncomment in order to show ads
			setBoardToFinishedState();
		}
	}

	protected void showGameEndPopup(final View layout, final String message) {
	}

	@Override
	public void onNotationClicked(int pos) {}

	@Override
	public void updateParentView() {
		if (boardFrame != null) { // shouldn't be null...
			boardFrame.invalidate();
			getActivityFace().updateActionBarBackImage(); // for staunton theme we have nice overdraw effect :D
		}
	}

	protected void setBoardToFinishedState() { // TODO implement state conditions logic for board
		getBoardFace().setFinished(true);

		getSoundPlayer().playGameEnd();
	}

	protected void sendPGN(String message) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");  // TODO localize
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);
		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail_)));
	}

	protected abstract void restoreGame();

	@Override
	public void showChoosePieceDialog(final int col, final int row) {
		new AlertDialog.Builder(getActivity()) // TODO replace with FragmentDialog
				.setTitle(getString(R.string.choose_a_piece)) // add localized strings
				.setItems(getResources().getStringArray(R.array.promotion_options),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (which == 4) {
									boardView.invalidate();
									return;
								}
								boardView.promote(4 - which, col, row);
							}
						}).setCancelable(false)
				.create().show();
	}

	protected void playLastMoveAnimation() {
		if (getActivity() == null) {
			return;
		}

		Move move = getBoardFace().getNextMove();
		if (move == null) {
			return;
		}
		boardView.setMoveAnimator(move, true);
		getBoardFace().takeNext();

		invalidateGameScreen();

	}

	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onCheck() {
	}

	protected void dismissEndGameDialog() {
		if (getEndPopupDialogFragment() != null) {
			getEndPopupDialogFragment().dismiss();
		}
	}

	protected BasePopupDialogFragment getEndPopupDialogFragment() {
		return (BasePopupDialogFragment) getFragmentManager().findFragmentByTag(END_GAME_TAG);
	}

	@Override
	public void onClosed() {
		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);
		// reconfirm bcz of glitches
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);
			}
		}, TOUCH_MODE_RECONFIRM_DELAY);
	}

	public class ShareItem {

		private final String gameLink;
		private BaseGameItem currentGame;
		private long gameId;
		private String gameType;

		public ShareItem(BaseGameItem currentGame, long gameId, String gameType) {
			this.currentGame = currentGame;
			this.gameId = gameId;
			this.gameType = gameType;
			if (gameType.equals(getString(R.string.live))) {
				gameLink = RestHelper.getInstance().getLiveGameLink(gameId);
			} else {
				gameLink = RestHelper.getInstance().getOnlineGameLink(gameId);
			}
		}


		public String composeMessage() {
			String vsStr = getString(R.string.vs);
			String space = Symbol.SPACE;
			return currentGame.getWhiteUsername() + space + vsStr + space + currentGame.getBlackUsername()
					+ " - " + gameType + space + getString(R.string.chess) + space
					+ getString(R.string.via_chesscom) + space
					+ gameLink;
		}

		public String getTitle() {
			String vsStr = getString(R.string.vs);
			return "Chess: " + currentGame.getWhiteUsername() + Symbol.SPACE
					+ vsStr + Symbol.SPACE + currentGame.getBlackUsername(); // TODO adjust i18n
		}
	}

	protected class ImageUpdateListener extends ImageReadyListenerLight {

		public static final int TOP_AVATAR = 0;
		public static final int BOTTOM_AVATAR = 1;
		private int code;

		public ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null/* || bitmap == null*/) {
				Log.e("TEST", "ImageLoader bitmap == null");
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					getTopPanelView().invalidate();

					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					getBottomPanelView().invalidate();
					break;
			}
		}
	}

	protected View getTopPanelView() {
		return topPanelView;
	}

	protected View getBottomPanelView() {
		return bottomPanelView;
	}

	public static class LabelsConfig {

		public BoardAvatarDrawable topAvatar;
		public BoardAvatarDrawable bottomAvatar;
		public String topPlayerName;
		public String bottomPlayerName;
		public String topPlayerRating;
		public String bottomPlayerRating;
		public String topPlayerAvatar;
		public String bottomPlayerAvatar;
		public String topPlayerTime;
		public String bottomPlayerTime;
		public String topPlayerCountry;
		public String bottomPlayerCountry;
		public int topPlayerPremiumStatus;
		public int bottomPlayerPremiumStatus;
		public int userSide;

		public int getOpponentSide() {
			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}
	}

}
