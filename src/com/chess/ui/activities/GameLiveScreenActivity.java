package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.live.client.User;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.core.MainApp;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.CommonUtils;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameLiveScreenActivity extends GameBaseActivity implements View.OnClickListener {

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private CharSequence[] menuOptionsItems;

	private int resignOrAbort = R.string.resign;
	private View submitButtonsLay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive);
		init();
		widgetsInit();
		onPostCreate();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		if (lccHolder.getWhiteClock() != null && lccHolder.getBlackClock() != null) {
			whiteClockView.setVisibility(View.VISIBLE);
			blackClockView.setVisibility(View.VISIBLE);

			lccHolder.getWhiteClock().paint();
			lccHolder.getBlackClock().paint();

			Game game = lccHolder.getGame(extras.getLong(GameListItem.GAME_ID));
			User whiteUser = game.getWhitePlayer();
			User blackUser = game.getBlackPlayer();
			Boolean isWhite = (!game.isMoveOf(whiteUser) && !game.isMoveOf(blackUser)) ? null : game.isMoveOf(whiteUser);
			lccHolder.setClockDrawPointer(isWhite);
		}

		newBoardView.setBoardFace(new ChessBoard(this));
		newBoardView.setGameActivityFace(this);
		newBoardView.getBoardFace().setInit(true);
		newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		newBoardView.getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);

<<<<<<< HEAD
<<<<<<< HEAD

=======
>>>>>>> origin/developLive
=======
>>>>>>> 2a8ff33143743012d750bd73bdadaadb34777952
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DRAW_OFFER:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.drawoffer)
						.setMessage(getString(R.string.are_you_sure_q))
						.setPositiveButton(getString(R.string.ok), drawOfferDialogListener)
						.setNegativeButton(getString(R.string.cancel), drawOfferDialogListener)
						.create();
			case DIALOG_ABORT_OR_RESIGN:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.abort_resign_game)
						.setMessage(getString(R.string.are_you_sure_q))
						.setPositiveButton(R.string.ok, abortGameDialogListener)
						.setNegativeButton(R.string.cancel, abortGameDialogListener)
						.create();

			default:
				break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void init() {
		super.init();
<<<<<<< HEAD
<<<<<<< HEAD
		mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));
=======
        mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));
>>>>>>> origin/developLive
=======
        mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));
>>>>>>> 2a8ff33143743012d750bd73bdadaadb34777952
		changeResigntTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			Game game = lccHolder.getGame(mainApp.getGameId());
			LccHolder.LOG.info(AppConstants.REQUEST_DRAW + game);
			lccHolder.getAndroid().runMakeDrawTask(game);
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			Game game = lccHolder.getGame(mainApp.getGameId());

			if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
				System.out.println(AppConstants.LCCLOG_RESIGN_GAME_BY_FAIR_PLAY_RESTRICTION + game);
				LccHolder.LOG.info(AppConstants.RESIGN_GAME + game);
				lccHolder.getAndroid().runMakeResignTask(game);
			} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
				LccHolder.LOG.info(AppConstants.LCCLOG_ABORT_GAME + game);
				lccHolder.getAndroid().runAbortGameTask(game);
			} else {
				LccHolder.LOG.info(AppConstants.LCCLOG_RESIGN_GAME + game);
				lccHolder.getAndroid().runMakeResignTask(game);
			}
			finish();
		}
	}

	@Override
	protected void getOnlineGame(long game_id) {
		super.getOnlineGame(game_id);
		if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
			update(CALLBACK_GAME_STARTED);
		}
	}

	@Override
	public void update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(newBoardView.getBoardFace()))
					onBackPressed();
				break;
			case INIT_ACTIVITY:
				if (newBoardView.getBoardFace().isInit() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					getOnlineGame(mainApp.getGameId());
					newBoardView.getBoardFace().setInit(false);
				} else if (!newBoardView.getBoardFace().isInit()) {
					if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) && appService != null
							&& appService.getRepeatableTimer() == null) {
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
					}
				}
				break;
			case CALLBACK_REPAINT_UI: {
				if (newBoardView.getBoardFace().isSubmit())
					showSubmitButtonsLay(true);

				whitePlayerLabel.setVisibility(View.VISIBLE);
				blackPlayerLabel.setVisibility(View.VISIBLE);

				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					if (mainApp.getCurrentGame() != null) {
						whitePlayerLabel.setText(mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get(GameItem.WHITE_RATING) + ")");
						blackPlayerLabel.setText(mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get(GameItem.BLACK_RATING) + ")");
					}
				}

				newBoardView.addMove2Log(newBoardView.getBoardFace().getMoveListSAN());
				newBoardView.invalidate();

				handler.post(new Runnable() {
					@Override
					public void run() {
						newBoardView.requestFocus();
					}
				});
				break;
			}
			case CALLBACK_SEND_MOVE: {
				showSubmitButtonsLay(false);

				//String myMove = newBoardView.getBoardFace().MoveSubmit();
				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
					final String move = newBoardView.getBoardFace().convertMoveLive();
					LccHolder.LOG.info("LCC make move: " + move);
					try {
						lccHolder.makeMove(mainApp.getCurrentGameId(), move);
					} catch (IllegalArgumentException e) {
						LccHolder.LOG.info("LCC illegal move: " + move);
						e.printStackTrace();
					}
				}
				break;
			}
			case CALLBACK_COMP_MOVE: {
				whitePlayerLabel.setVisibility(View.GONE);
				blackPlayerLabel.setVisibility(View.GONE);
				thinking.setVisibility(View.VISIBLE);
				break;
			}
			case CALLBACK_PLAYER_MOVE: {
				whitePlayerLabel.setVisibility(View.VISIBLE);
				blackPlayerLabel.setVisibility(View.VISIBLE);
				thinking.setVisibility(View.GONE);
				break;
			}
			case CALLBACK_ECHESS_MOVE_WAS_SENT:
				// move was made
				if (mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
						+ AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 2) {
					finish();
				} else if (mainApp.getSharedData().getInt(mainApp.getSharedData()
						.getString(AppConstants.USERNAME, "") + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 0) {

					int i;
					ArrayList<GameListItem> currentGames = new ArrayList<GameListItem>();
					for (GameListItem gameListItem : mainApp.getGameListItems()) {
						if (gameListItem.type == 1 && gameListItem.values.get(GameListItem.IS_MY_TURN).equals("1")) {
							currentGames.add(gameListItem);
						}
					}
					for (i = 0; i < currentGames.size(); i++) {
						if (currentGames.get(i).values.get(GameListItem.GAME_ID)
								.contains(mainApp.getCurrentGame().values.get(GameListItem.GAME_ID))) {
							if (i + 1 < currentGames.size()) {
								newBoardView.setBoardFace(new ChessBoard(this));
								newBoardView.getBoardFace().setAnalysis(false);
								newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}

								getOnlineGame(Long.parseLong(currentGames.get(i + 1).values.get(GameListItem.GAME_ID)));
								return;
							} else {
								finish();
								return;
							}
						}
					}
					finish();
					return;
				}
				break;
			case CALLBACK_GAME_REFRESH:
				if (newBoardView.getBoardFace().isAnalysis())
					return;

				if (!mainApp.isLiveChess()) {
					game = ChessComApiParser.GetGameParseV3(responseRepeatable);
				}

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				int[] moveFT;
				if (!mainApp.getCurrentGame().equals(game)) {
					if (!mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).equals(game.values.get(AppConstants.MOVE_LIST))) {
						mainApp.setCurrentGame(game);
						String[] moves;

						if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())))) {

							int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) ? 0 : 1;

							moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

							if (moves.length - newBoardView.getBoardFace().getMovesCount() == 1) {
								if (mainApp.isLiveChess()) {
									moveFT = MoveParser.parseCoordinate(newBoardView.getBoardFace(), moves[moves.length - 1]);
								} else {
									moveFT = MoveParser.parse(newBoardView.getBoardFace(), moves[moves.length - 1]);
								}
								boolean playSound = (mainApp.isLiveChess()
										&& lccHolder.getGame(mainApp.getCurrentGameId())
										.getSeq() == moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move m;
									if (moveFT[3] == 2) {
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									} else {
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									}
									newBoardView.getBoardFace().makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									newBoardView.getBoardFace().makeMove(m, playSound);
								}
								//mainApp.showToast("Move list updated!");
								newBoardView.getBoardFace().setMovesCount(moves.length);
								newBoardView.invalidate();
								update(CALLBACK_REPAINT_UI);
							}
						}
						return;
					}

                    checkMessages();
				}
				break;

			case CALLBACK_GAME_STARTED:
				getSoundPlayer().playGameStart();

				mainApp.setCurrentGame(new GameItem(lccHolder.getGameData(mainApp.getGameId(), -1), true));
				executePausedActivityGameEvents();
				//lccHolder.setActivityPausedMode(false);
				lccHolder.getWhiteClock().paint();
				lccHolder.getBlackClock().paint();
				/*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
											   lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
											   lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*/

				if (openChatActivity()) {
                    return;
				}

				if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
					newBoardView.getBoardFace().setChess960(true);


				if (!isUserColorWhite()) {
					newBoardView.getBoardFace().setReside(true);
				}
				String[] Moves = {};


				if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					Moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					newBoardView.getBoardFace().setMovesCount(Moves.length);
				} else if (!mainApp.isLiveChess()) {
					newBoardView.getBoardFace().setMovesCount(0);
				}

				Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				String FEN = mainApp.getCurrentGame().values.get(GameItem.STARTING_FEN_POSITION);
				if (!FEN.equals("")) {
					newBoardView.getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, newBoardView.getBoardFace());
				}

				update(CALLBACK_REPAINT_UI);
				newBoardView.getBoardFace().takeBack();
				newBoardView.invalidate();

				playLastMoveAnimation();

				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) && appService != null && appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
				}
				break;

			default:
				break;
		}
	}
    
    private boolean openChatActivity(){
        if(!chat)
            return false;

        mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, mainApp.getCurrentGame().values.get(
                isUserColorWhite() ? AppConstants.BLACK_USERNAME : AppConstants.WHITE_USERNAME));
        mainApp.getSharedDataEditor().commit();

        mainApp.getCurrentGame().values.put(GameItem.HAS_NEW_MESSAGE, "0");
        gamePanelView.haveNewMessage(false);

        Intent intent = new Intent(coreContext, ChatLiveActivity.class);
        intent.putExtra(GameListItem.GAME_ID, mainApp.getCurrentGame().values.get(GameListItem.GAME_ID));
        intent.putExtra(GameListItem.TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));
        startActivity(intent);

        chat = false;
        return true;
    }
    

    private void checkMessages(){
        if (game.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
            mainApp.setCurrentGame(game);
            // show notification instead
            gamePanelView.haveNewMessage(true);
            CommonUtils.showNotification(coreContext, "", mainApp.getGameId(), "", "",ChatLiveActivity.class);
        }
    }

	@Override
	public void newGame() {
		startActivity(new Intent(this, OnlineNewGameActivity.class));
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
		newBoardView.getBoardFace().setSubmit(show);
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
				// test
				chat = true;
				getOnlineGame(mainApp.getGameId());
				break;
		}
		return super.onOptionsItemSelected(item);
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
		public void onClick(DialogInterface dialogInterface, int i) {
//			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i) {
				case LIVE_SETTINGS:
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					break;
				case LIVE_RESIDE:
					newBoardView.getBoardFace().setReside(!newBoardView.getBoardFace().isReside());
					newBoardView.invalidate();
					break;
				case LIVE_DRAW_OFFER:
					showDialog(DIALOG_DRAW_OFFER);
					break;
				case LIVE_RESIGN_OR_ABORT:
					showDialog(DIALOG_ABORT_OR_RESIGN);
					break;
				case LIVE_MESSAGES:
					chat = true;
					getOnlineGame(mainApp.getGameId());
					break;
			}
		}
	}

	protected void changeChatIcon(Menu menu) {
		if (mainApp.getCurrentGame().values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}

	protected void changeResigntTitle() {
		if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
			resignOrAbort = R.string.resign;
		} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
			resignOrAbort = R.string.abort;
		} else {
			resignOrAbort = R.string.resign;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainApp.getCurrentGame() != null && (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())
				|| MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace()))) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (extras.containsKey(AppConstants.LIVE_CHESS)) {
			mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));
			if (!mainApp.isLiveChess()) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... voids) {
						mainApp.getLccHolder().logout();
						return null;
					}
				}.execute();
			}
		}

		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));

		if (mainApp.isLiveChess() && mainApp.getGameId() > 0 /* && mainApp.getGameId() != null*/ /*&& !mainApp.getGameId().equals("")*/
				&& lccHolder.getGame(mainApp.getGameId()) != null) {
			game = new GameItem(lccHolder.getGameData(mainApp.getGameId(),
					lccHolder.getGame(mainApp.getGameId()).getSeq() - 1), true);
//			lccHolder.getAndroid().setGameActivity(this); // TODO
			if (lccHolder.isActivityPausedMode()) {
				executePausedActivityGameEvents();
				lccHolder.setActivityPausedMode(false);
			}
			//lccHolder.updateClockTime(lccHolder.getGame(mainApp.getGameId()));
		}
	}

	@Override
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
//		chatPanel.setVisibility(View.GONE);
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
//			chatPanel.setVisibility(View.VISIBLE);
			gamePanelView.haveNewMessage(true);
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);
		/*if (view.getId() == R.id.chat) {
			chat = true;
			getOnlineGame(mainApp.getGameId());
			chatPanel.setVisibility(View.GONE);
		} else*/
		if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			newBoardView.getBoardFace().takeBack();
			newBoardView.getBoardFace().decreaseMovesCount();
			newBoardView.invalidate();
		} else if (view.getId() == R.id.submit) {
			update(CALLBACK_SEND_MOVE);
		} /*else if (view.getId() == R.id.newGame) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}*/
	}
}

