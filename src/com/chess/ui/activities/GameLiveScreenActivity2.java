package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoard2;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.views.ChessBoardLiveView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameLiveScreenActivity2 extends GameBaseActivity2 {

    private static final String TAG = "GameLiveScreenActivity";
	private static final String WARNING_TAG = "warning message popup";


	private MenuOptionsDialogListener menuOptionsDialogListener;
	private CharSequence[] menuOptionsItems;

	private int resignOrAbort = R.string.resign;
	private View submitButtonsLay;
	private GameItem currentGame;
	private long gameId;
	private ChessBoardLiveView boardView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_live);

		init();
		widgetsInit();

		// change labels and label's drawables according player color
		// so current player(user) name must be always at the bottom
//		String blackPlayerName = lccHolder.getGame(gameId).getBlackPlayer().getUsername();
		String blackPlayerName = getLccHolder().getBlackUserName(gameId);
		String userName = getLccHolder().getCurrentuserName();

		userPlayWhite = !userName.equals(blackPlayerName);
		int opponentIndicator = userPlayWhite ? R.drawable.player_indicator_black : R.drawable.player_indicator_white;

		whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(opponentIndicator, 0, 0, 0);
		gamePanelView.setWhiteIndicator(userPlayWhite);
		// change players colors
		changePlayersLabelColors();


		// TODO show popup warning
		Log.d("Live Game","GameLiveScreenActivity started ");
		if(getLccHolder().getPendingWarnings().size() > 0){
			// get last warning
			String message = getLccHolder().getLastWarningMessage() ;

			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.warning);
			popupItem.setMessage(message);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
			popupDialogFragment.show(getSupportFragmentManager(), WARNING_TAG);
		}
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		boardView = (ChessBoardLiveView) findViewById(R.id.boardview);
		boardView.setFocusable(true);

		boardView.setGamePanelView(gamePanelView);

        ChessBoard chessBoard = (ChessBoard) getLastCustomNonConfigurationInstance();
        if (chessBoard != null) {
            boardView.setBoardFace(chessBoard);
        } else {
            boardView.setBoardFace(new ChessBoard2(this));
            boardView.getBoardFace().setInit(true);
            boardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
            boardView.getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}
		boardView.setGameActivityFace(this);
		setBoardView(boardView);

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

//		getLccHolder().paintClocks(); // TODO check

		gamePanelView.enableAnalysisMode(false);

		// hide black dot for right label
		blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		whitePlayerLabel.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
	}


	@Override
	public void init() {
		super.init();
		gameId = extras.getLong(GameListItem.GAME_ID);
		changeResignTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

	@Override
	protected void onResume() {
		super.onResume();
		DataHolder.getInstance().setLiveChess(true);

		registerReceiver(gameMoveReceiver, new IntentFilter(IntentConstants.ACTION_GAME_MOVE));
		registerReceiver(gameEndMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_END));
		registerReceiver(gameInfoMessageReceived, new IntentFilter(IntentConstants.ACTION_GAME_INFO));
		registerReceiver(showGameEndPopupReceiver, new IntentFilter(IntentConstants.ACTION_SHOW_GAME_END_POPUP));
		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
		registerReceiver(drawOfferedMessageReceiver, new IntentFilter(IntentConstants.FILTER_DRAW_OFFERED));

		updateGameSate();

		newGame = getLccHolder().getGameItem(this, gameId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(gameMoveReceiver);
		unregisterReceiver(gameEndMessageReceiver);
		unregisterReceiver(gameInfoMessageReceived);
		unregisterReceiver(showGameEndPopupReceiver);
		unregisterReceiver(drawOfferedMessageReceiver);
        unregisterReceiver(chatMessageReceiver);

		getLccHolder().setActivityPausedMode(true);
	}

	private void updateGameSate() {
		if (boardView.getBoardFace().isInit()) {
			getOnlineGame();
			boardView.getBoardFace().setInit(false);
		}
	}

	private void getOnlineGame() {
		onGameStarted();
	}

	private void onGameStarted() {
		showSubmitButtonsLay(false);
		getSoundPlayer().playGameStart();

		currentGame = getLccHolder().getGameItem(gameId); // new GameItem(lccHolder.getGameData(gameId, -1), true);

        getLccHolder().executePausedActivityGameEvents(this);
		//lccHolder.setActivityPausedMode(false);
//        getLccHolder().paintClocks(); // TODO check

		/*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
													   lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
													   lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*/

		if (openChatActivity()) {
			return;
		}

		if (currentGame.values.get(GameListItem.GAME_TYPE).equals("2"))
			boardView.getBoardFace().setChess960(true);


		if (!isUserColorWhite()) {
			boardView.getBoardFace().setReside(true);
		}
		String[] moves;


		if (currentGame.values.get(AppConstants.MOVE_LIST).contains("1.")) {
			moves = currentGame.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
			boardView.getBoardFace().setMovesCount(moves.length);
		}

		getLccHolder().checkAndReplayMoves(gameId);

		String FEN = currentGame.values.get(GameItem.STARTING_FEN_POSITION);
		if (!FEN.equals("")) {
			boardView.getBoardFace().genCastlePos(FEN);
			MoveParser.fenParse(FEN, boardView.getBoardFace());
		}

//		update(CALLBACK_REPAINT_UI);
		invalidateGameScreen();
		boardView.getBoardFace().takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

	}

	protected BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			newGame = (GameItem) intent.getSerializableExtra(AppConstants.OBJECT);
//			update(CALLBACK_GAME_REFRESH);
			onGameRefresh();
		}
	};

	public void onGameRefresh() {
		if (boardView.getBoardFace().isAnalysis())
			return;

		int[] moveFT;
		if (!currentGame.equals(newGame)) {
			if (!currentGame.values.get(AppConstants.MOVE_LIST).equals(newGame.values.get(AppConstants.MOVE_LIST))) {
				currentGame = newGame;
				String[] moves;

				int beginIndex = 0;

				moves = currentGame.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

				if (moves.length - boardView.getBoardFace().getMovesCount() == 1) {

					moveFT = MoveParser.parseCoordinate(boardView.getBoardFace(), moves[moves.length - 1]);

					boolean playSound = getLccHolder().isPlaySound(gameId, moves);

					if (moveFT.length == 4) {
						Move move;
						if (moveFT[3] == 2) {
							move = new Move(moveFT[0], moveFT[1], 0, 2);
						} else {
							move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
						}
						boardView.getBoardFace().makeMove(move, playSound);
					} else {
						Move move = new Move(moveFT[0], moveFT[1], 0, 0);
						boardView.getBoardFace().makeMove(move, playSound);
					}
					//mainApp.showToast("Move list updated!");
					boardView.getBoardFace().setMovesCount(moves.length);
					boardView.invalidate();
//					update(CALLBACK_REPAINT_UI);
					invalidateGameScreen();
				}
				return;
			}

			checkMessages();
		}
	}

	protected BroadcastReceiver gameEndMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());

			Game game = LccHolder.getInstance(context).getGame(gameId);
			Integer newWhiteRating = null;
			Integer newBlackRating = null;
			switch (game.getGameTimeConfig().getGameTimeClass()) {
				case BLITZ: {
					newWhiteRating = game.getWhitePlayer().getBlitzRating();
					newBlackRating = game.getBlackPlayer().getBlitzRating();
					break;
				}
				case LIGHTNING: {
					newWhiteRating = game.getWhitePlayer().getQuickRating();
					newBlackRating = game.getBlackPlayer().getQuickRating();
					break;
				}
				case STANDARD: {
					newWhiteRating = game.getWhitePlayer().getStandardRating();
					newBlackRating = game.getBlackPlayer().getStandardRating();
					break;
				}
			}
			updatePlayerLabels(game, newWhiteRating, newBlackRating);
			boardView.setFinished(true);

			if (MopubHelper.isShowAds(context)) {
				final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(R.layout.ad_popup,
						(ViewGroup) findViewById(R.id.layout_root));
				showGameEndPopup(layout, intent.getExtras().getString(AppConstants.TITLE) + ": " + intent.getExtras().getString(AppConstants.MESSAGE));

				final View newGame = layout.findViewById(R.id.newGame);
				newGame.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (adPopup != null) {
							try {
								adPopup.dismiss();
							} catch (Exception ignored) {
							}
							adPopup = null;
						}
						onBackPressed();
					}
				});
				newGame.setVisibility(View.VISIBLE);

				final View home = layout.findViewById(R.id.home);
				home.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (adPopup != null) {
							try {
								adPopup.dismiss();
							} catch (Exception ignored) {
							}
							adPopup = null;
						}
						backToHomeActivity();
					}
				});
				home.setVisibility(View.VISIBLE);
			}

			endOfGameMessage.setText(intent.getExtras().getString(AppConstants.MESSAGE));

			findViewById(R.id.endOfGameButtons).setVisibility(View.VISIBLE);
			findViewById(R.id.newGame).setOnClickListener(GameLiveScreenActivity2.this);
			findViewById(R.id.home).setOnClickListener(GameLiveScreenActivity2.this);

			gamePanelView.showBottomPart(false);
			getSoundPlayer().playGameEnd();
			onGameEndMsgReceived();
		}
	};

	//	protected void executePausedActivityGameEvents() {
//		if (lccHolder.getPausedActivityGameEvents().size() > 0) {
//
//			GameEvent gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.Move);
//			if (gameEvent != null && (lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
//				//lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
//				//fullGameProcessed = true;
//				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
//				//lccHolder.getAndroidStuff().processMove(gameEvent.getGameId(), gameEvent.moveIndex);
//				newGame = new GameItem(lccHolder.getGameData(gameEvent.getGameId(), gameEvent.getMoveIndex()), true);
////				update(CALLBACK_GAME_REFRESH);
//				onGameRefresh();
//			}
//
//			gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.DrawOffer);
//			if (gameEvent != null &&
//					(lccHolder.getCurrentGameId() == null
//							|| lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
//				/*if (!fullGameProcessed)
//											{
//											  lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
//											  fullGameProcessed = true;
//											}*/
//				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
//				lccHolder.getAndroidStuff().processDrawOffered(gameEvent.getDrawOffererUsername());
//			}
//
//			gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.EndOfGame);
//			if (gameEvent != null &&
//					(lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
//				/*if (!fullGameProcessed)
//											{
//											  lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
//											  fullGameProcessed = true;
//											}*/
//				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
//				lccHolder.getAndroidStuff().processGameEnd(gameEvent.getGameEndedMessage());
//			}
//		}
//	}
	private final BroadcastReceiver drawOfferedMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.confirm);
			popupItem.setMessage(R.string.signout_confirm);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem,
					GameLiveScreenActivity2.this);
			popupDialogFragment.show(getSupportFragmentManager(), DRAW_OFFER_RECEIVED_TAG);
			popupDialogFragment.getDialog().setCanceledOnTouchOutside(true);
			popupDialogFragment.getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					gameTaskRunner.runRejectDrawTask(gameId);
				}
			});
			popupDialogFragment.getDialog().getWindow().setGravity(Gravity.BOTTOM);

			// TODO make popUpFragmentDialog
			final AlertDialog alertDialog = new AlertDialog.Builder(context)
					// .setTitle(intent.getExtras().getString(AppConstants.TITLE))
					.setMessage(intent.getExtras().getString(AppConstants.MESSAGE))
					.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							gameTaskRunner.runMakeDrawTask(gameId);
						}
					}).setNeutralButton(getString(R.string.decline), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							gameTaskRunner.runRejectDrawTask(gameId);
						}
					})
					.create();
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					gameTaskRunner.runRejectDrawTask(gameId);
				}
			});
			alertDialog.getWindow().setGravity(Gravity.BOTTOM);
			alertDialog.show();
		}
	};

	protected BroadcastReceiver showGameEndPopupReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			if (!MopubHelper.isShowAds(context)) {
				return;
			}

			final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.ad_popup, (ViewGroup) findViewById(R.id.layout_root));
			showGameEndPopup(layout, intent.getExtras().getString(AppConstants.MESSAGE));

			final Button ok = (Button) layout.findViewById(R.id.home);
			ok.setText(getString(R.string.okay));
			ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (adPopup != null) {
						try {
							adPopup.dismiss();
						} catch (Exception ignored) {
						}
						adPopup = null;
					}
					if (intent.getBooleanExtra(AppConstants.FINISHABLE, false)) {
						finish();
					}
				}
			});
			ok.setVisibility(View.VISIBLE);
		}
	};

	protected BroadcastReceiver gameInfoMessageReceived = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			String title = intent.getStringExtra(AppConstants.TITLE);
			String message = intent.getStringExtra(AppConstants.MESSAGE);
			showSinglePopupDialog(title, message);
		}
	};

	protected void sendMove() {
		showSubmitButtonsLay(false);

		final String move = boardView.getBoardFace().convertMoveLive();
		Log.i(TAG, "LCC make move: " + move);
		try {
			getLccHolder().makeMove(gameId, move);
		} catch (IllegalArgumentException e) {
			Log.i(TAG, "LCC illegal move: " + move);
			e.printStackTrace();
		}
	}

	private void updatePlayerLabels() {
		if (userPlayWhite) {
			whitePlayerLabel.setText(getWhitePlayerName());
			gamePanelView.setWhiteTimer(getBlackPlayerName());
		} else {
			whitePlayerLabel.setText(getWhitePlayerName());
			gamePanelView.setWhiteTimer(getBlackPlayerName());
		}
	}

	private boolean openChatActivity() {
		if (!chat)
			return false;

		preferencesEditor.putString(AppConstants.OPPONENT, currentGame.values.get(
				isUserColorWhite() ? AppConstants.BLACK_USERNAME : AppConstants.WHITE_USERNAME));
		preferencesEditor.commit();

		currentGame.values.put(GameItem.HAS_NEW_MESSAGE, "0");
		gamePanelView.haveNewMessage(false);

		Intent intent = new Intent(this, ChatLiveActivity.class);
		intent.putExtra(GameListItem.GAME_ID, gameId);
		intent.putExtra(GameListItem.TIMESTAMP, currentGame.values.get(GameListItem.TIMESTAMP));
		startActivity(intent);

		chat = false;
		return true;
	}

	private void checkMessages() {
		if (newGame.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			currentGame = newGame;
			// show notification instead
			gamePanelView.haveNewMessage(true);
			AppUtils.showNotification(this, StaticData.SYMBOL_EMPTY, gameId, StaticData.SYMBOL_EMPTY, StaticData.SYMBOL_EMPTY, ChatLiveActivity.class);
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
		if (boardView.getBoardFace().isSubmit())
			showSubmitButtonsLay(true);

		whitePlayerLabel.setVisibility(View.VISIBLE);
		blackPlayerLabel.setVisibility(View.VISIBLE);

		updatePlayerLabels();

		boardView.addMove2Log(boardView.getBoardFace().getMoveListSAN());
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
		boardView.getBoardFace().setSubmit(show);
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
				chat = true;
				getOnlineGame();
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
					boardView.getBoardFace().setReside(!boardView.getBoardFace().isReside());
					boardView.invalidate();
					break;
				case LIVE_DRAW_OFFER:
					popupItem.setTitle(R.string.drawoffer);
					popupItem.setMessage(R.string.are_you_sure_q);
					popupDialogFragment.show(getSupportFragmentManager(), DRAW_OFFER_RECEIVED_TAG);
					break;
				case LIVE_RESIGN_OR_ABORT:
					popupItem.setTitle(R.string.abort_resign_game);
					popupItem.setMessage(R.string.are_you_sure_q);
					popupDialogFragment.show(getSupportFragmentManager(), ABORT_GAME_TAG);
					break;
				case LIVE_MESSAGES:
					chat = true;
					getOnlineGame();
					break;
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if(fragment.getTag().equals(LOGOUT_TAG)){
			getLccHolder().logout();
			backToHomeActivity();
		}else if(fragment.getTag().equals(DRAW_OFFER_RECEIVED_TAG)){
			Log.i(TAG, AppConstants.REQUEST_DRAW + getLccHolder().getGame(gameId));
			gameTaskRunner.runMakeDrawTask(gameId);
//			onDrawOffered();
		}else if(fragment.getTag().equals(ABORT_GAME_TAG)){
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
			finish();
		}
	}

	protected void changeChatIcon(Menu menu) {
		if (currentGame.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}

	protected void changeResignTitle() {
		resignOrAbort = getLccHolder().getResignTitle(gameId);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (currentGame != null) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            Log.i(TAG, AppConstants.REQUEST_DRAW + getLccHolder().getGame(gameId));
            gameTaskRunner.runMakeDrawTask(gameId);
		}
	}

	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
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
			finish();
		}
	}

	// ---------------- Players names and labels -----------------------------------------------------------------

	@Override
	public String getWhitePlayerName() {
		if(currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.values.get(AppConstants.WHITE_USERNAME) + StaticData.SYMBOL_LEFT_PAR + currentGame.values.get(GameItem.WHITE_RATING) + StaticData.SYMBOL_RIGHT_PAR;  // TODO check
	}

	@Override
	public String getBlackPlayerName() {
		if(currentGame == null)
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
	}

	@Override
	protected void restoreGame() {
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			gamePanelView.haveNewMessage(true);
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);
		/*if (view.getId() == R.id.chat) {
			chat = true;
			getOnlineGame(gameId);
			chatPanel.setVisibility(View.GONE);
		} else*/
		if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			boardView.getBoardFace().takeBack();
			boardView.getBoardFace().decreaseMovesCount();
			boardView.invalidate();
		} else if (view.getId() == R.id.submit) {
//			update(CALLBACK_SEND_MOVE);
			sendMove();
		}
	}
}

