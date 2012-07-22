package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.views.ChessBoardLiveView;
import com.chess.utilities.MopubHelper;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameLiveScreenActivity extends GameBaseActivity implements LccEventListener, LccChatMessageListener {

	private static final String TAG = "GameLiveScreenActivity";
	private static final String WARNING_TAG = "warning message popup";
	private static final String END_GAME_TAG = "end game popup";

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;


	private MenuOptionsDialogListener menuOptionsDialogListener;

	private View submitButtonsLay;
	private GameItem currentGame;
	private Long gameId;
	private ChessBoardLiveView boardView;
	private int whitePlayerNewRating;
	private int blackPlayerNewRating;
	private int currentPlayerRating;

    private String whiteTimer;
    private String blackTimer;
    private View fadeLay;
	private View gameBoardView;
	private boolean lccInitiated;
	private Button submitBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Log.d("TEST","onCreate GameLiveActivity, savedInstanceState = " + savedInstanceState);
		setContentView(R.layout.boardview_live);

		widgetsInit();
		lccInitiated = init();

		if(!lccInitiated){
			return;
		}
		// change labels and label's drawables according player color
		// so current player(user) name must be always at the bottom
		String blackPlayerName = getLccHolder().getBlackUserName(gameId);
		String userName = getLccHolder().getCurrentuserName();

		userPlayWhite = !userName.equals(blackPlayerName);
		int opponentIndicator = userPlayWhite ? R.drawable.player_indicator_black : R.drawable.player_indicator_white;

		whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(opponentIndicator, 0, 0, 0);
		gamePanelView.setWhiteIndicator(userPlayWhite);
		// change players colors
		changePlayersLabelColors();

		Log.d("Live Game", "GameLiveScreenActivity started ");
		if (getLccHolder().getPendingWarnings().size() > 0) {
			// get last warning
			String message = getLccHolder().getLastWarningMessage();
			getLccHolder().getPendingWarnings().remove(message);

			showPopupDialog(R.string.warning, message, WARNING_TAG);
		}
	}

	private boolean init() {
		if(!getLccHolder().isConnected()){
//			showSinglePopupDialog(R.string.network_was_changed_please_relogin);
			Log.d("TEST", "!getLccHolder().isConnected() -> EXIT");
			showToast(R.string.application_was_killed);
			return false;
		}

		gameId = extras.getLong(GameListItem.GAME_ID);
		currentGame = getLccHolder().getGameItem(gameId);

		getLccHolder().setLccEventListener(this);

		int resignOrAbort = getLccHolder().getResignTitle(gameId);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		return true;
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

        fadeLay = findViewById(R.id.fadeLay);
		gameBoardView = findViewById(R.id.gameBoard);

		boardView = (ChessBoardLiveView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		setBoardView(boardView);

		ChessBoard chessBoard = (ChessBoard) getLastCustomNonConfigurationInstance();
		if (chessBoard != null) {
			boardView.setBoardFace(chessBoard);
		} else {
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setInit(true);
			getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
			getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}
		boardView.setGameActivityFace(this);

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		submitBtn = (Button) findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(this);

		findViewById(R.id.cancelBtn).setOnClickListener(this);

		gamePanelView.enableAnalysisMode(false);

		// hide black dot for right label
		blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		whitePlayerLabel.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
	}



	@Override
	protected void onResume() {
		super.onResume();

		if(!lccInitiated){
			finish();
			return;
		}



        getLccHolder().setActivityPausedMode(false);
		updateGameState();
	}

	@Override
	protected void onPause() {
		if(endPopupFragment != null)
			endPopupFragment.dismiss();

		super.onPause();
		getLccHolder().setActivityPausedMode(true);

		handler.removeCallbacks(blinkSubmitButton);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("TEST","onDestroy called");
	}

	private void updateGameState() {
		if (getBoardFace().isInit()) {
			onGameStarted();
			getBoardFace().setInit(false);
		}

        getLccHolder().executePausedActivityGameEvents();
	}

	private void onGameStarted() {
		showSubmitButtonsLay(false);
		getSoundPlayer().playGameStart();

		currentGame = getLccHolder().getGameItem(gameId);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		checkMessages();

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		getLccHolder().checkAndReplayMoves(gameId);

		invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();

		playLastMoveAnimation();
	}

    public void setWhitePlayerTimer(String timeString) {
        whiteTimer = timeString;
        runOnUiThread(new Runnable() {
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
        });
    }

    public void setBlackPlayerTimer(String timeString) {
        blackTimer = timeString;
        runOnUiThread(new Runnable() {
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
        });
    }

	// ----------------------Lcc Events ---------------------------------------------

	public void onGameRefresh(GameItem gameItem) {
        blockGame(false);

		if (getBoardFace().isAnalysis())
			return;

		int[] moveFT;
		if (!currentGame.equals(gameItem)) {
			if (!currentGame.values.get(AppConstants.MOVE_LIST).equals(gameItem.values.get(AppConstants.MOVE_LIST))) {
				currentGame = gameItem;
				String[] moves;

				int beginIndex = 0;

				moves = currentGame.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "")
						.replaceAll("  ", " ").substring(beginIndex).split(" ");

				if (moves.length - getBoardFace().getMovesCount() == 1) { // if have new move

					moveFT = MoveParser.parseCoordinate(getBoardFace(), moves[moves.length - 1]);

					boolean playSound = getLccHolder().isPlaySound(gameId, moves);

					if (moveFT.length == 4) {
						Move move;
						if (moveFT[3] == 2) {
							move = new Move(moveFT[0], moveFT[1], 0, 2);
						} else {
							move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
						}
						getBoardFace().makeMove(move, playSound);
					} else {
						Move move = new Move(moveFT[0], moveFT[1], 0, 0);
						getBoardFace().makeMove(move, playSound);
					}
					getBoardFace().setMovesCount(moves.length);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							boardView.invalidate();
							invalidateGameScreen();
						}
					});
				}
				return;
			}

			checkMessages();
		}
	}

    @Override
    public void onConnectionBlocked(boolean blocked) {
        super.onConnectionBlocked(blocked);
        blockGame(blocked);
    }

    @Override
    public void onMessageReceived() {
        gamePanelView.haveNewMessage(true);
    }

	@Override
	public void onInform(String title, String message){
		showSinglePopupDialog(title, message);
	}

    @Override
    public void onDrawOffered(String drawOfferUsername) {
        String message = drawOfferUsername + StaticData.SYMBOL_SPACE + getString(R.string.has_offered_draw);

		popupItem.setPositiveBtnId(R.string.accept);
		popupItem.setNegativeBtnId(R.string.decline);
		showPopupDialog(message, DRAW_OFFER_RECEIVED_TAG);

		popupDialogFragment.setCancelable(false);
    }

    @Override
    public void onGameEnd(final String gameEndMessage) {
        final Game game = getLccHolder().getGame(gameId);
        switch (game.getGameTimeConfig().getGameTimeClass()) {
            case BLITZ: {
                whitePlayerNewRating = game.getWhitePlayer().getBlitzRating();
                blackPlayerNewRating = game.getBlackPlayer().getBlitzRating();
                currentPlayerRating = getLccHolder().getUser().getBlitzRating();
                break;
            }
            case LIGHTNING: {
                whitePlayerNewRating = game.getWhitePlayer().getQuickRating();
                blackPlayerNewRating = game.getBlackPlayer().getQuickRating();
                currentPlayerRating = getLccHolder().getUser().getQuickRating();
                break;
            }
            case STANDARD: {
                whitePlayerNewRating = game.getWhitePlayer().getStandardRating();
                blackPlayerNewRating = game.getBlackPlayer().getStandardRating();
                currentPlayerRating = getLccHolder().getUser().getStandardRating();
                break;
            }
        }

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout;
        if (!MopubHelper.isShowAds(this)) {
            layout = inflater.inflate(R.layout.popup_end_game, null, false);
        } else {
            layout = inflater.inflate(R.layout.popup_end_game_free, null, false);
        }

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updatePlayerLabels(game, whitePlayerNewRating, blackPlayerNewRating);
				showGameEndPopup(layout, getString(R.string.game_over), gameEndMessage);
				onGameEndMsgReceived();
			}
		});

    }

	// -----------------------------------------------------------------------------------

    private void blockGame(final boolean block){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (block) {
					fadeLay.setVisibility(View.VISIBLE);
				} else {
					fadeLay.setVisibility(View.INVISIBLE);
				}

				boardView.lockBoard(block);
				gameBoardView.invalidate();
			}
		});
    }

	protected void sendMove() {
		showSubmitButtonsLay(false);

		String move = getBoardFace().convertMoveLive();
		Log.i(TAG, "LCC make move: " + move);

		getLccHolder().makeMove(gameId, move, gameTaskRunner);
	}

	private void updatePlayerLabels() {
		if (userPlayWhite) {
			whitePlayerLabel.setText(getBlackPlayerName());
			gamePanelView.setWhiteTimer(getWhitePlayerName());
		} else {
			whitePlayerLabel.setText(getWhitePlayerName());
			gamePanelView.setWhiteTimer(getBlackPlayerName());
		}
	}

	@Override
	public void switch2Chat() {
		openChatActivity();
	}

	private boolean openChatActivity() {
		preferencesEditor.putString(AppConstants.OPPONENT, currentGame.values.get(
				isUserColorWhite() ? AppConstants.BLACK_USERNAME : AppConstants.WHITE_USERNAME));
		preferencesEditor.commit();

		currentGame.values.put(GameItem.HAS_NEW_MESSAGE, "0");
		gamePanelView.haveNewMessage(false);

		Intent intent = new Intent(this, ChatLiveActivity.class);
		intent.putExtra(GameListItem.GAME_ID, gameId);
		intent.putExtra(GameListItem.TIMESTAMP, currentGame.values.get(GameListItem.TIMESTAMP));
		startActivity(intent);

		return true;
	}

	private void checkMessages() {
		if (currentGame.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			gamePanelView.haveNewMessage(true);
		}
	}

	@Override
	public void newGame() {
		startActivity(new Intent(this, LiveNewGameActivity.class));
	}

	@Override
	public void updateAfterMove() {
		sendMove();
	}

	@Override
	public void invalidateGameScreen() {
		if (getBoardFace().isSubmit())
			showSubmitButtonsLay(true);

		whitePlayerLabel.setVisibility(View.VISIBLE);
		blackPlayerLabel.setVisibility(View.VISIBLE);

		updatePlayerLabels();

		boardView.addMove2Log(getBoardFace().getMoveListSAN());
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		submitButtonsLay.setVisibility(show ? View.VISIBLE : View.GONE);
		getBoardFace().setSubmit(show);
		
		if(show){
			blinkSubmitBtn();
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_live, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_chat:
				openChatActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Boolean isUserColorWhite() {
		return currentGame.values.get(AppConstants.WHITE_USERNAME).toLowerCase()
				.equals(AppData.getUserName(this));
	}

    private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		private final int LIVE_SETTINGS = 0;
		private final int LIVE_RESIDE = 1;
		private final int LIVE_DRAW_OFFER = 2;
		private final int LIVE_RESIGN_OR_ABORT = 3;
		private final int LIVE_MESSAGES = 4;

		final CharSequence[] items;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int pos) {
			switch (pos) {
				case LIVE_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case LIVE_RESIDE:
					getBoardFace().setReside(!getBoardFace().isReside());
					boardView.invalidate();
					break;
				case LIVE_DRAW_OFFER:
					showPopupDialog(R.string.drawoffer, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
					break;
				case LIVE_RESIGN_OR_ABORT:
					showPopupDialog(R.string.abort_resign_game, R.string.are_you_sure_q, ABORT_GAME_TAG);
					break;
				case LIVE_MESSAGES:
					openChatActivity();
					break;
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_RECEIVED_TAG)) {
			Log.i(TAG, AppConstants.REQUEST_DRAW + getLccHolder().getGame(gameId));
			gameTaskRunner.runMakeDrawTask(gameId);
		} else if (fragment.getTag().equals(ABORT_GAME_TAG)) {
			Game game = getLccHolder().getGame(gameId);

			if (getLccHolder().isFairPlayRestriction(gameId)) {
				System.out.println(AppConstants.LCCLOG_RESIGN_GAME_BY_FAIR_PLAY_RESTRICTION + game);
				Log.i(TAG, AppConstants.RESIGN_GAME + game);
				gameTaskRunner.runMakeResignTask(gameId);
			} else if (getLccHolder().isAbortableBySeq(gameId)) {
				Log.i(TAG, AppConstants.LCCLOG_ABORT_GAME + game);
				gameTaskRunner.runAbortGameTask(gameId);
			} else {
				Log.i(TAG, AppConstants.LCCLOG_RESIGN_GAME + game);
				gameTaskRunner.runMakeResignTask(gameId);
			}
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_RECEIVED_TAG)) {
			Log.i(TAG, AppConstants.DECLINE_DRAW + getLccHolder().getGame(gameId));
			gameTaskRunner.runRejectDrawTask(gameId);
		}
	}

	protected void changeChatIcon(Menu menu) {
		if (currentGame.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (currentGame != null) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	// ---------------- Players names and labels -----------------------------------------------------------------

	@Override
	public String getWhitePlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.values.get(AppConstants.WHITE_USERNAME) + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.values.get(GameItem.WHITE_RATING) + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public String getBlackPlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.values.get(AppConstants.BLACK_USERNAME) + StaticData.SYMBOL_LEFT_PAR + currentGame.values.get(GameItem.BLACK_RATING) + StaticData.SYMBOL_RIGHT_PAR;
	}

	private void updatePlayerLabels(Game game, int newWhiteRating, int newBlackRating) {
		if (userPlayWhite) {
			whitePlayerLabel.setText(game.getBlackPlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR
					+ newBlackRating + StaticData.SYMBOL_RIGHT_PAR);
			gamePanelView.setWhiteTimer(game.getWhitePlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR
					+ newWhiteRating + StaticData.SYMBOL_RIGHT_PAR); // always at the bottom
		} else {
			whitePlayerLabel.setText(game.getWhitePlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR + newWhiteRating + StaticData.SYMBOL_RIGHT_PAR);
			gamePanelView.setWhiteTimer(game.getBlackPlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR + newBlackRating + StaticData.SYMBOL_RIGHT_PAR);
		}
	}

	@Override
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		gamePanelView.enableAnalysisMode(true);

        boardView.setFinished(true);
        gamePanelView.showBottomPart(false);
        getSoundPlayer().playGameEnd();
	}

	private void blinkSubmitBtn(){
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			submitBtn.setBackgroundResource(R.drawable.button_grey_selector);
			submitBtn.invalidate();
			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			submitBtn.setBackgroundResource(R.drawable.button_orange_selector);
			submitBtn.invalidate();
			blinkSubmitBtn();
		}
	};


	private void showGameEndPopup(View layout, String title, String message) {

		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating;
		if (userPlayWhite) {
			currentPlayerNewRating = whitePlayerNewRating;
		} else {
			currentPlayerNewRating = blackPlayerNewRating;
		}

		int ratingDiff;
		String sign;
		if(currentPlayerRating < currentPlayerNewRating){ // 800 1200
			ratingDiff = currentPlayerNewRating - currentPlayerRating;
			sign = StaticData.SYMBOL_PLUS;
		} else { // 800 700
			ratingDiff = currentPlayerRating - currentPlayerNewRating;
			sign = StaticData.SYMBOL_MINUS;
		}
		
		String rating = getString(R.string.your_end_game_rating, sign + ratingDiff, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		MopubHelper.showRectangleAd(adViewWrapper, this);
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getSupportFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);
		if (MopubHelper.isShowAds(this)) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}

	}

	@Override
	protected void restoreGame() {
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.cancelBtn) {
			showSubmitButtonsLay(false);

			getBoardFace().takeBack();
			getBoardFace().decreaseMovesCount();
			boardView.invalidate();
		} else if (view.getId() == R.id.submitBtn) {
			sendMove();
		} else if (view.getId() == R.id.newGamePopupBtn) {
			Intent intent = new Intent(this, LiveNewGameActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (view.getId() == R.id.rematchPopupBtn) {
			// TODO send rematch request
			endPopupFragment.dismiss();
		} else if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));
		}
	}
}

