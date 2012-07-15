package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameItem;
import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.interfaces.GameTacticsActivityFace;
import com.chess.ui.views.ChessBoardTacticsView;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameTacticsScreenActivity extends GameBaseActivity implements GameTacticsActivityFace {

	private static final int TACTICS_NEXT = 0;
	private static final int TACTICS_RETRY = 1;
	private static final int TACTICS_STOP = 2;

	private TextView timer;
	private Timer tacticsTimer = null;
	private ChessBoardTacticsView boardView;

	private boolean noInternet;

	private GetTacticsUpdateListener getTacticsUpdateListener;
	private TacticsCorrectUpdateListener tacticsCorrectUpdateListener;
	private TacticsWrongUpdateListener tacticsWrongUpdateListener;

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private static final String FIRST_TACTICS_TAG = "first tactics";
	private static final String TACTICS_LIMIT_TAG = "daily tactics limit";
	private static final String HUNDRED_TACTICS_TAG = "hundred tactics reached";
	private static final String OFFLINE_RATING_TAG = "tactics offline rating";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_tactics);

		init();
		widgetsInit();
	}

	public void init() {
		menuOptionsItems = new CharSequence[]{
				getString(R.string.skipproblem),
				getString(R.string.showanswer),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		getTacticsUpdateListener = new GetTacticsUpdateListener();
		tacticsCorrectUpdateListener = new TacticsCorrectUpdateListener();
		tacticsWrongUpdateListener = new TacticsWrongUpdateListener();

	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		boardView = (ChessBoardTacticsView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		boardView.setBoardFace(new ChessBoard(this));
		boardView.setGameActivityFace(this);

		setBoardView(boardView);

		ChessBoard chessBoard = (ChessBoard) getLastCustomNonConfigurationInstance();
		if (chessBoard != null) {
			boardView.setBoardFace(chessBoard);
		} else {
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setInit(true);
			getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}

		timer = (TextView) findViewById(R.id.timer);
		timer.setVisibility(View.VISIBLE);

		whitePlayerLabel.setVisibility(View.GONE);
		blackPlayerLabel.setVisibility(View.GONE);


		if (getLastCustomNonConfigurationInstance() == null) {
			DataHolder.getInstance().setPendingTacticsLoad(false);
			showPopupDialog(R.string.ready_for_first_tackics_q, FIRST_TACTICS_TAG);
			popupItem.setPositiveBtnId(R.string.yes);
			popupItem.setNegativeBtnId(R.string.no);
		}

		gamePanelView.hideChatButton();
	}

	@Override
	protected void onResume() {
		super.onResume();

        // TODO show register confirmation dialog
		if (getBoardFace().isTacticCanceled()) {
			getBoardFace().setTacticCanceled(false);
			popupItem.setPositiveBtnId(R.string.yes);
			popupItem.setNegativeBtnId(R.string.no);
			showPopupDialog(R.string.ready_for_first_tackics_q, FIRST_TACTICS_TAG);

		} else if (getTacticItem() != null && getTacticItem().values.get(AppConstants.STOP).equals("0")
            && getBoardFace().getMovesCount() > 0) {
            invalidateGameScreen();
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

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {
		return null;
	}

	@Override
	public void onGameRefresh(GameItem newGame) {
		// TODO change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void checkMove() {

		noInternet = !AppUtils.isNetworkAvailable(getContext());

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
				invalidateGameScreen();
				boardView.invalidate();
			} else {
				if (DataHolder.getInstance().isGuest() || getBoardFace().isRetry() || noInternet) {
					new AlertDialog.Builder(this)
							.setTitle(R.string.correct_ex)
							.setItems(getResources().getTextArray(R.array.correcttactic),
									correctDialogListener)
							.create().show();
					stopTacticsTimer();
				} else {
					LoadItem loadItem = new LoadItem();
					loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
					loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
					loadItem.addRequestParams(RestHelper.P_TACTICS_ID, getTacticItem().values.get(AppConstants.ID));
					loadItem.addRequestParams(RestHelper.P_PASSED, "1");
					loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, String.valueOf(getBoardFace().getTacticsCorrectMoves()));
					loadItem.addRequestParams(RestHelper.P_SECONDS, String.valueOf(getBoardFace().getSec()));

					new GetStringObjTask(tacticsCorrectUpdateListener).executeTask(loadItem);
					stopTacticsTimer();
				}
			}
		} else {
			if (DataHolder.getInstance().isGuest() || getBoardFace().isRetry() || noInternet) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.wrong_ex)
						.setItems(getResources().getTextArray(R.array.wrongtactic), wrongDialogListener)
						.create().show();
				stopTacticsTimer();
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_TACTICS_ID, getTacticItem().values.get(AppConstants.ID));
				loadItem.addRequestParams(RestHelper.P_PASSED, "0");
				loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, String.valueOf(getBoardFace().getTacticsCorrectMoves()));
				loadItem.addRequestParams(RestHelper.P_SECONDS, String.valueOf(getBoardFace().getSec()));

				new GetStringObjTask(tacticsWrongUpdateListener).executeTask(loadItem);
				stopTacticsTimer();
			}
		}
	}


	private void getTacticsGame(final String id) {
		FlurryAgent.onEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED, null);

		noInternet = !AppUtils.isNetworkAvailable(getContext());

		if (!noInternet) {
			boardView.setBoardFace(new ChessBoard(this));

			if (getTacticItem() != null
					&& id.equals(getTacticItem().values.get(AppConstants.ID))) {
				getBoardFace().setRetry(true);
				String FEN = getTacticItem().values.get(AppConstants.FEN);
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

				if (getTacticItem().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					getBoardFace().setTacticMoves(getTacticItem()
							.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
                            .replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
                            .substring(1).split(StaticData.SYMBOL_SPACE));
					getBoardFace().setMovesCount(1);
				}

				getBoardFace().setSec(0);
				getBoardFace().setLeft(Integer.parseInt(getTacticItem().values.get(AppConstants.AVG_SECONDS)));

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
				invalidateGameScreen();
				getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();
				return;
			}
		}


		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_TACTICS_ID, id);

		new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);

		DataHolder.getInstance().setPendingTacticsLoad(true);
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;// TODO change body of implemented methods use File | Settings | File Templates.
	}


	private class GetTacticsUpdateListener extends ChessUpdateListener {
		public GetTacticsUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			boardView.setBoardFace(new ChessBoard(GameTacticsScreenActivity.this));

			String[] tmp = returnedObj.trim().split("[|]");
			if (tmp.length < 3 || tmp[2].trim().equals(StaticData.SYMBOL_EMPTY)) {
				showLimitDialog();
				return;
			}

			DataHolder.getInstance().setTactic(new TacticItem(tmp[2].split(":")));

			String FEN = getTacticItem().values.get(AppConstants.FEN);
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

			if (getTacticItem().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(getTacticItem().
						values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}
			getBoardFace().setSec(0);
			getBoardFace().setLeft(Integer.parseInt(getTacticItem().values.get(AppConstants.AVG_SECONDS)));
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
			invalidateGameScreen();
			getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (noInternet) {
				if (DataHolder.getInstance().isOffline()) {
					getGuestTacticsGame();
				} else {
					DataHolder.getInstance().setOffline(true);
					showOfflineRatingDialog();
				}
			}
		}
	}

	private void showLimitDialog() {
		FlurryAgent.onEvent(FlurryData.TACTICS_DAILY_LIMIT_EXCEDED);
		showPopupDialog(R.string.daily_limit_exceeded, R.string.max_tactics_reached, TACTICS_LIMIT_TAG);
	}

	private void getGuestTacticsGame() {
		FlurryAgent.onEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_GUEST);

		if (getCurrentProblem() >= getTacticsBatch().size()) {
			showPopupDialog(R.string.hundred_tackics_completed, HUNDRED_TACTICS_TAG);
			popupDialogFragment.setButtons(1);
			return;
		}

		boardView.setBoardFace(new ChessBoard(this));

		String FEN = getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.FEN);
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
		if (getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.MOVE_LIST).contains("1.")) {
			getBoardFace().setTacticMoves(getTacticsBatch()
					.get(getCurrentProblem()).values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
			getBoardFace().setMovesCount(1);
		}

		getBoardFace().setSec(0);
		getBoardFace().setLeft(Integer.parseInt(getTacticsBatch()
				.get(getCurrentProblem()).values.get(AppConstants.AVG_SECONDS)));

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
		invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();

		DataHolder.getInstance().setTactic(getTacticsBatch().get(getCurrentProblem()));
		playLastMoveAnimation();
	}

	private void showAnswer() {
		boardView.setBoardFace(new ChessBoard(this));
		getBoardFace().setRetry(true);

		if (DataHolder.getInstance().isGuest() || noInternet) {
			String FEN = getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.FEN);
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
			if (getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}
		} else {
			String FEN = getTacticItem().values.get(AppConstants.FEN);
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

			if (getTacticItem().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(getTacticItem().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}
		}
		boardView.invalidate();


		new Thread(new Runnable() { // TODO replace with AsyncTask
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
					invalidateGameScreen();
					boardView.invalidate();
				}
			};
		}).start();
	}

	private class TacticsCorrectUpdateListener extends ChessUpdateListener {
		public TacticsCorrectUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			String[] tmp = returnedObj.split("[|]");
			if (tmp.length < 2 || tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)) {
				showLimitDialog();
				return;
			}

			TacticResultItem result = new TacticResultItem(tmp[1].split(":"));

			new AlertDialog.Builder(getContext())
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
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (noInternet) {
				if (DataHolder.getInstance().isOffline()) {
					getGuestTacticsGame();
				} else {
					DataHolder.getInstance().setOffline(true);
					showOfflineRatingDialog();
				}
			}
		}

	}

	private void showOfflineRatingDialog() {
		showPopupDialog(R.string.offline_mode, R.string.no_network_rating_not_changed, OFFLINE_RATING_TAG);
	}

	private class TacticsWrongUpdateListener extends ChessUpdateListener {
		public TacticsWrongUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			String[] tmp = returnedObj.split("[|]");
			if (tmp.length < 2 || tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)) {
				showLimitDialog();
				return;
			}

			TacticResultItem result = new TacticResultItem(tmp[1].split(":"));

			new AlertDialog.Builder(getContext())
					.setTitle(getString(R.string.wrong_score,
							result.values.get(AppConstants.USER_RATING_CHANGE),
							result.values.get(AppConstants.USER_RATING)))
					.setItems(getResources().getTextArray(R.array.wrongtactic), wrongScoreDialogListener)
					.create().show();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (noInternet) {
				if (DataHolder.getInstance().isOffline()) {
					getGuestTacticsGame();
				} else {
					DataHolder.getInstance().setOffline(true);
					showOfflineRatingDialog();
				}
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
	}

	@Override
	public void invalidateGameScreen() {
		boardView.addMove2Log(getBoardFace().getMoveListSAN());
	}

	@Override
	public void newGame() {
		if (DataHolder.getInstance().isGuest()) {
			increaseCurrentProblem();
			getGuestTacticsGame();
		} else {
			if (noInternet)
				increaseCurrentProblem();
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
	public void showSubmitButtonsLay(boolean show) {
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
				break;
			case R.id.menu_next:
				boardView.moveForward();
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
					if (DataHolder.getInstance().isGuest() || noInternet) {
						increaseCurrentProblem();
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
		boardView.setFinished(false);
		if (getTacticItem() != null) {
			getTacticItem().values.put(AppConstants.STOP, "0");
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
	protected void restoreGame() {
		if (getTacticItem() != null && getTacticItem().values.get(AppConstants.STOP).equals("1")) {
			openOptionsMenu();
			return;
		}

		int secondsSpent = getBoardFace().getSec();

		if (DataHolder.getInstance().isGuest() || noInternet) {
			// set new board
			boardView.setBoardFace(new ChessBoard(this));

			String FEN = getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.FEN);
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

			if (getTacticsBatch().get(getCurrentProblem()).values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(getTacticsBatch()
						.get(getCurrentProblem()).values
						.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
						.replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
						.substring(1).split(StaticData.SYMBOL_SPACE));

				getBoardFace().setMovesCount(1);
			}

			getBoardFace().setSec(secondsSpent);
			getBoardFace().setLeft(Integer.parseInt(getTacticsBatch()
					.get(getCurrentProblem()).values.get(AppConstants.AVG_SECONDS)) - secondsSpent);

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

			invalidateGameScreen();
			getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		} else {
			if (getTacticItem() != null && getTacticItem().values.get(AppConstants.STOP).equals("1")) {
				openOptionsMenu();
				return;
			}
			boardView.setBoardFace(new ChessBoard(this));

			String FEN = getTacticItem().values.get(AppConstants.FEN);
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

			if (getTacticItem().values.get(AppConstants.MOVE_LIST).contains("1.")) {
				getBoardFace().setTacticMoves(getTacticItem()
						.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
						.replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
						.substring(1).split(StaticData.SYMBOL_SPACE));
				getBoardFace().setMovesCount(1);
			}

			getBoardFace().setSec(secondsSpent);
			getBoardFace().setLeft(Integer.parseInt(getTacticItem()
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

			invalidateGameScreen();
			getBoardFace().takeBack();
			boardView.invalidate();

			playLastMoveAnimation();
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(FIRST_TACTICS_TAG)) { // TODO move to AsyncTask
			InputStream inputStream = getResources().openRawResource(R.raw.tactics100batch);
			try {
				ByteArrayBuffer baf = new ByteArrayBuffer(50);
				int current;
				while ((current = inputStream.read()) != -1) {
					baf.append((byte) current);
				}

				String input = new String(baf.toByteArray());
				String[] tmp = input.split("[|]");
				int count = tmp.length - 1;

				DataHolder.getInstance().setTacticsBatch(new ArrayList<TacticItem>(count));
				for (int i = 1; i <= count; i++) {
					getTacticsBatch().add(new TacticItem(tmp[i].split(":")));
				}
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (DataHolder.getInstance().isGuest())
				getGuestTacticsGame();
			else
				getTacticsGame(StaticData.SYMBOL_EMPTY);
		} else if (fragment.getTag().equals(TACTICS_LIMIT_TAG)) {
			FlurryAgent.onEvent(FlurryData.UPGRADE_FROM_TACTICS, null);
			startActivity(AppData.getMembershipIntent(StaticData.SYMBOL_EMPTY, getContext()));
		} else if (fragment.getTag().equals(HUNDRED_TACTICS_TAG)) {
			DataHolder.getInstance().setCurrentTacticProblem(0);
			onBackPressed();
		} else if (fragment.getTag().equals(OFFLINE_RATING_TAG)) {
			getGuestTacticsGame();
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(FIRST_TACTICS_TAG)) {
			getBoardFace().setTacticCanceled(true);
			DataHolder.getInstance().setPendingTacticsLoad(false);
			onBackPressed();
		} else if (fragment.getTag().equals(TACTICS_LIMIT_TAG)) {
			getBoardFace().setTacticCanceled(true);
			DataHolder.getInstance().setPendingTacticsLoad(false);
			onBackPressed();
		} else if (fragment.getTag().equals(OFFLINE_RATING_TAG)) {
			onBackPressed();
			getBoardFace().setTacticCanceled(true);
			DataHolder.getInstance().setPendingTacticsLoad(false);
		}
	}

	private DialogInterface.OnClickListener correctDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 1) {
				if (DataHolder.getInstance().isGuest()) {
					increaseCurrentProblem();
					getGuestTacticsGame();
				} else {
					if (noInternet)
						increaseCurrentProblem();
					getTacticsGame(StaticData.SYMBOL_EMPTY);
				}
			}
		}
	};

	private DialogInterface.OnClickListener wrongDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == TACTICS_NEXT) {
				if (DataHolder.getInstance().isGuest()) {
					increaseCurrentProblem();
					getGuestTacticsGame();
				} else {
					if (noInternet)
						increaseCurrentProblem();
					getTacticsGame(StaticData.SYMBOL_EMPTY);
				}
			}
			if (which == TACTICS_RETRY) {
				if (DataHolder.getInstance().isGuest() || noInternet) {
					getBoardFace().setRetry(true);
					getGuestTacticsGame();
				} else {
					getTacticsGame(getTacticItem().values.get(AppConstants.ID));
				}
			}
			if (which == TACTICS_STOP) {
				boardView.setFinished(true);
				getTacticItem().values.put(AppConstants.STOP, "1");
			}
		}
	};

	private DialogInterface.OnClickListener wrongScoreDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == TACTICS_NEXT) {
				getTacticsGame(StaticData.SYMBOL_EMPTY);
			}
			if (which == TACTICS_RETRY) {
				getBoardFace().setRetry(true);
				getTacticsGame(getTacticItem().values.get(AppConstants.ID));
			}
			if (which == TACTICS_STOP) {
				boardView.setFinished(true);
				getTacticItem().values.put(AppConstants.STOP, "1");
			}
		}
	};

	private TacticItem getTacticItem(){
		return DataHolder.getInstance().getTactic();
	}
	
	private List<TacticItem> getTacticsBatch(){
		return DataHolder.getInstance().getTacticsBatch();
	}

	private int getCurrentProblem(){
		return DataHolder.getInstance().getCurrentTacticProblem();
	}

	private void increaseCurrentProblem(){
		DataHolder.getInstance().increaseCurrentTacticsProblem();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getBoardFace().setTacticCanceled(true);
			DataHolder.getInstance().setPendingTacticsLoad(false);
		}
		return super.onKeyDown(keyCode, event);
	}


}
