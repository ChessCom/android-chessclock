package com.chess.ui.fragments.tactics;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.TacticProblemItem;
import com.chess.backend.entity.api.TacticRatingData;
import com.chess.backend.entity.api.TacticTrainerItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.tasks.GetOfflineTacticsBatchTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveTacticsBatchTask;
import com.chess.model.PopupItem;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.FlurryData;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.engine.FenHelper;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.explorer.GameExplorerFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.BasePopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.TacticBoardFace;
import com.chess.ui.interfaces.game_ui.GameTacticsFace;
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

	private static final long MOVE_RESULT_HIDE_DELAY = 2000;
	private static final long TACTIC_ANSWER_DELAY = 1500;
	private static final long TIMER_UPDATE = 1000;
	private static final long START_DELAY = 500;
	private static final long RESUME_TACTIC_DELAY = 1000;
	private static final int COUNT_BACK = 2;

	private static final int NON_INIT = -1;
	private static final int CORRECT_RESULT = 0;
	private static final int WRONG_RESULT = 1;
	private static final int HINTED_RESULT = 4;
	// Quick action ids
//	private static final int ID_NEXT_TACTIC = 0;
	private static final int ID_SHOW_ANSWER = 1;
	private static final int ID_PRACTICE = 2;
//	private static final int ID_HINT = 3;
	private static final int ID_PERFORMANCE = 3;
	private static final int ID_SETTINGS = 4;

	private ChessBoardTacticsView boardView;

	private boolean noNetwork;
	private boolean firstRun = true;

	private GetTacticsUpdateListener getTacticsUpdateListener;
	private TacticsTrainerUpdateListener tacticCorrectUpdateListener;
	private TacticsTrainerUpdateListener tacticWrongUpdateListener;
	private TacticsTrainerUpdateListener tacticHintedUpdateListener;
	private DbTacticBatchSaveListener dbTacticBatchSaveListener;

	private static final String FIRST_TACTICS_TAG = "first tactics";
	private static final String DEMO_TACTICS_TAG = "demo tactics reached";
	private static final String OFFLINE_RATING_TAG = "tactics offline rating";
	private static final String TACTIC_SOLVED_TAG = "tactic solved popup";
	private static final String WRONG_MOVE_TAG = "wrong move popup";

	private LayoutInflater inflater;
	private int currentTacticAnswerCnt;
	private int maxTacticAnswerCnt;
	private TacticTrainerItem.Data trainerData;
	private PanelInfoTacticsView topPanelView;
	private ControlsTacticsView controlsTacticsView;
	private boolean isAnalysis;
	private boolean serverError;
	private boolean userSawOfflinePopup;
	private ImageUpdateListener imageUpdateListener;
	private ImageView topAvatarImg;
	private ImageDownloaderToListener imageDownloader;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private LabelsConfig labelsConfig;
	private TextView moveResultTxt;
	private int correctMovesBeforeHint;
	private int startCount;

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
	public void onResume() {
		super.onResume();
		startCount = COUNT_BACK;

		dismissDialogs();

		if (firstRun) {

			if (DbDataManager.haveSavedTacticGame(getActivity(), getUsername())) {

				trainerData = DbDataManager.getLastTacticTrainerFromDb(getActivity(), getUsername());

				if (trainerData.isCompleted()) {
					adjustBoardForGame();

					if (getBoardFace().isLatestMoveMadeUser()) {
						verifyMove();
					}
				} else {
					showToast(R.string.ready_q_);
					startCount--;
					handler.postDelayed(resumeLoadedTacticRunnable, RESUME_TACTIC_DELAY);
				}

			} else {
				showPopupDialog(R.string.ready_, FIRST_TACTICS_TAG);
			}
		} else {
			if (trainerData.isCompleted()) {
				resumeTacticSolving();
			} else {
				showToast(R.string.ready_q_);
				startCount--;
				handler.postDelayed(resumeContinueTacticRunnable, RESUME_TACTIC_DELAY);
			}
		}
	}

	@Override
	public void onPause() {
		dismissDialogs();
		super.onPause();

		stopTacticsTimer();

		if (needToSaveTactic()) {
			DbDataManager.saveTacticTrainerToDb(getContentResolver(), trainerData, getUsername());
		}

		handler.removeCallbacks(hideMoveResultTask);
		handler.removeCallbacks(showTacticMoveTask);
		handler.removeCallbacks(resumeLoadedTacticRunnable);
		handler.removeCallbacks(resumeContinueTacticRunnable);
	}

	private Runnable resumeLoadedTacticRunnable = new Runnable() {
		@Override
		public void run() {
			if (startCount == 0) {

				adjustBoardForGame();

				if (getBoardFace().isLatestMoveMadeUser()) {
					verifyMove();
				}
				handler.removeCallbacks(this);
			} else {
				showToast(R.string.go_ex);
				startCount--;

				handler.postDelayed(this, START_DELAY);
			}
		}
	};

	private Runnable resumeContinueTacticRunnable = new Runnable() {
		@Override
		public void run() {
			if (startCount == 0) {
				resumeTacticSolving();

				handler.removeCallbacks(this);
			} else {
				showToast(R.string.go_ex);
				startCount--;
				handler.postDelayed(this, START_DELAY);
			}
		}
	};

	private void resumeTacticSolving() {
		if (!trainerData.isStop() && getBoardFace().getMovesCount() > 0) {
			trainerData.setRetry(true);

			invalidateGameScreen();
			getBoardFace().takeBack();
			boardView.invalidate();
			playLastMoveAnimationAndCheck();
		} else if (trainerData.isStop() && !getBoardFace().isFinished()) {
			startTacticsTimer(trainerData);
			topPanelView.setPlayerTimeLeft(trainerData.getSecondsSpentStr());
			adjustBoardForGame();
		} else {
			verifyMove();
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
		dismissAllPopups();
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
		return trainerData != null && trainerData.getTacticsProblem() != null;
	}

	@Override
	public TacticBoardFace getBoardFace() {
		return ChessBoardTactics.getInstance(this);
	}

	@Override
	public void verifyMove() {

		noNetwork = !AppUtils.isNetworkAvailable(getContext());

		TacticBoardFace boardFace = getBoardFace();

		if (trainerData.hintWasUsed() && boardFace.isLastTacticMoveCorrect()) { // used hint
			if (boardFace.getMovesCount() < boardFace.getTacticMoves().length - 1) { // if it's not last move, make comp move
				final Move move = boardFace.convertMoveAlgebraic(boardFace.getTacticMoves()[boardFace.getPly()]);
				boardView.setMoveAnimator(move, true);
				boardView.resetValidMoves();
				boardFace.makeMove(move, true);
				invalidateGameScreen();
			} else {
				if (trainerData.isRetry() || noNetwork) {
					String newRatingStr = Symbol.EMPTY;
					if (trainerData.getRatingInfo() != null) {
						newRatingStr = trainerData.getPositiveScore();
					}
					showHintedViews(newRatingStr);
				} else {
					submitHintedResult();
				}
				stopTacticsTimer();
			}
		} else if (trainerData.isAnswerWasShowed()) { // used "show answer" feature
			stopTacticsTimer();
		} else if (boardFace.isLastTacticMoveCorrect()) { // correct
			boardFace.increaseTacticsCorrectMoves();

			if (boardFace.getMovesCount() < boardFace.getTacticMoves().length - 1) { // if it's not last move, make comp move
				final Move move = boardFace.convertMoveAlgebraic(boardFace.getTacticMoves()[boardFace.getPly()]);
				boardView.setMoveAnimator(move, true);
				boardView.resetValidMoves();
				boardFace.makeMove(move, true);
				invalidateGameScreen();
			} else {
				if (trainerData.isRetry() || trainerData.isCompleted() || noNetwork) {
					String newRatingStr = Symbol.EMPTY;
					if (trainerData.getRatingInfo() != null) {
						newRatingStr = trainerData.getPositiveScore();
					}
					showCorrectViews(newRatingStr);
				} else {
					submitCorrectResult();
				}
				stopTacticsTimer();
			}
		} else { // wrong
			if (trainerData.isRetry() || noNetwork) {
				String newRatingStr = Symbol.EMPTY;
				if (trainerData.getRatingInfo() != null) {
					newRatingStr = trainerData.getNegativeScore();
				}
				showWrongViews(newRatingStr);
			} else {
				submitWrongResult();
			}
			stopTacticsTimer();
		}
	}

	private void submitCorrectResult() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_TACTIC_TRAINER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_TACTICS_ID, trainerData.getId());
		loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_TRUE);
//		loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getCorrectMovesCnt());
		loadItem.addRequestParams(RestHelper.P_SECONDS, trainerData.getSecondsSpent());
//		loadItem.addRequestParams(RestHelper.P_ENCODED_MOVES, RestHelper.V_FALSE);

		new RequestJsonTask<TacticTrainerItem>(tacticCorrectUpdateListener).executeTask(loadItem);
		lockBoard(true);
	}

	private void submitHintedResult() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_TACTIC_TRAINER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_TACTICS_ID, trainerData.getId());
		loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_FALSE);
		loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, correctMovesBeforeHint);
		loadItem.addRequestParams(RestHelper.P_SECONDS, trainerData.getSecondsSpent());

		new RequestJsonTask<TacticTrainerItem>(tacticHintedUpdateListener).executeTask(loadItem);
		lockBoard(true);
	}

	private void submitWrongResult() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_TACTIC_TRAINER);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_TACTICS_ID, trainerData.getId());
		loadItem.addRequestParams(RestHelper.P_PASSED, RestHelper.V_FALSE);
		loadItem.addRequestParams(RestHelper.P_CORRECT_MOVES, getBoardFace().getCorrectMovesCnt());
		loadItem.addRequestParams(RestHelper.P_SECONDS, trainerData.getSecondsSpent());

		new RequestJsonTask<TacticTrainerItem>(tacticWrongUpdateListener).executeTask(loadItem);
		lockBoard(true);
	}

	@Override
	public void showHint() {
		// remember the move before the hint
		final TacticBoardFace boardFace = getBoardFace();
		correctMovesBeforeHint = boardFace.getCorrectMovesCnt();

		int hintMoveNumber = boardFace.getPly();
		if (hintMoveNumber == getBoardFace().getTacticMoves().length) {
			return;
		}

		trainerData.setHintWasUsed(true);

		// get next valid move
		final Move move = boardFace.convertMoveAlgebraic(boardFace.getTacticMoves()[hintMoveNumber]);
		boardFace.setMovesCount(boardFace.getMovesCount() + hintMoveNumber);

		// play move animation
		boardView.setMoveAnimator(move, true);
		boardView.resetValidMoves();
		// make actual move
		boardFace.makeMove(move, true);
		invalidateGameScreen();

		// restore move back
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
				boardView.resetValidMoves();
				getBoardFace().takeBack();
				invalidateGameScreen();
			}
		}, START_DELAY);
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

		customView.findViewById(R.id.upgradeBtn).setOnClickListener(this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		PopupCustomViewFragment customViewFragment = PopupCustomViewFragment.createInstance(popupItem);
		customViewFragment.show(getFragmentManager(), TACTIC_SOLVED_TAG);
	}

	@Override
	public Boolean isUserColorWhite() {
		return labelsConfig.userSide == ChessBoard.WHITE_SIDE;
	}

	@Override
	public Long getGameId() {
		if (!currentGameExist()) {
			return null;
		} else {
			return trainerData.getId();
		}
	}

	private void getNextTactic() {
		handler.removeCallbacks(showTacticMoveTask);

		if (currentGameExist()) {
			String[] arguments = new String[]{String.valueOf(trainerData.getId()), trainerData.getUser()};
			getContentResolver().delete(DbScheme.uriArray[DbScheme.Tables.TACTICS_TRAINER.ordinal()],
					DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments);
		}

		if (DbDataManager.haveSavedTacticGame(getActivity(), getUsername())) {

			trainerData = DbDataManager.getLastTacticTrainerFromDb(getActivity(), getUsername());

			adjustBoardForGame();
			currentTacticAnswerCnt = 0;
		} else {
			loadNewTactic();
		}
	}

	@Override
	public void showAnswer() {
		stopTacticsTimer();

		trainerData.setAnswerWasShowed(true);

		ChessBoardTactics.resetInstance();
		boardView.setGameFace(this);

		trainerData.setRetry(true);
		TacticBoardFace boardFace = getBoardFace();
		boardFace.setupBoard(trainerData.getInitialFen());
		boardFace.setReside(!boardFace.isReside()); // we should always reside board in Tactics, because user should make next move

		boardFace.setTacticMoves(trainerData.getCleanMoveString());
		boardFace.setMovesCount(1);

		boardView.invalidate();

		currentTacticAnswerCnt = 0;
		maxTacticAnswerCnt = boardFace.getTacticMoves().length;
		handler.postDelayed(showTacticMoveTask, TACTIC_ANSWER_DELAY);
	}

	@Override
	public void showExplorer() {
		getActivityFace().openFragment(GameExplorerFragment.createInstance(getBoardFace().generateBaseFen()));
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
				controlsTacticsView.showAfterRetry();
				return;
			}

			final Move move = boardFace.convertMoveAlgebraic(boardFace.getTacticMoves()[currentTacticAnswerCnt]);
			boardView.setMoveAnimator(move, true);
			boardView.resetValidMoves();
			boardFace.makeMove(move, true);
			invalidateGameScreen();

			currentTacticAnswerCnt++;
			handler.postDelayed(this, TACTIC_ANSWER_DELAY);
		}
	};

	@Override
	public void onValueSelected(int code) {
		/*if (code == ID_NEXT_TACTIC) {
			getNextTactic();
		} else*/
		if (code == ID_SHOW_ANSWER) {
			showAnswer();
		} else /*if (code == ID_HINT) {
			showHint();
		} else*/ if (code == ID_PERFORMANCE) {
			getActivityFace().openFragment(new StatsGameTacticsFragment());
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

	private class GetTacticsUpdateListener extends ChessLoadUpdateListener<TacticTrainerItem> {

		private GetTacticsUpdateListener() {
			super(TacticTrainerItem.class);
		}

		@Override
		public void updateData(TacticTrainerItem returnedObj) {
			noNetwork = false;

			DbDataManager.saveTacticTrainerToDb(getContentResolver(), returnedObj.getData(), getUsername());
			getNextTactic();

			serverError = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {  // TODO restore
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCodes.TACTICS_DAILY_LIMIT_REACHED:
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

	private class TacticsTrainerUpdateListener extends ChessLoadUpdateListener<TacticTrainerItem> {

		private final int listenerCode;

		public TacticsTrainerUpdateListener(int listenerCode) {
			super(TacticTrainerItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(TacticTrainerItem returnedObj) {
			noNetwork = false;

			TacticProblemItem.Data tacticsProblem = returnedObj.getData().getTacticsProblem();
			if (tacticsProblem != null) {
				DbDataManager.saveTacticTrainerToDb(getContentResolver(), returnedObj.getData(), getUsername());
			} else {
				showLimitReachedPopup();
			}

			TacticRatingData ratingInfo = returnedObj.getData().getRatingInfo();
			// if we request first tactic, there will be no rating_info! It might be returned only after you solve one.
			if (ratingInfo != null) {
				ratingInfo.setId(trainerData.getId());
				ratingInfo.setUser(trainerData.getUser());
				trainerData.setRatingInfo(ratingInfo);
			}
			String newRatingStr = Symbol.EMPTY;
			switch (listenerCode) {
				case CORRECT_RESULT:
					if (trainerData.getRatingInfo() != null) {
						newRatingStr = trainerData.getPositiveScore();
						trainerData.setRetry(true); // set auto retry because we will save tactic
					}

					showCorrectViews(newRatingStr);
					break;
				case HINTED_RESULT:
					if (trainerData.getRatingInfo() != null) {
						newRatingStr = trainerData.getNegativeScore();
						trainerData.setRetry(true); // set auto retry because we will save tactic
					}
					showHintedViews(newRatingStr);
					break;
				case WRONG_RESULT:
					if (trainerData.getRatingInfo() != null) {
						newRatingStr = trainerData.getNegativeScore();
						trainerData.setRetry(true); // set auto retry because we will save tactic
					}
					showWrongViews(newRatingStr);
					break;
			}
			lockBoard(false);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				switch (serverCode) {
					case ServerErrorCodes.TACTICS_DAILY_LIMIT_REACHED:
						showLimitReachedPopup();
						break;
				}
			} else {
				if (resultCode == StaticData.NO_NETWORK) {
					switch (listenerCode) {
						case CORRECT_RESULT:
							showCorrectViews(Symbol.EMPTY);
							break;
						case WRONG_RESULT:
							showWrongViews(Symbol.EMPTY);
							break;
					}
					handleErrorRequest();
				}
			}
		}
	}

	private void showCorrectViews(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(trainerData.getUserRating());
			topPanelView.setPlayerScore(trainerData.getUserRating());
		}
		topPanelView.showCorrect(true, newRatingStr);
		controlsTacticsView.showCorrect();
		trainerData.setCompleted(true);
		getBoardFace().setFinished(true);

		moveResultTxt.setVisibility(View.VISIBLE);
		moveResultTxt.setText(getString(R.string.correct) + Symbol.EX);

		handler.postDelayed(hideMoveResultTask, MOVE_RESULT_HIDE_DELAY);
	}

	private void showHintedViews(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(trainerData.getUserRating());
			topPanelView.setPlayerScore(trainerData.getUserRating());
		}
		topPanelView.showWrong(true, newRatingStr);
		controlsTacticsView.showAfterRetry();
		trainerData.setCompleted(true);
		getBoardFace().setFinished(true);

		moveResultTxt.setVisibility(View.VISIBLE);
		moveResultTxt.setText(R.string.solved_with_hint);

		handler.postDelayed(hideMoveResultTask, MOVE_RESULT_HIDE_DELAY);
	}

	private void showWrongViews(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(trainerData.getUserRating());
			topPanelView.setPlayerScore(trainerData.getUserRating());
		}
		topPanelView.showWrong(true, newRatingStr);
		controlsTacticsView.showWrong();
		getBoardFace().setFinished(true);

		moveResultTxt.setVisibility(View.VISIBLE);
		moveResultTxt.setText(R.string.incorrect);

		handler.postDelayed(hideMoveResultTask, MOVE_RESULT_HIDE_DELAY);
	}

	private Runnable hideMoveResultTask = new Runnable() {
		@Override
		public void run() {
			moveResultTxt.setVisibility(View.GONE);
			handler.removeCallbacks(hideMoveResultTask);
		}
	};

	private void handleErrorRequest() {
		lockBoard(false);

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

			if (trainerData.isRetry()) {
				controlsTacticsView.showAfterRetry();
			} else {
				controlsTacticsView.showDefault();
			}
		} else {
			controlsTacticsView.showAnalysis();
		}
		topPanelView.showPractice(isAnalysis);
		getBoardFace().setAnalysis(isAnalysis);
		topPanelView.showClock(!isAnalysis);
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
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}

		if (currentGameExist() && trainerData.isRetry()) { // don't show practice before first try
			optionsArray.put(ID_PRACTICE, getString(R.string.practice));
		} else {
			optionsArray.remove(ID_PRACTICE);
		}

//		if (controlsTacticsView.getState() == ControlsTacticsView.State.AFTER_RETRY) {
//			optionsArray.remove(ID_PRACTICE);
//			optionsArray.put(ID_HINT, getString(R.string.hint));
//		} else {
//			optionsArray.put(ID_PRACTICE, getString(R.string.practice));
//			optionsArray.remove(ID_HINT);
//		}

		if (trainerData.isAnswerWasShowed()) {
			optionsArray.remove(ID_SHOW_ANSWER);
		} else {
			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	public void stopTacticsTimer() {
		if (currentGameExist()) {
			trainerData.setStop(true);
		}

		handler.removeCallbacks(timerUpdateTask);
	}

	public void startTacticsTimer(TacticTrainerItem.Data trainerData) {
		getBoardFace().setFinished(false);
		trainerData.setStop(false);

		handler.removeCallbacks(timerUpdateTask);
		handler.postDelayed(timerUpdateTask, TIMER_UPDATE);
		lockBoard(false);
	}

	private Runnable timerUpdateTask = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);
			handler.postDelayed(timerUpdateTask, TIMER_UPDATE);

			if (getBoardFace().isAnalysis()) {
				return;
			}

			trainerData.increaseSecondsPassed();
			topPanelView.setPlayerTimeLeft(trainerData.getSecondsSpentStr());
		}
	};

	@Override
	protected void restoreGame() {
		if (!currentGameExist() || trainerData.isStop()) {
			return;
		}

		adjustBoardForGame();
	}

	@Override
	public void restart() {
		trainerData.setRetry(true);
		adjustBoardForGame();
		controlsTacticsView.showAfterRetry();
	}

	private void adjustBoardForGame() {
		if (!currentGameExist()) { // TODO verify if we need it
			return;
		}

		ChessBoardTactics.resetInstance();

		final TacticBoardFace boardFace = ChessBoardTactics.getInstance(this);

		int currentRating = getAppData().getUserTacticsRating();

		topPanelView.setPlayerScore(currentRating);

		boardFace.setupBoard(trainerData.getInitialFen());

		// based on FEN we detect which player is next to move
		boolean whiteToMove = trainerData.getInitialFen().contains(FenHelper.WHITE_TO_MOVE);
		// if whiteToMove that means that comp makes first move and user is on black side
		labelsConfig.userSide = whiteToMove ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		// reside board for user to move
		boardFace.setReside(!boardFace.isReside());

		boardFace.setTacticMoves(trainerData.getCleanMoveString());
		boardFace.setMovesCount(1);

		if (trainerData.isAnswerWasShowed() || trainerData.isCompleted()) {
			topPanelView.setPlayerTimeLeft(trainerData.getSecondsSpentStr());

			String[] moves = boardFace.getTacticMoves();
			boardFace.setMovesCount(moves.length);
			for (String move : moves) {
				boardFace.makeMove(move, false);
			}
		} else { // setup first move
			startTacticsTimer(trainerData);

			boardFace.makeMove(boardFace.getTacticMoves()[0], true);

			// animate last move
			boardView.resetValidMoves();
			boardFace.takeBack();
			boardView.invalidate();

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					playLastMoveAnimation();
				}
			}, START_DELAY);
		}

		firstRun = false;
		lockBoard(false);

		if (trainerData.isRetry()) {
			controlsTacticsView.showAfterRetry();
		} else if (trainerData.isCompleted()) {
			controlsTacticsView.showCorrect();
		} else {
			controlsTacticsView.showDefault();
		}

		topPanelView.showDefault(); // TODO remove if unused

		if (boardFace.getSide() == ChessBoard.WHITE_SIDE) {
			topPanelView.setSide(ChessBoard.WHITE_SIDE);
		} else {
			topPanelView.setSide(ChessBoard.BLACK_SIDE);
		}

		if (isAnalysis) {
			controlsTacticsView.showAnalysis();
			topPanelView.showPractice(isAnalysis);
			getBoardFace().setAnalysis(isAnalysis);
			topPanelView.showClock(!isAnalysis);
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

	private void loadNewTactic() {
		noNetwork = !AppUtils.isNetworkAvailable(getActivity());
		if (noNetwork || serverError) {
			loadOfflineTacticsBatch();
		} else {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_TACTIC_TRAINER);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

			new RequestJsonTask<TacticTrainerItem>(getTacticsUpdateListener).executeTask(loadItem);
			lockBoard(true);
		}
	}

	private void loadOfflineTacticsBatch() {
		if (getAppData().isDemoTacticsLoaded()) {
			popupItem.setButtons(1);
			showPopupDialog(R.string.ten_tactics_completed, DEMO_TACTICS_TAG);
		} else {
			new GetOfflineTacticsBatchTask(new DemoTacticsUpdateListener(), getResources()).executeTask(R.raw.tactics10batch_new);
			FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_GUEST);

			getAppData().setDemoTacticsLoaded(true);
		}
	}

	private class DemoTacticsUpdateListener extends ChessLoadUpdateListener<TacticProblemItem.Data> {

		public DemoTacticsUpdateListener() {
			super();
			useList = true;
		}

		@Override
		public void updateListData(List<TacticProblemItem.Data> itemsList) {
			new SaveTacticsBatchTask(dbTacticBatchSaveListener, itemsList, getContentResolver()).executeTask();
		}
	}

	private class DbTacticBatchSaveListener extends ChessLoadUpdateListener<TacticProblemItem.Data> {
		@Override
		public void updateData(TacticProblemItem.Data returnedObj) {
			getNextTactic();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				String tacticShareStr = "http://www.chess.com/tactics/?id=" + trainerData.getId();

				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				/*
				Chess Problem #67816
				Problem Difficulty Rating: 896, Average Time: 47 seconds
				 */
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Chess Problem #" + trainerData.getId() + Symbol.NEW_STR
						+ "Problem Difficulty Rating: " + trainerData.getProblemRating()
						+ ", Average Time: " + trainerData.getAvgSeconds() + " seconds"
						+ Symbol.NEW_STR + tacticShareStr);
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		// comment to test if dismissAllPopups works fine
//		if (getActivity() == null) { // this happens when app was restored and popup re-appears again
//			return;
//		}
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(FIRST_TACTICS_TAG)) {
			loadNewTactic();
		} else if (tag.equals(DEMO_TACTICS_TAG)) {
			getActivityFace().showPreviousFragment();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
			// user saw popup, don't show it again
			if (!userSawOfflinePopup) {
				getNextTactic();
			}
			userSawOfflinePopup = true;
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
//		if (getActivity() == null) { // this happens when app was restored and popup re-appears again
//			return;
//		}
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
			String[] arguments = new String[]{String.valueOf(trainerData.getId()), trainerData.getUser()};
			getContentResolver().delete(DbScheme.uriArray[DbScheme.Tables.TACTICS_TRAINER.ordinal()],
					DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments);
		}
	}

	private void releaseResources() {
//		tacticsTimer = null;
		inflater = null;

		getTacticsUpdateListener.releaseContext();
		getTacticsUpdateListener = null;
		tacticCorrectUpdateListener.releaseContext();
		tacticCorrectUpdateListener = null;
		tacticWrongUpdateListener.releaseContext();
		tacticWrongUpdateListener = null;
		tacticHintedUpdateListener.releaseContext();
		tacticHintedUpdateListener = null;
		dbTacticBatchSaveListener.releaseContext();
		dbTacticBatchSaveListener = null;
	}

	private void init() {
		FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED);

		labelsConfig = new LabelsConfig();

		inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		imageUpdateListener = new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR);
		imageDownloader = new ImageDownloaderToListener(getContext());

		getTacticsUpdateListener = new GetTacticsUpdateListener();
		tacticCorrectUpdateListener = new TacticsTrainerUpdateListener(CORRECT_RESULT);
		tacticWrongUpdateListener = new TacticsTrainerUpdateListener(WRONG_RESULT);
		tacticHintedUpdateListener = new TacticsTrainerUpdateListener(HINTED_RESULT);
		dbTacticBatchSaveListener = new DbTacticBatchSaveListener();

		correctMovesBeforeHint = NON_INIT;
	}

	private void widgetsInit(View view) {
		moveResultTxt = (TextView) view.findViewById(R.id.moveResultTxt);

		topPanelView = (PanelInfoTacticsView) view.findViewById(R.id.topPanelView);
		topPanelView.setPlayerScore(getAppData().getUserTacticsRating());
		controlsTacticsView = (ControlsTacticsView) view.findViewById(R.id.controlsTacticsView);

		boardView = (ChessBoardTacticsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsTacticsView);

		controlsTacticsView.setBoardViewFace(boardView);

		setBoardView(boardView);
		boardView.setGameFace(this);

		final ChessBoard chessBoard = ChessBoardTactics.getInstance(this);
		firstRun = chessBoard.isJustInitialized();

		lockBoard(true);

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String userAvatarUrl = getAppData().getUserAvatar();
			imageDownloader.download(userAvatarUrl, imageUpdateListener, AVATAR_SIZE);
		}

		{// options list setup
			optionsArray = new SparseArray<String>();
//			optionsArray.put(ID_NEXT_TACTIC, getString(R.string.next_tactic));
			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
			optionsArray.put(ID_PRACTICE, getString(R.string.practice));
//			optionsArray.put(ID_HINT, getString(R.string.hint));
			optionsArray.put(ID_PERFORMANCE, getString(R.string.performance));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

	private class ImageUpdateListener extends ImageReadyListenerLight {
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

	private void lockBoard(boolean lock) {
		controlsTacticsView.enableGameControls(!lock);
		boardView.lockBoard(lock);
	}

}
