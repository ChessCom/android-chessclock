package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;
import com.chess.R;
import com.chess.core.*;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.GameEvent;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.User;
import com.chess.model.GameListElement;
import com.chess.utilities.*;
import com.chess.views.BoardView2;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;

import java.util.ArrayList;
import java.util.Timer;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameOnlineScreenActivity extends CoreActivityActionBar implements View.OnClickListener{

	private final static int DIALOG_TACTICS_LIMIT = 0;
	private final static int DIALOG_TACTICS_START_TACTICS = 1;
	private final static int DIALOG_TACTICS_HUNDRED = 2;
	private final static int DIALOG_TACTICS_OFFLINE_RATING = 3;
	private final static int DIALOG_DRAW_OFFER = 4;
	private final static int DIALOG_ABORT_OR_RESIGN = 5;

	private final static int CALLBACK_GAME_STARTED = 10;
	private final static int CALLBACK_GET_TACTICS = 7;
	private final static int CALLBACK_ECHESS_MOVE_WAS_SENT = 8;
	private final static int CALLBACK_REPAINT_UI = 0;
	private final static int CALLBACK_GAME_REFRESH = 9;
	private final static int CALLBACK_TACTICS_CORRECT = 6;
	private final static int CALLBACK_TACTICS_WRONG = 5;
	private final static int CALLBACK_SEND_MOVE = 1;
	private final static int CALLBACK_GET_ECHESS_GAME_AND_SEND_MOVE = 12;

	public BoardView2 boardView;
	private LinearLayout analysisLL;
	private LinearLayout analysisButtons;
	private RelativeLayout chatPanel;
	private ImageButton chatButton;
	private TextView white;
	private TextView black;
	private TextView thinking;
	private TextView timer;
	private TextView movelist;
	private Timer onlineGameUpdate = null;
	private Timer tacticsTimer = null;
	private boolean msgShowed = false, isMoveNav = false, chat = false;
	// private String gameId = "";
	private int UPDATE_DELAY = 10000;
	private int resignOrAbort = R.string.resign;

	private com.chess.model.Game game;

	private TextView whiteClockView;
	private TextView blackClockView;

	protected AlertDialog adPopup;
	private TextView endOfGameMessage;
	private LinearLayout adviewWrapper;

	private DrawOfferDialogListener drawOfferDialogListener;
	private AbortGameDialogListener abortGameDialogListener;

	private MenuOptionsDialogListener menuOptionsDialogListener;

	private CharSequence[] menuOptionsItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(CommonUtils.needFullScreen(this)){
			setFullscreen();
			savedInstanceState = new Bundle();
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN,true);
		}
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive2);
//			lccHolder.getAndroid().setGameActivity(this);   //TODO


		init();

		analysisLL = (LinearLayout) findViewById(R.id.analysis);
		analysisButtons = (LinearLayout) findViewById(R.id.analysisButtons);
		if (mainApp.isLiveChess() && !MainApp.isTacticsGameMode(extras.getInt(AppConstants.GAME_MODE))) {
			chatPanel = (RelativeLayout) findViewById(R.id.chatPanel);
			chatButton = (ImageButton) findViewById(R.id.chat);
			chatButton.setOnClickListener(this);
		}
		if (!mainApp.isLiveChess()) {
			findViewById(R.id.prev).setOnClickListener(this);
			findViewById(R.id.next).setOnClickListener(this);
		}

		white = (TextView) findViewById(R.id.white);
		black = (TextView) findViewById(R.id.black);
		thinking = (TextView) findViewById(R.id.thinking);
		timer = (TextView) findViewById(R.id.timer);
		movelist = (TextView) findViewById(R.id.movelist);

		whiteClockView = (TextView) findViewById(R.id.whiteClockView);
		blackClockView = (TextView) findViewById(R.id.blackClockView);
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

		endOfGameMessage = (TextView) findViewById(R.id.endOfGameMessage);

		boardView = (BoardView2) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setBoard((Board2) getLastNonConfigurationInstance());

		lccHolder = mainApp.getLccHolder();

		if (boardView.getBoard() == null) {
			boardView.setBoard(new Board2(this));
			boardView.getBoard().init = true;
			boardView.getBoard().mode = extras.getInt(AppConstants.GAME_MODE);
			boardView.getBoard().GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
			//boardView.getBoard().GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

			if (MainApp.isComputerGameMode(boardView)
					&& !mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").equals("")) {
				int i;
				String[] moves = mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").split("[|]");
				for (i = 1; i < moves.length; i++) {
					String[] move = moves[i].split(":");
					boardView.getBoard().makeMove(new Move(
							Integer.parseInt(move[0]),
							Integer.parseInt(move[1]),
							Integer.parseInt(move[2]),
							Integer.parseInt(move[3])), false);
				}
				if (MainApp.isComputerVsHumanBlackGameMode(boardView))
					boardView.getBoard().setReside(true);
			} else {
				if (MainApp.isComputerVsHumanBlackGameMode(boardView)) {
					boardView.getBoard().setReside(true);
					boardView.invalidate();
					boardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (MainApp.isComputerVsComputerGameMode(boardView)) {
					boardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (MainApp.isLiveOrEchessGameMode(boardView) || MainApp.isFinishedEchessGameMode(boardView))
					mainApp.setGameId(extras.getString(AppConstants.GAME_ID));
			}
			if (MainApp.isTacticsGameMode(boardView)) {
				showDialog(DIALOG_TACTICS_START_TACTICS);
				return;
			}
		}

		if (MobclixHelper.isShowAds(mainApp) /*&& getRectangleAdview() == null*/ && mainApp.getTabHost() != null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4")) {
			setRectangleAdview(new MobclixIABRectangleMAdView(this));
			getRectangleAdview().setRefreshTime(-1);
			getRectangleAdview().addMobclixAdViewListener(new MobclixAdViewListenerImpl(true, mainApp));
			mainApp.setForceRectangleAd(false);
		}

		Update(CALLBACK_REPAINT_UI);
	}

	private class DrawOfferDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) {
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
	}

	private class AbortGameDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) {
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
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DRAW_OFFER:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.drawoffer)
						.setMessage(getString(R.string.are_you_sure_q))
						.setPositiveButton(getString(R.string.ok), drawOfferDialogListener)
						.setNegativeButton(getString(R.string.cancel),drawOfferDialogListener)
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

	private void init(){
		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.backtogamelist),
				getString(R.string.messages),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(R.string.resignorabort)};

		drawOfferDialogListener = new DrawOfferDialogListener();
		abortGameDialogListener = new AbortGameDialogListener();

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);

	}


	private void GetOnlineGame(final String game_id) {
		if (appService != null && appService.getRepeatableTimer() != null) {
			appService.getRepeatableTimer().cancel();
			appService.setRepeatableTimer(null);
		}
		mainApp.setGameId(game_id);

		if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) {
			Update(CALLBACK_GAME_STARTED);
		} else {
			if (appService != null) {
				appService.RunSingleTask(CALLBACK_GAME_STARTED,
						"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
						null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
			}
		}
	}


//	@Override
	public void LoadPrev(int code) {
		if (boardView.getBoard() != null && MainApp.isTacticsGameMode(boardView)) {
//			//mainApp.getTabHost().setCurrentTab(0);
			boardView.getBoard().setTacticCanceled(true);
			onBackPressed();
		} else {
			finish();
		}
	}

	@Override
	public void Update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				onBackPressed();
				break;
			case INIT_ACTIVITY:
				if (boardView.getBoard().init && MainApp.isLiveOrEchessGameMode(boardView) || MainApp.isFinishedEchessGameMode(boardView)) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					GetOnlineGame(mainApp.getGameId());
					boardView.getBoard().init = false;
				} else if (!boardView.getBoard().init) {
					if (MainApp.isLiveOrEchessGameMode(boardView) && appService != null
							&& appService.getRepeatableTimer() == null) {
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
						if (!mainApp.isLiveChess()) {
							appService.RunRepeatbleTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
									"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
									null/*progressDialog*/
							);
						}
					}
				}
				break;
			case CALLBACK_REPAINT_UI: {
				switch (boardView.getBoard().mode) {
					case AppConstants.GAME_MODE_LIVE_OR_ECHESS: {
						if (boardView.getBoard().submit)
							findViewById(R.id.moveButtons).setVisibility(View.VISIBLE);
						findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Update(CALLBACK_SEND_MOVE);
							}
						});
						findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								findViewById(R.id.moveButtons).setVisibility(View.GONE);
								boardView.getBoard().takeBack();
								boardView.getBoard().movesCount--;
								boardView.invalidate();
								boardView.getBoard().submit = false;
							}
						});
						if (boardView.getBoard().analysis) {
							white.setVisibility(View.GONE);
							black.setVisibility(View.GONE);
							analysisLL.setVisibility(View.VISIBLE);
							if (!mainApp.isLiveChess() && analysisButtons != null) {
								showAnalysisButtons();
							}
						} else {
							white.setVisibility(View.VISIBLE);
							black.setVisibility(View.VISIBLE);
							analysisLL.setVisibility(View.GONE);
							if (!mainApp.isLiveChess() && analysisButtons != null) {
								hideAnalysisButtons();
							}
						}

						break;
					}
					default:
						break;
				}

				if (MainApp.isComputerGameMode(boardView)) {
					hideAnalysisButtons();
				}

				if (MainApp.isLiveOrEchessGameMode(boardView) || MainApp.isFinishedEchessGameMode(boardView)) {
					if (mainApp.getCurrentGame() != null) {
						white.setText(mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("white_rating") + ")");
						black.setText(mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("black_rating") + ")");
					}
				}

				if (MainApp.isTacticsGameMode(boardView)) {
					if (boardView.getBoard().analysis) {
						timer.setVisibility(View.GONE);
						analysisLL.setVisibility(View.VISIBLE);
						if (!mainApp.isLiveChess() && analysisButtons != null) {
							showAnalysisButtons();
						}
					} else {
						white.setVisibility(View.GONE);
						black.setVisibility(View.GONE);
						timer.setVisibility(View.VISIBLE);
						analysisLL.setVisibility(View.GONE);
						if (!mainApp.isLiveChess() && analysisButtons != null) {
							hideAnalysisButtons();
						}
					}
				}
				movelist.setText(boardView.getBoard().MoveListSAN());
				/*if(mainApp.getCurrentGame() != null && mainApp.getCurrentGame().values.get("move_list") != null)
								{
								  movelist.setText(mainApp.getCurrentGame().values.get("move_list"));
								}
								else
								{
								  movelist.setText(boardView.getBoard().MoveListSAN());
								}*/
				boardView.invalidate();

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						boardView.requestFocus();
					}
				});
				break;
			}
			case CALLBACK_SEND_MOVE: {
				findViewById(R.id.moveButtons).setVisibility(View.GONE);
				boardView.getBoard().submit = false;
				//String myMove = boardView.getBoard().MoveSubmit();
				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) {
					final String move = boardView.getBoard().convertMoveLive();
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
										boardView.getBoard().convertMoveEchess() + "&timestamp=" +
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
									boardView.getBoard().convertMoveEchess() + "&timestamp=" +
									mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP),
									progressDialog = new MyProgressDialog(
									ProgressDialog.show(this, null, getString(R.string.sendinggameinfo), true)));
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(1);
					Notifications.resetCounter();
				}
				break;
			}
			case 2: {
				white.setVisibility(View.GONE);
				black.setVisibility(View.GONE);
				thinking.setVisibility(View.VISIBLE);
				break;
			}
			case 3: {
				white.setVisibility(View.VISIBLE);
				black.setVisibility(View.VISIBLE);
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
								boardView.setBoard(new Board2(this));
								boardView.getBoard().analysis = false;
								boardView.getBoard().mode = AppConstants.GAME_MODE_LIVE_OR_ECHESS;

								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}

								GetOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
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
				if (boardView.getBoard().analysis)
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
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)))) {

							int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) ? 0 : 1;

							Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

							if (Moves.length - boardView.getBoard().movesCount == 1) {
								if (mainApp.isLiveChess()) {
									moveFT = MoveParser2.parseCoordinate(boardView.getBoard(), Moves[Moves.length - 1]);
								} else {
									moveFT = MoveParser2.Parse(boardView.getBoard(), Moves[Moves.length - 1]);
								}
								boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).getSeq() == Moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move m;
									if (moveFT[3] == 2)
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									boardView.getBoard().makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									boardView.getBoard().makeMove(m, playSound);
								}
								//mainApp.ShowMessage("Move list updated!");
								boardView.getBoard().movesCount = Moves.length;
								boardView.invalidate();
								Update(CALLBACK_REPAINT_UI);
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
											GetOnlineGame(mainApp.getGameId());
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

				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) {
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
					startActivity(new Intent(coreContext, mainApp.isLiveChess() ? ChatLive.class : Chat.class).
							putExtra(AppConstants.GAME_ID, mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).
							putExtra(AppConstants.TIMESTAMP, mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP)));
					chat = false;
					return;
				}

				if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
					boardView.getBoard().chess960 = true;


				if (!isUserColorWhite()) {
					boardView.getBoard().setReside(true);
				}
				String[] Moves = {};


				if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
					Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					boardView.getBoard().movesCount = Moves.length;
				} else if (!mainApp.isLiveChess()) {
					boardView.getBoard().movesCount = 0;
				}

				final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				String FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
				if (!FEN.equals("")) {
					boardView.getBoard().GenCastlePos(FEN);
					MoveParser2.FenParse(FEN, boardView.getBoard());
				}

				int i;
				//System.out.println("@@@@@@@@ POINT 2 boardView.getBoard().movesCount=" + boardView.getBoard().movesCount);
				//System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

				if (!mainApp.isLiveChess()) {
					for (i = 0; i < boardView.getBoard().movesCount; i++) {
						//System.out.println("@@@@@@@@ POINT 4 i=" + i);
						//System.out.println("================ POINT 5 Moves[i]=" + Moves[i]);
						moveFT = MoveParser2.Parse(boardView.getBoard(), Moves[i]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2) {
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							} else {
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							}
							boardView.getBoard().makeMove(m, false);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							boardView.getBoard().makeMove(m, false);
						}
					}
				}

				Update(CALLBACK_REPAINT_UI);
				boardView.getBoard().takeBack();
				boardView.invalidate();

				//last move anim
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1300);
							boardView.getBoard().takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {
						}
					}

					private Handler update = new Handler() {
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							Update(CALLBACK_REPAINT_UI);
							boardView.invalidate();
						}
					};
				}).start();

				if (MainApp.isLiveOrEchessGameMode(boardView) && appService != null && appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					if (!mainApp.isLiveChess()) {
						appService.RunRepeatbleTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
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
	public Object onRetainNonConfigurationInstance() {
		return boardView.getBoard();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();



		if (MainApp.isTacticsGameMode(boardView)) {
			menuInflater.inflate(R.menu.game_echess, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_next_game:
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
							boardView.getBoard().analysis = false;
							boardView.setBoard(new Board2(this));
							boardView.getBoard().mode = AppConstants.GAME_MODE_LIVE_OR_ECHESS;
							GetOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
							return true;
						} else {
							onBackPressed();
							return true;
						}
					}
				}
				onBackPressed();
				break;
			case R.id.menu_options:
				new AlertDialog.Builder(this)
						.setTitle(R.string.options)
						.setItems(menuOptionsItems, menuOptionsDialogListener).show();
				break;
			case R.id.menu_analysis:
				boardView.getBoard().analysis = true;
				Update(CALLBACK_REPAINT_UI);
				break;
			case R.id.menu_chat:
				chat = true;
				GetOnlineGame(mainApp.getGameId());
				break;
			case R.id.menu_previous:
				boardView.finished = false;
				boardView.sel = false;
				boardView.getBoard().takeBack();
				boardView.invalidate();
				Update(CALLBACK_REPAINT_UI);
				isMoveNav = true;
				break;
			case R.id.menu_next:
				boardView.getBoard().takeNext();
				boardView.invalidate();
				Update(CALLBACK_REPAINT_UI);
				isMoveNav = true;
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener{
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
			switch (i){
				case ECHESS_SETTINGS:
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					break;
				case ECHESS_BACK_TO_GAME_LIST:
					onBackPressed();
					break;
				case ECHESS_MESSAGES:
					chat = true;
					GetOnlineGame(mainApp.getGameId());
					break;
				case ECHESS_RESIDE:
					boardView.getBoard().setReside(!boardView.getBoard().reside);
					boardView.invalidate();
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

	protected void changeChatIcon(Menu menu){
		if (mainApp.getCurrentGame().values.get("has_new_message").equals("1")){
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		}else{
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainApp.getCurrentGame() != null && (MainApp.isLiveOrEchessGameMode(boardView)
				|| MainApp.isFinishedEchessGameMode(boardView))) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		if (isMoveNav) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					openOptionsMenu();
				}
			}, 10);
			isMoveNav = false;
		}
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		boardView.requestFocus();
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		if (MobclixHelper.isShowAds(mainApp) && mainApp.getTabHost() != null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4") && adviewWrapper != null && getRectangleAdview() != null) {
			adviewWrapper.addView(getRectangleAdview());
			if (mainApp.isForceRectangleAd()) {
				getRectangleAdview().getAd();
			}
		}

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

		super.onResume();

		registerReceiver(gameMoveReceiver, new IntentFilter(IntentConstants.ACTION_GAME_MOVE ));
		registerReceiver(gameEndMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_END));
		registerReceiver(gameInfoMessageReceived, new IntentFilter(IntentConstants.ACTION_GAME_INFO));
		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
		registerReceiver(showGameEndPopupReceiver, new IntentFilter(IntentConstants.ACTION_SHOW_GAME_END_POPUP));

//		if (mainApp.isLiveChess() && mainApp.getGameId() != null && mainApp.getGameId() != ""
//				&& lccHolder.getGame(mainApp.getGameId()) != null) {
//			game = new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(),
//					lccHolder.getGame(mainApp.getGameId()).getSeq() - 1), true);
////			lccHolder.getAndroid().setGameActivity(this); // TODO
//			if (lccHolder.isActivityPausedMode()) {
//				executePausedActivityGameEvents();
//				lccHolder.setActivityPausedMode(false);
//			}
//			//lccHolder.updateClockTime(lccHolder.getGame(mainApp.getGameId()));
//		}

		/*MobclixAdView bannerAdview = mainApp.getBannerAdview();
	 LinearLayout bannerAdviewWrapper = mainApp.getBannerAdviewWrapper();
	 if (bannerAdviewWrapper != null)
	 {
		 bannerAdviewWrapper.removeView(bannerAdview);
	 }*/
		MobclixHelper.pauseAdview(mainApp.getBannerAdview(), mainApp);
		/*mainApp.setBannerAdview(null);
	 mainApp.setBannerAdviewWrapper(null);*/
		//mainApp.setForceBannerAdOnFailedLoad(true);

		disableScreenLock();
	}

	@Override
	protected void onPause() {
		System.out.println("LCCLOG2: GAME ONPAUSE");
		unregisterReceiver(gameMoveReceiver);
		unregisterReceiver(gameEndMessageReceiver);
		unregisterReceiver(gameInfoMessageReceived);
		unregisterReceiver(chatMessageReceiver);
		unregisterReceiver(showGameEndPopupReceiver);

		super.onPause();
		System.out.println("LCCLOG2: GAME ONPAUSE adviewWrapper="
				+ adviewWrapper + ", getRectangleAdview() " + getRectangleAdview());
		if (adviewWrapper != null && getRectangleAdview() != null) {
			System.out.println("LCCLOG2: GAME ONPAUSE 1");
			getRectangleAdview().cancelAd();
			System.out.println("LCCLOG2: GAME ONPAUSE 2");
			adviewWrapper.removeView(getRectangleAdview());
			System.out.println("LCCLOG2: GAME ONPAUSE 3");
		}
		lccHolder.setActivityPausedMode(true);
		lccHolder.getPausedActivityGameEvents().clear();

		boardView.stopThinking = true;

		stopTacticsTimer();
		if (onlineGameUpdate != null)
			onlineGameUpdate.cancel();

		/*if (MobclixHelper.isShowAds(mainApp))
		{
			MobclixHelper.pauseAdview(getRectangleAdview(), mainApp);
		}*/

		enableScreenLock();
	}

	public void stopTacticsTimer() {
		if (tacticsTimer != null) {
			tacticsTimer.cancel();
			tacticsTimer = null;
		}
	}


	private BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
			game = (com.chess.model.Game) intent.getSerializableExtra(AppConstants.OBJECT);
			Update(CALLBACK_GAME_REFRESH);
		}
	};

	private BroadcastReceiver gameEndMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());

			final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
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
			/*final String whiteRating =
					(newWhiteRating != null && newWhiteRating != 0) ?
					newWhiteRating.toString() : mainApp.getCurrentGame().values.get("white_rating");
				  final String blackRating =
					(newBlackRating != null && newBlackRating != 0) ?
					newBlackRating.toString() : mainApp.getCurrentGame().values.get("black_rating");*/
			white.setText(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")");
			black.setText(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
			boardView.finished = true;

			if (MobclixHelper.isShowAds(mainApp)) {
				final LayoutInflater inflater = (LayoutInflater) coreContext.getSystemService(LAYOUT_INFLATER_SERVICE);
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
							} catch (Exception e) {
							}
							adPopup = null;
						}
						startActivity(new Intent(coreContext, OnlineNewGame.class));
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
							} catch (Exception e) {
							}
							adPopup = null;
						}
						startActivity(new Intent(coreContext, Tabs.class));
					}
				});
				home.setVisibility(View.VISIBLE);
			}

			endOfGameMessage.setText(/*intent.getExtras().getString(AppConstants.TITLE) + ": " +*/ intent.getExtras().getString(AppConstants.MESSAGE));
			//mainApp.ShowDialog(Game.this, intent.getExtras().getString(AppConstants.TITLE), intent.getExtras().getString(AppConstants.MESSAGE));
			findViewById(R.id.moveButtons).setVisibility(View.GONE);
			findViewById(R.id.endOfGameButtons).setVisibility(View.VISIBLE);
			chatPanel.setVisibility(View.GONE);
			findViewById(R.id.newGame).setOnClickListener(GameOnlineScreenActivity.this);
			findViewById(R.id.home).setOnClickListener(GameOnlineScreenActivity.this);
			getSoundPlayer().playGameEnd();
		}
	};

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

	private void executePausedActivityGameEvents() {
		if (/*lccHolder.isActivityPausedMode() && */lccHolder.getPausedActivityGameEvents().size() > 0) {
			//boolean fullGameProcessed = false;
			GameEvent gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.Move);
			if (gameEvent != null &&
					(lccHolder.getCurrentGameId() == null
							|| lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
				//lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
				//fullGameProcessed = true;
				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
				//lccHolder.getAndroid().processMove(gameEvent.getGameId(), gameEvent.moveIndex);
				game = new com.chess.model.Game(lccHolder.getGameData(
						gameEvent.getGameId().toString(), gameEvent.getMoveIndex()), true);
				Update(CALLBACK_GAME_REFRESH);
			}

			gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.DrawOffer);
			if (gameEvent != null &&
					(lccHolder.getCurrentGameId() == null
							|| lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
				/*if (!fullGameProcessed)
						{
						  lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
						  fullGameProcessed = true;
						}*/
				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
				lccHolder.getAndroid().processDrawOffered(gameEvent.getDrawOffererUsername());
			}

			gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.EndOfGame);
			if (gameEvent != null &&
					(lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
				/*if (!fullGameProcessed)
						{
						  lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
						  fullGameProcessed = true;
						}*/
				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
				lccHolder.getAndroid().processGameEnd(gameEvent.getGameEndedMessage());
			}
		}
	}

	/*public void onStop()
	  {
		mainApp.getCurrentGame() = null;
		boardView.board = null;
		super.onStop();
	  }*/

	private void showAnalysisButtons() {
		analysisButtons.setVisibility(View.VISIBLE);
		findViewById(R.id.moveButtons).setVisibility(View.GONE);
		/*boardView.getBoard().takeBack();
			boardView.getBoard().movesCount--;
			boardView.invalidate();
			boardView.getBoard().submit = false;*/
	}

	private void hideAnalysisButtons() {
		analysisButtons.setVisibility(View.GONE);
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
			chatPanel.setVisibility(View.VISIBLE);
		}
	};

	private BroadcastReceiver showGameEndPopupReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			if (!MobclixHelper.isShowAds(mainApp)) {
				return;
			}

			final LayoutInflater inflater = (LayoutInflater) coreContext.getSystemService(LAYOUT_INFLATER_SERVICE);
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
						} catch (Exception e) {
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

	public void showGameEndPopup(final View layout, final String message) {
		if (!MobclixHelper.isShowAds(mainApp)) {
			return;
		}

		if (adPopup != null) {
			try {
				adPopup.dismiss();
			} catch (Exception e) {
				System.out.println("MOBCLIX: EXCEPTION IN showGameEndPopup");
				e.printStackTrace();
			}
			adPopup = null;
		}

		try {
			if (adviewWrapper != null && getRectangleAdview() != null) {
				adviewWrapper.removeView(getRectangleAdview());
			}
			adviewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
			System.out.println("MOBCLIX: GET WRAPPER " + adviewWrapper);
			adviewWrapper.addView(getRectangleAdview());

			adviewWrapper.setVisibility(View.VISIBLE);
			//showGameEndAds(adviewWrapper);

			TextView endOfGameMessagePopup = (TextView) layout.findViewById(R.id.endOfGameMessage);
			endOfGameMessagePopup.setText(message);

			adPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialogInterface) {
					if (adviewWrapper != null && getRectangleAdview() != null) {
						adviewWrapper.removeView(getRectangleAdview());
					}
				}
			});
			adPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialogInterface) {
					if (adviewWrapper != null && getRectangleAdview() != null) {
						adviewWrapper.removeView(getRectangleAdview());
					}
				}
			});
		} catch (Exception e) {
			System.out.println("MOBCLIX: EXCEPTION IN showGameEndPopup");
			e.printStackTrace();
		}

		new Handler().postDelayed(new Runnable() {
			public void run() {
				AlertDialog.Builder builder;
				//Context mContext = getApplicationContext();
				builder = new AlertDialog.Builder(coreContext);
				builder.setView(layout);
				adPopup = builder.create();
				adPopup.setCancelable(true);
				adPopup.setCanceledOnTouchOutside(true);
				try {
					adPopup.show();
				} catch (Exception e) {
					return;
				}
			}
		}, 1500);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (boardView.getBoard().analysis) {
				boardView.setBoard(new Board2(this));
				boardView.getBoard().init = true;
				boardView.getBoard().mode = extras.getInt(AppConstants.GAME_MODE);

				if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
					boardView.getBoard().chess960 = true;

				if (!isUserColorWhite()) {
					boardView.getBoard().setReside(true);
				}
				String[] Moves = {};
				if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
					Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					boardView.getBoard().movesCount = Moves.length;
				}

				String FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
				if (!FEN.equals("")) {
					boardView.getBoard().GenCastlePos(FEN);
					MoveParser2.FenParse(FEN, boardView.getBoard());
				}

				int i;
				for (i = 0; i < boardView.getBoard().movesCount; i++) {

					int[] moveFT = mainApp.isLiveChess() ? MoveParser2.parseCoordinate(boardView.getBoard(), Moves[i]) : MoveParser2.Parse(boardView.getBoard(), Moves[i]);
					if (moveFT.length == 4) {
						Move m;
						if (moveFT[3] == 2)
							m = new Move(moveFT[0], moveFT[1], 0, 2);
						else
							m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
						boardView.getBoard().makeMove(m, false);
					} else {
						Move m = new Move(moveFT[0], moveFT[1], 0, 0);
						boardView.getBoard().makeMove(m, false);
					}
				}
				Update(CALLBACK_REPAINT_UI);
				boardView.getBoard().takeBack();
				boardView.invalidate();

				//last move anim
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1300);
							boardView.getBoard().takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {
						}
					}

					private Handler update = new Handler() {
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							Update(CALLBACK_REPAINT_UI);
							boardView.invalidate();
						}
					};
				}).start();
			} else {
				LoadPrev(MainApp.loadPrev);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.chat){
			chat = true;
			GetOnlineGame(mainApp.getGameId());
			chatPanel.setVisibility(View.GONE);
		}else if(view.getId() == R.id.prev){
			boardView.finished = false;
			boardView.sel = false;
			boardView.getBoard().takeBack();
			boardView.invalidate();
			Update(CALLBACK_REPAINT_UI);
			isMoveNav = true;
		}else if(view.getId() == R.id.next){
			boardView.getBoard().takeNext();
			boardView.invalidate();
			Update(CALLBACK_REPAINT_UI);
			isMoveNav = true;
		}else if(view.getId() == R.id.newGame){
			startActivity(new Intent(this, OnlineNewGame.class));
		}else if(view.getId() == R.id.home){
			startActivity(new Intent(this, Tabs.class));
		}
	}

}