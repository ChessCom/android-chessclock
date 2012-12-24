package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.TacticItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.SaveTacticsBatchTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.model.TacticItemOld;
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
	private static final int CORRECT_RESULT = 0;
	private static final int WRONG_RESULT = 1;
	private static final int GET_TACTIC = 2;

	private TextView timerTxt;
	private Handler tacticsTimer;
	private ChessBoardTacticsView boardView;

	private boolean noInternet;
	private boolean firstRun = true;

	private TacticsUpdateListener getTacticsUpdateListener;
	private TacticsUpdateListener tacticsCorrectUpdateListener;
	private TacticsUpdateListener tacticsWrongUpdateListener;
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
    private TacticItem.TacticsData tacticItem;
    private boolean offlineBatchWasLoaded;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_tactics);

		widgetsInit();
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
//		boardView.setBoardFace(chessBoard);
		boardView.setGameActivityFace(GameTacticsScreenActivity.this);


		timerTxt = (TextView) findViewById(R.id.timerTxt);
		timerTxt.setVisibility(View.VISIBLE);

		whitePlayerLabel.setVisibility(View.GONE);
		blackPlayerLabel.setVisibility(View.GONE);

		gamePanelView.hideChatButton();
		gamePanelView.enableGameControls(false);
	}

	private void init() {
		tacticsTimer = new Handler();
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.showanswer),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		getTacticsUpdateListener = new TacticsUpdateListener(GET_TACTIC);
		tacticsCorrectUpdateListener = new TacticsUpdateListener(CORRECT_RESULT);
		tacticsWrongUpdateListener = new TacticsUpdateListener(WRONG_RESULT);
		dbTacticBatchSaveListener = new DbTacticBatchSaveListener();
	}

	@Override
	protected void onStart() {
		init();

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

			if (DBDataManager.haveSavedTacticGame(this)) {
                // TODO load tactic item from batch
                tacticItem = DBDataManager.getLastTacticItemFromDb(this);
				setTacticToBoard(tacticItem);

				if (getBoardFace().isLatestMoveMadeUser()) {
					verifyMove();
				}

			} else {
				popupItem.setPositiveBtnId(R.string.yes);
				popupItem.setNegativeBtnId(R.string.no);
				showPopupDialog(R.string.ready_for_first_tackics_q, FIRST_TACTICS_TAG);
			}
		} else {
			// TODO show register confirmation dialog
            if (!tacticItem.isStop() && getBoardFace().getMovesCount() > 0) {
				tacticItem.setRetry(true);

				invalidateGameScreen();
				getBoardFace().takeBack();
				boardView.invalidate();
				playLastMoveAnimationAndCheck();
			} else if(tacticItem.isStop()) {
				startTacticsTimer(tacticItem);
				timerTxt.setText(getString(R.string.timer_, tacticItem.getSecondsSpentStr()));
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
            DBDataManager.saveTacticItemToDb(this, tacticItem);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		releaseResources();
	}

	/**
	 * Check if tactic was canceled or limit reached
	 *
	 * @return true if need to Save
	 */
	private boolean needToSaveTactic() {
		return !getBoardFace().isTacticCanceled()
                && !TacticsDataHolder.getInstance().isTacticLimitReached()
				&& tacticItemIsValid();
	}

	private void playLastMoveAnimationAndCheck() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				getBoardFace().takeNext();
				invalidateGameScreen();

				if (getBoardFace().isLatestMoveMadeUser())
					verifyMove();
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
		return tacticItemIsValid();
	}

	@Override
	public TacticBoardFace getBoardFace() {
		return ChessBoardTactics.getInstance(this);
	}

	@Override
	public void verifyMove() {

		noInternet = !AppUtils.isNetworkAvailable(getContext());
		final boolean userIsGuest = AppData.isGuest(this);

		TacticBoardFace boardFace = getBoardFace();

		if (boardFace.lastTacticMoveIsCorrect()) {
			boardFace.increaseTacticsCorrectMoves();

			if (boardFace.getMovesCount() < boardFace.getTacticMoves().length - 1) { // if it's not last move, make comp move
				boardFace.updateMoves(boardFace.getTacticMoves()[boardFace.getHply()], true);
				invalidateGameScreen();
			} else {
				if (tacticItem.isWasShowed()) {
					showSolvedTacticPopup(getString(R.string.problem_solved_), false);

				} else if (userIsGuest || tacticItem.isRetry() || noInternet) {

					String title;
					if (tacticItem.getResultItem() != null && !userIsGuest) {
						title = getString(R.string.problem_solved, tacticItem.getResultItem().getUserRatingChange(),
                                tacticItem.getResultItem().getUserRating());
					} else {
						title = getString(R.string.problem_solved_);
					}

					showSolvedTacticPopup(title, false);
				} else {

/*
tacticsId		\d+	true	Tactics ID.
passed			0|1	true	1 or 0 if `tacticsId` is present.
correctMoves	\d+	true	required if `tacticsId` is present and `passed` is `0`.
seconds			\d+	true	Required if `tacticsId` is present.
encodedMoves	0|1	true	Encoded moves. Default is `0`.
	 */
					LoadItem loadItem = new LoadItem();
//					loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
					loadItem.setLoadPath(RestHelper.CMD_TACTIC_TRAINER);
					loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
					loadItem.addRequestParams(RestHelper.P_TACTICS_ID, tacticItem.getId());
					loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_TRUE);
					loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, boardFace.getTacticsCorrectMoves());
					loadItem.addRequestParams(RestHelper.P_SECONDS, tacticItem.getSecondsSpent());
					loadItem.addRequestParams(RestHelper.P_ENCODED_MOVES, RestHelper.V_FALSE);

//					new GetStringObjTask(tacticsCorrectUpdateListener).executeTask(loadItem);
					new RequestJsonTask<TacticItem>(tacticsCorrectUpdateListener).executeTask(loadItem);
					gamePanelView.enableGameControls(false);
				}
				stopTacticsTimer();
			}
		} else {
			boolean tacticResultItemIsValid = tacticItem.getResultItem() != null
					&& tacticItem.getResultItem().getUserRatingChange() < 0; // if saved for wrong move. Note that after loading next tactic result is automaically assigns as a positive resultItem.

			if (userIsGuest) {
				showWrongMovePopup(getString(R.string.wrong_ex));
			} else if (tacticResultItemIsValid && (tacticItem.isRetry() || noInternet)) {
				String title = getString(R.string.wrong_score,
                        tacticItem.getResultItem().getUserRatingChange(),
                        tacticItem.getResultItem().getUserRating());
				showWrongMovePopup(title);
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_TACTICS_ID, tacticItem.getId());
				loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_FALSE);
				loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getTacticsCorrectMoves());
				loadItem.addRequestParams(RestHelper.P_SECONDS, tacticItem.getSecondsSpent());

//				new GetStringObjTask(tacticsWrongUpdateListener).executeTask(loadItem);
				new RequestJsonTask<TacticItem>(tacticsWrongUpdateListener).executeTask(loadItem);
				gamePanelView.enableGameControls(false);
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
		if (!tacticItemIsValid()) {
			return null;
		} else {
			return tacticItem.getId();
		}
	}

	private void showLimitDialog() {
		FlurryAgent.logEvent(FlurryData.TACTICS_DAILY_LIMIT_EXCEEDED);
		showSolvedTacticPopup(StaticData.SYMBOL_EMPTY, true);
	}

	private void getNextTactic() {
		handler.removeCallbacks(showTacticMoveTask);

		if (tacticItemIsValid()) {
			String[] arguments = new String[]{String.valueOf(tacticItem.getId()), tacticItem.getUser()};
			getContentResolver().delete(DBConstants.TACTICS_BATCH_CONTENT_URI,
					DBDataManager.SELECTION_TACTIC_ID_AND_USER, arguments);
		}

        if (DBDataManager.haveSavedTacticGame(this)){

            tacticItem = DBDataManager.getLastTacticItemFromDb(this);

            setTacticToBoard(tacticItem);
            currentTacticAnswerCnt = 0;
        } else {
            loadNewTacticsBatch();
        }
	}


//	private class GetTacticsUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			String[] tmp = returnedObj.trim().split(RestHelper.SYMBOL_ITEM_SPLIT);
//			if (tmp.length < 2) {
//				showLimitDialog();   // This is also wrong step, because we should never reach this condition
//				return;
//			}
//		}
//
//		@Override
//		public void errorHandle(Integer resultCode) {
//			handleErrorRequest();
//		}
//	}

	private void showAnswer() {
		stopTacticsTimer();

        tacticItem.setWasShowed(true);

		ChessBoardTactics.resetInstance();
//		boardView.setBoardFace(ChessBoardTactics.getInstance(this));
		boardView.setGameActivityFace(GameTacticsScreenActivity.this);

		tacticItem.setRetry(true);

		getBoardFace().setupBoard(tacticItem.getInitialFen());

		if (tacticItem.getCleanMoveString().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			getBoardFace().setTacticMoves(tacticItem.getCleanMoveString());
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

//	private class TacticsUpdateListener extends ChessUpdateListener {
	private class TacticsUpdateListener extends ActionBarUpdateListener<TacticItem> {
		private int listenerCode;

		private TacticsUpdateListener(int listenerCode) {
			super(getInstance(), TacticItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(TacticItem returnedObj) {
//			String[] tmp = returnedObj.getData().split(RestHelper.SYMBOL_ITEM_SPLIT);

//			if (tmp.length < 2) { // "Success+||"   - means we reached limit and there is no tactics
			if (returnedObj.getCount() == 0) { // "Success+||"   - means we reached limit and there is no tactics
				showLimitDialog(); // limit dialog should be shown after updating tactic, while getting new
				return;
			}

			switch (listenerCode){
				case GET_TACTIC:
//					int count = tmp.length - 1;
//					List<TacticItem.TacticsData> tacticBatch = new ArrayList<TacticItem.TacticsData>(count);
//					for (int i = 1; i <= count; i++) {
//						TacticItemOld tacticItem = new TacticItemOld(tmp[i].split(StaticData.SYMBOL_COLON));
//						tacticItem.setUser(AppData.getUserName(getContext()));
//						tacticBatch.add(tacticItem);
//					}

					new SaveTacticsBatchTask(dbTacticBatchSaveListener, returnedObj.getData(),
							getContentResolver()).executeTask();
					break;
				case CORRECT_RESULT:
//					if (!tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)) { // means we sent duplicate tactic_id, so result is the same
//						tacticItem.setResultItem(tmp[1].split(RestHelper.SYMBOL_PARAMS_SPLIT));
//					}
//
//					String title;
//					if (tacticItem.getResultItem() != null) {
//						title = getString(R.string.problem_solved, tacticItem.getResultItem().getUserRatingChange(),
//								tacticItem.getResultItem().getUserRating());
//					}else {
//						title = getString(R.string.problem_solved_);
//					}
//
//					showSolvedTacticPopup(title, false);

					break;
				case WRONG_RESULT:
//					if (!tmp[1].trim().equals(StaticData.SYMBOL_EMPTY)) { // means we sent duplicate tactic_id, so result is the same
//						tacticItem.setResultItem(tmp[1].split(RestHelper.SYMBOL_PARAMS_SPLIT));
//					}
//
//					if (tacticItem.getResultItem() != null) {
//						title = getString(R.string.wrong_score, tacticItem.getResultItem().getUserRatingChange(),
//								tacticItem.getResultItem().getUserRating());
//					} else {
//						title = getString(R.string.wrong_ex);
//					}
//
//					showWrongMovePopup(title);

					tacticItem.setRetry(true); // set auto retry because we save tactic

					break;
			}
			gamePanelView.enableGameControls(true);
		}

		@Override
		public void errorHandle(String resultMessage) {
			if (listenerCode == GET_TACTIC) {
				if (resultMessage.equals(RestHelper.R_TACTICS_LIMIT_REACHED)) {
					showLimitDialog();  // This should be the only way to show limit dialog for registered user
				} else {
					showSinglePopupDialog(resultMessage);
				}
			}
		}


		@Override
		public void errorHandle(Integer resultCode) {
			handleErrorRequest();
		}
	}

	private void handleErrorRequest() {
		gamePanelView.enableGameControls(true);

		noInternet = true;
		showPopupDialog(R.string.offline_mode, R.string.no_network_rating_not_changed, OFFLINE_RATING_TAG);
		loadOfflineTacticsBatch(); // There is a case when you connected to wifi, but no internet connection over it.
	}

//	private class TacticsWrongUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//		}
//
//		@Override
//		public void errorHandle(Integer resultCode) {
//			handleErrorRequest();
//		}
//	}


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
		getNextTactic();
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
		private final int TACTICS_SHOW_ANSWER = 0;
		private final int TACTICS_SETTINGS = 1;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i) {
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
        if (tacticItemIsValid()) {
            tacticItem.setStop(true);
        }

		tacticsTimer.removeCallbacks(timerUpdateTask);
	}

	public void startTacticsTimer(TacticItem.TacticsData tacticItem) {
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

            tacticItem.increaseSecondsPassed();
			timerTxt.setText(getString(R.string.timer_, tacticItem.getSecondsSpentStr()));
		}
	};

	@Override
	protected void restoreGame() {
		if (!tacticItemIsValid()) {
			return;
		}

		if (tacticItem != null && tacticItem.isStop()) {
			openOptionsMenu();
			return;
		}

		setTacticToBoard(tacticItem);
	}

	private void setTacticToBoard(TacticItem.TacticsData tacticItem) {
		if (!tacticItemIsValid()) { // just in case something weird happen :)
			return;
		}

		ChessBoardTactics.resetInstance();
		final TacticBoardFace boardFace = ChessBoardTactics.getInstance(this);
//		boardView.setBoardFace(boardFace);
		boardView.setGameActivityFace(GameTacticsScreenActivity.this);


		boardFace.setupBoard(tacticItem.getInitialFen());

		if (tacticItem.getCleanMoveString().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			boardFace.setTacticMoves(tacticItem.getCleanMoveString());
			boardFace.setMovesCount(1);
		}

		startTacticsTimer(tacticItem);

		boardFace.updateMoves(boardFace.getTacticMoves()[0], true);

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

        firstRun = false;
		gamePanelView.enableGameControls(true);
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
				getNextTactic();
			}
		} else if (view.getId() == R.id.stopBtn) {
			boardView.setFinished(true);
			tacticItem.setStop(true);
			stopTacticsTimer();
			dismissDialogs();
		} else if (view.getId() == R.id.retryBtn) {

			if (AppData.isGuest(this) || noInternet) {
				getNextTactic();
			} else {
				setTacticToBoard(tacticItem);
			}
            tacticItem.setRetry(true);
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
			onBackPressed();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
			getNextTactic();
		}
		super.onPositiveBtnClick(fragment);
	}


	private void loadNewTacticsBatch() {
		noInternet = !AppUtils.isNetworkAvailable(this);
		if (AppData.isGuest(this) || noInternet) {
			loadOfflineTacticsBatch();
		} else {

			LoadItem loadItem = new LoadItem();
//			loadItem.setLoadPath(RestHelper.GET_TACTICS_PROBLEM_BATCH);
			loadItem.setLoadPath(RestHelper.CMD_TACTICS);
//			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_IS_INSTALL, RestHelper.V_ZERO);

//			new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);
			new RequestJsonTask<TacticItem>(getTacticsUpdateListener).executeTask(loadItem);
			gamePanelView.enableGameControls(false);
		}
	}

	private void loadOfflineTacticsBatch() {
        if (offlineBatchWasLoaded) {
            if (AppData.isGuest(this)) {
				showPopupDialog(R.string.ten_tactics_completed, TEN_TACTICS_TAG);
				getLastPopupFragment().setButtons(1);
                return;
			}
        }
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
			String[] tmp = input.split(RestHelper.SYMBOL_ITEM_SPLIT);
			int count = tmp.length - 1;
			                       // TODO restore with predefined values
//			List<TacticItem.TacticsData> tacticBatch = new ArrayList<TacticItem.TacticsData>(count);
//			for (int i = 1; i <= count; i++) {
//				TacticItemOld tacticItem = new TacticItemOld(tmp[i].split(RestHelper.SYMBOL_PARAMS_SPLIT));
//				tacticItem.setUser(DBDataManager.getUserName(getContext()));
//				tacticBatch.add(tacticItem);
//			}
//
//            new SaveTacticsBatchTask(dbTacticBatchSaveListener, tacticBatch,
//					getContentResolver()).executeTask();
            offlineBatchWasLoaded = true;

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class DbTacticBatchSaveListener extends AbstractUpdateListener<TacticItem.TacticsData> {
		public DbTacticBatchSaveListener() {
			super(getContext());
		}

		@Override
		public void updateData(TacticItem.TacticsData returnedObj) {
            getNextTactic();
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

	private void cancelTacticAndLeave() {
		getBoardFace().setTacticCanceled(true);
		clearSavedTactics();
		onBackPressed();
	}

	private void clearSavedTactics() {
        if (tacticItemIsValid()){
            String[] arguments = new String[]{String.valueOf(tacticItem.getId()), tacticItem.getUser()};
            getContentResolver().delete(DBConstants.TACTICS_BATCH_CONTENT_URI,
                    DBDataManager.SELECTION_TACTIC_ID_AND_USER, arguments);
        }
	}

    private boolean tacticItemIsValid() {
        return tacticItem != null;
    }

	private void releaseResources() {
//		tacticsTimer = null;
		inflater = null;

		getTacticsUpdateListener.releaseContext();
		getTacticsUpdateListener = null;
		tacticsCorrectUpdateListener.releaseContext();
		tacticsCorrectUpdateListener = null;
		tacticsWrongUpdateListener.releaseContext();
		tacticsWrongUpdateListener = null;
		dbTacticBatchSaveListener.releaseContext();
		dbTacticBatchSaveListener = null;
	}


}
