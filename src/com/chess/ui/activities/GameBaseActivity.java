package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardToGameActivityFace;
import com.chess.ui.interfaces.GameActivityFace;
import com.chess.ui.views.ChessBoardBaseView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

import java.text.SimpleDateFormat;

/**
 * GameBaseActivity class
 *
 * @author alien_roger
 * @created at: 05.03.12 21:18
 */
public abstract class GameBaseActivity extends LiveBaseActivity implements
		BoardToGameActivityFace, GameActivityFace {

	protected static final String GAME_GOES = "*";
	protected static final String WHITE_WINS = "1-0";
	protected static final String BLACK_WINS = "0-1";

	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";
	protected SimpleDateFormat datePgnFormat = new SimpleDateFormat("yyyy.MM.dd");


	protected TextView whitePlayerLabel;
	protected TextView blackPlayerLabel;

	protected boolean chat;

	protected TextView analysisTxt;
	protected ViewGroup statusBarLay;

	protected PopupCustomViewFragment endPopupFragment;


	protected CharSequence[] menuOptionsItems;
	protected GamePanelView gamePanelView;
	protected boolean isWhitePlayerMove = true;
	protected boolean initTimer = true;
	protected boolean userPlayWhite = true;
	private ChessBoardBaseView boardView;
	protected View endGamePopupView;
	protected String endGameMessage;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (AppUtils.needFullScreen(this)) {
			setFullScreen();
			savedInstanceState = new Bundle();
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		} else if (AppUtils.noNeedTitleBar(this)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			savedInstanceState = new Bundle();
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		}
		super.onCreate(savedInstanceState);
	}

	protected void widgetsInit() {
		statusBarLay = (ViewGroup) findViewById(R.id.statusBarLay);

		whitePlayerLabel = (TextView) findViewById(R.id.white);
		blackPlayerLabel = (TextView) findViewById(R.id.black);
		whitePlayerLabel.setSelected(true);
		blackPlayerLabel.setSelected(true);


		analysisTxt = (TextView) findViewById(R.id.analysisTxt);
		gamePanelView = (GamePanelView) findViewById(R.id.gamePanelView);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (AppUtils.isNeedToUpgrade(this)) {
			MopubHelper.createRectangleAd(this);
		}

		invalidateGameScreen();
	}

	protected void setBoardView(ChessBoardBaseView boardView) {
		this.boardView = boardView;
	}

    // todo: remove soon
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return boardView.getBoardFace();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// update boardView if boardId has changed
		boardView.updateBoardAndPiecesImgs();
		enableScreenLockTimer();
	}

	protected void onDestroy() {
		// try to destroy ad here as Mopub team suggested
		if (AppUtils.isNeedToUpgrade(this)) {
			MopubHelper.destroyRectangleAd();
		}

		super.onDestroy();
	}

	@Override
	public void turnScreenOff() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public abstract String getWhitePlayerName();

	@Override
	public abstract String getBlackPlayerName();

	protected void setWhitePlayerDot(boolean whitePlayerMove){
		if (whitePlayerMove) {
			whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.player_indicator_white, 0, 0, 0);
			blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		} else {
			whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.player_indicator_black, 0);
		}
	}

	protected void enableScreenLockTimer() {
		// set touches listener to chessboard. If user don't do any moves, screen will automatically turn off after WAKE_SCREEN_TIMEOUT time
		boardView.enableTouchTimer();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onGameOver(String message, boolean need2Finish) {
		endGameMessage = message;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		if (!AppUtils.isNeedToUpgrade(this)) {
			endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
		}else {
			endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
		}

		if(getBoardFace().isSubmit()){
			showToast(R.string.checkmate);
		} else {
			showGameEndPopup(endGamePopupView, endGameMessage);
			setBoardToFinishedState();
		}
	}

	protected void showGameEndPopup(final View layout, final String message){
	}

	protected void setBoardToFinishedState(){ // TODO implement state conditions logic for board
		showSubmitButtonsLay(false);
		boardView.enableAnalysis();

		boardView.setFinished(true);
		//gamePanelView.showBottomPart(false);
		getSoundPlayer().playGameEnd();
	}

	protected void sendPGN(String message) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");  // TODO localize
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);
		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		showSubmitButtonsLay(false);
		if (isAnalysis) {
			analysisTxt.setVisibility(View.VISIBLE);
			whitePlayerLabel.setVisibility(View.INVISIBLE);
			blackPlayerLabel.setVisibility(View.INVISIBLE);
		} else {
			analysisTxt.setVisibility(View.INVISIBLE);
			whitePlayerLabel.setVisibility(View.VISIBLE);
			blackPlayerLabel.setVisibility(View.VISIBLE);
			restoreGame();
		}
	}

	@Override
	public void switch2Chat() {
		chat = true;
	}

	protected abstract void restoreGame();

	@Override
	public void showChoosePieceDialog(final int col, final int row) {
		new AlertDialog.Builder(this)
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
		handler.postDelayed(new Runnable() {    // seems to be workign that way
			@Override
			public void run() {
				getBoardFace().takeNext();
				invalidateGameScreen();
			}
		},1300);
	}

	public Context getMeContext() {
		return this;
	}

	public void onCheck() {
		showToast(R.string.check);
	}

	protected BoardFace getBoardFace(){
		return boardView.getBoardFace();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.homePopupBtn) {
			backToHomeActivity();
		} else if (view.getId() == R.id.reviewPopupBtn) {
			endPopupFragment.dismiss();
		} else if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));
		}
	}
}
