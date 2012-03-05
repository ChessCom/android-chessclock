package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.core.*;
import com.chess.core.interfaces.BoardFace;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.GameEvent;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.*;
import com.chess.views.NewBoardView;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;

import java.util.Timer;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameCompScreenActivity extends CoreActivityActionBar implements View.OnClickListener {

	private final static int DIALOG_DRAW_OFFER = 4;
	private final static int DIALOG_ABORT_OR_RESIGN = 5;

	private final static int CALLBACK_GAME_STARTED = 10;
	private final static int CALLBACK_REPAINT_UI = 0;
	private final static int CALLBACK_GAME_REFRESH = 9;

	public NewBoardView newBoardView;
	private TextView whitePlayerLabel;
	private TextView blackPlayerLabel;
	private TextView thinking;
	private TextView movelist;
	private Timer onlineGameUpdate = null;
	private boolean msgShowed = false, isMoveNav = false, chat = false;

	private com.chess.model.Game game;

	private TextView whiteClockView;
	private TextView blackClockView;

	protected AlertDialog adPopup;
	private TextView endOfGameMessage;
	private LinearLayout adViewWrapper;

//	private FirstTacticsDialogListener firstTackicsDialogListener;
//	private MaxTacticksDialogListener maxTackicksDialogListener;
//	private HundredTacticsDialogListener hundredTackicsDialogListener;
//	private OfflineModeDialogListener offlineModeDialogListener;
	private DrawOfferDialogListener drawOfferDialogListener;
	private AbortGameDialogListener abortGameDialogListener;
	private MenuOptionsDialogListener menuOptionsDialogListener;

//	private WrongScoreDialogListener wrongScoreDialogListener;

	private CharSequence[] menuOptionsItems;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(CommonUtils.needFullScreen(this)){
			setFullscreen();
			savedInstanceState = new Bundle();
			savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN,true);
		}
		super.onCreate(savedInstanceState);


		/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(extras.getInt(AppConstants.GAME_MODE))) {
			setContentView(R.layout.boardviewlive2);
//			lccHolder.getAndroid().setGameActivity(this);   //TODO
		} else*/
		{
			setContentView(R.layout.boardview_comp);
		}

		init();

//		analysisLL = (LinearLayout) findViewById(R.id.analysis);
//		analysisButtons = (LinearLayout) findViewById(R.id.analysisButtons);
//		if (mainApp.isLiveChess() && !MainApp.isTacticsGameMode(extras.getInt(AppConstants.GAME_MODE))) {
//		chatPanel = (RelativeLayout) findViewById(R.id.chatPanel);
//		ImageButton chatButton = (ImageButton) findViewById(R.id.chat);
//		chatButton.setOnClickListener(this);
//		}


		whitePlayerLabel = (TextView) findViewById(R.id.white);
		blackPlayerLabel = (TextView) findViewById(R.id.black);
		thinking = (TextView) findViewById(R.id.thinking);
//		timer = (TextView) findViewById(R.id.timer);
		movelist = (TextView) findViewById(R.id.movelist);

		whiteClockView = (TextView) findViewById(R.id.whiteClockView);
		blackClockView = (TextView) findViewById(R.id.blackClockView);
		/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(extras.getInt(AppConstants.GAME_MODE))
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
		}*/

		endOfGameMessage = (TextView) findViewById(R.id.endOfGameMessage);

		newBoardView = (BoardFace) findViewById(R.id.boardview);
		newBoardView.setFocusable(true);
		newBoardView.setBoard((Board2) getLastNonConfigurationInstance());

		lccHolder = mainApp.getLccHolder();

		if (newBoardView.getBoardFace() == null) {
			newBoardView.setBoard(new Board2(this));
			newBoardView.getBoardFace().setInit(true);//init = true;
			newBoardView.setBoardMode() = extras.getInt(AppConstants.GAME_MODE);
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
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (MainApp.isComputerVsComputerGameMode(newBoardView)) {
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (MainApp.isLiveOrEchessGameMode(newBoardView) || MainApp.isFinishedEchessGameMode(newBoardView))
					mainApp.setGameId(extras.getString(AppConstants.GAME_ID));
			}
//			if (MainApp.isTacticsGameMode(newBoardView)) {
//				showDialog(DIALOG_TACTICS_START_TACTICS);
//				return;
//			}
		}

		if (MobclixHelper.isShowAds(mainApp) /*&& getRectangleAdview() == null*/
				&& mainApp.getTabHost() != null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4")) {
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
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView)) {
					final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
					LccHolder.LOG.info("Request draw: " + game);
					lccHolder.getAndroid().runMakeDrawTask(game);
				} else */
				{
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
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView)) {
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
				} else*/
				{
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



//	private class WrongScoreDialogListener implements DialogInterface.OnClickListener {
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			if (which == 0) {
//				GetTacticsGame("");
//			} else if (which == 1) {
//				newBoardView.getBoardFaceFace().retry = true;
//				GetTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
//			} else if (which == 2) {
//				newBoardView.finished = true;
//				mainApp.getTactic().values.put(AppConstants.STOP, "1");
//			}
//		}
//	}

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

	private void init() {
		menuOptionsItems = new CharSequence[]{
				getString(R.string.ngwhite),
				getString(R.string.ngblack),
				getString(R.string.emailgame),
				getString(R.string.settings)};
//		firstTackicsDialogListener = new FirstTacticsDialogListener();
//		maxTackicksDialogListener = new MaxTacticksDialogListener();
//		hundredTackicsDialogListener = new HundredTacticsDialogListener();
//		offlineModeDialogListener = new OfflineModeDialogListener();
		drawOfferDialogListener = new DrawOfferDialogListener();
		abortGameDialogListener = new AbortGameDialogListener();
		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
//		wrongScoreDialogListener = new WrongScoreDialogListener();
		

				
//		displayMetrics.density
//		if(getWindowManager().getDefaultDisplay().getMetrics();)
	}




	private void GetOnlineGame(final String game_id) {
		if (appService != null && appService.getRepeatableTimer() != null) {
			appService.getRepeatableTimer().cancel();
			appService.setRepeatableTimer(null);
		}
		mainApp.setGameId(game_id);

		/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView)) {
			Update(CALLBACK_GAME_STARTED);
		} else*/
		{
			if (appService != null) {
				appService.RunSingleTask(CALLBACK_GAME_STARTED,
						"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
						null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
			}
		}
	}





	//	@Override
	public void LoadPrev(int code) {
		if (newBoardView.getBoardFace() != null && MainApp.isTacticsGameMode(newBoardView)) {
//			//mainApp.getTabHost().setCurrentTab(0);
			newBoardView.getBoardFace().setTacticCanceled(true);
			onBackPressed();
		} else {
			finish();
		}
	}

	@Override
	public void Update(int code) {
		int UPDATE_DELAY = 10000;
		int[] moveFT = new int[]{};
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(newBoardView))
					finish();
				/*else if (MainApp.isTacticsGameMode(newBoardView)) {
					*//*//mainApp.getTabHost().setCurrentTab(0);
					newBoardView.getBoardFaceFace().getTactic()Canceled = true;*//*
					if (mainApp.noInternet) {
						if (mainApp.offline) {
							GetGuestTacticsGame();
						} else {
							mainApp.offline = true;
							showDialog(DIALOG_TACTICS_OFFLINE_RATING);
						}
						return;
					}
				}*/
				//finish();
				break;
			case INIT_ACTIVITY:

				if (newBoardView.getBoardFace().isInit() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())
						|| MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					GetOnlineGame(mainApp.getGameId());
					newBoardView.getBoardFace().setInit(false);
				}
				break;
			case CALLBACK_REPAINT_UI: {
				switch (newBoardView.getBoardFace().getBoardMode()) {
					case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {	//w - human; b - comp
						whitePlayerLabel.setText(getString(R.string.Human));
						blackPlayerLabel.setText(getString(R.string.Computer));
						break;
					}
					case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {	//w - comp; b - human
						whitePlayerLabel.setText(getString(R.string.Computer));
						blackPlayerLabel.setText(getString(R.string.Human));
						break;
					}
					case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {	//w - human; b - human
						whitePlayerLabel.setText(getString(R.string.Human));
						blackPlayerLabel.setText(getString(R.string.Human));
						break;
					}
					case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {	//w - comp; b - comp
						whitePlayerLabel.setText(getString(R.string.Computer));
						blackPlayerLabel.setText(getString(R.string.Computer));
						break;
					}

					default:
						break;
				}

				if (MainApp.isComputerGameMode(newBoardView.getBoardFace())) {
//					hideAnalysisButtons();
				}

				/*if (MainApp.isLiveOrEchessGameMode(newBoardView) || MainApp.isFinishedEchessGameMode(newBoardView)) {
					if (mainApp.getCurrentGame() != null) {
						whitePlayerLabel.setText(mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("white_rating") + ")");
						blackPlayerLabel.setText(mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("black_rating") + ")");
					}
				}*/

				/*if (MainApp.isTacticsGameMode(newBoardView)) {
					if (newBoardView.getBoardFaceFace().analysis) {
						timer.setVisibility(View.GONE);
						analysisLL.setVisibility(View.VISIBLE);
						if (!mainApp.isLiveChess() && analysisButtons != null) {
							showAnalysisButtons();
						}
					} else {
						whitePlayerLabel.setVisibility(View.GONE);
						blackPlayerLabel.setVisibility(View.GONE);
						timer.setVisibility(View.VISIBLE);
						analysisLL.setVisibility(View.GONE);
						if (!mainApp.isLiveChess() && analysisButtons != null) {
							hideAnalysisButtons();
						}
					}
				}*/
				movelist.setText(newBoardView.getBoardFace().MoveListSAN());
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

			case 2: {
				whitePlayerLabel.setVisibility(View.GONE);
				blackPlayerLabel.setVisibility(View.GONE);
				thinking.setVisibility(View.VISIBLE);
				break;
			}
			case 3: {
				whitePlayerLabel.setVisibility(View.VISIBLE);
				blackPlayerLabel.setVisibility(View.VISIBLE);
				thinking.setVisibility(View.GONE);
				break;
			}
//			case 4: {
////				CheckTacticMoves();
//				break;
//			}

			case CALLBACK_GAME_REFRESH:
//				if (newBoardView.getBoardFaceFace().analysis)
//					return;
//				if (!mainApp.isLiveChess()) {
//					game = ChessComApiParser.GetGameParseV3(responseRepeatable);
//				}
				//System.out.println("!!!!!!!! mainApp.getCurrentGame() " + mainApp.getCurrentGame());
				//System.out.println("!!!!!!!! game " + game);

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				if (!mainApp.getCurrentGame().equals(game)) {
					if (!mainApp.getCurrentGame().values.get("move_list").equals(game.values.get("move_list"))) {
						mainApp.setCurrentGame(game);
						String[] Moves = {};

						if (mainApp.getCurrentGame().values.get("move_list").contains("1.")
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView)))) {

							int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView)) ? 0 : 1;

							Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

							if (Moves.length - newBoardView.getBoardFace().movesCount == 1) {
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
								newBoardView.getBoardFace().movesCount = Moves.length;
								newBoardView.invalidate();
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

				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView)) {
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
						mainApp.getSharedDataEditor().putString("opponent", mainApp.getCurrentGame()
								.values.get(AppConstants.WHITE_USERNAME));
					else
						mainApp.getSharedDataEditor().putString("opponent", mainApp.getCurrentGame()
								.values.get(AppConstants.BLACK_USERNAME));
					mainApp.getSharedDataEditor().commit();
					mainApp.getCurrentGame().values.put("has_new_message", "0");
					startActivity(new Intent(coreContext, mainApp.isLiveChess() ? ChatLive.class : Chat.class).
							putExtra(AppConstants.GAME_ID, mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).
							putExtra(AppConstants.TIMESTAMP, mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP)));
					chat = false;
					return;
				}

				if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
					newBoardView.getBoardFace().chess960 = true;


				if (!isUserColorWhite()) {
					newBoardView.getBoardFace().setReside(true);
				}
				String[] Moves = {};


				if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
					Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					newBoardView.getBoardFace().movesCount = Moves.length;
				} else if (!mainApp.isLiveChess()) {
					newBoardView.getBoardFace().movesCount = 0;
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
				//System.out.println("@@@@@@@@ POINT 2 newBoardView.getBoardFaceFace().movesCount=" + newBoardView.getBoardFaceFace().movesCount);
				//System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

				if (!mainApp.isLiveChess()) {
					for (i = 0; i < newBoardView.getBoardFace().movesCount; i++) {
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

				Update(CALLBACK_REPAINT_UI);
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
							Update(CALLBACK_REPAINT_UI);
							newBoardView.invalidate();
						}
					};
				}).start();

//				if (MainApp.isLiveOrEchessGameMode(newBoardView) && appService != null && appService.getRepeatableTimer() == null) {
//					if (progressDialog != null) {
//						progressDialog.dismiss();
//						progressDialog = null;
//					}
//					if (!mainApp.isLiveChess()) {
//						appService.RunRepeatbleTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
//								"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
//								null/*progressDialog*/
//						);
//					}
//				}
				break;

			default:
				break;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return newBoardView.getBoardFace();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		if (MainApp.isComputerGameMode(newBoardView)) {
			menuInflater.inflate(R.menu.game_comp, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		if (mainApp.getCurrentGame() != null && (MainApp.isLiveOrEchessGameMode(newBoardView)
//				|| MainApp.isFinishedEchessGameMode(newBoardView))) {
//			int itemPosition = mainApp.isLiveChess() ? 1 : 3;
//			if (mainApp.getCurrentGame().values.get("has_new_message").equals("1"))
//				menu.getItem(itemPosition).setIcon(R.drawable.chat_nm);
//			else
//				menu.getItem(itemPosition).setIcon(R.drawable.chat);
//		}
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_new_game:
				newBoardView.stopThinking = true;
//				finish();
				onBackPressed();
				break;
			case R.id.menu_options:
				newBoardView.stopThinking = true;

				new AlertDialog.Builder(this)
						.setTitle(R.string.options)
						.setItems(menuOptionsItems, menuOptionsDialogListener).show();
				break;
			case R.id.menu_reside:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.getBoardFace().setReside(!newBoardView.getBoardFace().reside);
					if (MainApp.isComputerVsHumanGameMode(newBoardView)) {
						if (MainApp.isComputerVsHumanWhiteGameMode(newBoardView)) {
							newBoardView.setBoardMode() = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
						} else if (MainApp.isComputerVsHumanBlackGameMode(newBoardView)) {
							newBoardView.setBoardMode() = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
						}
						//newBoardView.getBoardFaceFace().mode ^= 1;
						newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
								.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
										+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					}
					newBoardView.invalidate();
					Update(CALLBACK_REPAINT_UI);
				}
				break;
			case R.id.menu_hint:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.hint = true;
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
							.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
									+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				break;
			case R.id.menu_previous:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.finished = false;
					newBoardView.sel = false;
					newBoardView.getBoardFace().takeBack();
					newBoardView.invalidate();
					Update(CALLBACK_REPAINT_UI);
					isMoveNav = true;
				}
				break;
			case R.id.menu_next:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.sel = false;
					newBoardView.getBoardFace().takeNext();
					newBoardView.invalidate();
					Update(CALLBACK_REPAINT_UI);
					isMoveNav = true;
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener{
		final CharSequence[] items;
		private final int NEW_GAME_WHITE = 0;
		private final int NEW_GAME_BLACK = 1;
		private final int EMAIL_GAME = 2;
		private final int SETTINGS = 3;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i) {
				case NEW_GAME_WHITE: {
					newBoardView.setBoard(new Board2(GameCompScreenActivity.this));
					newBoardView.setBoardMode() = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
					newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					newBoardView.invalidate();
					Update(CALLBACK_REPAINT_UI);
					break;
				}
				case NEW_GAME_BLACK: {
					// TODO encapsulate
					newBoardView.setBoard(new Board2(GameCompScreenActivity.this));
					newBoardView.setBoardMode() = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
					newBoardView.getBoardFace().setReside(true);
					newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					newBoardView.invalidate();
					Update(CALLBACK_REPAINT_UI);
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
							.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
									+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					break;
				}
				case EMAIL_GAME: {
					String moves = movelist.getText().toString();
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "[Site \"Chess.com Android\"]\n [White \"" + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\"]\n [White \"" + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\"]\n [Result \"X-X\"]\n \n \n " + moves + " \n \n Sent from my Android");
					startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail) /*"Send mail..."*/));
					break;
				}

				case SETTINGS: {
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					break;
				}
			}
		}
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
		newBoardView.requestFocus();
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		if (MobclixHelper.isShowAds(mainApp) && mainApp.getTabHost() != null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4") && adViewWrapper != null && getRectangleAdview() != null) {
			adViewWrapper.addView(getRectangleAdview());
			if (mainApp.isForceRectangleAd()) {
				getRectangleAdview().getAd();
			}
		}

		if (/*!mainApp.isNetworkChangedNotification() && */extras.containsKey(AppConstants.LIVE_CHESS)) {
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

		registerReceiver(gameMoveReceiver, new IntentFilter(IntentConstants.ACTION_GAME_MOVE));
		registerReceiver(gameEndMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_END));
		registerReceiver(gameInfoMessageReceived, new IntentFilter(IntentConstants.ACTION_GAME_INFO));
//		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
		registerReceiver(showGameEndPopupReceiver, new IntentFilter(IntentConstants.ACTION_SHOW_GAME_END_POPUP));

		/*if (MainApp.isTacticsGameMode(newBoardView)) {
			if (newBoardView.getBoardFaceFace().isTacticCanceled()) {
				newBoardView.getBoardFaceFace().setTacticCanceled(false);
				showDialog(DIALOG_TACTICS_START_TACTICS);
				startTacticsTimer();
			} else if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("0")) {
				startTacticsTimer();
			}
		}*/
		/*if (mainApp.isLiveChess() && mainApp.getGameId() != null && mainApp.getGameId() != ""
				&& lccHolder.getGame(mainApp.getGameId()) != null) {
			game = new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(),
					lccHolder.getGame(mainApp.getGameId()).getSeq() - 1), true);
//			lccHolder.getAndroid().setGameActivity(this); // TODO
			if (lccHolder.isActivityPausedMode()) {
				executePausedActivityGameEvents();
				lccHolder.setActivityPausedMode(false);
			}
			//lccHolder.updateClockTime(lccHolder.getGame(mainApp.getGameId()));
		}*/

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
//		unregisterReceiver(chatMessageReceiver);
		unregisterReceiver(showGameEndPopupReceiver);

		super.onPause();
		System.out.println("LCCLOG2: GAME ONPAUSE adViewWrapper="
				+ adViewWrapper + ", getRectangleAdview() " + getRectangleAdview());
		if (adViewWrapper != null && getRectangleAdview() != null) {
			System.out.println("LCCLOG2: GAME ONPAUSE 1");
			getRectangleAdview().cancelAd();
			System.out.println("LCCLOG2: GAME ONPAUSE 2");
			adViewWrapper.removeView(getRectangleAdview());
			System.out.println("LCCLOG2: GAME ONPAUSE 3");
		}
		lccHolder.setActivityPausedMode(true);
		lccHolder.getPausedActivityGameEvents().clear();

		newBoardView.stopThinking = true;

//		stopTacticsTimer();
		if (onlineGameUpdate != null)
			onlineGameUpdate.cancel();

		/*if (MobclixHelper.isShowAds(mainApp))
		{
			MobclixHelper.pauseAdview(getRectangleAdview(), mainApp);
		}*/

		enableScreenLock();
	}

//	public void stopTacticsTimer() {
//		if (tacticsTimer != null) {
//			tacticsTimer.cancel();
//			tacticsTimer = null;
//		}
//	}

//	public void startTacticsTimer() {
//		stopTacticsTimer();
//		newBoardView.finished = false;
//		if (mainApp.getTactic() != null) {
//			mainApp.getTactic().values.put(AppConstants.STOP, "0");
//		}
//		tacticsTimer = new Timer();
//		tacticsTimer.scheduleAtFixedRate(new TimerTask() {
//			@Override
//			public void run() {
//				if (newBoardView.getBoardFaceFace().analysis)
//					return;
//				newBoardView.getBoardFaceFace().sec++;
//				if (newBoardView.getBoardFaceFace().left > 0)
//					newBoardView.getBoardFaceFace().left--;
//				update.sendEmptyMessage(0);
//			}
//
//			private Handler update = new Handler() {
//				@Override
//				public void dispatchMessage(Message msg) {
//					super.dispatchMessage(msg);
//					timer.setText(getString(R.string.bonus_time_left, newBoardView.getBoardFaceFace().left
//							, newBoardView.getBoardFaceFace().sec));
//				}
//			};
//		}, 0, 1000);
//	}

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
			whitePlayerLabel.setText(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")");
			blackPlayerLabel.setText(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
			newBoardView.finished = true;

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
//			findViewById(R.id.moveButtons).setVisibility(View.GONE);
			findViewById(R.id.endOfGameButtons).setVisibility(View.VISIBLE);
//			chatPanel.setVisibility(View.GONE);
			findViewById(R.id.newGame).setOnClickListener(GameCompScreenActivity.this);
			findViewById(R.id.home).setOnClickListener(GameCompScreenActivity.this);
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
		newBoardView.board = null;
		super.onStop();
	  }*/


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
			if (adViewWrapper != null && getRectangleAdview() != null) {
				adViewWrapper.removeView(getRectangleAdview());
			}
			adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
			System.out.println("MOBCLIX: GET WRAPPER " + adViewWrapper);
			adViewWrapper.addView(getRectangleAdview());

			adViewWrapper.setVisibility(View.VISIBLE);
			//showGameEndAds(adViewWrapper);

			TextView endOfGameMessagePopup = (TextView) layout.findViewById(R.id.endOfGameMessage);
			endOfGameMessagePopup.setText(message);

			adPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialogInterface) {
					if (adViewWrapper != null && getRectangleAdview() != null) {
						adViewWrapper.removeView(getRectangleAdview());
					}
				}
			});
			adPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialogInterface) {
					if (adViewWrapper != null && getRectangleAdview() != null) {
						adViewWrapper.removeView(getRectangleAdview());
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
				}
			}
		}, 1500);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (newBoardView.getBoardFace().isAnalysis()) {
				if (!MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
					newBoardView.getBoardFace().setBoard(new Board2(this));
					newBoardView.getBoardFace().init = true;
					newBoardView.getBoardFace().setBoardMode() = extras.getInt(AppConstants.GAME_MODE);

					if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
						newBoardView.getBoardFace().chess960 = true;

					if (!isUserColorWhite()) {
						newBoardView.getBoardFace().setReside(true);
					}
					String[] Moves = {};
					if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
						Moves = mainApp.getCurrentGame().values.get("move_list")
								.replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
						newBoardView.getBoardFace().setMovesCount( Moves.length);
					}

					String FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
					if (!FEN.equals("")) {
						newBoardView.getBoardFace().genCastlePos(FEN);
						MoveParser2.FenParse(FEN, newBoardView.getBoardFace().getBoard());
					}

					int i;
					for (i = 0; i < newBoardView.getBoardFace().getMovesCount(); i++) {

						int[] moveFT = mainApp.isLiveChess() ? 
								MoveParser2.parseCoordinate(newBoardView.getBoardFace().getBoard(), Moves[i]) :
								MoveParser2.Parse(newBoardView.getBoardFace().getBoard(), Moves[i]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							newBoardView.getBoardFace().makeMove(m, false);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							newBoardView.getBoardFace().makeMove(m, false);
						}
					}
					Update(CALLBACK_REPAINT_UI);
					newBoardView.getBoardFace().takeBack();
					newBoardView.invalidate();

					//last move anim
					new Thread(new Runnable() {
						@Override
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
								Update(CALLBACK_REPAINT_UI);
								newBoardView.invalidate();
							}
						};
					}).start();
				}
			} else {
				LoadPrev(MainApp.loadPrev);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		/*if (view.getId() == R.id.chat) {
			chat = true;
			GetOnlineGame(mainApp.getGameId());
			chatPanel.setVisibility(View.GONE);
		} else*/ /*if (view.getId() == R.id.prev) {
			newBoardView.finished = false;
			newBoardView.sel = false;
			newBoardView.getBoardFaceFace().takeBack();
			newBoardView.invalidate();
			Update(CALLBACK_REPAINT_UI);
			isMoveNav = true;
		} else if (view.getId() == R.id.next) {
			newBoardView.getBoardFaceFace().takeNext();
			newBoardView.invalidate();
			Update(CALLBACK_REPAINT_UI);
			isMoveNav = true;
		} else */
		if (view.getId() == R.id.newGame) {
			startActivity(new Intent(this, OnlineNewGame.class));
		} else if (view.getId() == R.id.home) {
			startActivity(new Intent(this, Tabs.class));
		}
	}
}