package com.chess.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.ui.interfaces.GameActivityFace;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.views.ChessBoardBaseView;
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
public abstract class GameBaseFragment extends CommonLogicFragment implements GameActivityFace {

	protected static final String GAME_GOES = "*";
	protected static final String WHITE_WINS = "1-0";
	protected static final String BLACK_WINS = "0-1";

	protected static final String END_GAME_TAG = "end game popup";
	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";
	protected SimpleDateFormat datePgnFormat = new SimpleDateFormat("yyyy.MM.dd");

	protected ViewGroup statusBarLay;

	protected CharSequence[] menuOptionsItems;
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
	}

	protected void widgetsInit(View view) {
		statusBarLay = (ViewGroup) view.findViewById(R.id.statusBarLay);

//		whitePlayerLabel = (TextView) view.findViewById.analysisTxt);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.createRectangleAd(getActivity());
		}

		getActivityFace().setTouchModeToSlidingMenu(SlidingMenu.TOUCHMODE_NONE); // don't mess with boardview touches
		invalidateGameScreen();
	}


	protected void setBoardView(ChessBoardBaseView boardView) {
		this.boardView = boardView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// update boardView if boardId has changed
		boardView.updateBoardAndPiecesImgs();
		enableScreenLockTimer();
	}

	@Override
	public void onDestroy() {
		// try to destroy ad here as Mopub team suggested
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.destroyRectangleAd();
		}

		super.onDestroy();
	}

	@Override
	public void turnScreenOff() {
		Activity activity = getActivity();
		if (activity != null)
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public abstract String getWhitePlayerName();

	@Override
	public abstract String getBlackPlayerName();

	protected void updatePlayerDots(boolean whitePlayerMove){
//		if (whitePlayerMove) {
//			whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.player_indicator_white, 0, 0, 0);
//			blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//		} else {
//			whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//			blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.player_indicator_black, 0);
//		}
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
			}else {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
			}

			showGameEndPopup(endGamePopupView, endGameMessage);
			setBoardToFinishedState();
		}
	}

	protected void showGameEndPopup(final View layout, final String message){
	}

	protected void setBoardToFinishedState(){ // TODO implement state conditions logic for board
//		boardView.enableAnalysis(); // TODO recheck logic

//		boardView.setFinished(true);
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

/*
	@Override
	public void switch2Analysis(boolean isAnalysis) {
		showSubmitButtonsLay(false);
		if (isAnalysis) {
//			analysisTxt.setVisibility(View.VISIBLE);
//			whitePlayerLabel.setVisibility(View.INVISIBLE);
//			blackPlayerLabel.setVisibility(View.INVISIBLE);
		} else {
//			analysisTxt.setVisibility(View.INVISIBLE);
//			whitePlayerLabel.setVisibility(View.VISIBLE);
//			blackPlayerLabel.setVisibility(View.VISIBLE);
			restoreGame();
		}
	}
*/

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
		handler.postDelayed(new Runnable() {    // seems to be working that way
			@Override
			public void run() {
				getBoardFace().takeNext();
				invalidateGameScreen();
			}
		},1300);
	}

	public Context getMeContext() {
		return getActivity();
	}

	public void onCheck() {
		showToast(R.string.check);
	}

//	public BoardFace getBoardFace(){
//		return boardView.getBoardFace();
////		return boardView.getBoardFace();
//	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.homePopupBtn) {
			backToHomeFragment();
//			backToHomeActivity();
		} else if (view.getId() == R.id.reviewPopupBtn) {
			dismissDialogs();
		} else if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(getActivity()));
		}
	}

	protected void dismissDialogs() {
		if (getEndPopupDialogFragment() != null) {
			getEndPopupDialogFragment().dismiss();
		}
	}

	protected PopupCustomViewFragment getEndPopupDialogFragment(){
		return (PopupCustomViewFragment) getFragmentManager().findFragmentByTag(END_GAME_TAG);
	}
}
