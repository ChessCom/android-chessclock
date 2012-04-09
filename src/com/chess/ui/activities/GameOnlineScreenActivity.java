package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.core.MainApp;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.*;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameOnlineScreenActivity extends GameBaseActivity implements View.OnClickListener {

	private final static int CALLBACK_ECHESS_MOVE_WAS_SENT = 8;
	private final static int CALLBACK_SEND_MOVE = 1;
	private final static int CALLBACK_GET_ECHESS_GAME_AND_SEND_MOVE = 12;

	private int UPDATE_DELAY = 10000;
	private View submitButtonsLay;


	private MenuOptionsDialogListener menuOptionsDialogListener;


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

		newBoardView.setBoardFace(new ChessBoard(this));
		newBoardView.setGameActivityFace(this);
		newBoardView.getBoardFace().setInit(true);
		newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		newBoardView.getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);



		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_new_game);
	}

	@Override
	protected void init() {
		super.init();
        mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.backtogamelist),
				getString(R.string.messages),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(R.string.resignorabort)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
				Game game = lccHolder.getGame(mainApp.getGameId());
				LccHolder.LOG.info(AppConstants.REQUEST_DRAW + game);
				lccHolder.getAndroid().runMakeDrawTask(game);
			} else {
				String Draw = AppConstants.OFFERDRAW;
				if (mainApp.acceptdraw)
					Draw = AppConstants.ACCEPTDRAW;               // TODO hide to resthelper
				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.CHESSID_PARAMETER + mainApp.getCurrentGameId()
						+ AppConstants.COMMAND_PARAMETER + Draw + AppConstants.TIMESTAMP_PARAMETER
						+ mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP), "GET", null, null);
				if (result.contains(AppConstants.SUCCESS)) {
					mainApp.showDialog(coreContext, "", getString(R.string.drawoffered));
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Game.this, "Error", result);
				}
			}
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
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
			} else {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.CHESSID_PARAMETER + mainApp.getCurrentGameId()
						+ AppConstants.COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER
						+ mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP), "GET", null, null);
				if (result.contains(AppConstants.SUCCESS)) {
					if (MobclixHelper.isShowAds(mainApp)) {
						sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
								.putExtra(AppConstants.MESSAGE, "GAME OVER")
								.putExtra(AppConstants.FINISHABLE, true));
					} else {
						finish();
					}
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Game.this, "Error", result);
				}
			}
		}
	}


	@Override
	protected void getOnlineGame(long game_id) {
		super.getOnlineGame(game_id);
//		if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
//			update(CALLBACK_GAME_STARTED);
//		} else {
			if (appService != null) {
				appService.RunSingleTask(CALLBACK_GAME_STARTED,
						"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID
								+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
						null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
			}
//		}
	}

	@Override
	public void update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				onBackPressed();
				break;
			case INIT_ACTIVITY:
				if (newBoardView.getBoardFace().isInit() || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
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
						if (!mainApp.isLiveChess()) {
							appService.RunRepeatableTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
									"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
									null/*progressDialog*/
							);
						}
					}
				}
				break;
			case CALLBACK_REPAINT_UI: {
				if (newBoardView.getBoardFace().isSubmit())
					showSubmitButtonsLay(true);

				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					if (mainApp.getCurrentGame() != null) {
						whitePlayerLabel.setText(mainApp.getCurrentGame()
								.values.get(AppConstants.WHITE_USERNAME) + "\n("
								+ mainApp.getCurrentGame().values.get(GameItem.WHITE_RATING) + ")");
						blackPlayerLabel.setText(mainApp.getCurrentGame()
								.values.get(AppConstants.BLACK_USERNAME)
								+ "\n(" + mainApp.getCurrentGame().values.get(GameItem.BLACK_RATING) + ")");
					}
				}


				newBoardView.addMove2Log(newBoardView.getBoardFace().getMoveListSAN());
				/*if(mainApp.getCurrentGame() != null && mainApp.getCurrentGame().values.get("move_list") != null)
								{
								  movelist.setText(mainApp.getCurrentGame().values.get("move_list"));
								}
								else
								{
								  movelist.setText(newBoardView.getBoardFaceFace().getMoveListSAN());
								}*/
				newBoardView.invalidate();

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						newBoardView.requestFocus();
					}
				});
				break;
			}
			case CALLBACK_SEND_MOVE: {
				showSubmitButtonsLay(false);
				//String myMove = newBoardView.getBoardFaceFace().MoveSubmit();
				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
					final String move = newBoardView.getBoardFace().convertMoveLive();
					LccHolder.LOG.info("LCC make move: " + move);
					try {
						lccHolder.makeMove(mainApp.getCurrentGameId(), move);
					} catch (IllegalArgumentException e) {
						LccHolder.LOG.info("LCC illegal move: " + move);
						e.printStackTrace();
					}
				} else if (!mainApp.isLiveChess() && appService != null) {
					if (mainApp.getCurrentGame() == null) {
						if (appService.getRepeatableTimer() != null) {
							appService.getRepeatableTimer().cancel();
							appService.setRepeatableTimer(null);
						}
						appService.RunSingleTask(CALLBACK_GET_ECHESS_GAME_AND_SEND_MOVE,
								"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
								null);
					} else {
						appService.RunSingleTask(CALLBACK_ECHESS_MOVE_WAS_SENT,
								"http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + AppConstants.CHESSID_PARAMETER +
										mainApp.getCurrentGameId()  + AppConstants.COMMAND_SUBMIT_AND_NEWMOVE_PARAMETER +
										newBoardView.getBoardFace().convertMoveEchess() + AppConstants.TIMESTAMP_PARAMETER +
										mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP),
								progressDialog = new MyProgressDialog(
										ProgressDialog.show(this, null, getString(R.string.sendinggameinfo), true)));

						NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.cancel(1);
						Notifications.resetCounter();
					}
				}
				break;
			}
			case CALLBACK_GET_ECHESS_GAME_AND_SEND_MOVE: {
				mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));
				if (!mainApp.isLiveChess() && appService != null) {
					appService.RunSingleTask(CALLBACK_ECHESS_MOVE_WAS_SENT,
							"http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID +
									mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + AppConstants.CHESSID_PARAMETER +
									mainApp.getCurrentGameId()  + AppConstants.COMMAND_SUBMIT_AND_NEWMOVE_PARAMETER +
									newBoardView.getBoardFace().convertMoveEchess() + AppConstants.TIMESTAMP_PARAMETER +
									mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP),
									progressDialog = new MyProgressDialog(
									ProgressDialog.show(this, null, getString(R.string.sendinggameinfo), true)));

					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(1);
					Notifications.resetCounter();
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
				if (mainApp.getSharedData().getInt(mainApp.getUserName()
						+ AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 2) {
					finish();
				} else if (mainApp.getSharedData().getInt(mainApp.getSharedData()
						.getString(AppConstants.USERNAME, "") + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 0) {

					int i;
					ArrayList<GameListItem> currentGames = new ArrayList<GameListItem>();
					for (GameListItem gle : mainApp.getGameListItems()) {
						if (gle.type == 1 && gle.values.get(GameListItem.IS_MY_TURN).equals("1")) {
							currentGames.add(gle);
						}
					}
					for (i = 0; i < currentGames.size(); i++) {
						if (currentGames.get(i).getGameId() == mainApp.getCurrentGameId()) {
							if (i + 1 < currentGames.size()) {
								newBoardView.setBoardFace(new ChessBoard(this));
								newBoardView.getBoardFace().setAnalysis(false);
								newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}

								getOnlineGame(currentGames.get(i + 1).getGameId());
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
					if (!mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST)
							.equals(game.values.get(AppConstants.MOVE_LIST))) {
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
								boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGameId()).getSeq() == moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move move;
									if (moveFT[3] == 2)
										move = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									newBoardView.getBoardFace().makeMove(move, playSound);
								} else {
									Move move = new Move(moveFT[0], moveFT[1], 0, 0);
									newBoardView.getBoardFace().makeMove(move, playSound);
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

				/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
					mainApp.setCurrentGame(new GameItem(lccHolder.getGameData(mainApp.getGameId(), -1), true));
					executePausedActivityGameEvents();
					//lccHolder.setActivityPausedMode(false);
					lccHolder.getWhiteClock().paint();
					lccHolder.getBlackClock().paint();
					*//*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
							  lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
							  lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*//*
				} else {*/
					mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));
//				}

				if (openChatActivity()) {
					return;
				}

				if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
					newBoardView.getBoardFace().setChess960(true);


				if (!isUserColorWhite()) {
					newBoardView.getBoardFace().setReside(true);
				}
				String[] moves = {};


				if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "")
							.replaceAll("  ", " ").substring(1).split(" ");
					newBoardView.getBoardFace().setMovesCount(moves.length);
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


				for (int i = 0,cnt = newBoardView.getBoardFace().getMovesCount(); i < cnt; i++) {
					moveFT = MoveParser.parse(newBoardView.getBoardFace(), moves[i]);
					if (moveFT.length == 4) {
						Move move;
						if (moveFT[3] == 2) {
							move = new Move(moveFT[0], moveFT[1], 0, 2);
						} else {
							move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
						}
						newBoardView.getBoardFace().makeMove(move, false);
					} else {
						Move move = new Move(moveFT[0], moveFT[1], 0, 0);
						newBoardView.getBoardFace().makeMove(move, false);
					}
				}

				update(CALLBACK_REPAINT_UI);
				newBoardView.getBoardFace().takeBack();
				newBoardView.invalidate();

				playLastMoveAnimation();

				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) && appService != null
						&& appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					if (!mainApp.isLiveChess()) {
						appService.RunRepeatableTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
								"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID
										+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
										+ "&gid=" + mainApp.getGameId(),
								null/*progressDialog*/
						);
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

        Intent intent = new Intent(coreContext, ChatActivity.class);
        intent.putExtra(GameListItem.GAME_ID, mainApp.getCurrentGameId() );
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
            CommonUtils.showNotification(coreContext, "", mainApp.getGameId(),"","",ChatActivity.class);
        }
    }

	@Override
	public void newGame() {
		// TODO investigate where this came from
//		int i;
//		ArrayList<GameListItem> currentGames = new ArrayList<GameListItem>();
//		for (GameListItem gle : mainApp.getGameListItems()) {
//			if (gle.type == 1 && gle.values.get("is_my_turn").equals("1")) {
//				currentGames.add(gle);
//			}
//		}
//		for (i = 0; i < currentGames.size(); i++) {
//			if (currentGames.get(i).values.get(GameListItem.GAME_ID).contains(mainApp.getCurrentGame()
//																	.values.get(GameListItem.GAME_ID))) {
//				if (i + 1 < currentGames.size()) {
//					newBoardView.getBoardFace().setAnalysis(false);
//					newBoardView.setBoardFace(new ChessBoard(this));
//					newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);
//					getOnlineGame(currentGames.get(i + 1).values.get(GameListItem.GAME_ID));
//					return;
//				} else {
//					onBackPressed();
//					return;
//				}
//			}
//		}
//		onBackPressed();
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
		menuInflater.inflate(R.menu.game_echess, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_next_game: // TODO move to action bar
				newGame();
				break;
			case R.id.menu_options: // TODO move to action bar
				showOptions();
				break;
			case R.id.menu_analysis:
				newBoardView.switchAnalysis();
				break;
			case R.id.menu_chat:
				chat = true;
				getOnlineGame(mainApp.getGameId());
				break;
			case R.id.menu_previous:
				newBoardView.moveBack();
				isMoveNav = true;
				break;
			case R.id.menu_next:
				newBoardView.moveForward();
				isMoveNav = true;
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		final CharSequence[] items;
		private final int ECHESS_SETTINGS = 0;
		private final int ECHESS_BACK_TO_GAME_LIST = 1;
		private final int ECHESS_MESSAGES = 2;
		private final int ECHESS_RESIDE = 3;
		private final int ECHESS_DRAW_OFFER = 4;
		private final int ECHESS_RESIGN_OR_ABORT = 5;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case ECHESS_SETTINGS:
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					break;
				case ECHESS_BACK_TO_GAME_LIST:
					onBackPressed();
					break;
				case ECHESS_MESSAGES:
					chat = true;
					getOnlineGame(mainApp.getGameId());
					break;
				case ECHESS_RESIDE:
					newBoardView.getBoardFace().setReside(!newBoardView.getBoardFace().isReside());
					newBoardView.invalidate();
					break;
				case ECHESS_DRAW_OFFER:
					showDialog(DIALOG_DRAW_OFFER);
					break;
				case ECHESS_RESIGN_OR_ABORT:
					showDialog(DIALOG_ABORT_OR_RESIGN);
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainApp.getCurrentGame() != null) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
			newBoardView.setBoardFace(new ChessBoard(this));
			newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		}

		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(chatMessageReceiver);
	}

	@Override
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		gamePanelView.haveNewMessage(true);
//		chatPanel.setVisibility(View.GONE);
	}

	/*public void onStop()
		  {
			mainApp.getCurrentGame() = null;
			newBoardView.boardBitmap = null;
			super.onStop();
		  }*/

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
//			chatPanel.setVisibility(View.VISIBLE);
			Log.d("TEST", "new message");
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
		} else */
		if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			newBoardView.getBoardFace().takeBack();
			newBoardView.getBoardFace().decreaseMovesCount();
			newBoardView.invalidate();
		} else if (view.getId() == R.id.submit) {
			update(CALLBACK_SEND_MOVE);
		}
	}

}