package com.chess.ui.fragments.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.model.BaseGameItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.game_ui.GameFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;
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
	protected int AVATAR_SIZE = 48;

	protected static final String END_GAME_TAG = "end game popup";
	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";
	protected static final String OPTION_SELECTION_TAG = "option select popup";

	protected SimpleDateFormat datePgnFormat = new SimpleDateFormat("yyyy.MM.dd");

	private ChessBoardBaseView boardView;
	protected View endGamePopupView;
	protected String endGameMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (AppUtils.needFullScreen(getActivity())) {
			getActivityFace().setFullScreen();
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		} else if (AppUtils.noNeedTitleBar(getActivity())) {
//			getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE); // TODO set via another method
			if (savedInstanceState == null) {
				savedInstanceState = new Bundle();
			}
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		}
		super.onCreate(savedInstanceState);

		// TODO check logic , add manual handling flag
		AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		switch( audio.getRingerMode() ){
			case AudioManager.RINGER_MODE_NORMAL:
				getAppData().setPlaySounds(getActivity(), true);
				break;
			case AudioManager.RINGER_MODE_SILENT:
				getAppData().setPlaySounds(getActivity(), false);
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				getAppData().setPlaySounds(getActivity(), false);
				break;
		}

		getActivityFace().addOnCloseMenuListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			MopubHelper.createRectangleAd(getActivity());
//		}

		invalidateGameScreen();
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
	}

	@Override
	public void onPause() {
		super.onPause();

		boardView.releaseRunnable();

		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.destroyRectangleAd();
		}

		getActivityFace().removeOnCloseMenuListener(this);
	}

	@Override
	public void turnScreenOff() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public abstract String getWhitePlayerName();

	@Override
	public abstract String getBlackPlayerName();

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

//			if (!AppUtils.isNeedToUpgrade(getActivity())) {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
//			}else {
//				endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
//			}

			showGameEndPopup(endGamePopupView, endGameMessage);
			setBoardToFinishedState();
		}
	}

	protected void showGameEndPopup(final View layout, final String message){
	}

	protected void setBoardToFinishedState(){ // TODO implement state conditions logic for board
//		boardView.enableAnalysis(); // TODO recheck logic

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
		Move move = getBoardFace().getNextMove();
		if (move == null) {
			return;
		}
		boardView.setMoveAnimator(move, true);
		getBoardFace().takeNext();

		if (getActivity() != null)
			invalidateGameScreen();

	}

	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onCheck() {
		showToast(R.string.check);
	}

	protected void dismissDialogs() {
		if (getEndPopupDialogFragment() != null) {
			getEndPopupDialogFragment().dismiss();
		}
	}

	protected PopupCustomViewFragment getEndPopupDialogFragment(){
		return (PopupCustomViewFragment) getFragmentManager().findFragmentByTag(END_GAME_TAG);
	}

	@Override
	public void onClosed() {
		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE);
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
				gameLink = RestHelper.getLiveGameLink(gameId);
			} else {
				gameLink =  RestHelper.getOnlineGameLink(gameId);
			}
		}


		public String composeMessage() {
			String vsStr = getString(R.string.vs);
			String space = StaticData.SYMBOL_SPACE;
			return currentGame.getWhiteUsername() + space + vsStr + space + currentGame.getBlackUsername()
					+ " - " +  gameType  + space + getString(R.string.chess) + space
					+ getString(R.string.via_chesscom) + space
					+ gameLink;
		}

		public String getTitle() {
			String vsStr = getString(R.string.vs);
			return "Chess: " + currentGame.getWhiteUsername() + StaticData.SYMBOL_SPACE
					+ vsStr + StaticData.SYMBOL_SPACE + currentGame.getBlackUsername(); // TODO adjust i18n
		}
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
