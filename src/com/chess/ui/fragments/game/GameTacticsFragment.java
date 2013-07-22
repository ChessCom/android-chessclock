package com.chess.ui.fragments.game;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.TacticInfoItem;
import com.chess.backend.entity.new_api.TacticItem;
import com.chess.backend.entity.new_api.TacticRatingData;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetOfflineTacticsBatchTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.SaveTacticsBatchTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.popup_fragments.BasePopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.fragments.stats.TacticsStatsFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.interfaces.game_ui.GameTacticsFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.TacticBoardFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.PanelInfoTacticsView;
import com.chess.ui.views.chess_boards.ChessBoardTacticsView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsTacticsView;
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
public class GameTacticsFragment extends GameBaseFragment implements GameTacticsFace, PopupListSelectionFace {

	private static final int TIMER_UPDATE = 1000;
	private static final long TACTIC_ANSWER_DELAY = 1500;
	private static final int CORRECT_RESULT = 0;
	private static final int WRONG_RESULT = 1;
	private static final int GET_TACTIC = 2;
	// Quick action ids
	private static final int ID_NEXT_TACTIC = 0;
	private static final int ID_SHOW_ANSWER = 1;
	private static final int ID_PRACTICE = 2;
	private static final int ID_SETTINGS = 3;


	private Handler tacticsTimer;
	private ChessBoardTacticsView boardView;

	private boolean noNetwork;
	private boolean firstRun = true;

	private GetTacticsUpdateListener getTacticsUpdateListener;
	private TacticsInfoUpdateListener tacticsCorrectUpdateListener;
	private TacticsInfoUpdateListener tacticsWrongUpdateListener;
	private DbTacticBatchSaveListener dbTacticBatchSaveListener;

	private static final String FIRST_TACTICS_TAG = "first tactics";
	private static final String TEN_TACTICS_TAG = "ten tactics reached";
	private static final String OFFLINE_RATING_TAG = "tactics offline rating";
	private static final String TACTIC_SOLVED_TAG = "tactic solved popup";
	private static final String WRONG_MOVE_TAG = "wrong move popup";

	private LayoutInflater inflater;
	private int currentTacticAnswerCnt;
	private int maxTacticAnswerCnt;
	private TacticItem.Data tacticItem;
	private PanelInfoTacticsView topPanelView;
	private ControlsTacticsView controlsTacticsView;
	private int currentRating;
	private boolean isAnalysis;
	private boolean serverError;
	private boolean userSawOfflinePopup;
	private ImageUpdateListener imageUpdateListener;
	private ImageView topAvatarImg;
	private ImageDownloaderToListener imageDownloader;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_tactics_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.tactics);

		widgetsInit(view);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED);
	}

	@Override
	public void onResume() {
		super.onResume();

		dismissDialogs();

		if (firstRun) {

			if (DBDataManager.haveSavedTacticGame(getActivity(), getUsername())) {
				// TODO load tactic item from batch
				tacticItem = DBDataManager.getLastTacticItemFromDb(getActivity(), getUsername());
				adjustBoardForGame();

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

	@Override
	public void onPause() {
		dismissDialogs();
		super.onPause();

		stopTacticsTimer();

		if (needToSaveTactic()) {
			DBDataManager.saveTacticItemToDb(getActivity(), tacticItem, getUsername());
		}
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


	/**
	 * Check if tactic was canceled or limit reached
	 *
	 * @return true if need to Save
	 */
	private boolean needToSaveTactic() {
		return !getBoardFace().isTacticCanceled()
				&& !TacticsDataHolder.getInstance().isTacticLimitReached()
				&& currentGameExist();
	}

	private void playLastMoveAnimationAndCheck() {
		Move move = getBoardFace().getNextMove();
		if (move == null) {
			return;
		}
		boardView.setMoveAnimator(move, true);
		getBoardFace().takeNext();
		invalidateGameScreen();

		if (getBoardFace().isLatestMoveMadeUser())
			verifyMove();
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
		return tacticItem != null;
	}

	@Override
	public TacticBoardFace getBoardFace() {
		return ChessBoardTactics.getInstance(this);
	}

	@Override
	public void verifyMove() {

		noNetwork = !AppUtils.isNetworkAvailable(getContext());

		TacticBoardFace boardFace = getBoardFace();

		if (boardFace.lastTacticMoveIsCorrect()) {
			boardFace.increaseTacticsCorrectMoves();

			if (boardFace.getMovesCount() < boardFace.getTacticMoves().length - 1) { // if it's not last move, make comp move
				final Move move = boardFace.convertMove(boardFace.getTacticMoves()[boardFace.getHply()]);
				boardView.setMoveAnimator(move, true);
				boardFace.makeMove(move, true);
				invalidateGameScreen();
			} else { // correct
				if (tacticItem.isWasShowed()) {
					sendWrongResult();
				} else if (tacticItem.isRetry() || noNetwork) {
					String newRatingStr = StaticData.SYMBOL_EMPTY;
					if (tacticItem.getResultItem() != null) {
						newRatingStr = tacticItem.getPositiveScore();
					}
					showCorrect(newRatingStr);
				} else {
					sendCorrectResult();
				}
				stopTacticsTimer();
			}
		} else {
			if (tacticItem.isRetry() || noNetwork) {
				String newRatingStr = StaticData.SYMBOL_EMPTY;
				if (tacticItem.getResultItem() != null) {
					newRatingStr = tacticItem.getNegativeScore();
				}
				showWrong(newRatingStr);
			} else {
				sendWrongResult();
			}
			stopTacticsTimer();
		}
	}

	private void sendCorrectResult() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_TACTIC_TRAINER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_TACTICS_ID, tacticItem.getId());
		loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_TRUE);
		loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getTacticsCorrectMoves());
		loadItem.addRequestParams(RestHelper.P_SECONDS, tacticItem.getSecondsSpent());
		loadItem.addRequestParams(RestHelper.P_ENCODED_MOVES, RestHelper.V_FALSE);

		new RequestJsonTask<TacticInfoItem>(tacticsCorrectUpdateListener).executeTask(loadItem);
		controlsTacticsView.enableGameControls(false);
	}

	private void sendWrongResult() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_TACTIC_TRAINER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_TACTICS_ID, tacticItem.getId());
		loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_FALSE);
		loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getTacticsCorrectMoves());
		loadItem.addRequestParams(RestHelper.P_SECONDS, tacticItem.getSecondsSpent());

		new RequestJsonTask<TacticInfoItem>(tacticsWrongUpdateListener).executeTask(loadItem);
		controlsTacticsView.enableGameControls(false);
	}

	@Override
	public void showHelp() {
		// TODO show help
	}

	@Override
	public void showStats() {
		getActivityFace().openFragment(new TacticsStatsFragment());
	}

	private void showLimitReachedPopup() {
		FlurryAgent.logEvent(FlurryData.TACTICS_DAILY_LIMIT_EXCEEDED);

		TacticsDataHolder.getInstance().setTacticLimitReached(true);

		LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_tactic_limit_reached, null, false);

		LinearLayout adViewWrapper = (LinearLayout) customView.findViewById(R.id.adview_wrapper);
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		} else {
			adViewWrapper.setVisibility(View.GONE);
		}

		clearSavedTactics();

		customView.findViewById(R.id.cancelBtn).setOnClickListener(this);
//		customView.findViewById(R.id.upgradeBtn).setOnClickListener(this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		PopupCustomViewFragment customViewFragment = PopupCustomViewFragment.createInstance(popupItem);
		customViewFragment.show(getFragmentManager(), TACTIC_SOLVED_TAG);
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;
	}

	@Override
	public Long getGameId() {
		if (!currentGameExist()) {
			return null;
		} else {
			return tacticItem.getId();
		}
	}

	private void getNextTactic() {
		handler.removeCallbacks(showTacticMoveTask);

		if (currentGameExist()) {
			String[] arguments = new String[]{String.valueOf(tacticItem.getId()), tacticItem.getUser()};
			getContentResolver().delete(DBConstants.uriArray[DBConstants.TACTICS_BATCH],
					DBDataManager.SELECTION_ITEM_ID_AND_USER, arguments);
		}

		if (DBDataManager.haveSavedTacticGame(getActivity(), getUsername())) {

			tacticItem = DBDataManager.getLastTacticItemFromDb(getActivity(), getUsername());

			adjustBoardForGame();
			currentTacticAnswerCnt = 0;
		} else {
			loadNewTacticsBatch();
		}
	}

	@Override
	public void showAnswer() {
		stopTacticsTimer();

		tacticItem.setWasShowed(true);

		ChessBoardTactics.resetInstance();
		boardView.setGameFace(this);

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

			final Move move = boardFace.convertMove(boardFace.getTacticMoves()[currentTacticAnswerCnt]);
			boardView.setMoveAnimator(move, true);
			boardFace.makeMove(move, true);
			invalidateGameScreen();

			currentTacticAnswerCnt++;
			handler.postDelayed(this, TACTIC_ANSWER_DELAY);
		}
	};

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEXT_TACTIC) {
			getNextTactic();
		} else if (code == ID_SHOW_ANSWER) {
			showAnswer();
		} else if (code == ID_PRACTICE) {
			switch2Analysis();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsBoardFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private class GetTacticsUpdateListener extends ChessLoadUpdateListener<TacticItem> {

		private GetTacticsUpdateListener() {
			super(TacticItem.class);
		}

		@Override
		public void updateData(TacticItem returnedObj) {
			noNetwork = false;

			new SaveTacticsBatchTask(dbTacticBatchSaveListener, returnedObj.getData(),
					getContentResolver()).executeTask();
			controlsTacticsView.enableGameControls(true);

			serverError = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {  // TODO restore
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCode.TACTICS_DAILY_LIMIT_REACHED:
						showLimitReachedPopup();
						break;
				}
			} else {
				if (resultCode == StaticData.NO_NETWORK) {
					handleErrorRequest();
				} else if (resultCode == StaticData.INTERNAL_ERROR) {
					serverError = true;
					handleErrorRequest();
				}
			}
		}
	}

	private class TacticsInfoUpdateListener extends ChessLoadUpdateListener<TacticInfoItem> {

		private final int listenerCode;

		public TacticsInfoUpdateListener(int listenerCode) {
			super(TacticInfoItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(TacticInfoItem returnedObj) {
			noNetwork = false;

			TacticRatingData tacticResultItem = returnedObj.getData().getRatingInfo();
			if (tacticResultItem != null) {
				tacticResultItem.setId(tacticItem.getId());
				tacticResultItem.setUser(tacticItem.getUser());
				tacticItem.setResultItem(tacticResultItem);
			}
			String newRatingStr = StaticData.SYMBOL_EMPTY;
			switch (listenerCode) {
				case CORRECT_RESULT:
					if (tacticItem.getResultItem() != null) {
						newRatingStr = tacticItem.getPositiveScore();
						tacticItem.setRetry(true); // set auto retry because we will save tactic
					}

					showCorrect(newRatingStr);
					break;
				case WRONG_RESULT:
					if (tacticItem.isWasShowed()) {
						showWrong(getString(R.string.solved_with_hint));
					} else {
						if (tacticItem.getResultItem() != null) {
							newRatingStr = tacticItem.getNegativeScore();
							tacticItem.setRetry(true); // set auto retry because we will save tactic
						}
						showWrong(newRatingStr);
					}

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
						showLimitReachedPopup();
						break;
				}
			} else {
				if (resultCode == StaticData.NO_NETWORK) {
					switch (listenerCode) {
						case CORRECT_RESULT:
							showCorrect(StaticData.SYMBOL_EMPTY);
							break;
						case WRONG_RESULT:
							showWrong(StaticData.SYMBOL_EMPTY);
							break;
					}
					handleErrorRequest();
				}
			}
		}
	}

	private void showCorrect(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(tacticItem.getResultItem().getUserRating());
			topPanelView.setPlayerScore(tacticItem.getResultItem().getUserRating());
		}
		topPanelView.showCorrect(true, newRatingStr);
		controlsTacticsView.showCorrect();
		getBoardFace().setFinished(true);
	}

	private void showWrong(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(tacticItem.getResultItem().getUserRating());
			topPanelView.setPlayerScore(tacticItem.getResultItem().getUserRating());
		}
		topPanelView.showWrong(true, newRatingStr);
		controlsTacticsView.showWrong();
		getBoardFace().setFinished(true);
	}

	private void handleErrorRequest() {
		controlsTacticsView.enableGameControls(true);

		noNetwork = true;      // TODO handle button click properly
		if (!userSawOfflinePopup) {
			showPopupDialog(R.string.offline_mode, R.string.no_network_rating_not_changed, OFFLINE_RATING_TAG);
		}
	}

	@Override
	public void switch2Analysis() {
		isAnalysis = !isAnalysis;
		if (!isAnalysis) {
			restoreGame();
		}
		topPanelView.showPractice(isAnalysis);
		getBoardFace().setAnalysis(isAnalysis);
		topPanelView.showClock(!isAnalysis);
		controlsTacticsView.showDefault();
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void toggleSides() {
	}

	@Override
	public void invalidateGameScreen() {
		boardView.invalidate();
	}

	@Override
	public void newGame() {
		getNextTactic();
	}

	@Override
	public void showOptions(View view) {
		if (optionsSelectFragment != null) {
			return;
		}

		if (currentGameExist() && tacticItem.isRetry()) {
			optionsArray.put(ID_PRACTICE, getString(R.string.practice_mode));
		} else {
			optionsArray.remove(ID_PRACTICE);
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	public void stopTacticsTimer() {
		if (currentGameExist()) {
			tacticItem.setStop(true);
		}

		tacticsTimer.removeCallbacks(timerUpdateTask);
	}

	public void startTacticsTimer(TacticItem.Data tacticItem) {
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
		if (!currentGameExist() || tacticItem.isStop()) {
			return;
		}

		adjustBoardForGame();
	}

	@Override
	public void restart() {
		tacticItem.setRetry(true);
		adjustBoardForGame();
		controlsTacticsView.showAfterRetry();
	}

	private void adjustBoardForGame() {
		if (!currentGameExist()) { // just in case something weird happen :)
			return;
		}

		ChessBoardTactics.resetInstance();
		final TacticBoardFace boardFace = ChessBoardTactics.getInstance(this);
		boardView.setGameFace(this);

		currentRating = getAppData().getUserTacticsRating();

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
		if (tacticItem.isRetry()) {
			controlsTacticsView.showAfterRetry();
		} else {
			controlsTacticsView.showDefault();
		}

		topPanelView.showDefault();

		if (boardFace.getSide() == ChessBoard.WHITE_SIDE) {
			topPanelView.setSide(ChessBoard.WHITE_SIDE);
		} else {
			topPanelView.setSide(ChessBoard.BLACK_SIDE);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.upgradeBtn) {

			dismissDialogs();

			if (TacticsDataHolder.getInstance().isTacticLimitReached()) {
				FlurryAgent.logEvent(FlurryData.UPGRADE_FROM_TACTICS);
				getActivityFace().openFragment(new UpgradeFragment());
			}
		} else if (view.getId() == R.id.cancelBtn) {
			dismissDialogs();

			if (TacticsDataHolder.getInstance().isTacticLimitReached()) {  // should be only way it was clicked
				cancelTacticAndLeave();
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
			getActivityFace().showPreviousFragment();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
//			loadOfflineTacticsBatch(); // There is a case when you connected to wifi, but no internet connection over it.
			// user saw popup, don't show it again
			if (!userSawOfflinePopup) {
				getNextTactic();
			}
			userSawOfflinePopup = true;
		}
		super.onPositiveBtnClick(fragment);
	}


	private void loadNewTacticsBatch() {
		noNetwork = !AppUtils.isNetworkAvailable(getActivity());
		if (noNetwork || serverError) {
			loadOfflineTacticsBatch();
		} else {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_TACTICS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_IS_INSTALL, RestHelper.V_FALSE);

			new RequestJsonTask<TacticItem>(getTacticsUpdateListener).executeTask(loadItem);
			controlsTacticsView.enableGameControls(false);
		}
	}

	private void loadOfflineTacticsBatch() {
		new GetOfflineTacticsBatchTask(new DemoTacticsUpdateListener(), getResources()).executeTask(R.raw.tactics10batch_new);
		FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_GUEST);
	}

	private class DemoTacticsUpdateListener extends ChessLoadUpdateListener<TacticItem.Data> {

		public DemoTacticsUpdateListener() {
			super();
			useList = true;
		}

		@Override
		public void updateListData(List<TacticItem.Data> itemsList) {
			new SaveTacticsBatchTask(dbTacticBatchSaveListener, itemsList, getContentResolver()).executeTask();
		}
	}

	private class DbTacticBatchSaveListener extends ChessUpdateListener<TacticItem.Data> {
		@Override
		public void updateData(TacticItem.Data returnedObj) {
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
		getActivityFace().showPreviousFragment();
	}

	private void clearSavedTactics() {
		if (currentGameExist()) {
			String[] arguments = new String[]{String.valueOf(tacticItem.getId()), tacticItem.getUser()};
			getContentResolver().delete(DBConstants.uriArray[DBConstants.TACTICS_BATCH],
					DBDataManager.SELECTION_ITEM_ID_AND_USER, arguments);
		}
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

	private void init() {
		tacticsTimer = new Handler();
		inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		imageUpdateListener = new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR);
		imageDownloader = new ImageDownloaderToListener(getContext());

		getTacticsUpdateListener = new GetTacticsUpdateListener();
		tacticsCorrectUpdateListener = new TacticsInfoUpdateListener(CORRECT_RESULT);
		tacticsWrongUpdateListener = new TacticsInfoUpdateListener(WRONG_RESULT);
		dbTacticBatchSaveListener = new DbTacticBatchSaveListener();
	}

	private void widgetsInit(View view) {
		topPanelView = (PanelInfoTacticsView) view.findViewById(R.id.topPanelView);
		topPanelView.setPlayerScore(getAppData().getUserTacticsRating());
		controlsTacticsView = (ControlsTacticsView) view.findViewById(R.id.controlsTacticsView);

		boardView = (ChessBoardTacticsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsTacticsView);

		controlsTacticsView.setBoardViewFace(boardView);

		setBoardView(boardView);

		final ChessBoard chessBoard = ChessBoardTactics.getInstance(this);
		firstRun = chessBoard.isJustInitialized();
		boardView.setGameFace(this);

		controlsTacticsView.enableGameControls(false);

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String userAvatarUrl = getAppData().getUserAvatar();
			imageDownloader.download(userAvatarUrl, imageUpdateListener, AVATAR_SIZE);
		}

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEXT_TACTIC, getString(R.string.next_tactic));
			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			switch (code) {
				case BOTTOM_AVATAR:
					BoardAvatarDrawable boardAvatarDrawable = new BoardAvatarDrawable(getContext(), bitmap);
					topAvatarImg.setImageDrawable(boardAvatarDrawable);
					topPanelView.invalidate();
					break;
			}
		}
	}

/*
1. help makes one move for you, and you fail the tactic. check how it works on iphone if you can.
2. michael, what is under options?
3. if no network, we should have offline tactics still (there should be 100 of them).

Actions:
- Next Tactic
- Show Answer
- Settings

And yeah, Help is actually Hint. It "reveals" the next move (just like in Vs Computer), but then your result is "Solved with Hint"; your score is calculated by treating the request for Hint as a wrong move.
*/

}
