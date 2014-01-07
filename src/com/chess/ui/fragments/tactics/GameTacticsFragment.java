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
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.tasks.GetOfflineTacticsBatchTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveTacticsBatchTask;
import com.chess.model.PgnItem;
import com.chess.model.PopupItem;
import com.chess.model.TacticsDataHolder;
import com.chess.statics.AppConstants;
import com.chess.statics.FlurryData;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.engine.FenHelper;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.comp.GameCompFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.BasePopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsTacticsFragment;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragmentTablet;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.boards.TacticBoardFace;
import com.chess.ui.interfaces.game_ui.GameTacticsFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.PanelInfoTacticsView;
import com.chess.ui.views.chess_boards.ChessBoardTacticsView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.ui.views.game_controls.ControlsTacticsView;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;

import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.02.13
 * Time: 7:10
 */
public class GameTacticsFragment extends GameBaseFragment implements GameTacticsFace, PopupListSelectionFace {

	private static final long MOVE_RESULT_HIDE_DELAY = 2000;
	private static final long DELAY_BETWEEN_MOVES = 1000;
	private static final long TIMER_UPDATE = 1000;
	private static final long START_DELAY = 500;
	private static final long RESUME_TACTIC_DELAY = 1000;
	private static final int COUNT_BACK = 2;

	private static final int NON_INIT = -1;
	private static final int CORRECT_RESULT = 0;
	private static final int WRONG_RESULT = 1;
	private static final int HINTED_RESULT = 4;
	// Menu Options ids
	private static final int ID_SHOW_ANSWER = 1;
	private static final int ID_PRACTICE = 2;
	private static final int ID_SHARE_PGN = 3;
	private static final int ID_PERFORMANCE = 4;
	private static final int ID_SETTINGS = 5;

	private ChessBoardTacticsView boardView;

	private boolean noNetwork;
	private boolean firstRun = true;

	private GetTacticsUpdateListener getTacticsUpdateListener;
	private TacticsTrainerUpdateListener tacticCorrectUpdateListener;
	private TacticsTrainerUpdateListener tacticWrongUpdateListener;
	private TacticsTrainerUpdateListener tacticHintedUpdateListener;
	private DbTacticBatchSaveListener dbTacticBatchSaveListener;

	private static final String DEMO_TACTICS_TAG = "demo tactics reached";
	private static final String OFFLINE_RATING_TAG = "tactics offline rating";
	private static final String TACTIC_SOLVED_TAG = "tactic solved popup";
	private static final String WRONG_MOVE_TAG = "wrong move popup";

	private LayoutInflater inflater;
	private int currentTacticAnswerCnt;
	private int maxTacticAnswerCnt;
	private TacticTrainerItem.Data trainerData;
	private PanelInfoTacticsView bottomPanelView;
	private ControlsTacticsView controlsView;
	private boolean isAnalysis;
	private boolean serverError;
	private boolean userSawOfflinePopup;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private TextView moveResultTxt;
	private int correctMovesBeforeHint;
	private int startCount;
	private int resultIconPadding;

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

		if (firstRun) {

			if (DbDataManager.haveSavedTacticGame(getActivity(), getUsername())) {

				trainerData = DbDataManager.getLastTacticTrainerFromDb(getActivity(), getUsername());

				if (trainerData.isCompleted() || trainerData.isRetry()) {
					adjustBoardForGame();

					if (isLastMoveMadeUser()) {
						verifyMove();
					}
				} else {
					showToast(R.string.ready_q_);
					startCount--;
					handler.postDelayed(resumeLoadedTacticRunnable, RESUME_TACTIC_DELAY);
				}

			} else {
				getControlsView().showStart();
				lockBoard(false);
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
		dismissEndGameDialog();
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

				if (isLastMoveMadeUser()) {
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
		lockBoard(false);
		if (!trainerData.isStop() && getBoardFace().getMovesCount() > 0) {
			trainerData.setRetry(true);

			invalidateGameScreen();
			getBoardFace().takeBack();
			boardView.invalidate();
			playLastMoveAnimationAndCheck();
		} else if (trainerData.isStop() && !getBoardFace().isFinished()) {
			startTacticsTimer(trainerData);
			bottomPanelView.setPlayerTimeLeft(trainerData.getSecondsSpentStr());
			adjustBoardForGame();
		} else {
			verifyMove();
		}
	}

	private void saveTrainerDataBeforeSubmit() {
		trainerData.setRetry(true);
		DbDataManager.saveTacticTrainerToDb(getContentResolver(), trainerData, getUsername());
	}

	@Override
	protected void dismissEndGameDialog() {
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
	 * @return {@code true} if need to Save
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

		if (isLastMoveMadeUser()) {
			verifyMove();
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
	public boolean currentGameExist() {
		return trainerData != null && trainerData.getTacticsProblem() != null;
	}

	@Override
	public TacticBoardFace getBoardFace() {
		return ChessBoardTactics.getInstance(this);
	}

//	@Override
//	public boolean isUserAbleToMove(int color) {
//		if (!currentGameExist()) {
//			return false;
//		}
//		boolean isUserColor;
//		if (isUserColorWhite()) {
//			isUserColor = color == ChessBoard.WHITE_SIDE;
//		} else {
//			isUserColor = color == ChessBoard.BLACK_SIDE;
//		}
//		return isUserColor || getBoardFace().isAnalysis();
//	}

	@Override
	public void verifyMove() {

		noNetwork = !isNetworkAvailable();

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
					trainerData.setCompleted(true);
					saveTrainerDataBeforeSubmit();

					submitHintedResult();
				}
				stopTacticsTimer();
			}
		} else if (trainerData.isAnswerWasShowed()) { // used "show answer" feature
			trainerData.setCompleted(true);
			saveTrainerDataBeforeSubmit();

			stopTacticsTimer();
		} else if (boardFace.isLastTacticMoveCorrect()) { // Correct
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
					trainerData.setCompleted(true);
					saveTrainerDataBeforeSubmit();

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
				saveTrainerDataBeforeSubmit();

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
		loadItem.addRequestParams(RestHelper.P_SECONDS, trainerData.getSecondsSpent());

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

	private void showLimitReachedPopup() {
		FlurryAgent.logEvent(FlurryData.TACTICS_DAILY_LIMIT_EXCEEDED);

		TacticsDataHolder.getInstance().setTacticLimitReached(true);

		LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_tactic_limit_reached, null, false);


		/*LinearLayout adViewWrapper = (LinearLayout) customView.findViewById(R.id.adview_wrapper);
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		} else {
			adViewWrapper.setVisibility(View.GONE);
		}*/

		clearSavedTactics();

		customView.findViewById(R.id.upgradeBtn).setOnClickListener(this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(customView);

		PopupCustomViewFragment customViewFragment = PopupCustomViewFragment.createInstance(popupItem);
		customViewFragment.show(getFragmentManager(), TACTIC_SOLVED_TAG);
	}

	@Override
	public boolean isUserColorWhite() {
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
		isAnalysis = false;

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
				if (getActivity() == null || getBoardFace().getLastMove() == null) {
					return;
				}

				boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
				boardView.resetValidMoves();
				getBoardFace().takeBack();
				invalidateGameScreen();
			}
		}, START_DELAY);
	}

	@Override
	public void showAnswer() {
		stopTacticsTimer();

		trainerData.setAnswerWasShowed(true);

		trainerData.setRetry(true);
		TacticBoardFace boardFace = getBoardFace();

		// if move was made and it was correct we continue, if not, rewind back to start
		if (boardFace.getPly() > 0 && !boardFace.isLastTacticMoveCorrect()) {
			while (boardFace.takeBack()) {
				currentTacticAnswerCnt--;
			}
			boardView.invalidate();
		}

		currentTacticAnswerCnt = boardFace.getPly();
		maxTacticAnswerCnt = boardFace.getTacticMoves().length;

		// show first move immediately
		boolean sizeExceed = currentTacticAnswerCnt >= boardFace.getTacticMoves().length;

		if (sizeExceed) { // rewind back
			while (boardFace.takeBack()) {
				currentTacticAnswerCnt--;
			}
			boardView.invalidate();
		}
		// get next valid move
		final Move move = boardFace.convertMoveAlgebraic(boardFace.getTacticMoves()[currentTacticAnswerCnt]);
		boardFace.setMovesCount(boardFace.getMovesCount() + currentTacticAnswerCnt);

		// play move animation
		boardView.setMoveAnimator(move, true);
		boardView.resetValidMoves();
		// make actual move
		boardFace.makeMove(move, true);
		invalidateGameScreen();

		currentTacticAnswerCnt++;

		handler.postDelayed(showTacticMoveTask, DELAY_BETWEEN_MOVES);
	}

	@Override
	public void vsComputer() {
		int compGameMode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
		getAppData().setCompGameMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);

		// rewind one move back for appropriate analysis
		TacticBoardFace boardFace = getBoardFace();
		while (boardFace.takeBack()) {
			// loop while we can move back
		}
		boardView.invalidate();
		if (!trainerData.isUserMoveFirst()) {
			boardFace.setMovesCount(1);
			boardFace.makeMove(boardFace.getTacticMoves()[0], false);
		}

		CompGameConfig.Builder builder = new CompGameConfig.Builder()
				.setMode(compGameMode)
				.setStrength(getAppData().getCompLevel())
				.setFen(getBoardFace().generateFullFen());

		getActivityFace().openFragment(GameCompFragment.createInstance(builder.build()));
	}

	@Override
	public void onStartTactic() {
		loadNewTactic();
	}

	@Override
	public boolean isLastMoveMadeUser() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			return getBoardFace().isLastMoveMadeWhitePlayer();
		} else {
			return getBoardFace().isLastMoveMadeBlackPlayer();
		}
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
				trainerData.setCompleted(true);

				getControlsView().showCorrect();
				showHintedViews(Symbol.EMPTY);
				return;
			}

			// get next valid move
			final Move move = boardFace.convertMoveAlgebraic(boardFace.getTacticMoves()[currentTacticAnswerCnt]);
			boardFace.setMovesCount(boardFace.getMovesCount() + currentTacticAnswerCnt);

			// play move animation
			boardView.setMoveAnimator(move, true);
			boardView.resetValidMoves();
			// make actual move
			boardFace.makeMove(move, true);
			invalidateGameScreen();

			currentTacticAnswerCnt++;
			handler.postDelayed(this, DELAY_BETWEEN_MOVES);
		}
	};

	@Override
	public void onValueSelected(int code) {
		if (code == ID_SHOW_ANSWER) {
			showAnswer();
		} else if (code == ID_PERFORMANCE) {
			getActivityFace().openFragment(new StatsGameTacticsFragment());
		} else if (code == ID_PRACTICE) {
			switch2Analysis();
		} else if (code == ID_SHARE_PGN) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(SettingsTacticsFragment.createInstance(true));
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private void sendPGN() {
		String moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = "--";
		String blackPlayerName = "--";
		String result = GAME_GOES;

		boolean finished = getBoardFace().isFinished();
		if (finished) {// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}
		String timeControl = "Avg Seconds " + trainerData.getAvgSeconds();
		String date = datePgnFormat.format(Calendar.getInstance().getTime());

		StringBuilder builder = new StringBuilder();
		builder.append("[Event \"").append("--").append("\"]")
				.append("\n [Site \" Chess.com\"]")
				.append("\n [Date \"").append(date).append("\"]")
				.append("\n [White \"").append(whitePlayerName).append("\"]")
				.append("\n [Black \"").append(blackPlayerName).append("\"]")
				.append("\n [Result \"").append(result).append("\"]")
				.append("\n [WhiteElo \"").append("--").append("\"]")
				.append("\n [BlackElo \"").append("--").append("\"]")
				.append("\n [TimeControl \"").append(timeControl).append("\"]");
		if (finished) {
			builder.append("\n [Termination \"").append(endGameReason).append("\"]");
		}
		builder.append("\n ").append(moves).append(Symbol.SPACE).append(result)
				.append("\n \n Sent from my Android");

		PgnItem pgnItem = new PgnItem(whitePlayerName, blackPlayerName);
		pgnItem.setStartDate(date);
		pgnItem.setPgn(builder.toString());

		sendPGN(pgnItem);
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
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.TACTICS_DAILY_LIMIT_REACHED) {
					showLimitReachedPopup();
					return;
				}
			} else {
				if (resultCode == StaticData.NO_NETWORK) {
					handleErrorRequest();
				} else if (resultCode == StaticData.INTERNAL_ERROR) {
					serverError = true;
					handleErrorRequest();
				}
			}
			super.errorHandle(resultCode);
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
			super.errorHandle(resultCode);
		}
	}

	private void showCorrectViews(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(trainerData.getUserRating());
			bottomPanelView.setPlayerScore(trainerData.getUserRating());
		}
		bottomPanelView.showCorrect(true, newRatingStr);
		getControlsView().showCorrect();
		trainerData.setCompleted(true);
		getBoardFace().setFinished(true);

		// show title at the top
		moveResultTxt.setText(R.string.correct);
		setIconToResultView(R.string.ic_check);
	}

	private void showHintedViews(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(trainerData.getUserRating());
			bottomPanelView.setPlayerScore(trainerData.getUserRating());
		}
		bottomPanelView.showWrong(true, newRatingStr);
		getControlsView().showPractice();
		trainerData.setCompleted(true);
		getBoardFace().setFinished(true);

		// show title at the top
		moveResultTxt.setText(R.string.solved_with_hint);
		setIconToResultView(R.string.ic_hint);
	}

	private void showWrongViews(String newRatingStr) {
		if (!TextUtils.isEmpty(newRatingStr)) {
			getAppData().setUserTacticsRating(trainerData.getUserRating());
			bottomPanelView.setPlayerScore(trainerData.getUserRating());
		}
		bottomPanelView.showWrong(true, newRatingStr);
		getControlsView().showWrong();
		getBoardFace().setFinished(true);

		// show title at the top
		moveResultTxt.setText(R.string.incorrect);
		setIconToResultView(R.string.ic_blocking);
	}

	private void setIconToResultView(int iconId) {
		IconDrawable iconDrawable = new IconDrawable(getActivity(), iconId, themeFontColorStateList,
				R.dimen.glyph_icon_big2);
		moveResultTxt.setVisibility(View.VISIBLE);
		moveResultTxt.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
		moveResultTxt.setCompoundDrawablePadding(resultIconPadding);

		handler.postDelayed(hideMoveResultTask, MOVE_RESULT_HIDE_DELAY);
	}

	private Runnable hideMoveResultTask = new Runnable() {
		@Override
		public void run() {
			moveResultTxt.setVisibility(View.INVISIBLE);
			handler.removeCallbacks(hideMoveResultTask);
		}
	};

	private void handleErrorRequest() {
		lockBoard(false);

		noNetwork = true;      // TODO handle button click properly
		if (!userSawOfflinePopup) {
			showPopupDialog(R.string.offline_mode, R.string.you_are_offline, OFFLINE_RATING_TAG);
		}
	}

	@Override
	public void switch2Analysis() {
		isAnalysis = !isAnalysis;
		if (!isAnalysis) {
			restoreGame();

			if (trainerData.isRetry()) {
				getControlsView().showPractice();
			} else {
				getControlsView().showDefault();
			}
		} else {
			getControlsView().showAnalysis();
		}
		bottomPanelView.showPractice(isAnalysis);
		getBoardFace().setAnalysis(isAnalysis);
		bottomPanelView.showClock(!isAnalysis);

		moveResultTxt.setVisibility(isAnalysis ? View.VISIBLE : View.INVISIBLE);
		moveResultTxt.setText(R.string.analysis);
		setIconToResultView(R.string.ic_board);
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

	/**
	 * The tactics timer should count down from 2x the average time to solve.
	 * Then, when 80% of that time is used up, we change the colour of the timer to red (CC0000 in the current app).
	 * At this point, the user risks earning zero rating points (or negative points) if they think any longer.
	 * When the timer hits zero, we just show --:-- (in the normal colour, not red)
	 */
	private Runnable timerUpdateTask = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);
			handler.postDelayed(timerUpdateTask, TIMER_UPDATE);

			if (getBoardFace().isAnalysis()) {
				return;
			}

			int timeToSolve = trainerData.getTacticsProblem().getAvgSeconds() * 2;

			trainerData.increaseSecondsPassed();

			// convert to timeLeft
			long timeLeft = timeToSolve - trainerData.getSecondsSpent();

			// check if time left is less than 80%
			int criticalTime = (int) (timeToSolve * 0.2f);
			bottomPanelView.makeTimerRed(timeLeft < criticalTime);

			String timeLeftStr = AppUtils.getSecondsTimeFromSecondsStr(timeLeft);
			if (timeLeft <= 0) {
				bottomPanelView.setPlayerTimeLeft(PanelInfoTacticsView.NO_TIME);
				bottomPanelView.makeTimerRed(false);
			} else {
				bottomPanelView.setPlayerTimeLeft(timeLeftStr);
			}
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
		if (isAnalysis) {
			BoardFace boardFace = getBoardFace();
			while (boardFace.takeBack()) {
				// loop while we can move back
			}
			boardView.invalidate();
		} else {
			trainerData.setRetry(true);
			adjustBoardForGame();
			getControlsView().showPractice();

			// show title at the top
			moveResultTxt.setVisibility(View.VISIBLE);
			moveResultTxt.setText(R.string.practice);
			setIconToResultView(R.string.ic_board);

			handler.postDelayed(hideMoveResultTask, MOVE_RESULT_HIDE_DELAY);
		}
	}

	private void adjustBoardForGame() {
		if (!currentGameExist()) { // TODO verify if we need it
			return;
		}

		ChessBoardTactics.resetInstance();

		final TacticBoardFace boardFace = ChessBoardTactics.getInstance(this);

		int currentRating = getAppData().getUserTacticsRating();

		bottomPanelView.setPlayerScore(currentRating);
		String initialFen = trainerData.getInitialFen();
		trainerData.getTacticsProblem().setFen(initialFen);
		boardFace.setupBoard(initialFen);

		boolean boardForWhite = initialFen.contains(FenHelper.WHITE_TO_MOVE);
		boolean userMoveFirst = trainerData.isUserMoveFirst();
		if (userMoveFirst) {
			labelsConfig.userSide = boardForWhite ? ChessBoard.WHITE_SIDE : ChessBoard.BLACK_SIDE;
		} else {
			labelsConfig.userSide = boardForWhite ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}

		String cleanMoveString = trainerData.getCleanMoveString();
		trainerData.getTacticsProblem().setMoveList(cleanMoveString);
		boardFace.setTacticMoves(cleanMoveString);

		if ((trainerData.isAnswerWasShowed() || trainerData.isCompleted()) && !isAnalysis) {
			bottomPanelView.setPlayerTimeLeft(trainerData.getSecondsSpentStr());

			String[] moves = boardFace.getTacticMoves();
			boardFace.setMovesCount(moves.length);
			for (String move : moves) {
				boardFace.makeMove(move, false);
			}
		} else { // setup first move
			startTacticsTimer(trainerData);

			if (!userMoveFirst) {
				boardFace.setReside(!boardFace.isReside()); // flip board

				boardFace.setMovesCount(1);
				boardFace.makeMove(boardFace.getTacticMoves()[0], false);

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
			} else {
				boardView.invalidate();
			}
		}

		firstRun = false;
		lockBoard(false);

		if (trainerData.isCompleted() || trainerData.isAnswerWasShowed()) {
			getControlsView().showCorrect();
		} else if (trainerData.isRetry()) {
			getControlsView().showPractice();
		} else {
			getControlsView().showDefault();
		}

		bottomPanelView.showDefault(); // TODO remove if unused
		bottomPanelView.setSide(labelsConfig.userSide);

		if (isAnalysis) {
			getControlsView().showAnalysis();
		} else {
			moveResultTxt.setVisibility(View.INVISIBLE);
		}
		bottomPanelView.showPractice(isAnalysis);
		getBoardFace().setAnalysis(isAnalysis);
		bottomPanelView.showClock(!isAnalysis);

		if (!getAppData().getShowTimerInTactics()){
			bottomPanelView.showClock(false);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.upgradeBtn) {

			dismissEndGameDialog();

			if (TacticsDataHolder.getInstance().isTacticLimitReached()) {
				FlurryAgent.logEvent(FlurryData.UPGRADE_FROM_TACTICS);
				if (!isTablet) {
					getActivityFace().openFragment(new UpgradeFragment());
				} else {
					getActivityFace().openFragment(new UpgradeFragmentTablet());
				}
			}
		} else if (view.getId() == R.id.cancelBtn) {
			dismissEndGameDialog();

			if (TacticsDataHolder.getInstance().isTacticLimitReached()) {  // should be only way it was clicked
				cancelTacticAndLeave();
			}
		}
	}

	private void loadNewTactic() {
		noNetwork = !isNetworkAvailable();
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
				if (trainerData == null) { // we haven't loaded tactic yet
					showToast(R.string.nothing_to_share);
					return true;
				}

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
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(DEMO_TACTICS_TAG)) {
			getActivityFace().showPreviousFragment();
		} else if (tag.equals(OFFLINE_RATING_TAG)) {
			// user saw popup, don't show it again
			if (!userSawOfflinePopup) {
				loadOfflineTacticsBatch();
			}
			userSawOfflinePopup = true;
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(OFFLINE_RATING_TAG)) {
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

//	private void releaseResources() { // TODO optimize resource usage
////		tacticsTimer = null;
//		inflater = null;
//
//		getTacticsUpdateListener.releaseContext();
//		getTacticsUpdateListener = null;
//		tacticCorrectUpdateListener.releaseContext();
//		tacticCorrectUpdateListener = null;
//		tacticWrongUpdateListener.releaseContext();
//		tacticWrongUpdateListener = null;
//		tacticHintedUpdateListener.releaseContext();
//		tacticHintedUpdateListener = null;
//		dbTacticBatchSaveListener.releaseContext();
//		dbTacticBatchSaveListener = null;
//	}

	protected ControlsTacticsView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsTacticsView) controlsView;
	}

	@Override
	protected View getBottomPanelView() {
		return bottomPanelView;
	}

	private void init() {
		FlurryAgent.logEvent(FlurryData.TACTICS_SESSION_STARTED_FOR_REGISTERED);

		labelsConfig = new LabelsConfig();

		inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		getTacticsUpdateListener = new GetTacticsUpdateListener();
		tacticCorrectUpdateListener = new TacticsTrainerUpdateListener(CORRECT_RESULT);
		tacticWrongUpdateListener = new TacticsTrainerUpdateListener(WRONG_RESULT);
		tacticHintedUpdateListener = new TacticsTrainerUpdateListener(HINTED_RESULT);
		dbTacticBatchSaveListener = new DbTacticBatchSaveListener();

		correctMovesBeforeHint = NON_INIT;

		resultIconPadding = getResources().getDimensionPixelSize(R.dimen.glyph_icon_padding);
	}

	private void widgetsInit(View view) {
		moveResultTxt = (TextView) view.findViewById(R.id.moveResultTxt);

		bottomPanelView = (PanelInfoTacticsView) view.findViewById(R.id.topPanelView);
		bottomPanelView.setPlayerScore(getAppData().getUserTacticsRating());
		setControlsView(view.findViewById(R.id.controlsView));

		boardView = (ChessBoardTacticsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(getControlsView());

		getControlsView().setBoardViewFace(boardView);

		setBoardView(boardView);
		boardView.setGameFace(this);

		final ChessBoard chessBoard = ChessBoardTactics.getInstance(this);
		firstRun = chessBoard.isJustInitialized();

		lockBoard(true);

		{// set avatars
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String userAvatarUrl = getAppData().getUserAvatar();
			imageDownloader.download(userAvatarUrl, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);
		}

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
			optionsArray.put(ID_PRACTICE, getString(R.string.practice));
			optionsArray.put(ID_SHARE_PGN, getString(R.string.share_pgn));
			optionsArray.put(ID_PERFORMANCE, getString(R.string.performance));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}

		getControlsView().enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				getControlsView().enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);
	}

	private class ImageUpdateListener extends ImageReadyListenerLight { // use here it for black/white border
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
					boardAvatarDrawable.setBorderThick((int) (2 * density));
					bottomAvatarImg.setImageDrawable(boardAvatarDrawable);
					bottomPanelView.invalidate();
					break;
			}
		}
	}

	private void lockBoard(boolean lock) {
		getControlsView().enableGameControls(!lock);
		boardView.lockBoard(lock);
	}

	// Problem logic tacitcs
//		r6k/1ppqNQp1/p6p/3Pp3/6P1/2P4P/P1P3K1/8 b - - 0 1
//		boardFace.setupBoard("r1q2r1k/3bb2p/p1p1N3/3pp2Q/6R1/2P1R3/1P3PPP/2B3K1 b - - 1 1"); // use as an example of disambiguation move
//		boardFace.setPuzzleMoves("1... Re8 2. Ng6+ Kh7 3. Qxd7");
//		boardFace.setPuzzleMoves("1... Bxe6 2. Qxh7+ Kxh7 3. Rh3+ Bh4 4. R3xh4#");

	// rn2kb1r/p1p2ppp/1p3n2/4p3/4P3/P1N5/1PPB1PPP/R3KbNR w KQkq - 0 1
	// 1. O-O-O Bxg2

	// verify this tactics from Michael
	// 2n2rk1/r4ppp/pqb5/3Bp3/3p4/6Q1/1P1B2PP/6K1 w - - 0 1
	// 1. Bh6 g6 2. Qxe5 1-0

	// r1b1qrk1/pp1p1p1p/5p2/4b2Q/2n5/3R4/8/7K w - - 0 1
	// 1. Rh3 h6 {a delaying move} 2. Qxh6 {threat Qh8#} f5 3. Qh7#

	// 2b2rk1/p3R2p/1r3n2/1p1p1B2/2pP4/2P4P/PP1B4/5RK1 b - - 0 1
	// 1... Bxf5 2. Rxf5 Ne4 3. Rxf8+ Kxf8 4. Rxa7 Nxd2

	//  ----- Incorrect first move tactics!
//	http://www.chess.com/tactics/?id=20935 - played this as black, should be white
	// r2nk1r1/pb3q1p/4p3/3p2pQ/8/BP6/PP3PPP/2R1R1K1 w q - 0 1
	// 1. Rc7 1... Qxh5 2. Re7+ Kf8 3. Rxb7+ Ke8 4. Re7+ Kf8 5. Rxh7+ Ke8 6. Rxh5

//	http://www.chess.com/tactics/?id=26350 - i played this one as white! should be black
	// r4rk1/ppp2pp1/7p/3P4/2P1Nnq1/5bB1/PP3PPP/R3QRK1 b - - 0 1
	// 1... Qh3 2. gxh3 Nxh3#
//
//	http://www.chess.com/tactics/?id=25378 - played this as black, should have been white
	// 1r2r1k1/2q2p1p/b2p1npB/2pP4/3bNP2/1P4PB/3Q3P/1R2R1K1 w - - 0 1
	// 1. Qxd4 cxd4 2. Nxf6+ Kh8 3. Rxe8+ Rxe8 4. Bg7+ Kxg7 5. Nxe8+ Kf8 6. Nxc7

	// http://www.chess.com/tactics/?id=748
	// 3r1bk1/ppq2Bpp/2p5/2P2Q2/8/1P4P1/P6P/5RK1 b - - 1 1
	// 1. ... Kh8 2. Be8 Rxe8 3. Qxf8+ Rxf8 4. Rxf8#


//	http://www.chess.com/tactics/?id=24734 - played this as white, should have been black
// ----------------------
	// ------ Correct examples
	// http://www.chess.com/tactics/?id=22368
	// 8/8/5Kpp/7k/7p/8/7P/8 w - - 0 1
	// 1. h3 g5 2. Kf5 g4 3. hxg4# 1-0


}
