package com.chess.ui.fragments;

import android.app.Activity;
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
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.TacticInfoItem;
import com.chess.backend.entity.new_api.TacticItem;
import com.chess.backend.entity.new_api.TacticRatingData;
import com.chess.backend.entity.new_api.stats.UserStatsItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetOfflineTacticsBatchTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.SaveTacticsBatchTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.ui.activities.PreferencesScreenActivity;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.interfaces.GameTacticsActivityFace;
import com.chess.ui.interfaces.TacticBoardFace;
import com.chess.ui.popup_fragments.BasePopupDialogFragment;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.views.ChessBoardTacticsView;
import com.chess.ui.views.ControlsTacticsView;
import com.chess.ui.views.PanelInfoTacticsView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;
import com.flurry.android.FlurryAgent;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.02.13
 * Time: 7:10
 */
public class GameTacticsFragment extends GameBaseFragment implements GameTacticsActivityFace {

	private static final int TIMER_UPDATE = 1000;
	private static final long TACTIC_ANSWER_DELAY = 1500;
	private static final int CORRECT_RESULT = 0;
	private static final int WRONG_RESULT = 1;
	private static final int GET_TACTIC = 2;

	private Handler tacticsTimer;
	private ChessBoardTacticsView boardView;

	private boolean noInternet;
	private boolean firstRun = true;

	private TacticsUpdateListener getTacticsUpdateListener;
	private TacticsInfoUpdateListener tacticsCorrectUpdateListener;
	private TacticsInfoUpdateListener tacticsWrongUpdateListener;
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
	private TacticItem.Data tacticItem;
	private boolean offlineBatchWasLoaded;
	private PanelInfoTacticsView topPanelView;
	private ControlsTacticsView controlsTacticsView;
	private int currentRating;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_tactics, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.tactics);
		topPanelView = (PanelInfoTacticsView) view.findViewById(R.id.topPanelView);

		widgetsInit(view);
	}

	@Override
	protected void widgetsInit(View view) {
		controlsTacticsView = (ControlsTacticsView) view.findViewById(R.id.controlsTacticsView);

		boardView = (ChessBoardTacticsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsTacticsView);
		boardView.setGameActivityFace(this);

		controlsTacticsView.setBoardViewFace(boardView);

		setBoardView(boardView);

		final ChessBoard chessBoard = ChessBoardTactics.getInstance(this);
		firstRun = chessBoard.isJustInitialized();
		boardView.setGameActivityFace(this);

		controlsTacticsView.enableGameControls(false);
	}


	@Override
	public void onStart() {
//		init();

		super.onStart();
		if (!AppData.isGuest(getActivity())) {
			FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED);
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		dismissDialogs();

		if (firstRun) {

			if (DBDataManager.haveSavedTacticGame(getActivity())) {
				// TODO load tactic item from batch
				tacticItem = DBDataManager.getLastTacticItemFromDb(getActivity());
				setTacticToBoard(tacticItem);

				if (getBoardFace().isLatestMoveMadeUser()) {
					verifyMove();
				}

			} else {
				popupItem.setPositiveBtnId(R.string.yes);
				popupItem.setNegativeBtnId(R.string.no);
				showPopupDialog(R.string.ready_for_first_tactics_q, FIRST_TACTICS_TAG);
			}
		} else {
			if (!tacticItem.isStop() && getBoardFace().getMovesCount() > 0) {
				tacticItem.setRetry(true);

				invalidateGameScreen();
				getBoardFace().takeBack();
				boardView.invalidate();
				playLastMoveAnimationAndCheck();
			} else if (tacticItem.isStop() && !getBoardFace().isFinished()) {
				startTacticsTimer(tacticItem);
				topPanelView.setPlayerTimeLeft(tacticItem.getSecondsSpentStr());
			} else {
				verifyMove();
			}
		}
	}

	private void init() {
		tacticsTimer = new Handler();
		inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.show_answer),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		getTacticsUpdateListener = new TacticsUpdateListener(GET_TACTIC);
		tacticsCorrectUpdateListener = new TacticsInfoUpdateListener(CORRECT_RESULT);
		tacticsWrongUpdateListener = new TacticsInfoUpdateListener(WRONG_RESULT);
		dbTacticBatchSaveListener = new DbTacticBatchSaveListener();
	}


	@Override
	protected void dismissDialogs() {
		if (findFragmentByTag(WRONG_MOVE_TAG) != null) {
			((BasePopupDialogFragment) findFragmentByTag(WRONG_MOVE_TAG)).dismiss();
		}
		if (findFragmentByTag(TACTIC_SOLVED_TAG) != null) {
			((BasePopupDialogFragment) findFragmentByTag(TACTIC_SOLVED_TAG)).dismiss();
		}
	}

	@Override
	public void onPause() {
		dismissDialogs();
		super.onPause();

		stopTacticsTimer();

		if (needToSaveTactic()) {
			DBDataManager.saveTacticItemToDb(getActivity(), tacticItem);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
//		releaseResources();
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
		final boolean userIsGuest = AppData.isGuest(getActivity());

		TacticBoardFace boardFace = getBoardFace();

		if (boardFace.lastTacticMoveIsCorrect()) {
			boardFace.increaseTacticsCorrectMoves();

			if (boardFace.getMovesCount() < boardFace.getTacticMoves().length - 1) { // if it's not last move, make comp move
				boardFace.updateMoves(boardFace.getTacticMoves()[boardFace.getHply()], true);
				invalidateGameScreen();
			} else { // correct
				String newRatingStr = StaticData.SYMBOL_EMPTY;
				if (tacticItem.isWasShowed()) {
					newRatingStr = getString(R.string.score_arg, tacticItem.getPositiveScore());
					showCorrect(newRatingStr);
				} else if (userIsGuest || tacticItem.isRetry() || noInternet) {
					if (/*tacticItem.getResultItem() != null &&*/ !userIsGuest) {
						newRatingStr = getString(R.string.score_arg, tacticItem.getPositiveScore());
					}

//					showSolvedTacticPopup(title, false);
					showCorrect(newRatingStr);
				} else {

					LoadItem loadItem = new LoadItem();
//					loadItem.setLoadPath(RestHelper.TACTICS_TRAINER);
					loadItem.setLoadPath(RestHelper.CMD_TACTIC_TRAINER);
					loadItem.setRequestMethod(RestHelper.POST);
					loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
					loadItem.addRequestParams(RestHelper.P_TACTICS_ID, tacticItem.getId());
					loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_TRUE);
					loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, boardFace.getTacticsCorrectMoves());
					loadItem.addRequestParams(RestHelper.P_SECONDS, tacticItem.getSecondsSpent());
					loadItem.addRequestParams(RestHelper.P_ENCODED_MOVES, RestHelper.V_FALSE);

//					new GetStringObjTask(tacticsCorrectUpdateListener).executeTask(loadItem);
					new RequestJsonTask<TacticInfoItem>(tacticsCorrectUpdateListener).executeTask(loadItem);
					controlsTacticsView.enableGameControls(false);
				}
				stopTacticsTimer();
			}
		} else {
			boolean tacticResultItemIsValid = tacticItem.getResultItem() != null
					&& tacticItem.getResultItem().getUserRatingChange() < 0; // if saved for wrong move. Note that after loading next tactic result is automatically assigns as a positive resultItem.

			String newRatingStr;
			if (userIsGuest) {
				newRatingStr = getString(R.string.score_arg, tacticItem.getNegativeScore());
				showWrong(newRatingStr);
			} else if (tacticResultItemIsValid && (tacticItem.isRetry() || noInternet)) {
				newRatingStr = getString(R.string.score_arg, tacticItem.getNegativeScore());
//				showWrongMovePopup(title);

				showWrong(newRatingStr);
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_TACTIC_TRAINER);
				loadItem.setRequestMethod(RestHelper.POST);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_TACTICS_ID, tacticItem.getId());
				loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_FALSE);
				loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getTacticsCorrectMoves());
				loadItem.addRequestParams(RestHelper.P_SECONDS, tacticItem.getSecondsSpent());

//				new GetStringObjTask(tacticsWrongUpdateListener).executeTask(loadItem);
				new RequestJsonTask<TacticInfoItem>(tacticsWrongUpdateListener).executeTask(loadItem);
				controlsTacticsView.enableGameControls(false);
			}
			stopTacticsTimer();
		}
	}

	@Override
	public void showHelp() {
		// TODO show help
	}

	@Override
	public void showStats() {
		getActivityFace().openFragment(new TacticsStatsFragment());
	}

	private void showWrongMovePopup(String title) {
		LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_tactic_incorrect, null, false);

		((TextView) customView.findViewById(R.id.titleTxt)).setText(title);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		PopupCustomViewFragment customViewFragment = PopupCustomViewFragment.newInstance(popupItem);
		customViewFragment.show(getFragmentManager(), WRONG_MOVE_TAG);


		customView.findViewById(R.id.retryBtn).setOnClickListener(this);
		customView.findViewById(R.id.stopBtn).setOnClickListener(this);
		customView.findViewById(R.id.solutionBtn).setOnClickListener(this);
		customView.findViewById(R.id.nextBtn).setOnClickListener(this);
	}

	private void showSolvedTacticPopup(String title, boolean limitReached) {
		TacticsDataHolder.getInstance().setTacticLimitReached(limitReached);

		LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_tactic_solved, null, false);

		LinearLayout adViewWrapper = (LinearLayout) customView.findViewById(R.id.adview_wrapper);
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.showRectangleAd(adViewWrapper, getActivity());
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

		PopupCustomViewFragment customViewFragment = PopupCustomViewFragment.newInstance(popupItem);
		customViewFragment.show(getFragmentManager(), TACTIC_SOLVED_TAG);
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
			getContentResolver().delete(DBConstants.uriArray[DBConstants.TACTICS_BATCH],
					DBDataManager.SELECTION_TACTIC_ID_AND_USER, arguments);
		}

		if (DBDataManager.haveSavedTacticGame(getActivity())) {

			tacticItem = DBDataManager.getLastTacticItemFromDb(getActivity());

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


	@Override
	public void showHint() {
		showAnswer();
	}

	private void showAnswer() {
		stopTacticsTimer();

		tacticItem.setWasShowed(true);

		ChessBoardTactics.resetInstance();
//		boardView.setBoardFace(ChessBoardTactics.getInstance(this));
		boardView.setGameActivityFace(this);

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

	private class TacticsUpdateListener extends ActionBarUpdateListener<TacticItem> {
		private int listenerCode;

		private TacticsUpdateListener(int listenerCode) {
			super(getInstance(), TacticItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(TacticItem returnedObj) {
			switch (listenerCode) {
				case GET_TACTIC:
					new SaveTacticsBatchTask(dbTacticBatchSaveListener, returnedObj.getData(),
							getContentResolver()).executeTask();
					break;
			}
			controlsTacticsView.enableGameControls(true);
		}

		@Override
		public void errorHandle(Integer resultCode) {  // TODO restore
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCode.TACTICS_DAILY_LIMIT_REACHED:
						showLimitDialog();
						break;
				}
			} else {
				if (resultCode == StaticData.NO_NETWORK) {
					handleErrorRequest();
				}
			}
//			if (returnedObj.getCount() == 0) { // "Success+||"   - means we reached limit and there is no tactics
//				showLimitDialog(); // limit dialog should be shown after updating tactic, while getting new
//				return;
//			}
//			if (listenerCode == GET_TACTIC) {
//				if (resultMessage.equals(RestHelper.R_TACTICS_LIMIT_REACHED)) {
//					showLimitDialog();  // This should be the only way to show limit dialog for registered user
//				} else {
//					showSinglePopupDialog(resultMessage);
//				}
//			}
		}
	}

	private class TacticsInfoUpdateListener extends ActionBarUpdateListener<TacticInfoItem> {

		private final int listenerCode;

		public TacticsInfoUpdateListener(int listenerCode) {
			super(getInstance(), TacticInfoItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(TacticInfoItem returnedObj) {
			TacticRatingData tacticResultItem = returnedObj.getData().getRatingInfo();
			if (tacticResultItem != null) {
				tacticResultItem.setId(tacticItem.getId());
				tacticResultItem.setUser(tacticItem.getUser());
				tacticItem.setResultItem(tacticResultItem);
			}
			switch (listenerCode) {
				case CORRECT_RESULT:

					String newRatingStr;

//					if (tacticItem.getResultItem() != null) {
						newRatingStr = getString(R.string.score_arg, tacticItem.getPositiveScore());
						tacticItem.setRetry(true); // set auto retry because we will save tactic
//					} else {
//						newRatingStr = getString(R.string.score_arg);
//					}
//					showSolvedTacticPopup(title, false);
					showCorrect(newRatingStr);

					break;
				case WRONG_RESULT:

//					if (tacticItem.getResultItem() != null) {
						newRatingStr = getString(R.string.score_arg, tacticItem.getNegativeScore());
						tacticItem.setRetry(true); // set auto retry because we will save tactic

//					} else {
//						newRatingStr = getString(R.string.score_arg);
//					}

//					showWrongMovePopup(title);

					showWrong(newRatingStr);

					break;
			}
			controlsTacticsView.enableGameControls(true);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCode.TACTICS_DAILY_LIMIT_REACHED:
						showLimitDialog();
						break;
				}
			} else {
				if (resultCode == StaticData.NO_NETWORK) {
					handleErrorRequest();
				}
			}
		}
	}

	private void showCorrect(String newRatingStr) {
		topPanelView.showCorrect(true, newRatingStr);
		topPanelView.setPlayerScore(tacticItem.getResultItem().getUserRating());
		controlsTacticsView.showCorrect();
		getBoardFace().setFinished(true);
	}

	private void showWrong(String newRatingStr) {
		topPanelView.showWrong(true, newRatingStr);
		topPanelView.setPlayerScore(tacticItem.getResultItem().getUserRating());
		controlsTacticsView.showWrong();
		getBoardFace().setFinished(true);
	}

	private void handleErrorRequest() {
		controlsTacticsView.enableGameControls(true);

		noInternet = true;      // TODO handle button click properly
		showPopupDialog(R.string.offline_mode, R.string.no_network_rating_not_changed, OFFLINE_RATING_TAG);
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
			topPanelView.showPractice(true);
			setTitle(R.string.practice_mode);

		} else {
//			analysisTxt.setVisibility(View.INVISIBLE);
			setTitle(R.string.tactics);
			restoreGame();
		}
		topPanelView.showClock(!isAnalysis);
		controlsTacticsView.showDefault();
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void invalidateGameScreen() {
		boardView.invalidate();
	}

	@Override
	public void newGame() {
		getNextTactic();
//		closeOptionsMenu();
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.game_tactics, menu);
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
//			Toast.makeText(getActivity(), items[i], Toast.LENGTH_SHORT).show();
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

	public void startTacticsTimer(TacticItem.Data tacticItem) {
//		boardView.setFinished(false);
		getBoardFace().setFinished(false);
		tacticItem.setStop(false);

		tacticsTimer.removeCallbacks(timerUpdateTask);
		tacticsTimer.postDelayed(timerUpdateTask, TIMER_UPDATE);
		controlsTacticsView.enableGameControls(true);
	}

	private Runnable timerUpdateTask = new Runnable() {
		@Override
		public void run() {
			tacticsTimer.removeCallbacks(this);
			tacticsTimer.postDelayed(timerUpdateTask, TIMER_UPDATE);

			if (getBoardFace().isAnalysis())
				return;

			tacticItem.increaseSecondsPassed();
			topPanelView.setPlayerTimeLeft(tacticItem.getSecondsSpentStr());
		}
	};

	@Override
	protected void restoreGame() {
		if (!tacticItemIsValid()) {
			return;
		}

		if (tacticItem.isStop()) {
//			openOptionsMenu();
			return;
		}

		setTacticToBoard(tacticItem);
	}

	@Override
	public void restart() {
		setTacticToBoard(tacticItem);
	}

	private void setTacticToBoard(TacticItem.Data tacticItem) {
		if (!tacticItemIsValid()) { // just in case something weird happen :)
			return;
		}

		ChessBoardTactics.resetInstance();
		final TacticBoardFace boardFace = ChessBoardTactics.getInstance(this);
//		boardView.setBoardFace(boardFace);
		boardView.setGameActivityFace(this);

		if (currentRating == 0) {
//			if (tacticItem.getResultItem() == null) {
				currentRating = DBDataManager.getUserTacticsRating(getActivity());
//			} else {
//				currentRating = tacticItem.getResultItem().getUserRating();
//			}
		}

		topPanelView.setPlayerScore(currentRating);

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
		controlsTacticsView.enableGameControls(true);
		controlsTacticsView.showDefault();
		topPanelView.showDefault();

		if (boardFace.getSide() == AppConstants.WHITE_SIDE) {
			topPanelView.setSide(AppConstants.WHITE_SIDE);
		} else {
			topPanelView.setSide(AppConstants.BLACK_SIDE);
		}
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
//			boardView.setFinished(true);
			getBoardFace().setFinished(true);
			tacticItem.setStop(true);
			stopTacticsTimer();
			dismissDialogs();
		} else if (view.getId() == R.id.retryBtn) {

			if (AppData.isGuest(getActivity()) || noInternet) {
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
//				onBackPressed();
				getActivityFace().showPreviousFragment();
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
			currentRating = DBDataManager.getUserTacticsRating(getActivity());

			if (currentRating == 0) {
				// get full users stats
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_USER_STATS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

				new RequestJsonTask<UserStatsItem>(new StatsItemUpdateListener()).executeTask(loadItem);
			} else {
				loadNewTacticsBatch();
			}

		} else if (tag.equals(TEN_TACTICS_TAG)) {
//			onBackPressed();
			getActivityFace().showPreviousFragment();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
//			loadOfflineTacticsBatch(); // There is a case when you connected to wifi, but no internet connection over it.

			getNextTactic();
		}
		super.onPositiveBtnClick(fragment);
	}


	private void loadNewTacticsBatch() {
		noInternet = !AppUtils.isNetworkAvailable(getActivity());
		if (AppData.isGuest(getActivity()) || noInternet) {
			loadOfflineTacticsBatch();
		} else {

			LoadItem loadItem = new LoadItem();
//			loadItem.setLoadPath(RestHelper.GET_TACTICS_PROBLEM_BATCH);
			loadItem.setLoadPath(RestHelper.CMD_TACTICS);
//			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_IS_INSTALL, RestHelper.V_FALSE);

//			new GetStringObjTask(getTacticsUpdateListener).executeTask(loadItem);
			new RequestJsonTask<TacticItem>(getTacticsUpdateListener).executeTask(loadItem);
			controlsTacticsView.enableGameControls(false);
		}
	}

	private void loadOfflineTacticsBatch() {
		if (offlineBatchWasLoaded) {
			if (AppData.isGuest(getActivity())) {
				showPopupDialog(R.string.ten_tactics_completed, TEN_TACTICS_TAG);
				getLastPopupFragment().setButtons(1);
				return;
			}
		}

		new GetOfflineTacticsBatchTask(new DemoTacticsUpdateListener(), getResources()).executeTask(R.raw.tactics10batch_new);
		FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_GUEST);
	}

	private class DemoTacticsUpdateListener extends ActionBarUpdateListener<TacticItem.Data> {

		public DemoTacticsUpdateListener() {
			super(getInstance());
			useList = true;
		}

		@Override
		public void updateListData(List<TacticItem.Data> itemsList) {
			new SaveTacticsBatchTask(dbTacticBatchSaveListener, itemsList,getContentResolver()).executeTask();
			offlineBatchWasLoaded = true;
		}
	}

	private class DbTacticBatchSaveListener extends ActionBarUpdateListener<TacticItem.Data> {
		public DbTacticBatchSaveListener() {
			super(getInstance());
		}

		@Override
		public void updateData(TacticItem.Data returnedObj) {
			getNextTactic();
		}
	}

	private class StatsItemUpdateListener extends ActionBarUpdateListener<UserStatsItem> {

		public StatsItemUpdateListener() {
			super(getInstance(), UserStatsItem.class);
		}

		@Override
		public void updateData(UserStatsItem returnedObj) {
			super.updateData(returnedObj);

			currentRating = returnedObj.getData().getTactics().getCurrent();
//			new SaveUserStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
			loadNewTacticsBatch();
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
//		onBackPressed();
		getActivityFace().showPreviousFragment();
	}

	private void clearSavedTactics() {
		if (tacticItemIsValid()) {
			String[] arguments = new String[]{String.valueOf(tacticItem.getId()), tacticItem.getUser()};
			getContentResolver().delete(DBConstants.uriArray[DBConstants.TACTICS_BATCH],
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
