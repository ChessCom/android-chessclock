package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.chess.backend.tasks.CalculateTacticsMoveTask;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.PopupItem;
import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.GameTacticsActivityFace;
import com.chess.ui.views.ChessBoardTacticsView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;
import com.flurry.android.FlurryAgent;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameTacticsScreenActivity extends GameBaseActivity implements GameTacticsActivityFace {

	private static final int TIMER_UPDATE = 1000;
	private TextView timerTxt;
	private Handler tacticsTimer;
	private ChessBoardTacticsView boardView;

	private boolean noInternet;
	private boolean firstRun = true;

	private GetTacticsUpdateListener getTacticsUpdateListener;
	private TacticsCorrectUpdateListener tacticsCorrectUpdateListener;
	private TacticsWrongUpdateListener tacticsWrongUpdateListener;

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private static final String FIRST_TACTICS_TAG = "first tactics";
	private static final String TEN_TACTICS_TAG = "ten tactics reached";
	private static final String OFFLINE_RATING_TAG = "tactics offline rating";
	private static final String TACTIC_SOLVED_TAG = "tactic solved popup";
	private static final String WRONG_MOVE_TAG = "wrong move popup";

	private TacticsCalculationUpdateListener tacticsCalculationUpdateListener;
	private PopupCustomViewFragment customViewFragment;
	private boolean limitReached;
	private boolean firsTacticStart;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_tactics);

		init();
		widgetsInit();
	}

	public void init() {
		tacticsTimer = new Handler();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.skipproblem),
				getString(R.string.showanswer),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		getTacticsUpdateListener = new GetTacticsUpdateListener();
		tacticsCorrectUpdateListener = new TacticsCorrectUpdateListener();
		tacticsWrongUpdateListener = new TacticsWrongUpdateListener();
		tacticsCalculationUpdateListener = new TacticsCalculationUpdateListener();

		firsTacticStart = preferences.getBoolean(AppConstants.PREF_FIRST_TACTIC_START, true);
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
			firstRun = false;
		} else {
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setInit(true);
			getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}

		timerTxt = (TextView) findViewById(R.id.timerTxt);
		timerTxt.setVisibility(View.VISIBLE);

		topPlayerlabel.setVisibility(View.GONE);
		topPlayerTimer.setVisibility(View.GONE);

		gamePanelView.hideChatButton();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!DataHolder.getInstance().isGuest())
			FlurryAgent.onEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED);
	}

	@Override
	protected void onResume() {
		super.onResume();


		if (firstRun) {
			firstRun = false;

			if (AppData.haveSavedTacticGame(this)) {
				String tacticString = preferences.getString(AppConstants.SAVED_TACTICS_ITEM, StaticData.SYMBOL_EMPTY);
				String tacticResultString = preferences.getString(AppConstants.SAVED_TACTICS_RESULT_ITEM, StaticData.SYMBOL_EMPTY);

				int secondsSpend = preferences.getInt(AppConstants.SPENT_SECONDS_TACTICS, 0);

				TacticItem tacticItem = new TacticItem(tacticString.split(StaticData.SYMBOL_COLON));
				setTacticToBoard(tacticItem, secondsSpend);

				DataHolder.getInstance().setTactic(tacticItem);

				TacticResultItem tacticResultItem = new TacticResultItem(tacticResultString.split(StaticData.SYMBOL_COLON));
				DataHolder.getInstance().setTacticResultItem(tacticResultItem);

				if (getBoardFace().getHply() > 0)
					checkMove();
			} else {
				popupItem.setPositiveBtnId(R.string.yes);
				popupItem.setNegativeBtnId(R.string.no);
				showPopupDialog(R.string.ready_for_first_tackics_q, FIRST_TACTICS_TAG);
			}
		}

		// TODO show register confirmation dialog
		if (getBoardFace().isTacticCanceled()) {
			getBoardFace().setTacticCanceled(false);
			popupItem.setPositiveBtnId(R.string.yes);
			popupItem.setNegativeBtnId(R.string.no);
			showPopupDialog(R.string.ready_for_first_tackics_q, FIRST_TACTICS_TAG);

		} else if (getTacticItem() != null && !getTacticItem().isStop() && getBoardFace().getMovesCount() > 0) {
			invalidateGameScreen();
			getBoardFace().takeBack();
			boardView.invalidate();
			startTacticsTimer();
			playLastMoveAnimation();
			if (getBoardFace().getHply() > 0)
				checkMove();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		stopTacticsTimer();

		if (!getBoardFace().isTacticCanceled()) {
			preferencesEditor.putString(AppConstants.SAVED_TACTICS_ITEM, getTacticItem().getSaveString());
			preferencesEditor.putString(AppConstants.SAVED_TACTICS_RESULT_ITEM,
					DataHolder.getInstance().getTacticResultItem().getSaveString());
			preferencesEditor.putInt(AppConstants.SPENT_SECONDS_TACTICS, getBoardFace().getSecondsPassed());
			preferencesEditor.commit();
		}
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

		if (getBoardFace().lastMoveContains(piece, moveTo)) { // if correct move
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
					TacticResultItem tacticResultItem = DataHolder.getInstance().getTacticResultItem();
					String title = getString(R.string.problem_solved, tacticResultItem.getUserRatingChange(),
							tacticResultItem.getUserRating());

					showSolvedTacticPopup(title, false);
				} else {
					LoadItem loadItem = new LoadItem();
					loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
					loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
					loadItem.addRequestParams(RestHelper.P_TACTICS_ID, getTacticItem().getId());
					loadItem.addRequestParams(RestHelper.P_PASSED, "0");
					loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, String.valueOf(getBoardFace().getTacticsCorrectMoves()));
					loadItem.addRequestParams(RestHelper.P_SECONDS, String.valueOf(getBoardFace().getSecondsPassed()));

					new GetStringObjTask(tacticsCorrectUpdateListener).executeTask(loadItem);
				}
				stopTacticsTimer();
			}
		} else {
			if (DataHolder.getInstance().isGuest() || getBoardFace().isRetry() || noInternet) {
				popupDialogFragment.setButtons(3);
				popupItem.setPositiveBtnId(R.string.next);
				popupItem.setNeutralBtnId(R.string.retry);
				popupItem.setNegativeBtnId(R.string.stop);
				showPopupDialog(R.string.wrong_ex, WRONG_MOVE_TAG);

			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_TACTICS_ID, getTacticItem().getId());
				loadItem.addRequestParams(RestHelper.P_PASSED, "0");
				loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, String.valueOf(getBoardFace().getTacticsCorrectMoves()));
				loadItem.addRequestParams(RestHelper.P_SECONDS, String.valueOf(getBoardFace().getSecondsPassed()));

				new GetStringObjTask(tacticsWrongUpdateListener).executeTask(loadItem);
			}
			stopTacticsTimer();
		}
	}

	private void showSolvedTacticPopup(String title, boolean limitReached) {
		this.limitReached = limitReached;

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View customView = inflater.inflate(R.layout.popup_tactic_solved, null, false);

		LinearLayout adViewWrapper = (LinearLayout) customView.findViewById(R.id.adview_wrapper);
		if (AppUtils.isNeedToUpgrade(this)) {
			MopubHelper.showRectangleAd(adViewWrapper, this);
		} else {
			adViewWrapper.setVisibility(View.GONE);
		}

		int nextBtnId = R.string.next_tactic_puzzle;
		int nextBtnColorId = R.drawable.button_orange_selector;
		if (limitReached) {
			title = getString(R.string.daily_limit_reached);
			nextBtnId = R.string.upgrade_to_continue;
			nextBtnColorId = R.drawable.button_green_selector;
		}

		((TextView) customView.findViewById(R.id.titleTxt)).setText(title);

		customView.findViewById(R.id.cancelBtn).setOnClickListener(this);
		Button nextBtn = (Button) customView.findViewById(R.id.nextBtn);
		nextBtn.setText(nextBtnId);
		nextBtn.setBackgroundResource(nextBtnColorId);
		nextBtn.setOnClickListener(this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		customViewFragment = PopupCustomViewFragment.newInstance(popupItem);
		customViewFragment.show(getSupportFragmentManager(), TACTIC_SOLVED_TAG);
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;
	}

	private void showLimitDialog() {
		FlurryAgent.onEvent(FlurryData.TACTICS_DAILY_LIMIT_EXCEDED);
		showSolvedTacticPopup(StaticData.SYMBOL_EMPTY, true);
	}

	private void getNewTactic() {
		noInternet = !AppUtils.isNetworkAvailable(this);
		DataHolder.getInstance().increaseCurrentTacticsProblem();
		getTacticFromBatch();

//		if(DataHolder.getInstance().isGuest() || noInternet){
//			DataHolder.getInstance().increaseCurrentTacticsProblem();
//			getTacticFromBatch();
//			preferencesEditor.putBoolean(AppConstants.PREF_FIRST_TACTIC_START, false);
//			preferencesEditor.commit();
//		}else {
//
////				LoadItem loadItem = new LoadItem();
////				loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
////				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
////				loadItem.addRequestParams(RestHelper.P_TACTICS_ID, StaticData.SYMBOL_EMPTY);
////
////				new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);
//			DataHolder.getInstance().increaseCurrentTacticsProblem();
//
//			getTacticFromBatch();
//
//		}
	}

	private void getTacticFromBatch() {

		if(getTacticsBatch() == null){ // if we load from saved tactic
			loadNewTacticsBatch();
			return;
		}

		if (getCurrentProblem() >= getTacticsBatch().size()) {
			if (DataHolder.getInstance().isGuest()) {
				showPopupDialog(R.string.ten_tactics_completed, TEN_TACTICS_TAG);
				popupDialogFragment.setButtons(1);
			} else {
				showLimitDialog();
			}
			return;
		}

		TacticItem tacticItem = getTacticsBatch().get(getCurrentProblem());
		setTacticToBoard(tacticItem, 0);

		DataHolder.getInstance().setTactic(tacticItem);
	}


	private class GetTacticsUpdateListener extends ChessUpdateListener {
		public GetTacticsUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				String[] tmp = returnedObj.trim().split("[|]");
				if (tmp.length < 3 || tmp[2].trim().equals(StaticData.SYMBOL_EMPTY)) {
					showLimitDialog();
					return;
				}

//			TacticItem tacticItem = new TacticItem(tmp[2].split(":"));
				int count = tmp.length - 1;
				List<TacticItem> tacticBatch = new ArrayList<TacticItem>(count);
				for (int i = 1; i <= count; i++) {
					tacticBatch.add(new TacticItem(tmp[i].split(StaticData.SYMBOL_COLON)));
				}

				DataHolder.getInstance().setCurrentTacticProblem(0);
				DataHolder.getInstance().setTacticsBatch(tacticBatch);
				getTacticFromBatch();
//			TacticItem tacticItem = new TacticItem(tmp[2].split(":"));
//			DataHolder.getInstance().setTactic(tacticItem);

//			setTacticToBoard(tacticItem, 0);

				preferencesEditor.putBoolean(AppConstants.PREF_FIRST_TACTIC_START, false);
				preferencesEditor.commit();
			} else {
				showSinglePopupDialog(returnedObj.substring(RestHelper.R_ERROR.length()));
			}

		}

		@Override
		public void errorHandle(Integer resultCode) {
			handleErrorRequest();
		}
	}

	private void showAnswer() {
		boardView.setBoardFace(new ChessBoard(this));
		getBoardFace().setRetry(true);

		TacticItem tacticItem;
		if (DataHolder.getInstance().isGuest() || noInternet) {
			tacticItem = getTacticsBatch().get(getCurrentProblem());
		} else {
			tacticItem = getTacticItem();
		}

		String FEN = tacticItem.getFen();
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
		if (tacticItem.getMoveList().contains("1.")) {
			getBoardFace().setTacticMoves(tacticItem.getMoveList()
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", StaticData.SYMBOL_SPACE)
					.substring(1).split(StaticData.SYMBOL_SPACE));
			getBoardFace().setMovesCount(1);
		}

		boardView.invalidate();

		new CalculateTacticsMoveTask(tacticsCalculationUpdateListener, getBoardFace()).executeTask();
	}

	private class TacticsCalculationUpdateListener extends ChessUpdateListener {
		public TacticsCalculationUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			invalidateGameScreen();
			boardView.invalidate();
			stopTacticsTimer();
		}
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

			TacticResultItem tacticResultItem = new TacticResultItem(tmp[1].split(":"));
			DataHolder.getInstance().setTacticResultItem(tacticResultItem);

			String title = getString(R.string.problem_solved, tacticResultItem.getUserRatingChange(),
					tacticResultItem.getUserRating());

			showSolvedTacticPopup(title, false);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			handleErrorRequest();
		}
	}

	private void handleErrorRequest() {
//		getTacticFromBatch();
//		if (noInternet) {
		showOfflineRatingDialog();
//			if (DataHolder.getInstance().isOffline()) {
//			} else {
//				DataHolder.getInstance().setOffline(true);
//			}
//		}
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

			TacticResultItem tacticResultItem = new TacticResultItem(tmp[1].split(":"));
			DataHolder.getInstance().setTacticResultItem(tacticResultItem);

			popupDialogFragment.setButtons(3);
			popupItem.setPositiveBtnId(R.string.next);
			popupItem.setNeutralBtnId(R.string.retry);
			popupItem.setNegativeBtnId(R.string.stop);

			String title = getString(R.string.wrong_score,
					tacticResultItem.getUserRatingChange(),
					tacticResultItem.getUserRating());

			showPopupDialog(title, WRONG_MOVE_TAG);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			handleErrorRequest();
		}
	}


	@Override
	public void switch2Analysis(boolean isAnalysis) {
		if (isAnalysis) {
			timerTxt.setVisibility(View.INVISIBLE);
			analysisTxt.setVisibility(View.VISIBLE);
		} else {
			timerTxt.setVisibility(View.VISIBLE);
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
		gamePanelView.invalidate();
	}

	@Override
	public void newGame() {
		getNewTactic();
		closeOptionsMenu();
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
					getNewTactic();
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

	public void stopTacticsTimer() {
		tacticsTimer.removeCallbacks(timerUpdateTask);
	}

	public void startTacticsTimer() {
		boardView.setFinished(false);
		if (getTacticItem() != null) {
			getTacticItem().setStop(false);
		}

		tacticsTimer.removeCallbacks(timerUpdateTask);
		tacticsTimer.postDelayed(timerUpdateTask, TIMER_UPDATE);
	}

	private Runnable timerUpdateTask = new Runnable() {
		@Override
		public void run() {
			tacticsTimer.removeCallbacks(this);
			tacticsTimer.postDelayed(timerUpdateTask, TIMER_UPDATE);

			if (getBoardFace().isAnalysis())
				return;

			getBoardFace().increaseSecondsPassed();

			timerTxt.setText(getString(R.string.bonus_time_left, getBoardFace().getSecondsLeft()
					, getBoardFace().getSecondsPassed()));
		}
	};

	@Override
	protected void restoreGame() {
		if (getTacticItem() != null && getTacticItem().isStop()) {
			openOptionsMenu();
			return;
		}

		int secondsSpent = getBoardFace().getSecondsPassed();

		TacticItem tacticItem;
		if (DataHolder.getInstance().isGuest() || noInternet) {
			tacticItem = getTacticsBatch().get(getCurrentProblem());
		} else {
			tacticItem = getTacticItem();
		}

		setTacticToBoard(tacticItem, secondsSpent);

	}

	private void setTacticToBoard(TacticItem tacticItem, int secondsSpent) {
		boardView.setBoardFace(new ChessBoard(this));

		String FEN = tacticItem.getFen();
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

		if (tacticItem.getMoveList().contains("1.")) {
			getBoardFace().setTacticMoves(tacticItem.getMoveList()
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", StaticData.SYMBOL_SPACE)
					.substring(1).split(StaticData.SYMBOL_SPACE));

			getBoardFace().setMovesCount(1);
		}

		getBoardFace().setSecondsPassed(secondsSpent);
		int secondsLeft = tacticItem.getAvgSecondsInt() - secondsSpent < 0 ?
				0 : tacticItem.getAvgSecondsInt() - secondsSpent;
		getBoardFace().setSecondsLeft(secondsLeft);

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
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.nextBtn) {
			customViewFragment.dismiss();
			if (limitReached) {
				FlurryAgent.onEvent(FlurryData.UPGRADE_FROM_TACTICS, null);
				startActivity(AppData.getMembershipIntent(StaticData.SYMBOL_EMPTY, getContext()));

			} else {
				getNewTactic();
			}
		} else if (view.getId() == R.id.cancelBtn) {
			customViewFragment.dismiss();

			if (limitReached) {
				cancelTacticAndLeave();
//				DataHolder.getInstance().setPendingTacticsLoad(false);
				onBackPressed();
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(FIRST_TACTICS_TAG)) { // TODO move to AsyncTask
			loadNewTacticsBatch();
		} else if (fragment.getTag().equals(TEN_TACTICS_TAG)) {
			DataHolder.getInstance().setCurrentTacticProblem(0);
			onBackPressed();
		} else if (fragment.getTag().equals(WRONG_MOVE_TAG)) { // Next
			getNewTactic();

		} else if (fragment.getTag().equals(OFFLINE_RATING_TAG)) {
			getTacticFromBatch();
		}
	}

	private void loadNewTacticsBatch(){

		if (DataHolder.getInstance().isGuest()) {
			FlurryAgent.onEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_GUEST);

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

				List<TacticItem> tacticBatch = new ArrayList<TacticItem>(count);
				for (int i = 1; i <= count; i++) {
					tacticBatch.add(new TacticItem(tmp[i].split(":")));
				}

				DataHolder.getInstance().setCurrentTacticProblem(0);
				DataHolder.getInstance().setTacticsBatch(tacticBatch);
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			getNewTactic();
		} else {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_TACTICS_PROBLEM_BATCH);
//			loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
//			loadItem.addRequestParams(RestHelper.P_TACTICS_ID, StaticData.SYMBOL_EMPTY);
			loadItem.addRequestParams(RestHelper.P_IS_INSTALL, RestHelper.V_ZERO);

			new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);

		}


//			getNewTactic();
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(FIRST_TACTICS_TAG)) { // Cancel
			cancelTacticAndLeave();
		} else if (fragment.getTag().equals(WRONG_MOVE_TAG)) { // Stop

			boardView.setFinished(true);
			getTacticItem().setStop(true);
			stopTacticsTimer();

		} else if (fragment.getTag().equals(OFFLINE_RATING_TAG)) {
			cancelTacticAndLeave();
		}
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		super.onNeutralBtnCLick(fragment);
		if (fragment.getTag().equals(WRONG_MOVE_TAG)) {  // Retry
			if (DataHolder.getInstance().isGuest() || noInternet) {
				getTacticFromBatch();
			} else {
				setTacticToBoard(getTacticItem(), 0);
			}
			getBoardFace().setRetry(true);
		}
	}

	private TacticItem getTacticItem() {
		return DataHolder.getInstance().getTactic();
	}

	private List<TacticItem> getTacticsBatch() {
		return DataHolder.getInstance().getTacticsBatch();
	}

	private int getCurrentProblem() {
		return DataHolder.getInstance().getCurrentTacticProblem();
	}

	private void cancelTacticAndLeave(){
		getBoardFace().setTacticCanceled(true);
		preferencesEditor.putString(AppConstants.SAVED_TACTICS_ITEM, StaticData.SYMBOL_EMPTY);
		preferencesEditor.putString(AppConstants.SAVED_TACTICS_RESULT_ITEM, StaticData.SYMBOL_EMPTY);
		preferencesEditor.commit();

		onBackPressed();
	}

}
