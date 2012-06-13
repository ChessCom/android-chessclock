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
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
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
			mainApp.setPendingTacticsLoad(false);
			showDialog(DIALOG_TACTICS_START_TACTICS);
		}

		gamePanelView.hideChatButton();
	}

	@Override
	protected void onResume() {
		super.onResume();

        if (getBoardFace().isTacticCanceled()) {
            getBoardFace().setTacticCanceled(false);
            showDialog(DIALOG_TACTICS_START_TACTICS);    // TODO show register confirmation dialog
        } else if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("0")
				&& getBoardFace().getMovesCount() > 0) {
			update(CALLBACK_REPAINT_UI);
			getBoardFace().takeBack();
			boardView.invalidate();
			startTacticsTimer();
			playLastMoveAnimation();
		}
    }

	@Override
	protected void onPause() {
		super.onPause();

		stopTacticsTimer();
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return getBoardFace();
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
					getTacticsGame(StaticData.SYMBOL_EMPTY);

			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				getBoardFace().setTacticCanceled(true);
				mainApp.setPendingTacticsLoad(false);
				onBackPressed();
			}
		}
	}

	private class MaxTacticsDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (whichButton == DialogInterface.BUTTON_POSITIVE) {
				FlurryAgent.onEvent("Upgrade From Tactics", null);
				startActivity(AppData.getMembershipIntent(StaticData.SYMBOL_EMPTY, getContext()));
			} else if (whichButton == DialogInterface.BUTTON_NEGATIVE) {
				getBoardFace().setTacticCanceled(true);
				mainApp.setPendingTacticsLoad(false);
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
				getBoardFace().setTacticCanceled(true);
				mainApp.setPendingTacticsLoad(false);
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
					getTacticsGame(StaticData.SYMBOL_EMPTY);
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
					getTacticsGame(StaticData.SYMBOL_EMPTY);
				}
			}
			if (which == 1) {  // Retry
				if (mainApp.guest || mainApp.noInternet) {
					getBoardFace().setRetry(true);
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
				getTacticsGame(StaticData.SYMBOL_EMPTY);
			}
			if (which == 1) {
				getBoardFace().setRetry(true);
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
	}

	private void getTacticsGame(final String id) {
		FlurryAgent.onEvent("Tactics Session Started For Registered", null);
		if (!mainApp.noInternet) {
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

			if (mainApp.getTactic() != null && id.equals(mainApp.getTactic().values.get(AppConstants.ID))) {
				getBoardFace().setRetry(true);

				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
					getBoardFace().genCastlePos(FEN);

					MoveParser.fenParse(FEN, getBoardFace());
					String[] tmp2 = FEN.split(StaticData.SYMBOL_SPACE);
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
							getBoardFace().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					getBoardFace().setTacticMoves(mainApp.getTactic()
							.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
							.replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
							.substring(1).split(StaticData.SYMBOL_SPACE));
					getBoardFace().setMovesCount(1);
				}

				getBoardFace().setSec(0);
				getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic().
						values.get(AppConstants.AVG_SECONDS)));

				startTacticsTimer();

				int[] moveFT = MoveParser.parse(getBoardFace(), getBoardFace().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move move;
					if (moveFT[3] == 2)
						move = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					getBoardFace().makeMove(move);
				} else {
					Move move = new Move(moveFT[0], moveFT[1], 0, 0);
					getBoardFace().makeMove(move);
				}
				update(CALLBACK_REPAINT_UI);
				getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();
				return;
			}
		}
		if (appService != null) {
			mainApp.setPendingTacticsLoad(true);
			appService.RunSingleTask(CALLBACK_GET_TACTICS,
					"http://www." + LccHolder.HOST + AppConstants.API_TACTICS_TRAINER_ID_PARAMETER
							+ preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY) + AppConstants.TACTICS_ID_PARAMETER + id,
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
		getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

		String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			getBoardFace().genCastlePos(FEN);
			MoveParser.fenParse(FEN, getBoardFace());
			String[] tmp = FEN.split(StaticData.SYMBOL_SPACE);
			if (tmp.length > 1) {
				if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
					getBoardFace().setReside(true);
				}
			}
		}
		if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
			getBoardFace().setTacticMoves(mainApp.getTacticsBatch()
					.get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
			getBoardFace().setMovesCount(1);
		}
		getBoardFace().setSec(0);
		getBoardFace().setLeft(Integer.parseInt(mainApp.getTacticsBatch()
				.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)));
		startTacticsTimer();
		int[] moveFT = MoveParser.parse(getBoardFace(), getBoardFace().getTacticMoves()[0]);
		if (moveFT.length == 4) {
			Move move;
			if (moveFT[3] == 2)
				move = new Move(moveFT[0], moveFT[1], 0, 2);
			else
				move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			getBoardFace().makeMove(move);
		} else {
			Move move = new Move(moveFT[0], moveFT[1], 0, 0);
			getBoardFace().makeMove(move);
		}
		update(CALLBACK_REPAINT_UI);
		getBoardFace().takeBack();
		boardView.invalidate();

		mainApp.setTactic(mainApp.getTacticsBatch().get(mainApp.currentTacticProblem));
		playLastMoveAnimation();
	}

	private void showAnswer() {
		boardView.setBoardFace(new ChessBoard(this));
		getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);
		getBoardFace().setRetry(true);

		if (mainApp.guest || mainApp.noInternet) {
			String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
			if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
				getBoardFace().genCastlePos(FEN);
				MoveParser.fenParse(FEN, getBoardFace());
				String[] tmp = FEN.split(StaticData.SYMBOL_SPACE);
				if (tmp.length > 1) {
					if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
						getBoardFace().setReside(true);
					}
				}
			}
			if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}
		} else {
			String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
			if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
				getBoardFace().genCastlePos(FEN);
				MoveParser.fenParse(FEN, getBoardFace());
				String[] tmp2 = FEN.split(StaticData.SYMBOL_SPACE);
				if (tmp2.length > 1) {
					if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
						getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(mainApp.getTactic().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}
		}
		boardView.invalidate();


		new Thread(new Runnable() {
			@Override
			public void run() {
				int i;
				for (i = 0; i < getBoardFace().getTacticMoves().length; i++) {
					int[] moveFT = MoveParser.parse(getBoardFace(), getBoardFace().getTacticMoves()[i]);
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

						getBoardFace().makeMove(move);
					} else {
						Move move = new Move(moveFT[0], moveFT[1], 0, 0);
						getBoardFace().makeMove(move);
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
		Move move = getBoardFace().getHistDat()[getBoardFace().getHply() - 1].move;
		String piece = StaticData.SYMBOL_EMPTY;
		int p = getBoardFace().getPieces()[move.to];
		if (p == 1) {
			piece = MoveParser.WHITE_KNIGHT;
		} else if (p == 2) {
			piece = MoveParser.WHITE_BISHOP;
		} else if (p == 3) {
			piece = MoveParser.WHITE_ROOK;
		} else if (p == 4) {
			piece = MoveParser.WHITE_QUEEN;
		} else if (p == 5) {
			piece = MoveParser.WHITE_KING;
		}
		String moveTo = MoveParser.positionToString(move.to);
		Log.d("!!!", piece + " | " + moveTo + " : " + getBoardFace().getTacticMoves()[getBoardFace().getHply() - 1]);
		

		if (getBoardFace().lastMoveContains(piece, moveTo)) {
			getBoardFace().increaseTacticsCorrectMoves();

			if (getBoardFace().getMovesCount() < getBoardFace().getTacticMoves().length - 1) {
				int[] moveFT = MoveParser.parse(getBoardFace(),
						getBoardFace().getTacticMoves()[getBoardFace().getHply()]);
				if (moveFT.length == 4) {
					if (moveFT[3] == 2)
						move = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					getBoardFace().makeMove(move);
				} else {
					move = new Move(moveFT[0], moveFT[1], 0, 0);
					getBoardFace().makeMove(move);
				}
				
				update(CALLBACK_REPAINT_UI);
				boardView.invalidate();
			} else {
				if (mainApp.guest || getBoardFace().isRetry() || mainApp.noInternet) {
					new AlertDialog.Builder(this)
							.setTitle(R.string.correct_ex)
							.setItems(getResources().getTextArray(R.array.correcttactic),
									correctDialogListener)
							.create().show();
					stopTacticsTimer();
				} else {
					if (appService != null) {
						String url = "http://www." + LccHolder.HOST + AppConstants.API_TACTICS_TRAINER_ID_PARAMETER +
								preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY)
								+ AppConstants.TACTICS_ID_PARAMETER + mainApp.getTactic().values.get(AppConstants.ID)
								+ AppConstants.PASSED_PARAMETER + 1 + AppConstants.CORRECT_MOVES_PARAMETER + getBoardFace().getTacticsCorrectMoves()
								+ AppConstants.SECONDS_PARAMETER + getBoardFace().getSec();
						Log.d("TEST", " url for correct tactics = " + url);
						appService.RunSingleTask(CALLBACK_TACTICS_CORRECT, url,
								progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
					}
					stopTacticsTimer();
				}
			}
		} else {
			if (mainApp.guest || getBoardFace().isRetry() || mainApp.noInternet) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.wrong_ex)
						.setItems(getResources().getTextArray(R.array.wrongtactic), wrongDialogListener)
						.create().show();
				stopTacticsTimer();
			} else {
				if (appService != null) {
					String url = "http://www." + LccHolder.HOST + AppConstants.API_TACTICS_TRAINER_ID_PARAMETER
							+ preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY)
							+ AppConstants.TACTICS_ID_PARAMETER + mainApp.getTactic().values.get(AppConstants.ID)
							+ AppConstants.PASSED_PARAMETER + 0 + AppConstants.CORRECT_MOVES_PARAMETER
							+ getBoardFace().getTacticsCorrectMoves() + AppConstants.SECONDS_PARAMETER + getBoardFace().getSec();
					Log.d("TEST", " url for wrong tactics = " + url);
					appService.RunSingleTask(CALLBACK_TACTICS_WRONG,url,
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
			case INIT_ACTIVITY: {
				if (!mainApp.guest && mainApp.isPendingTacticsLoad()) {
					getTacticsGame(StaticData.SYMBOL_EMPTY);
				}
			}
			case ERROR_SERVER_RESPONSE:
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
				break;
			case CALLBACK_REPAINT_UI: {

				boardView.addMove2Log(getBoardFace().getMoveListSAN());
				break;
			}
			case CALLBACK_CHECK_TACTICS_MOVE: {
				checkTacticMoves();
				break;
			}
			case CALLBACK_TACTICS_WRONG: {
				Log.d("TEST", "response for wrong tactics = "+ response);
				String[] tmp = response.split("[|]");
				if (response.trim().equals("Success+||")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}
				else if (tmp.length < 2 || tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)){
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
				Log.d("TEST", "response for correct tactics = "+ response);

				String[] tmp = response.split("[|]");
				if (response.trim().equals("Success+||")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}
				else if (tmp.length < 2 || tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)){
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
									getTacticsGame(StaticData.SYMBOL_EMPTY);
								}
							}
						})
						.create().show();
				break;
			}
			case CALLBACK_GET_TACTICS:

				boardView.setBoardFace(new ChessBoard(this));
				getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

				String[] tmp = response.trim().split("[|]");
				if (response.trim().equals("Success+||")) {
					showDialog(DIALOG_TACTICS_LIMIT);
					return;
				}
                else if (tmp.length < 3 || tmp[2].trim().equals(StaticData.SYMBOL_EMPTY)){
					return;
				}

				mainApp.setTactic(new TacticItem(tmp[2].split(":")));

				String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
				if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
					getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, getBoardFace());
					String[] tmp2 = FEN.split(StaticData.SYMBOL_SPACE);
					if (tmp2.length > 1) {
						if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
							getBoardFace().setReside(true);
						}
					}
				}

				if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					getBoardFace().setTacticMoves(mainApp.getTactic().
							values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
					getBoardFace().setMovesCount(1);
				}
				getBoardFace().setSec(0);
				getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic().values.get(AppConstants.AVG_SECONDS)));
				startTacticsTimer();
				int[] moveFT = MoveParser.parse(getBoardFace(), getBoardFace().getTacticMoves()[0]);
				if (moveFT.length == 4) {
					Move m;
					if (moveFT[3] == 2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					getBoardFace().makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					getBoardFace().makeMove(m);
				}
				update(CALLBACK_REPAINT_UI);
				getBoardFace().takeBack();
				boardView.invalidate();

				mainApp.setPendingTacticsLoad(false);

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
			getTacticsGame(StaticData.SYMBOL_EMPTY);
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
						getTacticsGame(StaticData.SYMBOL_EMPTY);
					break;
				}
				case TACTICS_SHOW_ANSWER: {
					showAnswer();
					break;
				}
				case TACTICS_SETTINGS: {
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));

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
				if (getBoardFace().isAnalysis())
					return;
				getBoardFace().increaseSec();
				if (getBoardFace().getLeft() > 0)
					getBoardFace().decreaseLeft();
				update.sendEmptyMessage(0);
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					timer.setText(getString(R.string.bonus_time_left, getBoardFace().getLeft()
							, getBoardFace().getSec()));
				}
			};
		}, 0, 1000);
	}

	@Override
	protected void restoreLastConfig() {
		if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
			openOptionsMenu();
			return;
		}

		int secondsSpent = getBoardFace().getSec();

		if (mainApp.guest || mainApp.noInternet) {
			// set new board
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS); // set game mode

			String FEN = mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.FEN);
			if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
				getBoardFace().genCastlePos(FEN); // restore castle position for current tactics problem
				MoveParser.fenParse(FEN, getBoardFace());

				String[] tmp = FEN.split(StaticData.SYMBOL_SPACE);
				if (tmp.length > 1) {
					if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
						getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTacticsBatch().get(mainApp.currentTacticProblem).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(mainApp.getTacticsBatch()
						.get(mainApp.currentTacticProblem).values
						.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
						.replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
						.substring(1).split(StaticData.SYMBOL_SPACE));

				getBoardFace().setMovesCount(1);
			}

			getBoardFace().setSec(secondsSpent);
			getBoardFace().setLeft(Integer.parseInt(mainApp.getTacticsBatch()
					.get(mainApp.currentTacticProblem).values.get(AppConstants.AVG_SECONDS)) - secondsSpent);

			startTacticsTimer();

			int[] moveFT = MoveParser.parse(getBoardFace(), getBoardFace().getTacticMoves()[0]);
			if (moveFT.length == 4) {
				Move move;
				if (moveFT[3] == 2)
					move = new Move(moveFT[0], moveFT[1], 0, 2);
				else
					move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

				getBoardFace().makeMove(move);
			} else {
				Move move = new Move(moveFT[0], moveFT[1], 0, 0);
				getBoardFace().makeMove(move);
			}

			update(CALLBACK_REPAINT_UI);
			getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		} else {
			if (mainApp.getTactic() != null && mainApp.getTactic().values.get(AppConstants.STOP).equals("1")) {
				openOptionsMenu();
				return;
			}
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setMode(AppConstants.GAME_MODE_TACTICS);

			String FEN = mainApp.getTactic().values.get(AppConstants.FEN);
			if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
				getBoardFace().genCastlePos(FEN);
				MoveParser.fenParse(FEN, getBoardFace());
				String[] tmp2 = FEN.split(StaticData.SYMBOL_SPACE);
				if (tmp2.length > 1) {
					if (tmp2[1].trim().equals(MoveParser.W_SMALL)) {
						getBoardFace().setReside(true);
					}
				}
			}

			if (mainApp.getTactic().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(mainApp.getTactic()
						.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
						.replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
						.substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}

			getBoardFace().setSec(secondsSpent);
			getBoardFace().setLeft(Integer.parseInt(mainApp.getTactic()
					.values.get(AppConstants.AVG_SECONDS)) - secondsSpent);

			int[] moveFT = MoveParser.parse(getBoardFace(), getBoardFace().getTacticMoves()[0]);

			if (moveFT.length == 4) {
				Move move;
				if (moveFT[3] == 2)
					move = new Move(moveFT[0], moveFT[1], 0, 2);
				else
					move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

				getBoardFace().makeMove(move);
			} else {
				Move move = new Move(moveFT[0], moveFT[1], 0, 0);
				getBoardFace().makeMove(move);
			}

			update(CALLBACK_REPAINT_UI);
			getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getBoardFace().setTacticCanceled(true);
			mainApp.setPendingTacticsLoad(false);
		}
		return super.onKeyDown(keyCode, event);
	}


}
