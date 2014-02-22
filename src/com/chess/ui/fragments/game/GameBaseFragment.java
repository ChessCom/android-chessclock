package com.chess.ui.fragments.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.chess.R;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.tasks.SaveTextFileToSDTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PgnItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.lessons.GameLessonFragment;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.GameLiveFragmentTablet;
import com.chess.ui.fragments.popup_fragments.BasePopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupPromotionFragment;
import com.chess.ui.fragments.tactics.GameTacticsFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.utilities.AppUtils;
import com.chess.widgets.ProfileImageView;
import com.mopub.mobileads.MoPubView;
import com.slidingmenu.lib.SlidingMenu;

import java.io.File;
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
	public static final String DOWNLOADS = "Download";
	protected int AVATAR_SIZE = 48;
	public static final int NOTATION_REWIND_DELAY = 400;

	protected static final String END_GAME_TAG = "end game popup";
	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";
	protected static final String OPTION_SELECTION_TAG = "option select popup";
	protected static final String PROMOTION_SELECTION_TAG = "promotion popup";

	protected static final String GAME_ID = "game_id";
	protected static final String USERNAME = "username";

	protected SimpleDateFormat datePgnFormat = new SimpleDateFormat("yyyy.MM.dd");

	private ChessBoardBaseView boardView;
	protected String endGameTitle;
	protected String endGameReason;
	protected long gameId;
	private View boardFrame;
	protected ProfileImageView topAvatarImg;
	protected ProfileImageView bottomAvatarImg;
	protected LabelsConfig labelsConfig;
	protected ImageDownloaderToListener imageDownloader;
	protected PanelInfoGameView topPanelView;
	protected PanelInfoGameView bottomPanelView;
	protected boolean userPlayWhite;

	protected LayoutInflater inflater;
	private PopupPromotionFragment promotionFragment;
	private PromotionSelectedListener promotionSelectedListener;
	private int promotionFile;
	private int promotionRank;
	private MoPubView mopubRectangleAd;
	protected BoardFace chessBoard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (AppUtils.isNeedFullScreen(getActivity())) {
			getActivityFace().setFullScreen();
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		} else if (AppUtils.isSmallScreen(getActivity())) {
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		}
		super.onCreate(savedInstanceState);

		labelsConfig = new LabelsConfig();
		imageDownloader = new ImageDownloaderToListener(getActivity());

		promotionSelectedListener = new PromotionSelectedListener();
		inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	private boolean fragmentHaveArrows() {
		return !(this instanceof GameLessonFragment || this instanceof GameTacticsFragment);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		boardFrame = view.findViewById(R.id.boardFrame);
		enableSlideMenus(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

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

		getActivityFace().removeOnCloseMenuListener(this);
		releaseScreenLockFlag();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (isNeedToUpgrade() && getMopubRectangleAd() != null) {
			getMopubRectangleAd().destroy();
		}
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
	public boolean isAlive() {
		return getActivity() != null;
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
	public void onGameOver(String title, String reason) {
		endGameTitle = title;
		endGameReason = reason;

		if (!getBoardFace().isSubmit()) {
			initShowAdsFlag();

			View endGamePopupView;
			if (!isNeedToUpgrade()) {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
			} else if (isNeedToUpgrade() && !showAdsForNewMembers) {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
			} else {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
			}

			showGameEndPopup(endGamePopupView, endGameTitle, endGameReason);
			//initUpgradeAndAdWidgets(endGamePopupView); //todo: uncomment in order to show ads
			setBoardToFinishedState();
		}

		if (!getAppData().isUserSawHelpForQuickScroll() && fragmentHaveArrows()) {
			showToastLong(R.string.help_toast_for_quick_in_game_navigation);
			getAppData().setUserSawHelpForQuickScroll(true);
		}
	}

	protected void showGameEndPopup(final View layout, final String title, String reason) {
	}

	@Override
	public void onNotationClicked(int pos) {
	}

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

	protected void sendPGN(PgnItem pgnItem) {
		String filename = pgnItem.getWhitePlayer() + "_vs_" + pgnItem.getBlackPlayer() + "_"
				+ pgnItem.getStartDate() + ".PGN";

		// save file as <white_player>_vs_<black_player>_<game_start_date>.pgn
		String path = DOWNLOADS;
		new SaveTextFileToSDTask(new FileSaveListener(filename), pgnItem.getPgn(), path).executeTask(filename);
	}

	private class FileSaveListener extends ChessLoadUpdateListener<String> {

		private String filename;

		public FileSaveListener(String filename) {

			this.filename = filename;
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);

			showToast(R.string.file_saved_to_download);

			File cacheDir;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				cacheDir = new File(Environment.getExternalStorageDirectory(), DOWNLOADS);
			} else {
				cacheDir = getActivity().getCacheDir();
			}

			File fileToSave = new File(cacheDir, filename);

			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");  // TODO localize
			shareIntent.putExtra(Intent.EXTRA_TEXT, filename);
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToSave));
			shareIntent.setType("file/txt");
			startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_pgn)));
		}
	}

	protected abstract void restoreGame();

	@Override
	public void showChoosePieceDialog(int file, int rank) {
		promotionFile = file;
		promotionRank = rank;

		// if it's a live game fragment
		if ((this instanceof GameLiveFragment || this instanceof GameLiveFragmentTablet) && getAppData().getAutoQueenForLive()) {
			boardView.promote(ChessBoard.QUEEN, promotionFile, promotionRank);
			return;
		}
		// show popup
		if (promotionFragment != null) {
			return;
		}

		promotionFragment = PopupPromotionFragment.createInstance(promotionSelectedListener, getBoardFace().getSide());
		promotionFragment.show(getFragmentManager(), PROMOTION_SELECTION_TAG);
	}

	private class PromotionSelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			promotionFragment.dismiss();
			promotionFragment = null;

			boardView.promote(code, promotionFile, promotionRank);
		}

		@Override
		public void onDialogCanceled() {
			promotionFragment = null;
			boardView.invalidateMe();
		}
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
		if (getFragmentManager() == null) {
			return null;
		} else {
			return (BasePopupDialogFragment) getFragmentManager().findFragmentByTag(END_GAME_TAG);
		}
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
		public static final int LIVE = 0;
		public static final int DAILY = 1;

		private BaseGameItem currentGame;
		private int gameType;
		private long gameId;

		public ShareItem(BaseGameItem currentGame, long gameId, int gameType) {
			this.currentGame = currentGame;
			this.gameType = gameType;
			this.gameId = gameId;
		}


		public String composeMessage() {
			String white = currentGame.getWhiteUsername();
			String black = currentGame.getBlackUsername();
			String shareStr;
			if (gameType == LIVE) {
				shareStr = getString(R.string.live_game_share_str, white, black, gameId);
			} else {
				shareStr = getString(R.string.daily_game_share_str, white, black, gameId);
			}

			return shareStr;
		}

		public String getTitle() {
			String vsStr = getString(R.string.vs);
			return currentGame.getWhiteUsername() + Symbol.SPACE
					+ vsStr + Symbol.SPACE + currentGame.getBlackUsername();
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
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topAvatarImg.setUsername(labelsConfig.topPlayerName, GameBaseFragment.this);
					int width = getTopPanelView().getWidth();
					int height = getTopPanelView().getHeight();
					getTopPanelView().invalidate(0, 0, width, height);

					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomAvatarImg.setUsername(labelsConfig.bottomPlayerName, GameBaseFragment.this);

					width = getBottomPanelView().getWidth();
					height = getBottomPanelView().getHeight();
					getBottomPanelView().invalidate(0, 0, width, height);
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

	protected void initPopupAdWidget(View layout) {
		mopubRectangleAd = (MoPubView) layout.findViewById(R.id.mopubRectangleAd);
	}

	public MoPubView getMopubRectangleAd() {
		return mopubRectangleAd;
	}

	protected void resetInstance() {
		chessBoard = null;
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
