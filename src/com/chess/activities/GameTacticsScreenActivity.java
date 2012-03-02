package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.core.*;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListElement;
import com.chess.model.Tactic;
import com.chess.model.TacticResult;
import com.chess.utilities.*;
import com.chess.views.BoardView2;
import com.flurry.android.FlurryAgent;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameTacticsScreenActivity extends CoreActivityActionBar implements View.OnClickListener{



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

	private TextView whitePlayerLabel;
	private TextView blackPlayerLabel;
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

	private FirstTackicsDialogListener firstTackicsDialogListener;
	private MaxTackicksDialogListener maxTackicksDialogListener;
	private HundredTackicsDialogListener hundredTackicsDialogListener;
	private OfflineModeDialogListener offlineModeDialogListener;
	private DrawOfferDialogListener drawOfferDialogListener;
	private AbortGameDialogListener abortGameDialogListener;
	private CorrectDialogListener correctDialogListener;
	private WrongDialogListener wrongDialogListener;
	private WrongScoreDialogListener wrongScoreDialogListener;

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

		setContentView(R.layout.boardview2);


		init();

		analysisLL = (LinearLayout) findViewById(R.id.analysis);
		analysisButtons = (LinearLayout) findViewById(R.id.analysisButtons);

		findViewById(R.id.prev).setOnClickListener(this);
		findViewById(R.id.next).setOnClickListener(this);

		whitePlayerLabel = (TextView) findViewById(R.id.white);
		blackPlayerLabel = (TextView) findViewById(R.id.black);
		thinking = (TextView) findViewById(R.id.thinking);
		timer = (TextView) findViewById(R.id.timer);
		movelist = (TextView) findViewById(R.id.movelist);

		whiteClockView = (TextView) findViewById(R.id.whiteClockView);
		blackClockView = (TextView) findViewById(R.id.blackClockView);


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



	private class FirstTackicsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				InputStream f = getResources().openRawResource(R.raw.tactics100batch);
				try {
					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current = 0;
					while ((current = f.read()) != -1) {
						baf.append((byte) current);
					}
					String input = new String(baf.toByteArray());
					String[] tmp = input.split("[|]");
					int count = tmp.length - 1;
					mainApp.setTacticsBatch(new ArrayList<Tactic>(count));
					int i;
					for (i = 1; i <= count; i++) {
						mainApp.getTacticsBatch().add(new Tactic(tmp[i].split(":")));
					}
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (mainApp.guest)
					GetGuestTacticsGame();
				else
					GetTacticsGame("");

			}else if(whichButton == DialogInterface.BUTTON_NEGATIVE){
				//mainApp.getTabHost().setCurrentTab(0);
				boardView.getBoard().setTacticCanceled(true);
			}
		}
	}

	private class  MaxTackicksDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				FlurryAgent.onEvent("Upgrade From Tactics", null);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html")));
			}else if(whichButton == DialogInterface.BUTTON_NEGATIVE){
				//mainApp.getTabHost().setCurrentTab(0);
				boardView.getBoard().setTacticCanceled(true);
			}
		}
	}

	private class HundredTackicsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			//mainApp.getTabHost().setCurrentTab(0);
			mainApp.currentTacticProblem = 0;
		}
	}

	private class OfflineModeDialogListener implements DialogInterface.OnClickListener{
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				GetGuestTacticsGame();
			}else if(whichButton == DialogInterface.BUTTON_NEGATIVE){
				//mainApp.getTabHost().setCurrentTab(0);
				boardView.getBoard().setTacticCanceled(true);
			}
		}
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


	private class CorrectDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 1) {
				if (mainApp.guest) {
					mainApp.currentTacticProblem++;
					GetGuestTacticsGame();
				} else {
					if (mainApp.noInternet) mainApp.currentTacticProblem++;
					GetTacticsGame("");
				}
			}
		}
	}

	private class WrongDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				if (mainApp.guest) {
					mainApp.currentTacticProblem++;
					GetGuestTacticsGame();
				} else {
					if (mainApp.noInternet) mainApp.currentTacticProblem++;
					GetTacticsGame("");
				}
			}
			if (which == 1) {
				if (mainApp.guest || mainApp.noInternet) {
					boardView.getBoard().retry = true;
					GetGuestTacticsGame();
				} else {
					GetTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
				}
			}
			if (which == 2) {
				boardView.finished = true;
				mainApp.getTactic().values.put(AppConstants.STOP, "1");
			}
		}
	}

	private class WrongScoreDialogListener implements  DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				GetTacticsGame("");
			}
			if (which == 1) {
				boardView.getBoard().retry = true;
				GetTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
			}
			if (which == 2) {
				boardView.finished = true;
				mainApp.getTactic().values.put(AppConstants.STOP, "1");
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_TACTICS_LIMIT:
				FlurryAgent.onEvent("Tactics Daily Limit Exceded", null);
				return new AlertDialog.Builder(this)
						.setTitle(getString(R.string.daily_limit_exceeded))
						.setMessage(getString(R.string.max_tackics_for_today_reached))
						.setPositiveButton(getString(R.string.ok), maxTackicksDialogListener)
						.setNegativeButton(R.string.cancel, maxTackicksDialogListener)
						.create();
			case DIALOG_TACTICS_START_TACTICS:
				return new AlertDialog.Builder(this)
						.setTitle(getString(R.string.ready_for_first_tackics_q))
						.setPositiveButton(R.string.yes, firstTackicsDialogListener)
						.setNegativeButton(R.string.no, firstTackicsDialogListener)
						.create();
			case DIALOG_TACTICS_HUNDRED:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.hundred_tackics_completed)
						.setNegativeButton(R.string.okay, hundredTackicsDialogListener)
						.create();
			case DIALOG_TACTICS_OFFLINE_RATING:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.offline_mode)
						.setMessage(getString(R.string.no_network_rating_not_changed))
						.setPositiveButton(R.string.okay,offlineModeDialogListener)
						.setNegativeButton(R.string.cancel, offlineModeDialogListener)
						.create();
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
				getString(R.string.skipproblem),
				getString(R.string.showanswer),
				getString(R.string.settings)};

		firstTackicsDialogListener = new FirstTackicsDialogListener();
		maxTackicksDialogListener = new MaxTackicksDialogListener();
		hundredTackicsDialogListener = new HundredTackicsDialogListener();
		offlineModeDialogListener = new OfflineModeDialogListener();
		drawOfferDialogListener = new DrawOfferDialogListener();
		abortGameDialogListener = new AbortGameDialogListener();
		correctDialogListener = new CorrectDialogListener();
		wrongDialogListener = new WrongDialogListener();
		wrongScoreDialogListener = new WrongScoreDialogListener();

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

	private void GetTacticsGame(final String id) {
		FlurryAgent.onEvent("Tactics Session Started For Registered", null);
		if (!mainApp.noInternet) {
			boardView.setBoard(new Board2(this));
			boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;

			if (mainApp.getTactic() != null
					&& id.equals(mainApp.getTactic().values.get(AppConstants.ID))) {
				boardView.getBoard().retry = true;
				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals("")) {
					boardView.getBoard().GenCastlePos(FEN);
					MoveParser2.FenParse(FEN, boardView.getBoard());
					String[] tmp2 = FEN.split(" ");
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals("w")) {
							boardView.getBoard().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					boardView.getBoard().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
					boardView.getBoard().movesCount = 1;
				}
				boardView.getBoard().sec = 0;
				boardView.getBoard().left = Integer.parseInt(mainApp.getTactic().values.get(AppConstants.AVG_SECONDS));
				startTacticsTimer();
				int[] moveFT = MoveParser2.Parse(boardView.getBoard(), boardView.getBoard().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move m;
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoard().makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoard().makeMove(m);
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

				return;
			}
		}
		if (appService != null) {
			appService.RunSingleTask(CALLBACK_GET_TACTICS,
					"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="
							+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&tactics_id=" + id,
					progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), false))
			);
		}
	}

	private void GetGuestTacticsGame() {
		FlurryAgent.onEvent("Tactics Session Started For Guest", null);

		if (mainApp.currentTacticProblem >= mainApp.getTacticsBatch().size()) {
			showDialog(DIALOG_TACTICS_HUNDRED);
			return;
		}

		boardView.setBoard(new Board2(this));
		boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;

		String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
		if (!FEN.equals("")) {
			boardView.getBoard().GenCastlePos(FEN);
			MoveParser2.FenParse(FEN, boardView.getBoard());
			String[] tmp = FEN.split(" ");
			if (tmp.length > 1) {
				if (tmp[1].trim().equals("w")) {
					boardView.getBoard().setReside(true);
				}
			}
		}
		if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
			boardView.getBoard().setTacticMoves(mainApp.getTacticsBatch()
					.get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "")
					.replaceAll("  ", " ").substring(1).split(" "));
			boardView.getBoard().movesCount = 1;
		}
		boardView.getBoard().sec = 0;
		boardView.getBoard().left = Integer.parseInt(mainApp.getTacticsBatch()
				.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS));
		startTacticsTimer();
		int[] moveFT = MoveParser2.Parse(boardView.getBoard(), boardView.getBoard().getTacticMoves()[0]);
		if (moveFT.length == 4) {
			Move m;
			if (moveFT[3] == 2)
				m = new Move(moveFT[0], moveFT[1], 0, 2);
			else
				m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			boardView.getBoard().makeMove(m);
		} else {
			Move m = new Move(moveFT[0], moveFT[1], 0, 0);
			boardView.getBoard().makeMove(m);
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
	}

	private void ShowAnswer() {
		boardView.setBoard(new Board2(this));
		boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;
		boardView.getBoard().retry = true;

		if (mainApp.guest || mainApp.noInternet) {
			String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
			if (!FEN.equals("")) {
				boardView.getBoard().GenCastlePos(FEN);
				MoveParser2.FenParse(FEN, boardView.getBoard());
				String[] tmp = FEN.split(" ");
				if (tmp.length > 1) {
					if (tmp[1].trim().equals("w")) {
						boardView.getBoard().setReside(true);
					}
				}
			}
			if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				boardView.getBoard().setTacticMoves(mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
				boardView.getBoard().movesCount = 1;
			}
		} else {
			String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
			if (!FEN.equals("")) {
				boardView.getBoard().GenCastlePos(FEN);
				MoveParser2.FenParse(FEN, boardView.getBoard());
				String[] tmp2 = FEN.split(" ");
				if (tmp2.length > 1) {
					if (tmp2[1].trim().equals("w")) {
						boardView.getBoard().setReside(true);
					}
				}
			}

			if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				boardView.getBoard().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
				boardView.getBoard().movesCount = 1;
			}
		}
		boardView.invalidate();


		new Thread(new Runnable() {
			public void run() {
				int i;
				for (i = 0; i < boardView.getBoard().getTacticMoves().length; i++) {
					int[] moveFT = MoveParser2.Parse(boardView.getBoard(), boardView.getBoard().getTacticMoves()[i]);
					try {
						Thread.sleep(1500);
					} catch (Exception e) {
					}
					if (moveFT.length == 4) {
						Move m;
						if (moveFT[3] == 2)
							m = new Move(moveFT[0], moveFT[1], 0, 2);
						else
							m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

						boardView.getBoard().makeMove(m);
					} else {
						Move m = new Move(moveFT[0], moveFT[1], 0, 0);
						boardView.getBoard().makeMove(m);
					}
					handler.sendEmptyMessage(0);
				}
			}

			private Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Update(CALLBACK_REPAINT_UI);
					boardView.invalidate();
				}
			};
		}).start();
	}

	private void CheckTacticMoves() {
		Move m = boardView.getBoard().histDat[boardView.getBoard().hply - 1].m;
		String f = "";
		int p = boardView.getBoard().piece[m.to];
		if (p == 1) {
			f = "N";
		} else if (p == 2) {
			f = "B";
		} else if (p == 3) {
			f = "R";
		} else if (p == 4) {
			f = "Q";
		} else if (p == 5) {
			f = "K";
		}
		String Moveto = MoveParser.positionToString(m.to);
		Log.d("!!!", f + " | " + Moveto + " : " + boardView.getBoard().getTacticMoves()[boardView.getBoard().hply - 1]);
		if (boardView.getBoard().getTacticMoves()[boardView.getBoard().hply - 1].contains(f) && boardView.getBoard().getTacticMoves()[boardView.getBoard().hply - 1].contains(Moveto)) {
			boardView.getBoard().TacticsCorrectMoves++;
			if (boardView.getBoard().movesCount < boardView.getBoard().getTacticMoves().length - 1) {
				int[] moveFT = MoveParser2.Parse(boardView.getBoard(), boardView.getBoard().getTacticMoves()[boardView.getBoard().hply]);
				if (moveFT.length == 4) {
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoard().makeMove(m);
				} else {
					m = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoard().makeMove(m);
				}
				Update(CALLBACK_REPAINT_UI);
				boardView.invalidate();
			} else {
				if (mainApp.guest || boardView.getBoard().retry || mainApp.noInternet) {
					new AlertDialog.Builder(this)
							.setTitle(R.string.correct_ex)
							.setItems(getResources().getTextArray(R.array.correcttactic),
									correctDialogListener)
							.create().show();
					stopTacticsTimer();
				} else {
					if (appService != null) {
						appService.RunSingleTask(CALLBACK_TACTICS_CORRECT,
								"http://www." + LccHolder.HOST + "/api/tactics_trainer?id=" +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
										+ "&tactics_id=" + mainApp.getTactic().values.get(AppConstants.ID)
										+ "&passed=" + 1 + "&correct_moves=" + boardView.getBoard().TacticsCorrectMoves
										+ "&seconds=" + boardView.getBoard().sec,
								progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
					}
					stopTacticsTimer();
				}
			}
		} else {
			if (mainApp.guest || boardView.getBoard().retry || mainApp.noInternet) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.wrong_ex)
						.setItems(getResources().getTextArray(R.array.wrongtactic),wrongDialogListener)
						.create().show();
				stopTacticsTimer();
			} else {
				if (appService != null) {
					appService.RunSingleTask(CALLBACK_TACTICS_WRONG,
							"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="
									+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
									+ "&tactics_id=" + mainApp.getTactic().values.get(AppConstants.ID)
									+ "&passed=" + 0 + "&correct_moves=" + boardView.getBoard().TacticsCorrectMoves + "&seconds=" + boardView.getBoard().sec,
							progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
				}
				stopTacticsTimer();
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
				if (!MainApp.isTacticsGameMode(boardView))
					finish();
				else if (MainApp.isTacticsGameMode(boardView)) {
					/*//mainApp.getTabHost().setCurrentTab(0);
					boardView.getBoard().getTactic()Canceled = true;*/
					if (mainApp.noInternet) {
						if (mainApp.offline) {
							GetGuestTacticsGame();
						} else {
							mainApp.offline = true;
							showDialog(DIALOG_TACTICS_OFFLINE_RATING);
						}
						return;
					}
				}
				//finish();
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

				if (MainApp.isTacticsGameMode(boardView)) {
					if (boardView.getBoard().analysis) {
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
			case 4: {
				CheckTacticMoves();
				break;
			}
			case CALLBACK_TACTICS_WRONG: {
				String[] tmp = response.split("[|]");
				if (tmp.length < 2 || tmp[1].trim().equals("")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				TacticResult result = new TacticResult(tmp[1].split(":"));

				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.wrong_score,
								result.values.get(AppConstants.USER_RATING_CHANGE),
								result.values.get(AppConstants.USER_RATING)))
						.setItems(getResources().getTextArray(R.array.wrongtactic), wrongScoreDialogListener)
						.create().show();
				break;
			}
			case CALLBACK_TACTICS_CORRECT: {
				String[] tmp = response.split("[|]");
				if (tmp.length < 2 || tmp[1].trim().equals("")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				TacticResult result = new TacticResult(tmp[1].split(":"));

				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.correct_score,
								result.values.get(AppConstants.USER_RATING_CHANGE),
								result.values.get(AppConstants.USER_RATING)))
						.setItems(getResources().getTextArray(R.array.correcttactic), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (which == 1) {
									GetTacticsGame("");
								}
							}
						})
						.create().show();
				break;
			}
			case CALLBACK_GET_TACTICS:

				boardView.setBoard(new Board2(this));
				boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;

				String[] tmp = response.trim().split("[|]");
				if (tmp.length < 3 || tmp[2].trim().equals("")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				mainApp.setTactic(new Tactic(tmp[2].split(":")));

				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals("")) {
					boardView.getBoard().GenCastlePos(FEN);
					MoveParser2.FenParse(FEN, boardView.getBoard());
					String[] tmp2 = FEN.split(" ");
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals("w")) {
							boardView.getBoard().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					boardView.getBoard().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
					boardView.getBoard().movesCount = 1;
				}
				boardView.getBoard().sec = 0;
				boardView.getBoard().left = Integer.parseInt(mainApp.getTactic().values.get(AppConstants.AVG_SECONDS));
				startTacticsTimer();
				int[] moveFT = MoveParser2.Parse(boardView.getBoard(), boardView.getBoard().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move m;
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoard().makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoard().makeMove(m);
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
				break;
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

				/*if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView)) {
					mainApp.setCurrentGame(new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(), -1), true));
					executePausedActivityGameEvents();
					//lccHolder.setActivityPausedMode(false);
					lccHolder.getWhiteClock().paint();
					lccHolder.getBlackClock().paint();
					*//*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
							  lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
							  lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*//*
				} else*/ {
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

				FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
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

				/*if (MainApp.isLiveOrEchessGameMode(boardView) && appService != null && appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					if (!mainApp.isLiveChess()) {
						appService.RunRepeatbleTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
								"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
								null*//*progressDialog*//*
						);
					}
				}*/
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
			menuInflater.inflate(R.menu.game_tactics, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}



	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		if (mainApp.getCurrentGame() != null && (MainApp.isLiveOrEchessGameMode(boardView)
//				|| MainApp.isFinishedEchessGameMode(boardView))) {
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
			case R.id.menu_next_game:
				if (mainApp.guest) {
					mainApp.currentTacticProblem++;
					GetGuestTacticsGame();
				} else {
					if (mainApp.noInternet) mainApp.currentTacticProblem++;
					closeOptionsMenu();
					GetTacticsGame("");
				}
				break;
			case R.id.menu_options:
				new AlertDialog.Builder(this)
						.setTitle(R.string.options)
						.setItems(menuOptionsItems, menuOptionsDialogListener).show();
				break;
			case R.id.menu_reside:
				boardView.getBoard().setReside(!boardView.getBoard().reside);
				boardView.invalidate();
				break;
			case R.id.menu_analysis:
				boardView.getBoard().analysis = true;
				Update(CALLBACK_REPAINT_UI);
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
		private final int TACTICS_SKIP_PROBLEM = 0;
		private final int TACTICS_SHOW_ANSWER = 1;
		private final int TACTICS_SETTINGS = 2;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i){
				case TACTICS_SKIP_PROBLEM: {
					if (mainApp.guest || mainApp.noInternet) {
						mainApp.currentTacticProblem++;
						GetGuestTacticsGame();
					} else
						GetTacticsGame("");
					break;
				}
				case TACTICS_SHOW_ANSWER: {
					ShowAnswer();
					break;
				}
				case TACTICS_SETTINGS: {
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

		registerReceiver(gameMoveReceiver, new IntentFilter(IntentConstants.ACTION_GAME_MOVE ));
		registerReceiver(gameEndMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_END));
		registerReceiver(gameInfoMessageReceived, new IntentFilter(IntentConstants.ACTION_GAME_INFO));
//		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
		registerReceiver(showGameEndPopupReceiver, new IntentFilter(IntentConstants.ACTION_SHOW_GAME_END_POPUP));

//		if (MainApp.isTacticsGameMode(boardView)) {
			if (boardView.getBoard().isTacticCanceled()) {
				boardView.getBoard().setTacticCanceled(false);
				showDialog(DIALOG_TACTICS_START_TACTICS);
				startTacticsTimer();
			} else if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("0")) {
				startTacticsTimer();
			}
//		}
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
//		unregisterReceiver(chatMessageReceiver);
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

	public void startTacticsTimer() {
		stopTacticsTimer();
		boardView.finished = false;
		if (mainApp.getTactic() != null) {
			mainApp.getTactic().values.put(AppConstants.STOP, "0");
		}
		tacticsTimer = new Timer();
		tacticsTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (boardView.getBoard().analysis)
					return;
				boardView.getBoard().sec++;
				if (boardView.getBoard().left > 0)
					boardView.getBoard().left--;
				update.sendEmptyMessage(0);
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					timer.setText(getString(R.string.bonus_time_left, boardView.getBoard().left
							, boardView.getBoard().sec));
				}
			};
		}, 0, 1000);
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
			whitePlayerLabel.setText(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")");
			blackPlayerLabel.setText(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
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
//			chatPanel.setVisibility(View.GONE);
			findViewById(R.id.newGame).setOnClickListener(GameTacticsScreenActivity.this);
			findViewById(R.id.home).setOnClickListener(GameTacticsScreenActivity.this);
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

//	private void executePausedActivityGameEvents() {
//		if (/*lccHolder.isActivityPausedMode() && */lccHolder.getPausedActivityGameEvents().size() > 0) {
//			//boolean fullGameProcessed = false;
//			GameEvent gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.Move);
//			if (gameEvent != null &&
//					(lccHolder.getCurrentGameId() == null
//							|| lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
//				//lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
//				//fullGameProcessed = true;
//				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
//				//lccHolder.getAndroid().processMove(gameEvent.getGameId(), gameEvent.moveIndex);
//				game = new com.chess.model.Game(lccHolder.getGameData(
//						gameEvent.getGameId().toString(), gameEvent.getMoveIndex()), true);
//				Update(CALLBACK_GAME_REFRESH);
//			}
//
//			gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.DrawOffer);
//			if (gameEvent != null &&
//					(lccHolder.getCurrentGameId() == null
//							|| lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
//				/*if (!fullGameProcessed)
//						{
//						  lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
//						  fullGameProcessed = true;
//						}*/
//				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
//				lccHolder.getAndroid().processDrawOffered(gameEvent.getDrawOffererUsername());
//			}
//
//			gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.EndOfGame);
//			if (gameEvent != null &&
//					(lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
//				/*if (!fullGameProcessed)
//						{
//						  lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
//						  fullGameProcessed = true;
//						}*/
//				lccHolder.getPausedActivityGameEvents().remove(gameEvent);
//				lccHolder.getAndroid().processGameEnd(gameEvent.getGameEndedMessage());
//			}
//		}
//	}

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

//	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
//			chatPanel.setVisibility(View.VISIBLE);
//		}
//	};

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
				if (MainApp.isTacticsGameMode(boardView)) {
					if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
						openOptionsMenu();
						return true;
					}
					int sec = boardView.getBoard().sec;
					if (mainApp.guest || mainApp.noInternet) {
						boardView.setBoard(new Board2(this));
						boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;

						String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
						if (!FEN.equals("")) {
							boardView.getBoard().GenCastlePos(FEN);
							MoveParser2.FenParse(FEN, boardView.getBoard());
							String[] tmp = FEN.split(" ");
							if (tmp.length > 1) {
								if (tmp[1].trim().equals("w")) {
									boardView.getBoard().setReside(true);
								}
							}
						}
						if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
							boardView.getBoard().setTacticMoves(mainApp.getTacticsBatch()
									.get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
							boardView.getBoard().movesCount = 1;
						}
						boardView.getBoard().sec = sec;
						boardView.getBoard().left = Integer.parseInt(mainApp.getTacticsBatch()
								.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)) - sec;
						startTacticsTimer();
						int[] moveFT = MoveParser2.Parse(boardView.getBoard(), boardView.getBoard().getTacticMoves()[0]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							boardView.getBoard().makeMove(m);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							boardView.getBoard().makeMove(m);
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
					} else {
						if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
							openOptionsMenu();
							return true;
						}
						boardView.setBoard(new Board2(this));
						boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;

						String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
						if (!FEN.equals("")) {
							boardView.getBoard().GenCastlePos(FEN);
							MoveParser2.FenParse(FEN, boardView.getBoard());
							String[] tmp2 = FEN.split(" ");
							if (tmp2.length > 1) {
								if (tmp2[1].trim().equals("w")) {
									boardView.getBoard().setReside(true);
								}
							}
						}

						if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
							boardView.getBoard().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
							boardView.getBoard().movesCount = 1;
						}
						boardView.getBoard().sec = sec;
						boardView.getBoard().left = Integer.parseInt(mainApp.getTactic()
								.values.get(AppConstants.AVG_SECONDS)) - sec;
						int[] moveFT = MoveParser2.Parse(boardView.getBoard(),
								boardView.getBoard().getTacticMoves()[0]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							boardView.getBoard().makeMove(m);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							boardView.getBoard().makeMove(m);
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
					}
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
		if(view.getId() == R.id.prev){
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
//			startActivity(new Intent(this, Tabs.class));
		}
	}

}