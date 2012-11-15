package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.QueryParams;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveTacticsBatchTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.model.TacticItem;
import com.chess.model.TacticResultItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.fragments.BasePopupDialogFragment;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.GameTacticsActivityFace;
import com.chess.ui.interfaces.TacticBoardFace;
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
	private static final long TACTIC_ANSWER_DELAY = 1500;

	private TextView timerTxt;
	private Handler tacticsTimer;
	private ChessBoardTacticsView boardView;

	private boolean noInternet;
	private boolean firstRun = true;

	private GetTacticsUpdateListener getTacticsUpdateListener;
	private TacticsCorrectUpdateListener tacticsCorrectUpdateListener;
	private TacticsWrongUpdateListener tacticsWrongUpdateListener;
	private DbBatchUpdateListener dbBatchUpdateListener;
	private DbTacticBatchSaveListener dbTacticBatchSaveListener;

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private static final String FIRST_TACTICS_TAG = "first tactics";
	private static final String TEN_TACTICS_TAG = "ten tactics reached";
	private static final String OFFLINE_RATING_TAG = "tactics offline rating";
	private static final String TACTIC_SOLVED_TAG = "tactic solved popup";
	private static final String WRONG_MOVE_TAG = "wrong move popup";

	private LayoutInflater inflater;
	private int currentTacticAnswerCnt;
	private int maxTacticAnswerCnt;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_tactics);

		init();
		widgetsInit();
	}

	public void init() {
		tacticsTimer = new Handler();
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.skipproblem),
				getString(R.string.showanswer),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		getTacticsUpdateListener = new GetTacticsUpdateListener();
		tacticsCorrectUpdateListener = new TacticsCorrectUpdateListener();
		tacticsWrongUpdateListener = new TacticsWrongUpdateListener();
		dbBatchUpdateListener = new DbBatchUpdateListener();
		dbTacticBatchSaveListener = new DbTacticBatchSaveListener();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		boardView = (ChessBoardTacticsView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		boardView.setGameActivityFace(this);

		setBoardView(boardView);

		final ChessBoard chessBoard = ChessBoardTactics.getInstance(this);
		firstRun = chessBoard.isJustInitialized();
		boardView.setBoardFace(chessBoard);

		timerTxt = (TextView) findViewById(R.id.timerTxt);
		timerTxt.setVisibility(View.VISIBLE);

		whitePlayerLabel.setVisibility(View.GONE);
		blackPlayerLabel.setVisibility(View.GONE);

		gamePanelView.hideChatButton();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!AppData.isGuest(this)) {
			FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		dismissDialogs();

		if (firstRun) {
			firstRun = false;

			Log.d("TEST", "Have saved games = " + AppData.haveSavedTacticGame(this));
			boolean haveGuestSavedGame = !preferences.getString(AppConstants.SAVED_TACTICS_ITEM, StaticData.SYMBOL_EMPTY)
					.equals(StaticData.SYMBOL_EMPTY);
			Log.d("TEST", "have Guest SavedGame = " + haveGuestSavedGame);

			final boolean userIsGuest = AppData.isGuest(this);

			if (AppData.haveSavedTacticGame(this) && !userIsGuest || haveGuestSavedGame && userIsGuest) {
				String userName = AppData.getUserName(this);
				if (userIsGuest) {
					userName = StaticData.SYMBOL_EMPTY;
				}

				String tacticString = preferences.getString(userName + AppConstants.SAVED_TACTICS_ITEM, StaticData.SYMBOL_EMPTY);
				String tacticResultString = preferences.getString(userName + AppConstants.SAVED_TACTICS_RESULT_ITEM, StaticData.SYMBOL_EMPTY);
				long showedTacticId = 0;
				try {
					showedTacticId = preferences.getLong(userName + AppConstants.SAVED_TACTICS_ID, 0);
				} catch (ClassCastException ex) {
					String tacticId = preferences.getString(userName + AppConstants.SAVED_TACTICS_ID, StaticData.SYMBOL_EMPTY);
					if (!TextUtils.isEmpty(tacticId)) { // or just catch NumberFormatException
						showedTacticId = Long.parseLong(tacticId);
					}
				}
				boolean isRetry = preferences.getBoolean(userName + AppConstants.SAVED_TACTICS_RETRY, false);

				int secondsSpend = preferences.getInt(userName + AppConstants.SPENT_SECONDS_TACTICS, 0);

				TacticItem tacticItem = new TacticItem(tacticString.split(StaticData.SYMBOL_COLON));
				TacticsDataHolder.getInstance().setTactic(tacticItem);
				setTacticToBoard(tacticItem, secondsSpend);

				if (!tacticResultString.equals(StaticData.SYMBOL_EMPTY)) {
					TacticResultItem tacticResultItem = new TacticResultItem(tacticResultString.split(StaticData.SYMBOL_COLON));
					TacticsDataHolder.getInstance().setTacticResultItem(tacticResultItem);
				}

				TacticsDataHolder.getInstance().addShowedTacticId(showedTacticId);

				getBoardFace().setRetry(isRetry);

				if (getBoardFace().isLatestMoveMadeUser()) {
					checkMove();
				}

				if (getTacticsBatch() == null) {
					loadSavedTacticsBatch();
				}
			} else {
				popupItem.setPositiveBtnId(R.string.yes);
				popupItem.setNegativeBtnId(R.string.no);
				showPopupDialog(R.string.ready_for_first_tackics_q, FIRST_TACTICS_TAG);
			}
		} else {
			// TODO show register confirmation dialog
			TacticItem tacticItem = getTacticItem();
			if (tacticItem == null) { // singleton can be killed
				if (getTacticsBatch() == null) {
					loadSavedTacticsBatch();
					return;
				}
				getNewTactic();
			} else if (!tacticItem.isStop() && getBoardFace().getMovesCount() > 0) {

				startTacticsTimer(tacticItem);
				getBoardFace().setRetry(true);

				invalidateGameScreen();
				getBoardFace().takeBack();
				boardView.invalidate();
				playLastMoveAnimationAndCheck();
			} else if(tacticItem.isStop()) {
				timerTxt.setText(getString(R.string.bonus_time_left, getBoardFace().getSecondsLeft()
						, getBoardFace().getSecondsPassed()));
			}
			if (getTacticsBatch() == null) { // happens when ad appears in the middle of the launch of Activity and interrupt load,  but set flags.
				loadNewTacticsBatch();
			}
		}
	}

	@Override
	protected void dismissDialogs() {
		if (findFragmentByTag(WRONG_MOVE_TAG) != null) {
			((BasePopupDialogFragment)findFragmentByTag(WRONG_MOVE_TAG)).dismiss();
		}
		if (findFragmentByTag(TACTIC_SOLVED_TAG) != null) {
			((BasePopupDialogFragment)findFragmentByTag(TACTIC_SOLVED_TAG)).dismiss();
		}
	}

	@Override
	protected void onPause() {
		dismissDialogs();
		super.onPause();

		stopTacticsTimer();

		if (needToSaveTactic()) {
			String userName = AppData.getUserName(this);
			if (AppData.isGuest(this)) { // for guest mode we should have different name
				userName = StaticData.SYMBOL_EMPTY;
			}

			preferencesEditor.putString(userName + AppConstants.SAVED_TACTICS_ITEM, getTacticItem().getSaveString());
			preferencesEditor.putInt(userName + AppConstants.SPENT_SECONDS_TACTICS, getBoardFace().getSecondsPassed());
			preferencesEditor.putBoolean(userName + AppConstants.SAVED_TACTICS_RETRY, getBoardFace().isRetry());

			final TacticResultItem tacticResultItem = TacticsDataHolder.getInstance().getTacticResultItem();
			String tacticResultString = StaticData.SYMBOL_EMPTY;
			if (tacticResultItem != null) {
				tacticResultString = tacticResultItem.getSaveString();
			}
			preferencesEditor.putString(userName + AppConstants.SAVED_TACTICS_RESULT_ITEM, tacticResultString);
			preferencesEditor.putInt(userName + AppConstants.SAVED_TACTICS_CURRENT_PROBLEM, getCurrentProblem());

			if (answerWasShowed()) {
				preferencesEditor.putLong(userName + AppConstants.SAVED_TACTICS_ID, getTacticItem().getId());
			}
			preferencesEditor.commit();

			// start task for saving tactics batch here
			if (getTacticsBatch() != null) {
				new SaveTacticsBatchTask(dbTacticBatchSaveListener, getTacticsBatch()).executeTask();
			}
		}

	}

	/**
	 * Check if tactic was canceled or limit reached
	 *
	 * @return true if need to Save
	 */
	private boolean needToSaveTactic() {
		return !getBoardFace().isTacticCanceled() && !TacticsDataHolder.getInstance().isTacticLimitReached()
				&& getTacticItem() != null;
	}

	private void playLastMoveAnimationAndCheck() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				getBoardFace().takeNext();
				invalidateGameScreen();

				if (getBoardFace().isLatestMoveMadeUser())
					checkMove();
			}
		}, 1300);
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
	public boolean currentGameExist() {
		return getTacticItem() != null;
	}

	@Override
	protected TacticBoardFace getBoardFace() {
		return boardView.getBoardFace();
	}

	@Override
	public void checkMove() {

		noInternet = !AppUtils.isNetworkAvailable(getContext());
		final boolean userIsGuest = AppData.isGuest(this);

		TacticBoardFace boardFace = getBoardFace();

		if (boardFace.lastTacticMoveIsCorrect()) {
			Log.d("TEST_MOVE", " Correct move");
			boardFace.increaseTacticsCorrectMoves();

			if (boardFace.getMovesCount() < boardFace.getTacticMoves().length - 1) { // if it's not last move, make comp move
				boardFace.updateMoves(boardFace.getTacticMoves()[boardFace.getHply()], true);
				invalidateGameScreen();
			} else {
				if (TacticsDataHolder.getInstance().tacticWasShowed(getTacticItem().getId())) {
					showSolvedTacticPopup(getString(R.string.problem_solved_), false);

				} else if (userIsGuest || boardFace.isRetry() || noInternet) {
					TacticResultItem tacticResultItem = TacticsDataHolder.getInstance().getTacticResultItem();

					String title;
					if (tacticResultItem != null && !userIsGuest) {
						title = getString(R.string.problem_solved, tacticResultItem.getUserRatingChange(),
								tacticResultItem.getUserRating());
					} else {
						title = getString(R.string.problem_solved_);
					}

					showSolvedTacticPopup(title, false);
				} else {
					LoadItem loadItem = new LoadItem();
					loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
					loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
					loadItem.addRequestParams(RestHelper.P_TACTICS_ID, getTacticItem().getId());
					loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_PASSED);
					loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, String.valueOf(boardFace.getTacticsCorrectMoves()));
					loadItem.addRequestParams(RestHelper.P_SECONDS, String.valueOf(boardFace.getSecondsPassed()));

					new GetStringObjTask(tacticsCorrectUpdateListener).executeTask(loadItem);
				}
				stopTacticsTimer();
			}
		} else {
			Log.d("TEST_MOVE", " Wrong move");
			TacticResultItem tacticResultItem = TacticsDataHolder.getInstance().getTacticResultItem();
			boolean tacticResultItemIsValid = tacticResultItem != null
					&& Integer.valueOf(tacticResultItem.getUserRatingChange()) < 0; // if saved for wrong move. Note that after loading next tactic result is automaically assigns as a positive resultItem.

			if (userIsGuest) {
				showWrongMovePopup(getString(R.string.wrong_ex));
			} else if (tacticResultItemIsValid && (getBoardFace().isRetry() || noInternet)) {
				String title = getString(R.string.wrong_score,
						tacticResultItem.getUserRatingChange(),
						tacticResultItem.getUserRating());
				showWrongMovePopup(title);
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_TACTICS_ID, getTacticItem().getId());
				loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_FAILED);
				loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getTacticsCorrectMoves());
				loadItem.addRequestParams(RestHelper.P_SECONDS, getBoardFace().getSecondsPassed());

				new GetStringObjTask(tacticsWrongUpdateListener).executeTask(loadItem);
			}
			stopTacticsTimer();
		}
	}

	private void showWrongMovePopup(String title) {
		LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_tactic_incorrect, null, false);

		((TextView) customView.findViewById(R.id.titleTxt)).setText(title);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		PopupCustomViewFragment customViewFragment = PopupCustomViewFragment.newInstance(popupItem);
		customViewFragment.show(getSupportFragmentManager(), WRONG_MOVE_TAG);


		customView.findViewById(R.id.retryBtn).setOnClickListener(this);
		customView.findViewById(R.id.stopBtn).setOnClickListener(this);
		customView.findViewById(R.id.solutionBtn).setOnClickListener(this);
		customView.findViewById(R.id.nextBtn).setOnClickListener(this);
	}

	private void showSolvedTacticPopup(String title, boolean limitReached) {
		TacticsDataHolder.getInstance().setTacticLimitReached(limitReached);

		LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_tactic_solved, null, false);

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

			clearSavedTactics();
		}

		((TextView) customView.findViewById(R.id.titleTxt)).setText(title);

		customView.findViewById(R.id.cancelBtn).setOnClickListener(this);
		Button nextBtn = (Button) customView.findViewById(R.id.nextBtn);
		nextBtn.setText(nextBtnId);
		nextBtn.setBackgroundResource(nextBtnColorId);
		nextBtn.setOnClickListener(this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		PopupCustomViewFragment  customViewFragment = PopupCustomViewFragment.newInstance(popupItem);
		customViewFragment.show(getSupportFragmentManager(), TACTIC_SOLVED_TAG);
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;
	}

	public Long getGameId() {
		if (getTacticItem() == null) {
			return null;
		} else {
			return getTacticItem().getId();
		}
	}

	private void showLimitDialog() {
		FlurryAgent.logEvent(FlurryData.TACTICS_DAILY_LIMIT_EXCEEDED);
		showSolvedTacticPopup(StaticData.SYMBOL_EMPTY, true);
	}

	private void getNewTactic() {
		if (getTacticsBatch() == null) { // happens when ad appears in the middle of the launch of Activity and interrupt load,  but set flags.
			loadNewTacticsBatch();
			return;
		}
		// remove current tactic item from DB record
		TacticItem tacticItem = getTacticsBatch().get(getCurrentProblem());
		String[] arguments = new String[]{String.valueOf(tacticItem.getId()), tacticItem.getUser()};
		int deletedCnt = getContentResolver().delete(DBConstants.TACTICS_BATCH_CONTENT_URI,
				DBDataManager.SELECTION_TACTIC_ID_AND_USER, arguments);
		// remove current tactics from batch
		List<TacticItem> removeList = new ArrayList<TacticItem>();
		removeList.add(tacticItem);
		getTacticsBatch().removeAll(removeList);

		TacticsDataHolder.getInstance().increaseCurrentTacticsProblem();
		getTacticFromBatch();
	}

	private void getTacticFromBatch() {
		if (getTacticsBatch() == null) { // if we load from saved tactic
			loadNewTacticsBatch();
			return;
		}

		if (getCurrentProblem() >= getTacticsBatch().size()) {

			if (AppData.isGuest(this)) {
				showPopupDialog(R.string.ten_tactics_completed, TEN_TACTICS_TAG);
				getLastPopupFragment().setButtons(1);
			} else {
				// Try to update tactics batch
				loadNewTacticsBatch();
			}
			return;
		}

		TacticItem tacticItem = getTacticsBatch().get(getCurrentProblem());
		setTacticToBoard(tacticItem, 0);
		currentTacticAnswerCnt = 0;

		TacticsDataHolder.getInstance().setTactic(tacticItem);

	}


	private class GetTacticsUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			String[] tmp = returnedObj.trim().split("[|]");
			if (tmp.length < 2) {
				showLimitDialog();   // This is also wrong step, because we should never reach this condition
				return;
			}

			int count = tmp.length - 1;
			List<TacticItem> tacticBatch = new ArrayList<TacticItem>(count);
			for (int i = 1; i <= count; i++) {
				TacticItem tacticItem = new TacticItem(tmp[i].split(StaticData.SYMBOL_COLON));
				tacticItem.setUser(AppData.getUserName(getContext()));
				tacticBatch.add(tacticItem);
			}

			TacticsDataHolder.getInstance().setCurrentTacticProblem(0);
			TacticsDataHolder.getInstance().setTacticsBatch(tacticBatch);

			getTacticFromBatch();
		}

		@Override
		public void errorHandle(String resultMessage) {
			if (resultMessage.equals(RestHelper.R_TACTICS_LIMIT_REACHED)) {
				showLimitDialog();  // This should be the only way to show limit dialog for registered user
			} else {
				showSinglePopupDialog(resultMessage);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			handleErrorRequest();
		}
	}

	private void showAnswer() {
		stopTacticsTimer();
		getTacticItem().setStop(true);
		TacticsDataHolder.getInstance().addShowedTacticId(getTacticItem().getId());

		ChessBoardTactics.resetInstance();
		boardView.setBoardFace(ChessBoardTactics.getInstance(this));
		getBoardFace().setRetry(true);

		TacticItem tacticItem;
		if (AppData.isGuest(this) || noInternet) {
			if (getTacticsBatch() == null ){
				loadNewTacticsBatch();
				return;
			}
			tacticItem = getTacticsBatch().get(getCurrentProblem());
		} else {
			if (getTacticItem() == null ) { // get rid of uncertain errors
				getNewTactic();
				return;
			}
			tacticItem = getTacticItem();
		}

		getBoardFace().setupBoard(tacticItem.getFen());

		if (tacticItem.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			getBoardFace().setTacticMoves(tacticItem.getMoveList());
			getBoardFace().setMovesCount(1);
		}

		boardView.invalidate();

		currentTacticAnswerCnt = 0;
		maxTacticAnswerCnt = getBoardFace().getTacticMoves().length;
		handler.postDelayed(showTacticMoveTask, TACTIC_ANSWER_DELAY);
	}

	private boolean answerWasShowed() {
		return currentTacticAnswerCnt == maxTacticAnswerCnt && maxTacticAnswerCnt != 0;
	}

	private Runnable showTacticMoveTask = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);

			TacticBoardFace boardFace = getBoardFace();
			boolean sizeExceed = currentTacticAnswerCnt >= boardFace.getTacticMoves().length;

			if (answerWasShowed() || sizeExceed) {
				return;
			}

			getBoardFace().updateMoves(boardFace.getTacticMoves()[currentTacticAnswerCnt], true);
			invalidateGameScreen();

			currentTacticAnswerCnt++;
			handler.postDelayed(this, TACTIC_ANSWER_DELAY);
		}
	};

	private class TacticsCorrectUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			String[] tmp = returnedObj.split("[|]");

			if (tmp.length < 2) { // "Success+||"   - means we reached limit and there is no tactics
				showLimitDialog(); // limit dialog should be shown after updating tactic, while getting new
				return;
			}

			TacticResultItem tacticResultItem;
			if (!tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)) { // means we sent duplicate tactic_id, so result is the same
				tacticResultItem = new TacticResultItem(tmp[1].split(":"));
				TacticsDataHolder.getInstance().setTacticResultItem(tacticResultItem);  // should be saved only after move
			} else {
				tacticResultItem = TacticsDataHolder.getInstance().getTacticResultItem();
			}

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
		noInternet = true;
		showPopupDialog(R.string.offline_mode, R.string.no_network_rating_not_changed, OFFLINE_RATING_TAG);
		loadOfflineTacticsBatch(); // There is a case when you connected to wifi, but no internet connection over it.
	}

	private class TacticsWrongUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			String[] tmp = returnedObj.split("[|]");
			if (tmp.length < 2) { // "Success+||"   - means we reached limit and there is no tactics
				showLimitDialog(); // limit dialog should be shown after updating tactic, while getting new
				return;
			}

			TacticResultItem tacticResultItem;
			if (!tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)) { // means we sent duplicate tactic_id, so result is the same
				tacticResultItem = new TacticResultItem(tmp[1].split(":"));
				TacticsDataHolder.getInstance().setTacticResultItem(tacticResultItem);  // should be saved only after move
			} else {
				tacticResultItem = TacticsDataHolder.getInstance().getTacticResultItem();
			}

			String title = getString(R.string.wrong_score,
					tacticResultItem.getUserRatingChange(),
					tacticResultItem.getUserRating());

			showWrongMovePopup(title);

			getBoardFace().setRetry(true); // set auto retry because we save tactic
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
		boardView.setMovesLog(getBoardFace().getMoveListSAN());
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

	public void startTacticsTimer(TacticItem tacticItem) {
		boardView.setFinished(false);
		tacticItem.setStop(false);

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

		if (AppData.isGuest(this) || noInternet) {
			if (getTacticsBatch() == null) // TODO handle properly
				return;

			tacticItem = getTacticsBatch().get(getCurrentProblem());
		} else {
			tacticItem = getTacticItem();
		}

		setTacticToBoard(tacticItem, secondsSpent);
	}

	private void setTacticToBoard(TacticItem tacticItem, int secondsSpent) {
		if (tacticItem == null) { // TODO adjust more proper handle
			return;
		}

		ChessBoardTactics.resetInstance();
		final TacticBoardFace boardFace = ChessBoardTactics.getInstance(this);
		boardView.setBoardFace(boardFace);

		boardFace.setupBoard(tacticItem.getFen());

		if (tacticItem.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			boardFace.setTacticMoves(tacticItem.getMoveList());
			boardFace.setMovesCount(1);
		}

		boardFace.setSecondsPassed(secondsSpent);
		int secondsLeft = tacticItem.getAvgSeconds() - secondsSpent < 0 ?
				0 : tacticItem.getAvgSeconds() - secondsSpent;
		boardFace.setSecondsLeft(secondsLeft);

		startTacticsTimer(tacticItem);

		boardFace.updateMoves(boardFace.getTacticMoves()[0], true);

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.nextBtn) {

			dismissDialogs();

			if (TacticsDataHolder.getInstance().isTacticLimitReached()) {
				FlurryAgent.logEvent(FlurryData.UPGRADE_FROM_TACTICS, null);
				startActivity(AppData.getMembershipIntent(StaticData.SYMBOL_EMPTY, getContext()));

			} else {
				getNewTactic();
			}
		} else if (view.getId() == R.id.stopBtn) {
			boardView.setFinished(true);
			getTacticItem().setStop(true);
			stopTacticsTimer();
			dismissDialogs();
		} else if (view.getId() == R.id.retryBtn) {

			if (AppData.isGuest(this) || noInternet) {
				getTacticFromBatch();
			} else {
				setTacticToBoard(getTacticItem(), 0);
			}
			getBoardFace().setRetry(true);
			dismissDialogs();

		} else if (view.getId() == R.id.solutionBtn) {
			showAnswer();
			dismissDialogs();
		} else if (view.getId() == R.id.cancelBtn) {

			dismissDialogs();

			if (TacticsDataHolder.getInstance().isTacticLimitReached()) {
				cancelTacticAndLeave();
				onBackPressed();
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(FIRST_TACTICS_TAG)) {
			loadNewTacticsBatch();
		} else if (tag.equals(TEN_TACTICS_TAG)) {
			TacticsDataHolder.getInstance().setCurrentTacticProblem(0);
			onBackPressed();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
			getTacticFromBatch();
		}
		super.onPositiveBtnClick(fragment);
	}


	private void loadNewTacticsBatch() {
		noInternet = !AppUtils.isNetworkAvailable(this);
		if (AppData.isGuest(this) || noInternet) {
			loadOfflineTacticsBatch();
		} else {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_TACTICS_PROBLEM_BATCH);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_IS_INSTALL, RestHelper.V_ZERO);

			new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);
		}
	}

	private void loadOfflineTacticsBatch() {
		FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_GUEST);
		// TODO move to AsyncTask
		InputStream inputStream = getResources().openRawResource(R.raw.tactics10batch);
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
				TacticItem tacticItem = new TacticItem(tmp[i].split(":"));
				tacticItem.setUser(AppData.getUserName(getContext()));
				tacticBatch.add(tacticItem);
			}

			TacticsDataHolder.getInstance().setCurrentTacticProblem(0);
			TacticsDataHolder.getInstance().setTacticsBatch(tacticBatch);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getNewTactic();
	}

	private void loadSavedTacticsBatch() {
		// TODO check if we have saved tactics batch for that user
		if (AppData.haveSavedTacticBatch(this)) { // TODO don't load tactic when limit reached
			String[] arguments = new String[]{AppData.getUserName(this)};
			QueryParams queryParams = new QueryParams();
			queryParams.setArguments(arguments);
			queryParams.setSelection(DBDataManager.SELECTION_TACTIC_BATCH_USER);
			queryParams.setUri(DBConstants.TACTICS_BATCH_CONTENT_URI);
			new LoadDataFromDbTask(dbBatchUpdateListener, queryParams).executeTask();
		} else {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_TACTICS_PROBLEM_BATCH);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_IS_INSTALL, RestHelper.V_ZERO);

			new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);
		}
	}

	private class DbBatchUpdateListener extends AbstractUpdateListener<Cursor> {
		public DbBatchUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			List<TacticItem> tacticBatch = new ArrayList<TacticItem>();

			do {
				TacticItem tacticItem = DBDataManager.getTacticItemFromCursor(returnedObj);
				tacticBatch.add(tacticItem);

			} while (returnedObj.moveToNext());

			int savedTacticProblem = AppData.getSavedTacticProblem(getContext());
			TacticsDataHolder.getInstance().setCurrentTacticProblem(savedTacticProblem);
			TacticsDataHolder.getInstance().setTacticsBatch(tacticBatch);

			if (getTacticItem() == null) {
				getTacticFromBatch();
			}
		}
	}

	private class DbTacticBatchSaveListener extends AbstractUpdateListener<TacticItem> {
		public DbTacticBatchSaveListener() {
			super(getContext());
		}

		@Override
		public void updateData(TacticItem returnedObj) {
			super.updateData(returnedObj);

			String userName = AppData.getUserName(getMeContext());
			if (AppData.isGuest(getMeContext())) { // for guest mode we should have different name
				userName = StaticData.SYMBOL_EMPTY;
			}
			preferencesEditor.putBoolean(userName + AppConstants.SAVED_TACTICS_BATCH, true);
			preferencesEditor.commit();
		}

	}


	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(FIRST_TACTICS_TAG)) { // Cancel
			cancelTacticAndLeave();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
			cancelTacticAndLeave();
		}
		super.onNegativeBtnClick(fragment);
	}

	private TacticItem getTacticItem() {
		return TacticsDataHolder.getInstance().getTactic();
	}

	private List<TacticItem> getTacticsBatch() {
		return TacticsDataHolder.getInstance().getTacticsBatch();
	}

	private int getCurrentProblem() {
		return TacticsDataHolder.getInstance().getCurrentTacticProblem();
	}

	private void cancelTacticAndLeave() {
		getBoardFace().setTacticCanceled(true);
		clearSavedTactics();
		onBackPressed();
	}

	private void clearSavedTactics() {
		String userName = AppData.getUserName(this);
		if (AppData.isGuest(this)) { // for guest mode we should have different name
			userName = StaticData.SYMBOL_EMPTY; // w/o userName
		}

		preferencesEditor.putString(userName + AppConstants.SAVED_TACTICS_ITEM, StaticData.SYMBOL_EMPTY);
		preferencesEditor.putString(userName + AppConstants.SAVED_TACTICS_RESULT_ITEM, StaticData.SYMBOL_EMPTY);
		preferencesEditor.putInt(userName + AppConstants.SPENT_SECONDS_TACTICS, 0);
		preferencesEditor.putLong(userName + AppConstants.SAVED_TACTICS_ID, 0);
		preferencesEditor.putInt(userName + AppConstants.SAVED_TACTICS_CURRENT_PROBLEM, 0);
		preferencesEditor.putBoolean(userName + AppConstants.SAVED_TACTICS_BATCH, false);
		preferencesEditor.commit();

		// delete tactics batch from DB
		String[] arguments = new String[]{AppData.getUserName(this)};
		getContentResolver().delete(DBConstants.TACTICS_BATCH_CONTENT_URI, DBDataManager.SELECTION_TACTIC_BATCH_USER, arguments);
	}

}
