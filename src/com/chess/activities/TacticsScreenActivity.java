package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.chess.R;
import com.chess.core.*;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.GameEvent;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.User;
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
 * TacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class TacticsScreenActivity extends CoreActivityActionBar implements View.OnClickListener{
	public BoardView2 boardView;
	private LinearLayout analysisLL;
	private LinearLayout analysisButtons;
	private RelativeLayout chatPanel;
	private ImageButton chatButton;
	private TextView white, black, thinking, timer, movelist;
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (boardView.getBoard().analysis) {
				if (boardView.getBoard().mode < 6) {
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
					Update(0);
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
								Update(0);
								boardView.invalidate();
							}
						};
					}).start();
				} else if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
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
						Update(0);
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
									Update(0);
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
						boardView.getBoard().left = Integer.parseInt(mainApp.getTactic().values.get(AppConstants.AVG_SECONDS)) - sec;
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
						Update(0);
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
									Update(0);
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
		if(view.getId() == R.id.chat){
			chat = true;
			GetOnlineGame(mainApp.getGameId());
			chatPanel.setVisibility(View.GONE);
		}else if(view.getId() == R.id.prev){
			boardView.finished = false;
			boardView.sel = false;
			boardView.getBoard().takeBack();
			boardView.invalidate();
			Update(0);
			isMoveNav = true;
		}else if(view.getId() == R.id.next){
			boardView.getBoard().takeNext();
			boardView.invalidate();
			Update(0);
			isMoveNav = true;
		}else if(view.getId() == R.id.newGame){
			startActivity(new Intent(this, OnlineNewGame.class));
		}else if(view.getId() == R.id.home){
			startActivity(new Intent(this, Tabs.class));
		}
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
				mainApp.getTabHost().setCurrentTab(0);
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
				mainApp.getTabHost().setCurrentTab(0);
				boardView.getBoard().setTacticCanceled(true);
			}
		}
	}

	private class HundredTackicsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			mainApp.getTabHost().setCurrentTab(0);
			mainApp.currentTacticProblem = 0;
		}
	}

	private class OfflineModeDialogListener implements DialogInterface.OnClickListener{
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				GetGuestTacticsGame();
			}else if(whichButton == DialogInterface.BUTTON_NEGATIVE){
				mainApp.getTabHost().setCurrentTab(0);
				boardView.getBoard().setTacticCanceled(true);
			}
		}
	}

	private class DrawOfferDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
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
				if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
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
			case 0:
				FlurryAgent.onEvent("Tactics Daily Limit Exceded", null);
				return new AlertDialog.Builder(this)
						.setTitle(getString(R.string.daily_limit_exceeded))
						.setMessage(getString(R.string.max_tackics_for_today_reached))
						.setPositiveButton(getString(R.string.ok), maxTackicksDialogListener)
						.setNegativeButton(R.string.cancel, maxTackicksDialogListener)
						.create();
			case 1:
				return new AlertDialog.Builder(this)
						.setTitle(getString(R.string.ready_for_first_tackics_q))
						.setPositiveButton(R.string.yes, firstTackicsDialogListener)
						.setNegativeButton(R.string.no, firstTackicsDialogListener)
						.create();
			case 2:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.hundred_tackics_completed)
						.setNegativeButton(R.string.okay, hundredTackicsDialogListener)
						.create();
			case 3:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.offline_mode)
						.setMessage(getString(R.string.no_network_rating_not_changed))
						.setPositiveButton(R.string.okay,offlineModeDialogListener)
						.setNegativeButton(R.string.cancel, offlineModeDialogListener)
						.create();
			case 4:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.drawoffer)
						.setMessage(getString(R.string.are_you_sure_q))
						.setPositiveButton(getString(R.string.ok), drawOfferDialogListener)
						.setNegativeButton(getString(R.string.cancel),drawOfferDialogListener)
						.create();
			case 5:
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
		firstTackicsDialogListener = new FirstTackicsDialogListener();
		maxTackicksDialogListener = new MaxTackicksDialogListener();
		hundredTackicsDialogListener = new HundredTackicsDialogListener();
		offlineModeDialogListener = new OfflineModeDialogListener();
		drawOfferDialogListener = new DrawOfferDialogListener();
		abortGameDialogListener = new AbortGameDialogListener();
		correctDialogListener = new CorrectDialogListener();
		wrongDialogListener = new WrongDialogListener();
		wrongScoreDialogListener = new WrongScoreDialogListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mainApp.isLiveChess() && extras.getInt(AppConstants.GAME_MODE) == 4) {
			setContentView(R.layout.boardviewlive2);
//			lccHolder.getAndroid().setGameActivity(this);   //TODO
		} else {
			setContentView(R.layout.boardview2);
		}

//		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		init();

		analysisLL = (LinearLayout) findViewById(R.id.analysis);
		analysisButtons = (LinearLayout) findViewById(R.id.analysisButtons);
		if (mainApp.isLiveChess() && extras.getInt(AppConstants.GAME_MODE) != AppConstants.GAME_MODE_TACTICS) {
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
		if (mainApp.isLiveChess() && extras.getInt(AppConstants.GAME_MODE) == 4
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

			if (boardView.getBoard().mode < 4
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
				if (boardView.getBoard().mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK)
					boardView.getBoard().setReside(true);
			} else {
				if (boardView.getBoard().mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK) {
					boardView.getBoard().setReside(true);
					boardView.invalidate();
					boardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (boardView.getBoard().mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
					boardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (boardView.getBoard().mode == 4 || boardView.getBoard().mode == 5)
					mainApp.setGameId(extras.getString(AppConstants.GAME_ID));
			}
			if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
				showDialog(1);
				return;
			}
		}

		if (MobclixHelper.isShowAds(mainApp) && getRectangleAdview() == null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4")) {
			setRectangleAdview(new MobclixIABRectangleMAdView(this));
			getRectangleAdview().setRefreshTime(-1);
			getRectangleAdview().addMobclixAdViewListener(new MobclixAdViewListenerImpl(true, mainApp));
			mainApp.setForceRectangleAd(false);
		}

		Update(0);
	}

	private void GetOnlineGame(final String game_id) {
		if (appService != null && appService.getRepeatableTimer() != null) {
			appService.getRepeatableTimer().cancel();
			appService.setRepeatableTimer(null);
		}
		mainApp.setGameId(game_id);

		if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
			Update(10);
		} else {
			if (appService != null) {
				appService.RunSingleTask(10,
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
				Update(0);
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
							Update(0);
							boardView.invalidate();
						}
					};
				}).start();

				return;
			}
		}
		if (appService != null) {
			appService.RunSingleTask(7,
					"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="
							+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&tactics_id=" + id,
					progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), false))
			);
		}
	}

	private void GetGuestTacticsGame() {
		FlurryAgent.onEvent("Tactics Session Started For Guest", null);

		if (mainApp.currentTacticProblem >= mainApp.getTacticsBatch().size()) {
			showDialog(2);
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
		Update(0);
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
					Update(0);
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
					Update(0);
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
				Update(0);
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
						appService.RunSingleTask(6,
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
					appService.RunSingleTask(5,
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
		if (boardView.getBoard() != null && boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
			mainApp.getTabHost().setCurrentTab(0);
			boardView.getBoard().setTacticCanceled(true);
		} else {
			finish();
		}
	}

	@Override
	public void Update(int code) {
		switch (code) {
			case -2:
				if (boardView.getBoard().mode < 6)
					finish();
				else if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
					/*mainApp.getTabHost().setCurrentTab(0);
					boardView.getBoard().getTactic()Canceled = true;*/
					if (mainApp.noInternet) {
						if (mainApp.offline) {
							GetGuestTacticsGame();
						} else {
							mainApp.offline = true;
							showDialog(3);
						}
						return;
					}
				}
				//finish();
				break;
			case -1:
				if (boardView.getBoard().init && boardView.getBoard().mode == 4 || boardView.getBoard().mode == 5) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					GetOnlineGame(mainApp.getGameId());
					boardView.getBoard().init = false;
				} else if (!boardView.getBoard().init) {
					if (boardView.getBoard().mode == 4 && appService != null
							&& appService.getRepeatableTimer() == null) {
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
						if (!mainApp.isLiveChess()) {
							appService.RunRepeatbleTask(9, UPDATE_DELAY, UPDATE_DELAY,
									"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
									null/*progressDialog*/
							);
						}
					}
				}
				break;
			case 0: {
				switch (boardView.getBoard().mode) {
					case 0: {	//w - human; b - comp
						white.setText(getString(R.string.Human));
						black.setText(getString(R.string.Computer));
						break;
					}
					case 1: {	//w - comp; b - human
						white.setText(getString(R.string.Computer));
						black.setText(getString(R.string.Human));
						break;
					}
					case 2: {	//w - human; b - human
						white.setText(getString(R.string.Human));
						black.setText(getString(R.string.Human));
						break;
					}
					case 3: {	//w - comp; b - comp
						white.setText(getString(R.string.Computer));
						black.setText(getString(R.string.Computer));
						break;
					}
					case 4: {
						if (boardView.getBoard().submit)
							findViewById(R.id.moveButtons).setVisibility(View.VISIBLE);
						findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Update(1);	//movesubmit
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

				if (boardView.getBoard().mode < 4) {
					hideAnalysisButtons();
				}

				if (boardView.getBoard().mode == 4 || boardView.getBoard().mode == 5) {
					if (mainApp.getCurrentGame() != null) {
						white.setText(mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("white_rating") + ")");
						black.setText(mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("black_rating") + ")");
					}
				}

				if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
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
			case 1: {
				// making the move
				findViewById(R.id.moveButtons).setVisibility(View.GONE);
				boardView.getBoard().submit = false;
				//String myMove = boardView.getBoard().MoveSubmit();
				if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
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
						appService.RunSingleTask(12,
								"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + mainApp.getGameId(),
								null);
					} else {
						appService.RunSingleTask(8,
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
			case 12: {
				mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));
				if (!mainApp.isLiveChess() && appService != null) {
					appService.RunSingleTask(8,
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
			case 4: {
				CheckTacticMoves();
				break;
			}
			case 5: {
				String[] tmp = response.split("[|]");
				if (tmp.length < 2 || tmp[1].trim().equals("")) {
					showDialog(0);
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
			case 6: {
				String[] tmp = response.split("[|]");
				if (tmp.length < 2 || tmp[1].trim().equals("")) {
					showDialog(0);
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
			case 7:

				boardView.setBoard(new Board2(this));
				boardView.getBoard().mode = AppConstants.GAME_MODE_TACTICS;

				String[] tmp = response.trim().split("[|]");
				if (tmp.length < 3 || tmp[2].trim().equals("")) {
					showDialog(0);
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
				Update(0);
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
							Update(0);
							boardView.invalidate();
						}
					};
				}).start();
				break;
			case 8:
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
								boardView.getBoard().mode = 4;

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
			case 9:
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
								|| ((mainApp.isLiveChess() && boardView.getBoard().mode == 4))) {

							int beginIndex = (mainApp.isLiveChess() && boardView.getBoard().mode == 4) ? 0 : 1;

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
								Update(0);
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
			case 10:
				// handle game start

				getSoundPlayer().playGameStart();

				if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
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

				Update(0);
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
							Update(0);
							boardView.invalidate();
						}
					};
				}).start();
				if (boardView.getBoard().mode == 4 && appService != null && appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					if (!mainApp.isLiveChess()) {
						appService.RunRepeatbleTask(9, UPDATE_DELAY, UPDATE_DELAY,
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
		if (boardView.getBoard().mode < 4) {
			menu.add(0, 0, 0, getString(R.string.newgame)).setIcon(R.drawable.newgame);
			SubMenu options = menu.addSubMenu(0, 1, 0, getString(R.string.options)).setIcon(R.drawable.options);
			menu.add(0, 2, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
			menu.add(0, 3, 0, getString(R.string.hint)).setIcon(R.drawable.hint);
			menu.add(0, 4, 0, getString(R.string.prev)).setIcon(R.drawable.prev);
			menu.add(0, 5, 0, getString(R.string.next)).setIcon(R.drawable.next);

			options.add(0, 6, 0, getString(R.string.ngwhite));
			options.add(0, 7, 0, getString(R.string.ngblack));
			options.add(0, 8, 0, getString(R.string.emailgame));
			options.add(0, 9, 0, getString(R.string.settings));
		} else if (boardView.getBoard().mode < 6) {
			SubMenu options;
			if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
				options = menu.addSubMenu(0, 0, 0, getString(R.string.options)).setIcon(R.drawable.options);
				if (mainApp.getCurrentGame().values.get("has_new_message").equals("1")) {
					menu.add(0, 6, 0, getString(R.string.chat)).setIcon(R.drawable.chat_nm);
				} else {
					menu.add(0, 6, 0, getString(R.string.chat)).setIcon(R.drawable.chat);
				}
			} else {
				menu.add(0, 0, 0, getString(R.string.nextgame)).setIcon(R.drawable.forward);
				options = menu.addSubMenu(0, 1, 0, getString(R.string.options)).setIcon(R.drawable.options);
				menu.add(0, 2, 0, getString(R.string.analysis)).setIcon(R.drawable.analysis);
				try {
					if (mainApp.getCurrentGame().values.get("has_new_message").equals("1")) {
						menu.add(0, 3, 0, getString(R.string.chat)).setIcon(R.drawable.chat_nm);
					} else {
						menu.add(0, 3, 0, getString(R.string.chat)).setIcon(R.drawable.chat);
					}
				} catch (Exception e) {
					menu.add(0, 3, 0, getString(R.string.chat)).setIcon(R.drawable.chat);
				}
				menu.add(0, 4, 0, getString(R.string.prev)).setIcon(R.drawable.prev);
				menu.add(0, 5, 0, getString(R.string.next)).setIcon(R.drawable.next);
			}

			if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
				options.add(0, 1, 0, getString(R.string.settings)).setIcon(R.drawable.options);
				options.add(0, 2, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
				options.add(0, 3, 0, getString(R.string.drawoffer));
				options.add(0, 4, 0, getString(resignOrAbort));
				options.add(0, 5, 0, getString(R.string.messages)).setIcon(R.drawable.chat);
			} else {
				options.add(0, 6, 0, getString(R.string.settings)).setIcon(R.drawable.options);
				options.add(0, 7, 0, getString(R.string.backtogamelist)).setIcon(R.drawable.prev);
				options.add(0, 8, 0, getString(R.string.messages)).setIcon(R.drawable.chat);
				options.add(0, 9, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
				options.add(0, 10, 0, getString(R.string.drawoffer));
				options.add(0, 11, 0, getString(R.string.resignorabort));
			}
		} else if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
			menu.add(0, 0, 0, getString(R.string.nextgame)).setIcon(R.drawable.forward);
			SubMenu Options = menu.addSubMenu(0, 1, 0, getString(R.string.options)).setIcon(R.drawable.options);
			menu.add(0, 2, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
			menu.add(0, 3, 0, getString(R.string.analysis)).setIcon(R.drawable.analysis);
			menu.add(0, 4, 0, getString(R.string.prev)).setIcon(R.drawable.prev);
			menu.add(0, 5, 0, getString(R.string.next)).setIcon(R.drawable.next);

			Options.add(0, 6, 0, getString(R.string.skipproblem));
			Options.add(0, 7, 0, getString(R.string.showanswer));
			Options.add(0, 8, 0, getString(R.string.settings));

		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainApp.getCurrentGame() != null && boardView.getBoard().mode < 6 && boardView.getBoard().mode > 3) {
			int itemPosition = mainApp.isLiveChess() ? 1 : 3;
			if (mainApp.getCurrentGame().values.get("has_new_message").equals("1"))
				menu.getItem(itemPosition).setIcon(R.drawable.chat_nm);
			else
				menu.getItem(itemPosition).setIcon(R.drawable.chat);
		}

		if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
			final SubMenu options = menu.getItem(0).getSubMenu();
			if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
				resignOrAbort = R.string.resign;
			} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
				resignOrAbort = R.string.abort;
			} else {
				resignOrAbort = R.string.resign;
			}
			options.findItem(4).setTitle(resignOrAbort);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (boardView.getBoard().mode < 4) {
			switch (item.getItemId()) {
				case 0:
					boardView.stopThinking = true;
					finish();
					return true;
				case 1:
					boardView.stopThinking = true;
					return true;
				case 2:
					boardView.stopThinking = true;
					if (!boardView.compmoving) {
						boardView.getBoard().setReside(!boardView.getBoard().reside);
						if (boardView.getBoard().mode < 2) {
							boardView.getBoard().mode ^= 1;
							boardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
									.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
											+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
						}
						boardView.invalidate();
						Update(0);
					}
					return true;
				case 3:
					boardView.stopThinking = true;
					if (!boardView.compmoving) {
						boardView.hint = true;
						boardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
								.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
										+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					}
					return true;
				case 4:
					boardView.stopThinking = true;
					if (!boardView.compmoving) {
						boardView.finished = false;
						boardView.sel = false;
						boardView.getBoard().takeBack();
						boardView.invalidate();
						Update(0);
						isMoveNav = true;
					}
					return true;
				case 5:
					boardView.stopThinking = true;
					if (!boardView.compmoving) {
						boardView.sel = false;
						boardView.getBoard().takeNext();
						boardView.invalidate();
						Update(0);
						isMoveNav = true;
					}
					return true;
				case 6: {
					boardView.setBoard(new Board2(this));
					boardView.getBoard().mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
					boardView.getBoard().GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					boardView.invalidate();
					Update(0);
					return true;
				}
				case 7: {
					boardView.setBoard(new Board2(this));
					boardView.getBoard().mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
					boardView.getBoard().setReside(true);
					boardView.getBoard().GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					boardView.invalidate();
					Update(0);
					boardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					return true;
				}
				case 8: {
					String moves = movelist.getText().toString();
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "[Site \"Chess.com Android\"]\n [White \"" + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\"]\n [White \"" + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\"]\n [Result \"X-X\"]\n \n \n " + moves + " \n \n Sent from my Android");
					startActivity(Intent.createChooser(emailIntent, "Send mail..."));
					return true;
				}
				case 9: {
					startActivity(new Intent(coreContext, Preferences.class));
					return true;
				}
			}
		} else if (boardView.getBoard().mode < 6) {
			if (mainApp.isLiveChess() && boardView.getBoard().mode == 4) {
				switch (item.getItemId()) {
					case 1: {
						startActivity(new Intent(coreContext, Preferences.class));
						return true;
					}
					case 2: {
						boardView.getBoard().setReside(!boardView.getBoard().reside);
						boardView.invalidate();
						return true;
					}
					case 3: {
						showDialog(4);
						return true;
					}
					case 4: {
						showDialog(5);
						return true;
					}
					case 5:
					case 6: {
						chat = true;
						GetOnlineGame(mainApp.getGameId());
						return true;
					}
				}
			} else {
				switch (item.getItemId()) {
					case 0:
						if (boardView.getBoard().mode == 4) {
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
										boardView.getBoard().mode = 4;
										GetOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
										return true;
									} else {
										finish();
										return true;
									}
								}
							}
							finish();
							return true;
						} else if (boardView.getBoard().mode == 5) {
							int i;
							ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
							for (GameListElement gle : mainApp.getGameListItems()) {
								if (gle.type == 2) {
									currentGames.add(gle);
								}
							}
							for (i = 0; i < currentGames.size(); i++) {
								if (currentGames.get(i).values.get(AppConstants.GAME_ID).contains(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID))) {
									if (i + 1 < currentGames.size()) {
										boardView.getBoard().analysis = false;
										boardView.setBoard(new Board2(this));
										boardView.getBoard().mode = 5;
										GetOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
										return true;
									} else {
										finish();
										return true;
									}
								}
							}
							finish();
							return true;
						}
						return true;
					case 2:
						boardView.getBoard().analysis = true;
						Update(0);
						return true;
					case 3:
						chat = true;
						GetOnlineGame(mainApp.getGameId());
						return true;
					case 4:
						boardView.finished = false;
						boardView.sel = false;
						boardView.getBoard().takeBack();
						boardView.invalidate();
						Update(0);
						isMoveNav = true;
						return true;
					case 5:
						boardView.getBoard().takeNext();
						boardView.invalidate();
						Update(0);
						isMoveNav = true;
						return true;
					case 6: {
						startActivity(new Intent(coreContext, Preferences.class));
						return true;
					}
					case 7: {
						finish();
						return true;
					}
					case 8: {
						chat = true;
						GetOnlineGame(mainApp.getGameId());
						return true;
					}
					case 9: {
						boardView.getBoard().setReside(!boardView.getBoard().reside);
						boardView.invalidate();
						return true;
					}
					case 10: {
						showDialog(4);
						return true;
					}
					case 11: {
						showDialog(5);
						return true;
					}
				}
			}
		} else if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
			switch (item.getItemId()) {
				case 0:
					if (mainApp.guest) {
						mainApp.currentTacticProblem++;
						GetGuestTacticsGame();
					} else {
						if (mainApp.noInternet) mainApp.currentTacticProblem++;
						closeOptionsMenu();
						GetTacticsGame("");
					}
					return true;
				case 2:
					boardView.getBoard().setReside(!boardView.getBoard().reside);
					boardView.invalidate();
					return true;
				case 3:
					boardView.getBoard().analysis = true;
					Update(0);
					return true;
				case 4:
					boardView.finished = false;
					boardView.sel = false;
					boardView.getBoard().takeBack();
					boardView.invalidate();
					Update(0);
					isMoveNav = true;
					return true;
				case 5:
					boardView.getBoard().takeNext();
					boardView.invalidate();
					Update(0);
					isMoveNav = true;
					return true;
				case 6: {
					if (mainApp.guest || mainApp.noInternet) {
						mainApp.currentTacticProblem++;
						GetGuestTacticsGame();
					} else
						GetTacticsGame("");
					return true;
				}
				case 7: {
					ShowAnswer();
					return true;
				}
				case 8: {
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					return true;
				}
			}
		}
		return false;
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
		if (MobclixHelper.isShowAds(mainApp) && !mainApp.getTabHost().getCurrentTabTag().equals("tab4") && adviewWrapper != null && getRectangleAdview() != null) {
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
		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
		registerReceiver(showGameEndPopupReceiver, new IntentFilter(IntentConstants.ACTION_SHOW_GAME_END_POPUP));

		if (boardView.getBoard().mode == AppConstants.GAME_MODE_TACTICS) {
			if (boardView.getBoard().isTacticCanceled()) {
				boardView.getBoard().setTacticCanceled(false);
				showDialog(1);
				startTacticsTimer();
			} else if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("0")) {
				startTacticsTimer();
			}
		}
		if (mainApp.isLiveChess() && mainApp.getGameId() != null && mainApp.getGameId() != ""
				&& lccHolder.getGame(mainApp.getGameId()) != null) {
			game = new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(),
					lccHolder.getGame(mainApp.getGameId()).getSeq() - 1), true);
//			lccHolder.getAndroid().setGameActivity(this); // TODO
			if (lccHolder.isActivityPausedMode()) {
				executePausedActivityGameEvents();
				lccHolder.setActivityPausedMode(false);
			}
			//lccHolder.updateClockTime(lccHolder.getGame(mainApp.getGameId()));
		}

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
			Update(9);
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
			findViewById(R.id.newGame).setOnClickListener(TacticsScreenActivity.this);
			findViewById(R.id.home).setOnClickListener(TacticsScreenActivity.this);
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
				Update(9);
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
}