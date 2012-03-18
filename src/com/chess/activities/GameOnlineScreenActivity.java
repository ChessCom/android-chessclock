package com.chess.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.IntentConstants;
import com.chess.core.MainApp;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.User;
import com.chess.model.GameListElement;
import com.chess.utilities.*;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameOnlineScreenActivity extends GameBaseActivity implements View.OnClickListener {

	private final static int DIALOG_TACTICS_START_TACTICS = 1;

	private final static int CALLBACK_ECHESS_MOVE_WAS_SENT = 8;
	private final static int CALLBACK_SEND_MOVE = 1;
	private final static int CALLBACK_GET_ECHESS_GAME_AND_SEND_MOVE = 12;

	//	private LinearLayout analysisLL;
//	private LinearLayout analysisButtons;
	private RelativeLayout chatPanel;
	private ImageButton chatButton;
	//	private TextView timer;
	private int UPDATE_DELAY = 10000;
	private View submitButtonsLay;


	private MenuOptionsDialogListener menuOptionsDialogListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive2);
		init();
		widgetsInit();
		onPostCreate();
	}

	protected void widgetsInit() {
		super.widgetsInit();

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
//		analysisLL = (LinearLayout) findViewById(R.id.analysis);
//		analysisButtons = (LinearLayout) findViewById(R.id.analysisButtons);
		if (mainApp.isLiveChess() && !MainApp.isTacticsGameMode(extras.getInt(AppConstants.GAME_MODE))) {
			chatPanel = (RelativeLayout) findViewById(R.id.chatPanel);
			chatButton = (ImageButton) findViewById(R.id.chat);
			chatButton.setOnClickListener(this);
		}
//		if (!mainApp.isLiveChess()) {
//			findViewById(R.id.prev).setOnClickListener(this);
//			findViewById(R.id.next).setOnClickListener(this);
//		}

//		timer = (TextView) findViewById(R.id.timer);

		if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(extras.getInt(AppConstants.GAME_MODE))
				&& lccHolder.getWhiteClock() != null && lccHolder.getBlackClock() != null) {
			whiteClockView.setVisibility(View.VISIBLE);
			blackClockView.setVisibility(View.VISIBLE);
			lccHolder.getWhiteClock().paint();
			lccHolder.getBlackClock().paint();
			final com.chess.live.client.Game game = lccHolder.getGame(new Long(extras.getString(AppConstants.GAME_ID)));
			final User whiteUser = game.getWhitePlayer();
			final User blackUser = game.getBlackPlayer();
			final Boolean isWhite = (!game.isMoveOf(whiteUser) && !game.isMoveOf(blackUser)) ? null : game.isMoveOf(whiteUser);
			lccHolder.setClockDrawPointer(isWhite);
		}


//		if (newBoardView.getBoardFace() == null) {
		newBoardView.setBoardFace(new Board2(this));
		newBoardView.setGameActivityFace(this);
		newBoardView.getBoardFace().setInit(true);
		newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
		//newBoardView.getBoardFaceFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

		if (MainApp.isComputerGameMode(newBoardView.getBoardFace())
				&& !mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").equals("")) {
			int i;
			String[] moves = mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").split("[|]");
			for (i = 1; i < moves.length; i++) {
				String[] move = moves[i].split(":");
				newBoardView.getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			}
			if (MainApp.isComputerVsHumanBlackGameMode(newBoardView.getBoardFace()))
				newBoardView.getBoardFace().setReside(true);
		} else {
			if (MainApp.isComputerVsHumanBlackGameMode(newBoardView.getBoardFace())) {
				newBoardView.getBoardFace().setReside(true);
				newBoardView.invalidate();
				newBoardView.computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
			if (MainApp.isComputerVsComputerGameMode(newBoardView.getBoardFace())) {
				newBoardView.computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
			if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace()))
				mainApp.setGameId(extras.getString(AppConstants.GAME_ID));
		}
		if (MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
			showDialog(DIALOG_TACTICS_START_TACTICS);
		}
//		}
	}

	protected void init() {
		super.init();
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
				final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
				LccHolder.LOG.info("Request draw: " + game);
				lccHolder.getAndroid().runMakeDrawTask(game);
			} else {
				String Draw = "OFFERDRAW";
				if (mainApp.acceptdraw)
					Draw = "ACCEPTDRAW";
				String result = Web.Request("http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&chessid=" + mainApp.getCurrentGame().values.get(AppConstants.GAME_ID) + "&command=" + Draw + "&timestamp=" + mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP), "GET", null, null);
				if (result.contains("Success")) {
					mainApp.ShowDialog(coreContext, "", getString(R.string.drawoffered));
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Game.this, "Error", result);
				}
			}
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
				final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());

				if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
					System.out.println("LCCLOG: resign game by fair play restriction: " + game);
					LccHolder.LOG.info("Resign game: " + game);
					lccHolder.getAndroid().runMakeResignTask(game);
				} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
					LccHolder.LOG.info("LCCLOG: abort game: " + game);
					lccHolder.getAndroid().runAbortGameTask(game);
				} else {
					LccHolder.LOG.info("LCCLOG: resign game: " + game);
					lccHolder.getAndroid().runMakeResignTask(game);
				}
				finish();
			} else {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/submit_echess_action?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&chessid=" + mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)
						+ "&command=RESIGN&timestamp="
						+ mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP), "GET", null, null);
				if (result.contains("Success")) {
					if (MobclixHelper.isShowAds(mainApp)) {
						sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
								.putExtra(AppConstants.MESSAGE, "GAME OVER")
								.putExtra(AppConstants.FINISHABLE, true));
					} else {
						finish();
					}
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Game.this, "Error", result);
				}
			}
		}
	}


	protected void getOnlineGame(final String game_id) {
		super.getOnlineGame(game_id);
		if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
			update(CALLBACK_GAME_STARTED);
		} else {
			if (appService != null) {
				appService.RunSingleTask(CALLBACK_GAME_STARTED,
						"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
						null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
			}
		}
	}

	@Override
	public void update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
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
						if (!mainApp.isLiveChess()) {
							appService.RunRepeatableTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
									"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
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
								+ mainApp.getCurrentGame().values.get("white_rating") + ")");
						blackPlayerLabel.setText(mainApp.getCurrentGame()
								.values.get(AppConstants.BLACK_USERNAME)
								+ "\n(" + mainApp.getCurrentGame().values.get("black_rating") + ")");
					}
				}


				newBoardView.addMove2Log(newBoardView.getBoardFace().MoveListSAN());
				/*if(mainApp.getCurrentGame() != null && mainApp.getCurrentGame().values.get("move_list") != null)
								{
								  movelist.setText(mainApp.getCurrentGame().values.get("move_list"));
								}
								else
								{
								  movelist.setText(newBoardView.getBoardFaceFace().MoveListSAN());
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
						lccHolder.makeMove(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID), move);
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
								"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
								null);
					} else {
						appService.RunSingleTask(CALLBACK_ECHESS_MOVE_WAS_SENT,
								"http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&chessid=" +
										mainApp.getCurrentGame().values.get(AppConstants.GAME_ID) + "&command=SUBMIT&newmove=" +
										newBoardView.getBoardFace().convertMoveEchess() + "&timestamp=" +
										mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP),
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
							"http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" +
									mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&chessid=" +
									mainApp.getCurrentGame().values.get(AppConstants.GAME_ID) + "&command=SUBMIT&newmove=" +
									newBoardView.getBoardFace().convertMoveEchess() + "&timestamp=" +
									mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP),
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
				if (mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
						+ AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 2) {
					finish();
				} else if (mainApp.getSharedData().getInt(mainApp.getSharedData()
						.getString(AppConstants.USERNAME, "") + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 0) {

					int i;
					ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
					for (GameListElement gle : mainApp.getGameListItems()) {
						if (gle.type == 1 && gle.values.get("is_my_turn").equals("1")) {
							currentGames.add(gle);
						}
					}
					for (i = 0; i < currentGames.size(); i++) {
						if (currentGames.get(i).values.get(AppConstants.GAME_ID)
								.contains(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID))) {
							if (i + 1 < currentGames.size()) {
								newBoardView.setBoardFace(new Board2(this));
								newBoardView.getBoardFace().setAnalysis(false);
								newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}

								getOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
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
				//System.out.println("!!!!!!!! mainApp.getCurrentGame() " + mainApp.getCurrentGame());
				//System.out.println("!!!!!!!! game " + game);

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				int[] moveFT;
				if (!mainApp.getCurrentGame().equals(game)) {
					if (!mainApp.getCurrentGame().values.get("move_list").equals(game.values.get("move_list"))) {
						mainApp.setCurrentGame(game);
						String[] Moves = {};

						if (mainApp.getCurrentGame().values.get("move_list").contains("1.")
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())))) {

							int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) ? 0 : 1;

							Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

							if (Moves.length - newBoardView.getBoardFace().getMovesCount() == 1) {
								if (mainApp.isLiveChess()) {
									moveFT = MoveParser2.parseCoordinate(newBoardView.getBoardFace(), Moves[Moves.length - 1]);
								} else {
									moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), Moves[Moves.length - 1]);
								}
								boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).getSeq() == Moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move m;
									if (moveFT[3] == 2)
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									newBoardView.getBoardFace().makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									newBoardView.getBoardFace().makeMove(m, playSound);
								}
								//mainApp.ShowMessage("Move list updated!");
								newBoardView.getBoardFace().setMovesCount(Moves.length);
								newBoardView.invalidate();
								update(CALLBACK_REPAINT_UI);
							}
						}
						return;
					}
					if (game.values.get("has_new_message").equals("1")) {
						mainApp.setCurrentGame(game);
						if (!msgShowed) {
							msgShowed = true;
							new AlertDialog.Builder(coreContext)
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setTitle(getString(R.string.you_got_new_msg))
									.setPositiveButton(R.string.browse, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											chat = true;
											getOnlineGame(mainApp.getGameId());
											msgShowed = false;
										}
									})
									.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
										}
									}).create().show();
						}
						return;
					} else {
						msgShowed = false;
					}
				}
				break;

			case CALLBACK_GAME_STARTED:
				getSoundPlayer().playGameStart();

				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
					mainApp.setCurrentGame(new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(), -1), true));
					executePausedActivityGameEvents();
					//lccHolder.setActivityPausedMode(false);
					lccHolder.getWhiteClock().paint();
					lccHolder.getBlackClock().paint();
					/*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
							  lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
							  lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*/
				} else {
					mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));
				}

				if (chat) {
					if (!isUserColorWhite())
						mainApp.getSharedDataEditor().putString("opponent", mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME));
					else
						mainApp.getSharedDataEditor().putString("opponent", mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME));
					mainApp.getSharedDataEditor().commit();
					mainApp.getCurrentGame().values.put("has_new_message", "0");
					startActivity(new Intent(coreContext, mainApp.isLiveChess() ? ChatLiveActivity.class : ChatActivity.class).
							putExtra(AppConstants.GAME_ID, mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).
							putExtra(AppConstants.TIMESTAMP, mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP)));
					chat = false;
					return;
				}

				if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
					newBoardView.getBoardFace().setChess960(true);


				if (!isUserColorWhite()) {
					newBoardView.getBoardFace().setReside(true);
				}
				String[] Moves = {};


				if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
					Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					newBoardView.getBoardFace().setMovesCount(Moves.length);
				} else if (!mainApp.isLiveChess()) {
					newBoardView.getBoardFace().setMovesCount(0);
				}

				final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				String FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
				if (!FEN.equals("")) {
					newBoardView.getBoardFace().genCastlePos(FEN);
					MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
				}

				int i;
				//System.out.println("@@@@@@@@ POINT 2 newBoardView.getBoardFaceFace().getMovesCount()=" + newBoardView.getBoardFaceFace().getMovesCount());
				//System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

				if (!mainApp.isLiveChess()) {
					for (i = 0; i < newBoardView.getBoardFace().getMovesCount(); i++) {
						//System.out.println("@@@@@@@@ POINT 4 i=" + i);
						//System.out.println("================ POINT 5 Moves[i]=" + Moves[i]);
						moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), Moves[i]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2) {
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							} else {
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							}
							newBoardView.getBoardFace().makeMove(m, false);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							newBoardView.getBoardFace().makeMove(m, false);
						}
					}
				}

				update(CALLBACK_REPAINT_UI);
				newBoardView.getBoardFace().takeBack();
				newBoardView.invalidate();

				//last move anim
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1300);
							newBoardView.getBoardFace().takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {
						}
					}

					private Handler update = new Handler() {
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							update(CALLBACK_REPAINT_UI);
							newBoardView.invalidate();
						}
					};
				}).start();

				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) && appService != null && appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					if (!mainApp.isLiveChess()) {
						appService.RunRepeatableTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
								"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
								null/*progressDialog*/
						);
					}
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void showChoosePieceDialog(final int col, final int row) {
		new AlertDialog.Builder(this)
				.setTitle("Choose a piece ")
				.setItems(new String[]{"Queen", "Rook", "Bishop", "Knight", "Cancel"},
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (which == 4) {
									newBoardView.invalidate();
									return;
								}
								newBoardView.promote(4 - which, col, row);
							}
						}).setCancelable(false)
				.create().show();
	}

	@Override
	public void newGame() {
		int i;
		ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
		for (GameListElement gle : mainApp.getGameListItems()) {
			if (gle.type == 1 && gle.values.get("is_my_turn").equals("1")) {
				currentGames.add(gle);
			}
		}
		for (i = 0; i < currentGames.size(); i++) {
			if (currentGames.get(i).values.get(AppConstants.GAME_ID).contains(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID))) {
				if (i + 1 < currentGames.size()) {
					newBoardView.getBoardFace().setAnalysis(false);
					newBoardView.setBoardFace(new Board2(this));
					newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);
					getOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
					return;
				} else {
					onBackPressed();
					return;
				}
			}
		}
		onBackPressed();
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
//				newBoardView.getBoardFace().setAnalysis(true);
//				update(CALLBACK_REPAINT_UI);
				break;
			case R.id.menu_chat:
				chat = true;
				getOnlineGame(mainApp.getGameId());
				break;
			case R.id.menu_previous:
				newBoardView.moveBack();
//				newBoardView.finished = false;
//				newBoardView.sel = false;
//				newBoardView.getBoardFace().takeBack();
//				newBoardView.invalidate();
//				update(CALLBACK_REPAINT_UI);
				isMoveNav = true;
				break;
			case R.id.menu_next:
				newBoardView.moveForward();
//				newBoardView.getBoardFace().takeNext();
//				newBoardView.invalidate();
//				update(CALLBACK_REPAINT_UI);
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
		if (mainApp.getCurrentGame().values.get("has_new_message").equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
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
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(chatMessageReceiver);
	}

	@Override
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		chatPanel.setVisibility(View.GONE);
	}


	private BroadcastReceiver gameInfoMessageReceived = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
			mainApp.ShowDialog(coreContext, intent.getExtras()
					.getString(AppConstants.TITLE), intent.getExtras().getString(AppConstants.MESSAGE));
		}
	};

	public TextView getWhiteClockView() {
		return whiteClockView;
	}

	public TextView getBlackClockView() {
		return blackClockView;
	}


	/*public void onStop()
		  {
			mainApp.getCurrentGame() = null;
			newBoardView.boardBitmap = null;
			super.onStop();
		  }*/

//	private void showAnalysisButtons() {
//		analysisButtons.setVisibility(View.VISIBLE);
//		findViewById(R.id.moveButtons).setVisibility(View.GONE);
//		/*newBoardView.getBoardFaceFace().takeBack();
//			newBoardView.getBoardFaceFace().getMovesCount()--;
//			newBoardView.invalidate();
//			newBoardView.getBoardFaceFace().setSubmit( false;*/
//	}
//
//	private void hideAnalysisButtons() {
//		analysisButtons.setVisibility(View.GONE);
//	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
			chatPanel.setVisibility(View.VISIBLE);
		}
	};


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.chat) {
			chat = true;
			getOnlineGame(mainApp.getGameId());
			chatPanel.setVisibility(View.GONE);
//		} else if (view.getId() == R.id.prev) {
//			newBoardView.finished = false;
//			newBoardView.sel = false;
//			newBoardView.getBoardFace().takeBack();
//			newBoardView.invalidate();
//			update(CALLBACK_REPAINT_UI);
//			isMoveNav = true;
//		} else if (view.getId() == R.id.next) {
//			newBoardView.getBoardFace().takeNext();
//			newBoardView.invalidate();
//			update(CALLBACK_REPAINT_UI);
//			isMoveNav = true;
		} else if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			newBoardView.getBoardFace().takeBack();
			newBoardView.getBoardFace().decreaseMovesCount();
			newBoardView.invalidate();
		} else if (view.getId() == R.id.submit) {
			update(CALLBACK_SEND_MOVE);
		} else if (view.getId() == R.id.newGame) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}
	}

}