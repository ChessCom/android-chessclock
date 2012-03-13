package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.IntentConstants;
import com.chess.core.MainApp;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListElement;
import com.chess.model.Tactic;
import com.chess.model.TacticResult;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Web;
import com.flurry.android.FlurryAgent;
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
public class GameTacticsScreenActivity extends GameBaseActivity implements View.OnClickListener {

	private final static int DIALOG_TACTICS_LIMIT = 0;
	private final static int DIALOG_TACTICS_START_TACTICS = 1;
	private final static int DIALOG_TACTICS_HUNDRED = 2;
	private final static int DIALOG_TACTICS_OFFLINE_RATING = 3;

	private final static int CALLBACK_GET_TACTICS = 7;
	private final static int CALLBACK_ECHESS_MOVE_WAS_SENT = 8;
	private final static int CALLBACK_TACTICS_CORRECT = 6;
	private final static int CALLBACK_TACTICS_WRONG = 5;

//	private LinearLayout analysisLL;
//	private LinearLayout analysisButtons;

	private TextView timer;
	private Timer tacticsTimer = null;
	private int UPDATE_DELAY = 10000;

	private FirstTacticsDialogListener firstTackicsDialogListener;
	private MaxTacticksDialogListener maxTackicksDialogListener;
	private HundredTacticsDialogListener hundredTackicsDialogListener;
	private OfflineModeDialogListener offlineModeDialogListener;
	private CorrectDialogListener correctDialogListener;
	private WrongDialogListener wrongDialogListener;
	private WrongScoreDialogListener wrongScoreDialogListener;

	private MenuOptionsDialogListener menuOptionsDialogListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview2);

		init();
		init();
		widgetsInit();
		onPostCreate();
	}


	@Override
	protected void widgetsInit() {
		super.widgetsInit();
//		analysisLL = (LinearLayout) findViewById(R.id.analysis);
//		analysisButtons = (LinearLayout) findViewById(R.id.analysisButtons);

//		findViewById(R.id.prev).setOnClickListener(this);
//		findViewById(R.id.next).setOnClickListener(this);

		timer = (TextView) findViewById(R.id.timer);


//		if (newBoardView.getBoardFace() == null) {
			newBoardView.setBoardFace(new Board2(this));
            newBoardView.setGameActivityFace(this);
			newBoardView.getBoardFace().setInit(true);
			newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
			newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
			//newBoardView.getBoardFaceFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");


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

			if (MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
				showDialog(DIALOG_TACTICS_START_TACTICS);
			}
//		}
	}


	private class FirstTacticsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
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

			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				//mainApp.getTabHost().setCurrentTab(0);
				newBoardView.getBoardFace().setTacticCanceled(true);
			}
		}
	}

	private class MaxTacticksDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				FlurryAgent.onEvent("Upgrade From Tactics", null);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html")));
			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				//mainApp.getTabHost().setCurrentTab(0);
				newBoardView.getBoardFace().setTacticCanceled(true);
			}
		}
	}

	private class HundredTacticsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			//mainApp.getTabHost().setCurrentTab(0);
			mainApp.currentTacticProblem = 0;
		}
	}

	private class OfflineModeDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				GetGuestTacticsGame();
			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				//mainApp.getTabHost().setCurrentTab(0);
				newBoardView.getBoardFace().setTacticCanceled(true);
			}
		}
	}

	private class DrawOfferDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
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
	}

	private class AbortGameDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
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
					newBoardView.getBoardFace().setRetry(true);
					GetGuestTacticsGame();
				} else {
					GetTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
				}
			}
			if (which == 2) {
				newBoardView.finished = true;
				mainApp.getTactic().values.put(AppConstants.STOP, "1");
			}
		}
	}

	private class WrongScoreDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				GetTacticsGame("");
			}
			if (which == 1) {
				newBoardView.getBoardFace().setRetry(true);
				GetTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
			}
			if (which == 2) {
				newBoardView.finished = true;
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
						.setPositiveButton(R.string.okay, offlineModeDialogListener)
						.setNegativeButton(R.string.cancel, offlineModeDialogListener)
						.create();
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

	protected void init() {
		menuOptionsItems = new CharSequence[]{
				getString(R.string.skipproblem),
				getString(R.string.showanswer),
				getString(R.string.settings)};

		firstTackicsDialogListener = new FirstTacticsDialogListener();
		maxTackicksDialogListener = new MaxTacticksDialogListener();
		hundredTackicsDialogListener = new HundredTacticsDialogListener();
		offlineModeDialogListener = new OfflineModeDialogListener();
		correctDialogListener = new CorrectDialogListener();
		wrongDialogListener = new WrongDialogListener();
		wrongScoreDialogListener = new WrongScoreDialogListener();

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
	}

	@Override
	protected void onAbortOffered(int whichButton) {
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

	private void GetTacticsGame(final String id) {
		FlurryAgent.onEvent("Tactics Session Started For Registered", null);
		if (!mainApp.noInternet) {
			newBoardView.setBoardFace(new Board2(this));
			newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

			if (mainApp.getTactic() != null
					&& id.equals(mainApp.getTactic().values.get(AppConstants.ID))) {
				newBoardView.getBoardFace().setRetry(true);
				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals("")) {
					newBoardView.getBoardFace().genCastlePos(FEN);
					MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
					String[] tmp2 = FEN.split(" ");
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals("w")) {
							newBoardView.getBoardFace().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					newBoardView.getBoardFace().setTacticMoves(mainApp.getTactic()
							.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "")
							.replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
					newBoardView.getBoardFace().setMovesCount(1);
				}
				newBoardView.getBoardFace().setSec(0);
				newBoardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic().
						values.get(AppConstants.AVG_SECONDS)));
				startTacticsTimer();
				int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(),
						newBoardView.getBoardFace().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move m;
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					newBoardView.getBoardFace().makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					newBoardView.getBoardFace().makeMove(m);
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

		newBoardView.setBoardFace(new Board2(this));
		newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

		String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
		if (!FEN.equals("")) {
			newBoardView.getBoardFace().genCastlePos(FEN);
			MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
			String[] tmp = FEN.split(" ");
			if (tmp.length > 1) {
				if (tmp[1].trim().equals("w")) {
					newBoardView.getBoardFace().setReside(true);
				}
			}
		}
		if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
			newBoardView.getBoardFace().setTacticMoves(mainApp.getTacticsBatch()
					.get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "")
					.replaceAll("  ", " ").substring(1).split(" "));
			newBoardView.getBoardFace().setMovesCount(1);
		}
		newBoardView.getBoardFace().setSec(0);
		newBoardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTacticsBatch()
				.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)));
		startTacticsTimer();
		int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), newBoardView.getBoardFace().getTacticMoves()[0]);
		if (moveFT.length == 4) {
			Move m;
			if (moveFT[3] == 2)
				m = new Move(moveFT[0], moveFT[1], 0, 2);
			else
				m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			newBoardView.getBoardFace().makeMove(m);
		} else {
			Move m = new Move(moveFT[0], moveFT[1], 0, 0);
			newBoardView.getBoardFace().makeMove(m);
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
	}

	private void showAnswer() {
		newBoardView.setBoardFace(new Board2(this));
		newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);
		newBoardView.getBoardFace().setRetry(true);

		if (mainApp.guest || mainApp.noInternet) {
			String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
			if (!FEN.equals("")) {
				newBoardView.getBoardFace().genCastlePos(FEN);
				MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
				String[] tmp = FEN.split(" ");
				if (tmp.length > 1) {
					if (tmp[1].trim().equals("w")) {
						newBoardView.getBoardFace().setReside(true);
					}
				}
			}
			if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				newBoardView.getBoardFace().setTacticMoves(mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
				newBoardView.getBoardFace().setMovesCount(1);
			}
		} else {
			String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
			if (!FEN.equals("")) {
				newBoardView.getBoardFace().genCastlePos(FEN);
				MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
				String[] tmp2 = FEN.split(" ");
				if (tmp2.length > 1) {
					if (tmp2[1].trim().equals("w")) {
						newBoardView.getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				newBoardView.getBoardFace().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
				newBoardView.getBoardFace().setMovesCount(1);
			}
		}
		newBoardView.invalidate();


		new Thread(new Runnable() {
			public void run() {
				int i;
				for (i = 0; i < newBoardView.getBoardFace().getTacticMoves().length; i++) {
					int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), newBoardView.getBoardFace().getTacticMoves()[i]);
					try {
						Thread.sleep(1500);
					} catch (Exception ignored) {
					}
					if (moveFT.length == 4) {
						Move m;
						if (moveFT[3] == 2)
							m = new Move(moveFT[0], moveFT[1], 0, 2);
						else
							m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

						newBoardView.getBoardFace().makeMove(m);
					} else {
						Move m = new Move(moveFT[0], moveFT[1], 0, 0);
						newBoardView.getBoardFace().makeMove(m);
					}
					handler.sendEmptyMessage(0);
				}
			}

			private Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					update(CALLBACK_REPAINT_UI);
					newBoardView.invalidate();
				}
			};
		}).start();
	}

	private void checkTacticMoves() {
		Move m = newBoardView.getBoardFace().getHistDat()[newBoardView.getBoardFace().getHply() - 1].m;
		String f = "";
		int p = newBoardView.getBoardFace().getPieces()[m.to];
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
		Log.d("!!!", f + " | " + Moveto + " : " + newBoardView.getBoardFace().getTacticMoves()[newBoardView.getBoardFace().getHply() - 1]);
		if (newBoardView.getBoardFace().getTacticMoves()[newBoardView.getBoardFace().getHply() - 1].contains(f)
				&& newBoardView.getBoardFace().getTacticMoves()[newBoardView.getBoardFace().getHply() - 1].contains(Moveto)) {
			newBoardView.getBoardFace().increaseTacticsCorrectMoves();
			if (newBoardView.getBoardFace().getMovesCount() < newBoardView.getBoardFace().getTacticMoves().length - 1) {
				int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(),
						newBoardView.getBoardFace().getTacticMoves()[newBoardView.getBoardFace().getHply()]);
				if (moveFT.length == 4) {
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					newBoardView.getBoardFace().makeMove(m);
				} else {
					m = new Move(moveFT[0], moveFT[1], 0, 0);
					newBoardView.getBoardFace().makeMove(m);
				}
				update(CALLBACK_REPAINT_UI);
				newBoardView.invalidate();
			} else {
				if (mainApp.guest || newBoardView.getBoardFace().isRetry() || mainApp.noInternet) {
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
										+ "&passed=" + 1 + "&correct_moves=" + newBoardView.getBoardFace().getTacticsCorrectMoves()
										+ "&seconds=" + newBoardView.getBoardFace().getSec(),
								progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
					}
					stopTacticsTimer();
				}
			}
		} else {
			if (mainApp.guest || newBoardView.getBoardFace().isRetry() || mainApp.noInternet) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.wrong_ex)
						.setItems(getResources().getTextArray(R.array.wrongtactic), wrongDialogListener)
						.create().show();
				stopTacticsTimer();
			} else {
				if (appService != null) {
					appService.RunSingleTask(CALLBACK_TACTICS_WRONG,
							"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="
									+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
									+ "&tactics_id=" + mainApp.getTactic().values.get(AppConstants.ID)
									+ "&passed=" + 0 + "&correct_moves=" + newBoardView.getBoardFace().getTacticsCorrectMoves() + "&seconds=" + newBoardView.getBoardFace().getSec(),
							progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
				}
				stopTacticsTimer();
			}
		}
	}


	@Override
	public void update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(newBoardView.getBoardFace()))
					finish();
				else if (MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
					/*//mainApp.getTabHost().setCurrentTab(0);
					newBoardView.getBoardFaceFace().getTactic()Canceled = true;*/
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
							appService.RunRepeatbleTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
									"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
									null/*progressDialog*/
							);
						}
					}
				}
				break;
			case CALLBACK_REPAINT_UI: {
				switch (newBoardView.getBoardFace().getMode()) {
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

				if (MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
					if (newBoardView.getBoardFace().isAnalysis()) {
						timer.setVisibility(View.GONE);
//						analysisLL.setVisibility(View.VISIBLE);
//						if (!mainApp.isLiveChess() && analysisButtons != null) {
//							showAnalysisButtons();
//						}
					} else {
						whitePlayerLabel.setVisibility(View.GONE);
						blackPlayerLabel.setVisibility(View.GONE);
						timer.setVisibility(View.VISIBLE);
//						analysisLL.setVisibility(View.GONE);
//						if (!mainApp.isLiveChess() && analysisButtons != null) {
//							hideAnalysisButtons();
//						}
					}
				}
				newBoardView.addMove2Log(newBoardView.getBoardFace().MoveListSAN());
				newBoardView.invalidate();
				/*if(mainApp.getCurrentGame() != null && mainApp.getCurrentGame().values.get("move_list") != null)
								{
								  movelist.setText(mainApp.getCurrentGame().values.get("move_list"));
								}
								else
								{
								  movelist.setText(newBoardView.getBoardFaceFace().MoveListSAN());
								}*/

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
			case 4: {
				checkTacticMoves();
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

				newBoardView.setBoardFace(new Board2(this));
				newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

				String[] tmp = response.trim().split("[|]");
				if (tmp.length < 3 || tmp[2].trim().equals("")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				mainApp.setTactic(new Tactic(tmp[2].split(":")));

				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals("")) {
					newBoardView.getBoardFace().genCastlePos(FEN);
					MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
					String[] tmp2 = FEN.split(" ");
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals("w")) {
							newBoardView.getBoardFace().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					newBoardView.getBoardFace().setTacticMoves(mainApp.getTactic().
							values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
					newBoardView.getBoardFace().setMovesCount(1);
				}
				newBoardView.getBoardFace().setSec(0);
				newBoardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic().values.get(AppConstants.AVG_SECONDS)));
				startTacticsTimer();
				int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), newBoardView.getBoardFace().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move m;
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					newBoardView.getBoardFace().makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					newBoardView.getBoardFace().makeMove(m);
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

				mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));

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

				FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
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
			break;
			default:
				break;
		}
	}

	@Override
	public void showChoosePieceDialog(final int col,final int row) {
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
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_tactics, menu);
		return super.onCreateOptionsMenu(menu);
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
				showOptions();
				break;
			case R.id.menu_reside:
				newBoardView.flipBoard();
//				newBoardView.getBoardFace().setReside(!newBoardView.getBoardFace().isReside());
//				newBoardView.invalidate();
				break;
			case R.id.menu_analysis:
				newBoardView.switchAnalysis();
//				newBoardView.getBoardFace().setAnalysis(true);
//				update(CALLBACK_REPAINT_UI);
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
		private final int TACTICS_SKIP_PROBLEM = 0;
		private final int TACTICS_SHOW_ANSWER = 1;
		private final int TACTICS_SETTINGS = 2;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i) {
				case TACTICS_SKIP_PROBLEM: {
					if (mainApp.guest || mainApp.noInternet) {
						mainApp.currentTacticProblem++;
						GetGuestTacticsGame();
					} else
						GetTacticsGame("");
					break;
				}
				case TACTICS_SHOW_ANSWER: {
					showAnswer();
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
	protected void onResume() {
		super.onResume();
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

		if (newBoardView.getBoardFace().isTacticCanceled()) {
			newBoardView.getBoardFace().setTacticCanceled(false);
			showDialog(DIALOG_TACTICS_START_TACTICS);
			startTacticsTimer();
		} else if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("0")) {
			startTacticsTimer();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		stopTacticsTimer();
	}

	@Override
	protected void onGameEndMsgReceived() {
//		showSubmitButtonsLay(false);
//		findViewById(R.id.moveButtons).setVisibility(View.GONE);
	}

	public void stopTacticsTimer() {
		if (tacticsTimer != null) {
			tacticsTimer.cancel();
			tacticsTimer = null;
		}
	}

	public void startTacticsTimer() {
		stopTacticsTimer();
		newBoardView.finished = false;
		if (mainApp.getTactic() != null) {
			mainApp.getTactic().values.put(AppConstants.STOP, "0");
		}
		tacticsTimer = new Timer();
		tacticsTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (newBoardView.getBoardFace().isAnalysis())
					return;
				newBoardView.getBoardFace().increaseSec();
				if (newBoardView.getBoardFace().getLeft() > 0)
					newBoardView.getBoardFace().decreaseLeft();
				update.sendEmptyMessage(0);
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					timer.setText(getString(R.string.bonus_time_left, newBoardView.getBoardFace().getLeft()
							, newBoardView.getBoardFace().getSec()));
				}
			};
		}, 0, 1000);
	}


//	private void showAnalysisButtons() {
//		analysisButtons.setVisibility(View.VISIBLE);
//		findViewById(R.id.moveButtons).setVisibility(View.GONE);
//		/*newBoardView.getBoardFaceFace().takeBack();
//			newBoardView.getBoardFaceFace().getMovesCount()--;
//			newBoardView.invalidate();
//			newBoardView.getBoardFaceFace().submit = false;*/
//	}
//
//	private void hideAnalysisButtons() {
//		analysisButtons.setVisibility(View.GONE);
//	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (newBoardView.getBoardFace().isAnalysis()) {
				if (MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
					if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
						openOptionsMenu();
						return true;
					}
					int sec = newBoardView.getBoardFace().getSec();
					if (mainApp.guest || mainApp.noInternet) {
						newBoardView.setBoardFace(new Board2(this));
						newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

						String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
						if (!FEN.equals("")) {
							newBoardView.getBoardFace().genCastlePos(FEN);
							MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
							String[] tmp = FEN.split(" ");
							if (tmp.length > 1) {
								if (tmp[1].trim().equals("w")) {
									newBoardView.getBoardFace().setReside(true);
								}
							}
						}
						if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
							newBoardView.getBoardFace().setTacticMoves(mainApp.getTacticsBatch()
									.get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
							newBoardView.getBoardFace().setMovesCount(1);
						}
						newBoardView.getBoardFace().setSec(sec);
						newBoardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTacticsBatch()
								.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)) - sec);
						startTacticsTimer();
						int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), newBoardView.getBoardFace().getTacticMoves()[0]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							newBoardView.getBoardFace().makeMove(m);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							newBoardView.getBoardFace().makeMove(m);
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
					} else {
						if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
							openOptionsMenu();
							return true;
						}
						newBoardView.setBoardFace(new Board2(this));
						newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

						String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
						if (!FEN.equals("")) {
							newBoardView.getBoardFace().genCastlePos(FEN);
							MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
							String[] tmp2 = FEN.split(" ");
							if (tmp2.length > 1) {
								if (tmp2[1].trim().equals("w")) {
									newBoardView.getBoardFace().setReside(true);
								}
							}
						}

						if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
							newBoardView.getBoardFace().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" "));
							newBoardView.getBoardFace().setMovesCount(1);
						}
						newBoardView.getBoardFace().setSec(sec);
						newBoardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic()
								.values.get(AppConstants.AVG_SECONDS)) - sec);
						int[] moveFT = MoveParser2.Parse(newBoardView.getBoardFace(),
								newBoardView.getBoardFace().getTacticMoves()[0]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							newBoardView.getBoardFace().makeMove(m);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							newBoardView.getBoardFace().makeMove(m);
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
		/*if (view.getId() == R.id.prev) {
			newBoardView.finished = false;
			newBoardView.sel = false;
			newBoardView.getBoardFace().takeBack();
			newBoardView.invalidate();
			update(CALLBACK_REPAINT_UI);
			isMoveNav = true;
		} else if (view.getId() == R.id.next) {
			newBoardView.getBoardFace().takeNext();
			newBoardView.invalidate();
			update(CALLBACK_REPAINT_UI);
			isMoveNav = true;
		} else*/ if (view.getId() == R.id.newGame) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}
	}

}