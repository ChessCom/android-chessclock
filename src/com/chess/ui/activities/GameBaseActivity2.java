package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.model.GameItem;
import com.chess.ui.interfaces.BoardToGameActivityFace2;
import com.chess.ui.interfaces.GameActivityFace2;
import com.chess.ui.views.ChessBoardBaseView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

import java.util.Timer;

/**
 * GameBaseActivity class
 *
 * @author alien_roger
 * @created at: 05.03.12 21:18
 */
public abstract class GameBaseActivity2 extends LiveBaseActivity2 implements View.OnClickListener,
		BoardToGameActivityFace2, GameActivityFace2, LccEventListener {

//	protected final static int DIALOG_DRAW_OFFER = 4;
//	protected final static int DIALOG_ABORT_OR_RESIGN = 5;
	protected static final String DRAW_OFFER_RECEIVED_TAG = "draw offer message received";
	protected static final String ABORT_GAME_TAG = "abort or resign game";


//	protected ChessBoardView boardView;
	protected TextView whitePlayerLabel;
	protected TextView blackPlayerLabel;
	protected TextView thinking;

	protected Timer onlineGameUpdate = null;
	protected boolean isMoveNav;
	protected boolean chat;

	protected GameItem newGame;

	protected TextView analysisTxt;
	protected ViewGroup statusBarLay;

	protected AlertDialog adPopup;
	protected TextView endOfGameMessage;

	protected CharSequence[] menuOptionsItems;
	protected GamePanelView gamePanelView;
	protected boolean isWhitePlayerMove = true;
	protected boolean initTimer = true;
	protected boolean userPlayWhite = true;
	private ChessBoardBaseView boardView;

	private boolean need2Finish;
	private String whiteTimer;
	private String blackTimer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (AppUtils.needFullScreen(this)) {
			setFullScreen();
			savedInstanceState = new Bundle();
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
		}else if (AppUtils.noNeedTitleBar(this)) {
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
        /*if (MobclixHelper.isShowAds(mainApp)) {
              setRectangleAdview(new MobclixIABRectangleMAdView(this));
              getRectangleAdview().setRefreshTime(-1);
              getRectangleAdview().addMobclixAdViewListener(new MobclixAdViewListenerImpl(true, mainApp));
              mainApp.setForceRectangleAd(false);
          }*/

        if (MopubHelper.isShowAds(this)) {
            MopubHelper.createRectangleAd(this);
        }

//		update(CALLBACK_REPAINT_UI);
        invalidateGameScreen();
    }


	protected void setBoardView(ChessBoardBaseView boardView){
		this.boardView = boardView;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		if (isMoveNav) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					openOptionsMenu();
				}
			}, 10);
			isMoveNav = false;
		}
		super.onOptionsMenuClosed(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*if (MobclixHelper.isShowAds(mainApp) && adViewWrapper != null && getRectangleAdview() != null) {
			adViewWrapper.addView(getRectangleAdview());
			if (mainApp.isForceRectangleAd()) {
				getRectangleAdview().getAd();
			}
		}*/


		//MobclixHelper.pauseAdview(mainApp.getBannerAdview(), mainApp);

		enableScreenLockTimer();
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return boardView.getBoardFace();
	}

	@Override
	protected void onPause() {
		super.onPause();

		/*if (adViewWrapper != null && getRectangleAdview() != null) {
			getRectangleAdview().cancelAd();
			adViewWrapper.removeView(getRectangleAdview());
		}*/
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

    protected void enableScreenLockTimer() {
        // set touches listener to chessboard. If user don't do any moves, screen will automatically turn off afer WAKE_SCREEN_TIMEOUT time
        boardView.enableTouchTimer();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

	public abstract void onGameRefresh();


	protected abstract void onGameEndMsgReceived();

	@Override
	public void onGameOver(String message, boolean need2Finish) {
		showToast(message);
		this.need2Finish = need2Finish;
		if (!MopubHelper.isShowAds(this)) {
			return;
		}

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.ad_popup2, (ViewGroup) findViewById(R.id.layout_root));
		showGameEndPopup(layout, message);

		Button ok = (Button) layout.findViewById(R.id.home);
		ok.setText(getString(R.string.okay));
		ok.setOnClickListener(this);
		ok.setVisibility(View.VISIBLE);
	}

	protected void showGameEndPopup(final View layout, final String message) {
		if (!MopubHelper.isShowAds(this)) {
			return;
		}

		if (adPopup != null) {
			adPopup.dismiss();
			adPopup = null;
		}

		/*if (adViewWrapper != null && getRectangleAdview() != null) {
			adViewWrapper.removeView(getRectangleAdview());
		}*/
		/*adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		System.out.println("MOBCLIX: GET WRAPPER " + adViewWrapper);
		adViewWrapper.addView(getRectangleAdview());

		adViewWrapper.setVisibility(View.VISIBLE);
		//showGameEndAds(adViewWrapper);*/

		TextView endOfGameMessagePopup = (TextView) layout.findViewById(R.id.endOfGameMessage);
		endOfGameMessagePopup.setText(message);
		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		MopubHelper.showRectangleAd(adViewWrapper, this);

		/*adPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				if (adViewWrapper != null && getRectangleAdview() != null) {
					adViewWrapper.removeView(getRectangleAdview());
				}
			}
		});
		adPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				if (adViewWrapper != null && getRectangleAdview() != null) {
					adViewWrapper.removeView(getRectangleAdview());
				}
			}
		});*/


		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder;
				//Context mContext = getApplicationContext();
				builder = new AlertDialog.Builder(getContext());
				builder.setView(layout);
				adPopup = builder.create();
				adPopup.setCancelable(true);
				adPopup.setCanceledOnTouchOutside(true);
				try {
					adPopup.show();
				} catch (Exception ignored) {
				}
			}
		}, 1500);
	}

	public void setWhitePlayerTimer(String timeString) {
		whiteTimer = timeString;
		runOnUiThread(updateWhitePlayerTimer);

//		if (userPlayWhite) {
//			gamePanelView.setBlackTimer(timeString);
//		} else {
//			blackPlayerLabel.setText(timeString);
//		}
//
//		if (!isWhitePlayerMove || initTimer) {
//			isWhitePlayerMove = true;
//			changePlayersLabelColors();
//		}
	}

	public void setBlackPlayerTimer(String timeString) {
		blackTimer = timeString;
		runOnUiThread(updateBlackPlayerTimer);

//		if (userPlayWhite) {
//			blackPlayerLabel.setText(timeString);
//		} else {
//			gamePanelView.setBlackTimer(timeString);
//		}
//
//		if (isWhitePlayerMove) {
//			isWhitePlayerMove = false;
//			changePlayersLabelColors();
//		}
	}

	private Runnable updateWhitePlayerTimer = new Runnable() {
		@Override
		public void run() {
			if (userPlayWhite) {
				gamePanelView.setBlackTimer(whiteTimer);
			} else {
				blackPlayerLabel.setText(whiteTimer);
			}

			if (!isWhitePlayerMove || initTimer) {
				isWhitePlayerMove = true;
				changePlayersLabelColors();
			}
		}
	};

	private Runnable updateBlackPlayerTimer = new Runnable() {
		@Override
		public void run() {
			if (userPlayWhite) {
				blackPlayerLabel.setText(blackTimer);
			} else {
				gamePanelView.setBlackTimer(blackTimer);
			}

			if (isWhitePlayerMove) {
				isWhitePlayerMove = false;
				changePlayersLabelColors();
			}
		}
	};

	protected void changePlayersLabelColors() {
		int hintColor = getResources().getColor(R.color.hint_text);
		int whiteColor = getResources().getColor(R.color.white);

		int topPlayerColor;

		if(isWhitePlayerMove){
			topPlayerColor = userPlayWhite? hintColor: whiteColor;
		}else{
			topPlayerColor = userPlayWhite? whiteColor: hintColor;
		}

		whitePlayerLabel.setTextColor(topPlayerColor);
		blackPlayerLabel.setTextColor(topPlayerColor);

		boolean activate = isWhitePlayerMove? userPlayWhite: !userPlayWhite;

		gamePanelView.activatePlayerTimer(!activate, activate); // bottom is always player
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




//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			boardView.getBoardFace().setAnalysis(false);
//			onBackPressed();
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	@Override
	public void showChoosePieceDialog(final int col, final int row) {
        new AlertDialog.Builder(this)
				.setTitle(getString(R.string.choose_a_piece))
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
//					update(CALLBACK_REPAINT_UI);
					invalidateGameScreen();
					boardView.invalidate();
				}
			};
		}).start();
	}

	public Context getMeContext(){
		return this;
	}
	
	public void onCheck(){
		showToast(R.string.check);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.home) {
			backToHomeActivity();
		} else if (view.getId() == R.id.homePopupBtn) {
			if (adPopup != null) {
				try {
					adPopup.dismiss();
				} catch (Exception ignored) {
				}
				adPopup = null;
			}
			if (need2Finish) {
				finish();
			}
		} else if (view.getId() == R.id.newGame) {
			onBackPressed();
		}
	}

}
