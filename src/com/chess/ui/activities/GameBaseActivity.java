package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.model.GameItem;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardToGameActivityFace;
import com.chess.ui.interfaces.GameActivityFace;
import com.chess.ui.views.ChessBoardBaseView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

/**
 * GameBaseActivity class
 *
 * @author alien_roger
 * @created at: 05.03.12 21:18
 */
public abstract class GameBaseActivity extends LiveBaseActivity implements
		BoardToGameActivityFace, GameActivityFace {


	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";


	protected TextView whitePlayerLabel;
	protected TextView blackPlayerLabel;
	protected TextView thinking;

	protected boolean chat;

	protected TextView analysisTxt;
	protected ViewGroup statusBarLay;

	protected TextView endOfGameMessage;
	protected PopupCustomViewFragment endPopupFragment;


	protected CharSequence[] menuOptionsItems;
	protected GamePanelView gamePanelView;
	protected boolean isWhitePlayerMove = true;
	protected boolean initTimer = true;
	protected boolean userPlayWhite = true;
	private ChessBoardBaseView boardView;


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

		thinking = (TextView) findViewById(R.id.thinking);
		analysisTxt = (TextView) findViewById(R.id.analysisTxt);
		endOfGameMessage = (TextView) findViewById(R.id.endOfGameMessage);
		gamePanelView = (GamePanelView) findViewById(R.id.gamePanelView);
	}

	public void init() {
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (MopubHelper.isShowAds(this)) {
			MopubHelper.createRectangleAd(this);
		}

		invalidateGameScreen();
	}

	protected void setBoardView(ChessBoardBaseView boardView) {
		this.boardView = boardView;
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return boardView.getBoardFace();
	}

	@Override
	protected void onResume() {
		super.onResume();

		enableScreenLockTimer();
	}


	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void onDestroy() {
		// try to destroy ad here as Mopub team suggested
		if (MopubHelper.isShowAds(this)) {
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

	public abstract void onGameRefresh(GameItem newGame);

	protected abstract void onGameEndMsgReceived();

	protected void enableScreenLockTimer() {
		// set touches listener to chessboard. If user don't do any moves, screen will automatically turn off after WAKE_SCREEN_TIMEOUT time
		boardView.enableTouchTimer();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onGameOver(String message, boolean need2Finish) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout;
		if (!MopubHelper.isShowAds(this)) {
			layout = inflater.inflate(R.layout.popup_end_game, null, false);
		}else {
			layout = inflater.inflate(R.layout.popup_end_game_free, null, false);
		}

		showGameEndPopup(layout, message);
	}

	protected void showGameEndPopup(final View layout, final String message){
	}

	protected void changePlayersLabelColors() {
		int hintColor = getResources().getColor(R.color.hint_text);
		int whiteColor = getResources().getColor(R.color.white);

		int topPlayerColor;

		if (isWhitePlayerMove) {
			topPlayerColor = userPlayWhite ? hintColor : whiteColor;
		} else {
			topPlayerColor = userPlayWhite ? whiteColor : hintColor;
		}

		whitePlayerLabel.setTextColor(topPlayerColor);
		blackPlayerLabel.setTextColor(topPlayerColor);

		boolean activate = isWhitePlayerMove ? userPlayWhite : !userPlayWhite;

		gamePanelView.activatePlayerTimer(!activate, activate); // bottom is always current user
		gamePanelView.activatePlayerTimer(activate, activate);

		initTimer = false;
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
				.setTitle(getString(R.string.choose_a_piece)) // TODO add strings
				.setItems(new String[]{"Queen", "Rook", "Bishop", "Knight", "Cancel"},
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1300);
					boardView.getBoardFace().takeNext();
					update.sendEmptyMessage(0);
				} catch (Exception ignored) {
				}
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					invalidateGameScreen();
					boardView.invalidate();
				}
			};
		}).start();
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
		} else if (view.getId() == R.id.rematchPopupBtn) {
			// TODO send rematch request
		} else if (view.getId() == R.id.reviewPopupBtn) {
			endPopupFragment.dismiss();
		}
	}



}
