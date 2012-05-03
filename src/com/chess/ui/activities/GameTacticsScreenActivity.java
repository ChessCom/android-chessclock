package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.MainApp;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MyProgressDialog;
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

	public final static int CALLBACK_GET_TACTICS = 7;
	public final static int CALLBACK_TACTICS_CORRECT = 6;
	public final static int CALLBACK_TACTICS_WRONG = 5;
	public final static int CALLBACK_CHECK_TACTICS_MOVE = 4;

	private TextView timer;
	private Timer tacticsTimer = null;
	private int UPDATE_DELAY = 10000;

	private FirstTacticsDialogListener firstTacticsDialogListener;
	private MaxTacticsDialogListener maxTacticsDialogListener;
	private HundredTacticsDialogListener hundredTacticsDialogListener;
	private OfflineModeDialogListener offlineModeDialogListener;
	private CorrectDialogListener correctDialogListener;
	private WrongDialogListener wrongDialogListener;
	private WrongScoreDialogListener wrongScoreDialogListener;

	private MenuOptionsDialogListener menuOptionsDialogListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview);

		init();
		widgetsInit();
		onPostCreate();
	}

    @Override
    protected void init() {
        menuOptionsItems = new CharSequence[]{
                getString(R.string.skipproblem),
                getString(R.string.showanswer),
                getString(R.string.settings)};

        firstTacticsDialogListener = new FirstTacticsDialogListener();
        maxTacticsDialogListener = new MaxTacticsDialogListener();
        hundredTacticsDialogListener = new HundredTacticsDialogListener();
        offlineModeDialogListener = new OfflineModeDialogListener();
        correctDialogListener = new CorrectDialogListener();
        wrongDialogListener = new WrongDialogListener();
        wrongScoreDialogListener = new WrongScoreDialogListener();

        menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
    }

    @Override
    protected void widgetsInit() {
        super.widgetsInit();

        timer = (TextView) findViewById(R.id.timer);
        timer.setVisibility(View.VISIBLE);
        whitePlayerLabel.setVisibility(View.GONE);
        blackPlayerLabel.setVisibility(View.GONE);


		if (getLastCustomNonConfigurationInstance() == null) {
			showDialog(DIALOG_TACTICS_START_TACTICS);
		}

        gamePanelView.hideGameButton(GamePanelView.B_CHAT_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (boardView.getBoardFace().isTacticCanceled()) {
            boardView.getBoardFace().setTacticCanceled(false);
            showDialog(DIALOG_TACTICS_START_TACTICS);    // TODO show register confirmation dialog
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
    public Object onRetainCustomNonConfigurationInstance () {
        return boardView.getBoardFace();
    }

	private class FirstTacticsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				InputStream f = getResources().openRawResource(R.raw.tactics100batch);
				try {
					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current;
					while ((current = f.read()) != -1) {
						baf.append((byte) current);
					}

					String input = new String(baf.toByteArray());
					String[] tmp = input.split("[|]");
					int count = tmp.length - 1;

					mainApp.setTacticsBatch(new ArrayList<TacticItem>(count));
					for (int i = 1; i <= count; i++) {
						mainApp.getTacticsBatch().add(new TacticItem(tmp[i].split(":")));
					}
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (mainApp.guest)
					getGuestTacticsGame();
				else
					getTacticsGame(AppConstants.SYMBOL_EMPTY);

			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				boardView.getBoardFace().setTacticCanceled(true);
				onBackPressed();
			}
		}
	}

	private class MaxTacticsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				FlurryAgent.onEvent("Upgrade From Tactics", null);
				startActivity(mainApp.getMembershipIntent(AppConstants.SYMBOL_EMPTY));
			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				boardView.getBoardFace().setTacticCanceled(true);
				onBackPressed();
			}
		}
	}

	private class HundredTacticsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			mainApp.currentTacticProblem = 0;
			onBackPressed();
		}
	}

	private class OfflineModeDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				getGuestTacticsGame();
			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				onBackPressed();
				boardView.getBoardFace().setTacticCanceled(true);
			}
		}
	}

	private class CorrectDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 1) {
				if (mainApp.guest) {
					mainApp.currentTacticProblem++;
					getGuestTacticsGame();
				} else {
					if (mainApp.noInternet) mainApp.currentTacticProblem++;
					getTacticsGame(AppConstants.SYMBOL_EMPTY);
				}
			}
		}
	}

	private class WrongDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {  // Next
				if (mainApp.guest) {
					mainApp.currentTacticProblem++;
					getGuestTacticsGame();
				} else {
					if (mainApp.noInternet) mainApp.currentTacticProblem++;
					getTacticsGame(AppConstants.SYMBOL_EMPTY);
				}
			}
			if (which == 1) {  // Retry
				if (mainApp.guest || mainApp.noInternet) {
					boardView.getBoardFace().setRetry(true);
					getGuestTacticsGame();
				} else {
					getTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
				}
			}
			if (which == 2) { // Stop
				boardView.finished = true;
				mainApp.getTactic().values.put(AppConstants.STOP, "1");
			}
		}
	}

	private class WrongScoreDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				getTacticsGame(AppConstants.SYMBOL_EMPTY);
			}
			if (which == 1) {
				boardView.getBoardFace().setRetry(true);
				getTacticsGame(mainApp.getTactic().values.get(AppConstants.ID));
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
						.setPositiveButton(getString(R.string.ok), maxTacticsDialogListener)
						.setNegativeButton(R.string.cancel, maxTacticsDialogListener)
						.create();
			case DIALOG_TACTICS_START_TACTICS:   // TODO show register confirmation dialog
				return new AlertDialog.Builder(this)
						.setTitle(getString(R.string.ready_for_first_tackics_q))
						.setPositiveButton(R.string.yes, firstTacticsDialogListener)
						.setNegativeButton(R.string.no, firstTacticsDialogListener)
						.create();
			case DIALOG_TACTICS_HUNDRED:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.hundred_tackics_completed)
						.setNegativeButton(R.string.okay, hundredTacticsDialogListener)
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

	@Override
	protected void onDrawOffered(int whichButton) {
	}

	@Override
	protected void onAbortOffered(int whichButton) {
	}


	@Override
	protected void getOnlineGame(long game_id) {
		super.getOnlineGame(game_id);
		if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())) {
			update(CALLBACK_GAME_STARTED);
		} else {
			if (appService != null) {
				appService.RunSingleTask(CALLBACK_GAME_STARTED,
						"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID
								+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + "&gid=" + game_id,
						null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
			}
		}
	}

	private void getTacticsGame(final String id) {
		FlurryAgent.onEvent("Tactics Session Started For Registered", null);
		if (!mainApp.noInternet) {
			boardView.setBoardFace(new ChessBoard(this));
			boardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

			if (mainApp.getTactic() != null
					&& id.equals(mainApp.getTactic().values.get(AppConstants.ID))) {
				boardView.getBoardFace().setRetry(true);
				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
					boardView.getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, boardView.getBoardFace());
					String[] tmp2 = FEN.split(AppConstants.SYMBOL_SPACE);
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
							boardView.getBoardFace().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					boardView.getBoardFace().setTacticMoves(mainApp.getTactic()
							.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY)
							.replaceAll("[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(1).split(AppConstants.SYMBOL_SPACE));
					boardView.getBoardFace().setMovesCount(1);
				}
				boardView.getBoardFace().setSec(0);
				boardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic().
						values.get(AppConstants.AVG_SECONDS)));
				startTacticsTimer();
				int[] moveFT = MoveParser.parse(boardView.getBoardFace(),
						boardView.getBoardFace().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move move;
					if (moveFT[3] == 2)
						move = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoardFace().makeMove(move);
				} else {
					Move move = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoardFace().makeMove(move);
				}
				update(CALLBACK_REPAINT_UI);
				boardView.getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();
				return;
			}
		}
		if (appService != null) {
			appService.RunSingleTask(CALLBACK_GET_TACTICS,
					"http://www." + LccHolder.HOST + AppConstants.API_TACTICS_TRAINER_ID_PARAMETER
							+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + AppConstants.TACTICS_ID_PARAMETER + id,
					progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), false))
			);
		}
	}

	private void getGuestTacticsGame() {
		FlurryAgent.onEvent("Tactics Session Started For Guest", null);

		if (mainApp.currentTacticProblem >= mainApp.getTacticsBatch().size()) {
			showDialog(DIALOG_TACTICS_HUNDRED);
			return;
		}

		boardView.setBoardFace(new ChessBoard(this));
		boardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

		String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
		if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
			boardView.getBoardFace().genCastlePos(FEN);
			MoveParser.fenParse(FEN, boardView.getBoardFace());
			String[] tmp = FEN.split(AppConstants.SYMBOL_SPACE);
			if (tmp.length > 1) {
				if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
					boardView.getBoardFace().setReside(true);
				}
			}
		}
		if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
			boardView.getBoardFace().setTacticMoves(mainApp.getTacticsBatch()
					.get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY).replaceAll("[.]", AppConstants.SYMBOL_EMPTY)
					.replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(1).split(AppConstants.SYMBOL_SPACE));
			boardView.getBoardFace().setMovesCount(1);
		}
		boardView.getBoardFace().setSec(0);
		boardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTacticsBatch()
				.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)));
		startTacticsTimer();
		int[] moveFT = MoveParser.parse(boardView.getBoardFace(), boardView.getBoardFace().getTacticMoves()[0]);
		if (moveFT.length == 4) {
			Move move;
			if (moveFT[3] == 2)
				move = new Move(moveFT[0], moveFT[1], 0, 2);
			else
				move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			boardView.getBoardFace().makeMove(move);
		} else {
			Move move = new Move(moveFT[0], moveFT[1], 0, 0);
			boardView.getBoardFace().makeMove(move);
		}
		update(CALLBACK_REPAINT_UI);
		boardView.getBoardFace().takeBack();
		boardView.invalidate();

		mainApp.setTactic(mainApp.getTacticsBatch().get(mainApp.currentTacticProblem));
		playLastMoveAnimation();
	}

	private void showAnswer() {
		boardView.setBoardFace(new ChessBoard(this));
		boardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);
		boardView.getBoardFace().setRetry(true);

		if (mainApp.guest || mainApp.noInternet) {
			String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
			if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
				boardView.getBoardFace().genCastlePos(FEN);
				MoveParser.fenParse(FEN, boardView.getBoardFace());
				String[] tmp = FEN.split(AppConstants.SYMBOL_SPACE);
				if (tmp.length > 1) {
					if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
						boardView.getBoardFace().setReside(true);
					}
				}
			}
			if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				boardView.getBoardFace().setTacticMoves(mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY).replaceAll("[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(1).split(AppConstants.SYMBOL_SPACE));
				boardView.getBoardFace().setMovesCount(1);
			}
		} else {
			String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
			if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
				boardView.getBoardFace().genCastlePos(FEN);
				MoveParser.fenParse(FEN, boardView.getBoardFace());
				String[] tmp2 = FEN.split(AppConstants.SYMBOL_SPACE);
				if (tmp2.length > 1) {
					if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
						boardView.getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				boardView.getBoardFace().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY).replaceAll("[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(1).split(AppConstants.SYMBOL_SPACE));
				boardView.getBoardFace().setMovesCount(1);
			}
		}
		boardView.invalidate();


		new Thread(new Runnable() {
			@Override
			public void run() {
				int i;
				for (i = 0; i < boardView.getBoardFace().getTacticMoves().length; i++) {
					int[] moveFT = MoveParser.parse(boardView.getBoardFace(), boardView.getBoardFace().getTacticMoves()[i]);
					try {
						Thread.sleep(1500);
					} catch (Exception ignored) {
					}
					if (moveFT.length == 4) {
						Move move;
						if (moveFT[3] == 2)
							move = new Move(moveFT[0], moveFT[1], 0, 2);
						else
							move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

						boardView.getBoardFace().makeMove(move);
					} else {
						Move move = new Move(moveFT[0], moveFT[1], 0, 0);
						boardView.getBoardFace().makeMove(move);
					}
					handler.sendEmptyMessage(0);
				}
			}

			private Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					update(CALLBACK_REPAINT_UI);
					boardView.invalidate();
				}
			};
		}).start();
	}

	private void checkTacticMoves() {
		Move move = boardView.getBoardFace().getHistDat()[boardView.getBoardFace().getHply() - 1].m;
		String f = AppConstants.SYMBOL_EMPTY;
		int p = boardView.getBoardFace().getPieces()[move.to];
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
		String moveTo = MoveParser.positionToString(move.to);
		Log.d("!!!", f + " | " + moveTo + " : " + boardView.getBoardFace().getTacticMoves()[boardView.getBoardFace().getHply() - 1]);
		if (boardView.getBoardFace().getTacticMoves()[boardView.getBoardFace().getHply() - 1].contains(f)
				&& boardView.getBoardFace().getTacticMoves()[boardView.getBoardFace().getHply() - 1].contains(moveTo)) {
			boardView.getBoardFace().increaseTacticsCorrectMoves();

			if (boardView.getBoardFace().getMovesCount() < boardView.getBoardFace().getTacticMoves().length - 1) {
				int[] moveFT = MoveParser.parse(boardView.getBoardFace(),
						boardView.getBoardFace().getTacticMoves()[boardView.getBoardFace().getHply()]);
				if (moveFT.length == 4) {
					if (moveFT[3] == 2)
						move = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoardFace().makeMove(move);
				} else {
					move = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoardFace().makeMove(move);
				}
				update(CALLBACK_REPAINT_UI);
				boardView.invalidate();
			} else {
				if (mainApp.guest || boardView.getBoardFace().isRetry() || mainApp.noInternet) {
					new AlertDialog.Builder(this)
							.setTitle(R.string.correct_ex)
							.setItems(getResources().getTextArray(R.array.correcttactic),
									correctDialogListener)
							.create().show();
					stopTacticsTimer();
				} else {
					if (appService != null) {
						appService.RunSingleTask(CALLBACK_TACTICS_CORRECT,
								"http://www." + LccHolder.HOST + AppConstants.API_TACTICS_TRAINER_ID_PARAMETER +
										mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
										+ AppConstants.TACTICS_ID_PARAMETER + mainApp.getTactic().values.get(AppConstants.ID)
										+ AppConstants.PASSED_PARAMETER + 1 + AppConstants.CORRECT_MOVES_PARAMETER + boardView.getBoardFace().getTacticsCorrectMoves()
										+ AppConstants.SECONDS_PARAMETER + boardView.getBoardFace().getSec(),
								progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
					}
					stopTacticsTimer();
				}
			}
		} else {
			if (mainApp.guest || boardView.getBoardFace().isRetry() || mainApp.noInternet) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.wrong_ex)
						.setItems(getResources().getTextArray(R.array.wrongtactic), wrongDialogListener)
						.create().show();
				stopTacticsTimer();
			} else {
				if (appService != null) {
					appService.RunSingleTask(CALLBACK_TACTICS_WRONG,
							"http://www." + LccHolder.HOST + AppConstants.API_TACTICS_TRAINER_ID_PARAMETER
									+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
									+ AppConstants.TACTICS_ID_PARAMETER + mainApp.getTactic().values.get(AppConstants.ID)
									+ AppConstants.PASSED_PARAMETER + 0 + AppConstants.CORRECT_MOVES_PARAMETER + boardView.getBoardFace().getTacticsCorrectMoves() + AppConstants.SECONDS_PARAMETER + boardView.getBoardFace().getSec(),
							progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
				}
				stopTacticsTimer();
			}
		}
	}


	@Override
	public void switch2Analysis(boolean isAnalysis) {
		if (isAnalysis) {
			timer.setVisibility(View.INVISIBLE);
			analysisTxt.setVisibility(View.VISIBLE);
		} else {
			timer.setVisibility(View.VISIBLE);
			analysisTxt.setVisibility(View.INVISIBLE);
			restoreGame();
		}
	}

    @Override
    public void updateAfterMove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invalidateGameScreen() {
        //TODO To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	protected void restoreGame() {
		restoreLastConfig();
	}

	@Override
	public void update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(boardView.getBoardFace()))
					finish();
				else if (MainApp.isTacticsGameMode(boardView.getBoardFace())) {
					/*onBackPressed();
					boardView.getBoardFaceFace().getTactic()Canceled = true;*/
					if (mainApp.noInternet) {
						if (mainApp.offline) {
							getGuestTacticsGame();
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
				if (boardView.getBoardFace().isInit() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())
						|| MainApp.isFinishedEchessGameMode(boardView.getBoardFace())) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					getOnlineGame(mainApp.getGameId());
					boardView.getBoardFace().setInit(false);
				} else if (!boardView.getBoardFace().isInit()) {
					if (MainApp.isLiveOrEchessGameMode(boardView.getBoardFace()) && appService != null
							&& appService.getRepeatableTimer() == null) {
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
						if (!mainApp.isLiveChess()) {
							appService.RunRepeatableTask(CALLBACK_GAME_REFRESH, UPDATE_DELAY, UPDATE_DELAY,
									"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + "&gid=" + mainApp.getGameId(),
									null/*progressDialog*/
							);
						}
					}
				}
				break;
			case CALLBACK_REPAINT_UI: {

				boardView.addMove2Log(boardView.getBoardFace().getMoveListSAN());
				boardView.invalidate();
                boardView.requestFocus();
				break;
			}
			case CALLBACK_CHECK_TACTICS_MOVE: {
				checkTacticMoves();
				break;
			}
			case CALLBACK_TACTICS_WRONG: {
				String[] tmp = response.split("[|]");
				if (tmp.length < 2 || tmp[1].trim().equals(AppConstants.SYMBOL_EMPTY)) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				TacticResultItem result = new TacticResultItem(tmp[1].split(":"));

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
				if (tmp.length < 2 || tmp[1].trim().equals(AppConstants.SYMBOL_EMPTY)) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				TacticResultItem result = new TacticResultItem(tmp[1].split(":"));

				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.correct_score,
								result.values.get(AppConstants.USER_RATING_CHANGE),
								result.values.get(AppConstants.USER_RATING)))
						.setItems(getResources().getTextArray(R.array.correcttactic), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (which == 1) {
									getTacticsGame(AppConstants.SYMBOL_EMPTY);
								}
							}
						})
						.create().show();
				break;
			}
			case CALLBACK_GET_TACTICS:

				boardView.setBoardFace(new ChessBoard(this));
				boardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

				String[] tmp = response.trim().split("[|]");
				if (tmp.length < 3 || tmp[2].trim().equals(AppConstants.SYMBOL_EMPTY)) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}

				mainApp.setTactic(new TacticItem(tmp[2].split(":")));

				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
					boardView.getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, boardView.getBoardFace());
					String[] tmp2 = FEN.split(AppConstants.SYMBOL_SPACE);
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
							boardView.getBoardFace().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					boardView.getBoardFace().setTacticMoves(mainApp.getTactic().
							values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY).replaceAll("[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(1).split(AppConstants.SYMBOL_SPACE));
					boardView.getBoardFace().setMovesCount(1);
				}
				boardView.getBoardFace().setSec(0);
				boardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic().values.get(AppConstants.AVG_SECONDS)));
				startTacticsTimer();
				int[] moveFT = MoveParser.parse(boardView.getBoardFace(), boardView.getBoardFace().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move m;
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoardFace().makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoardFace().makeMove(m);
				}
				update(CALLBACK_REPAINT_UI);
				boardView.getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();
				break;
			case CALLBACK_GAME_REFRESH:
				if (boardView.getBoardFace().isAnalysis())
					return;
				if (!mainApp.isLiveChess()) {
					game = ChessComApiParser.GetGameParseV3(responseRepeatable);
				}

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				if (!mainApp.getCurrentGame().equals(game)) {
					if (!mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).equals(game.values.get(AppConstants.MOVE_LIST))) {
						mainApp.setCurrentGame(game);
						String[] Moves;

						if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())))) {

							int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())) ? 0 : 1;

							Moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(beginIndex).split(AppConstants.SYMBOL_SPACE);

							if (Moves.length - boardView.getBoardFace().getMovesCount() == 1) {
								if (mainApp.isLiveChess()) {
									moveFT = MoveParser.parseCoordinate(boardView.getBoardFace(), Moves[Moves.length - 1]);
								} else {
									moveFT = MoveParser.parse(boardView.getBoardFace(), Moves[Moves.length - 1]);
								}
								boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGameId()).getSeq() == Moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move m;
									if (moveFT[3] == 2)
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									boardView.getBoardFace().makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									boardView.getBoardFace().makeMove(m, playSound);
								}
								//mainApp.showToast("Move list updated!");
								boardView.getBoardFace().setMovesCount(Moves.length);
								boardView.invalidate();
								update(CALLBACK_REPAINT_UI);
							}
						}
						return;
					}
					
				}
				break;

			case CALLBACK_GAME_STARTED:
				getSoundPlayer().playGameStart();

				mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));

				if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
					boardView.getBoardFace().setChess960(true);

				if (!isUserColorWhite()) {
					boardView.getBoardFace().setReside(true);
				}
				
				String[] moves = {};

				if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE).substring(1).split(AppConstants.SYMBOL_SPACE);
					boardView.getBoardFace().setMovesCount(moves.length);
				} else if (!mainApp.isLiveChess()) {
					boardView.getBoardFace().setMovesCount(0);
				}

				Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				FEN = mainApp.getCurrentGame().values.get(GameItem.STARTING_FEN_POSITION);
				if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
					boardView.getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, boardView.getBoardFace());
				}

				int i;

				if (!mainApp.isLiveChess()) {
					for (i = 0; i < boardView.getBoardFace().getMovesCount(); i++) {
						moveFT = MoveParser.parse(boardView.getBoardFace(), moves[i]);
						if (moveFT.length == 4) {
							Move m;
							if (moveFT[3] == 2) {
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							} else {
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							}
							boardView.getBoardFace().makeMove(m, false);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							boardView.getBoardFace().makeMove(m, false);
						}
					}
				}

				update(CALLBACK_REPAINT_UI);
				boardView.getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();
				break;
			default:
				break;
		}
	}

	@Override
	public void newGame() {
		if (mainApp.guest) {
			mainApp.currentTacticProblem++;
			getGuestTacticsGame();
		} else {
			if (mainApp.noInternet) mainApp.currentTacticProblem++;
			closeOptionsMenu();
			getTacticsGame(AppConstants.SYMBOL_EMPTY);
		}
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
				newGame();
				break;
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_reside:
				boardView.flipBoard();
				break;
			case R.id.menu_analysis:
				boardView.switchAnalysis();
				break;
			case R.id.menu_previous:
				boardView.moveBack();
				isMoveNav = true;
				break;
			case R.id.menu_next:
				boardView.moveForward();
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
						getGuestTacticsGame();
					} else
						getTacticsGame(AppConstants.SYMBOL_EMPTY);
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
	protected void onGameEndMsgReceived() {
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
				if (boardView.getBoardFace().isAnalysis())
					return;
				boardView.getBoardFace().increaseSec();
				if (boardView.getBoardFace().getLeft() > 0)
					boardView.getBoardFace().decreaseLeft();
				update.sendEmptyMessage(0);
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					timer.setText(getString(R.string.bonus_time_left, boardView.getBoardFace().getLeft()
							, boardView.getBoardFace().getSec()));
				}
			};
		}, 0, 1000);
	}

	@Override
	protected void restoreLastConfig(){
		if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
			openOptionsMenu();
			return;
		}

		int secondsSpent = boardView.getBoardFace().getSec();

		if (mainApp.guest || mainApp.noInternet) {
			// set new board
			boardView.setBoardFace(new ChessBoard(this));
			boardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS); // set game mode

			String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
			if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
				boardView.getBoardFace().genCastlePos(FEN); // restore castle position for current tactics problem
				MoveParser.fenParse(FEN, boardView.getBoardFace());

				String[] tmp = FEN.split(AppConstants.SYMBOL_SPACE);
				if (tmp.length > 1) {
					if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
						boardView.getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				boardView.getBoardFace().setTacticMoves(mainApp.getTacticsBatch()
						.get(mainApp.currentTacticProblem).values
						.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY)
						.replaceAll("[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE)
						.substring(1).split(AppConstants.SYMBOL_SPACE));

				boardView.getBoardFace().setMovesCount(1);
			}

			boardView.getBoardFace().setSec(secondsSpent);
			boardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTacticsBatch()
					.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)) - secondsSpent);

			startTacticsTimer();

			int[] moveFT = MoveParser.parse(boardView.getBoardFace(), boardView.getBoardFace().getTacticMoves()[0]);
			if (moveFT.length == 4) {
				Move move;
				if (moveFT[3] == 2)
					move = new Move(moveFT[0], moveFT[1], 0, 2);
				else
					move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

				boardView.getBoardFace().makeMove(move);
			} else {
				Move move = new Move(moveFT[0], moveFT[1], 0, 0);
				boardView.getBoardFace().makeMove(move);
			}

			update(CALLBACK_REPAINT_UI);
			boardView.getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		} else {
			if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
				openOptionsMenu();
				return;
			}
			boardView.setBoardFace(new ChessBoard(this));
			boardView.getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

			String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
			if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
				boardView.getBoardFace().genCastlePos(FEN);
				MoveParser.fenParse(FEN, boardView.getBoardFace());
				String[] tmp2 = FEN.split(AppConstants.SYMBOL_SPACE);
				if (tmp2.length > 1) {
					if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
						boardView.getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				boardView.getBoardFace().setTacticMoves(mainApp.getTactic()
						.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY)
						.replaceAll("[.]", AppConstants.SYMBOL_EMPTY).replaceAll("  ", AppConstants.SYMBOL_SPACE)
						.substring(1).split(AppConstants.SYMBOL_SPACE));
				boardView.getBoardFace().setMovesCount(1);
			}

			boardView.getBoardFace().setSec(secondsSpent);
			boardView.getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic()
					.values.get(AppConstants.AVG_SECONDS)) - secondsSpent);

			int[] moveFT = MoveParser.parse(boardView.getBoardFace(), boardView.getBoardFace().getTacticMoves()[0]);

			if (moveFT.length == 4) {
				Move move;
				if (moveFT[3] == 2)
					move = new Move(moveFT[0], moveFT[1], 0, 2);
				else
					move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

				boardView.getBoardFace().makeMove(move);
			} else {
				Move move = new Move(moveFT[0], moveFT[1], 0, 0);
				boardView.getBoardFace().makeMove(move);
			}

			update(CALLBACK_REPAINT_UI);
			boardView.getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		}
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			boardView.getBoardFace().setTacticCanceled(true);
		}
		return super.onKeyDown(keyCode, event);
	}


}
