package com.chess.ui.fragments.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LccHelper;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.live.rules.GameResult;
import com.chess.live.util.GameRatingClass;
import com.chess.model.GameAnalysisItem;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.game.GameAnalyzeFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.PanelInfoLiveView;
import com.chess.ui.views.chess_boards.ChessBoardLiveView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsLiveView;
import com.chess.utilities.AppUtils;

import java.util.List;

import static com.chess.live.rules.GameResult.WIN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.01.13
 * Time: 11:33
 */
public class GameLiveFragment extends GameBaseFragment implements GameNetworkFace, LccEventListener,
		LccChatMessageListener, PopupListSelectionFace {

	private static final String TAG = "LccLog-GameLiveFragment";
	private static final String WARNING_TAG = "warning message popup";

	// Options ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_OFFER_DRAW = 1;
	private static final int ID_ABORT_RESIGN = 2;
	private static final int ID_REMATCH = 3;
	private static final int ID_SETTINGS = 4;


	private ChessBoardLiveView boardView;

	private View fadeLay;
	private boolean lccInitiated;
	private String warningMessage;
	private ChessUpdateListener<Game> gameTaskListener;


	private NotationView notationsView;
	private PanelInfoLiveView topPanelView;
	private PanelInfoLiveView bottomPanelView;
	private ControlsLiveView controlsLiveView;
	private PopupOptionsMenuFragment optionsSelectFragment;

	private LabelsConfig labelsConfig;
	//	private UserInfoUpdateListener userInfoUpdateListener;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ImageDownloaderToListener imageDownloader;
	private int gameEndTitleId;
	private SparseArray<String> optionsMap;

	public GameLiveFragment() {
	}

	public static GameLiveFragment createInstance(long id) {
		GameLiveFragment fragment = new GameLiveFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
		}
		logLiveTest("onCreate");

		gameTaskListener = new ChessUpdateListener<Game>();
		imageDownloader = new ImageDownloaderToListener(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_live_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live);

		widgetsInit(view);
		try {
			init();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}
		enableSlideMenus(false);
	}

	@Override
	public void onResume() {
		super.onResume();

		logLiveTest("onResume");

		if (isLCSBound) {
			try {
				synchronized (LccHelper.LOCK) {
					onGameStarted();
				}
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				isLCSBound = false;
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		logLiveTest("onPause");

		dismissDialogs();
		if (isLCSBound) {
			try {
				getLiveService().setGameActivityPausedMode(true);
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				isLCSBound = false;
			}
		}
	}

	@Override
	public void onLiveServiceConnected() {
		super.onLiveServiceConnected();

	/*	init();

		updateGameState();

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		invalidateGameScreen();

		checkPendingWarnings();*/
	}

	// ----------------------Lcc Events ---------------------------------------------

	private void onGameStarted() throws DataNotValidException {
		logLiveTest("onGameStarted");

		LiveChessService liveService = getLiveService();
		GameLiveItem currentGame = liveService.getGameItem();

		ChessBoardLive.resetInstance();
		boardView.setGameFace(this);

		if (!liveService.isUserColorWhite()) {
			getBoardFace().setReside(true);
		}
		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		notationsView.resetNotations();
		boardView.resetValidMoves();

		invalidateGameScreen();
		if (liveService.getPendingWarnings().size() > 0) {
			warningMessage = liveService.getLastWarningMessage();
			Log.d("LCCLOG-WARNING", warningMessage);
			popupItem.setNegativeBtnId(R.string.fair_play_policy);
			showPopupDialog(R.string.warning, warningMessage, WARNING_TAG);
		}

		showSubmitButtonsLay(false);
		controlsLiveView.enableAnalysisMode(false);
		controlsLiveView.showDefault();

		getBoardFace().setFinished(false);
		getSoundPlayer().playGameStart();

		controlsLiveView.haveNewMessage(currentGame.hasNewMessage());

		// avoid races on update moves logic for active game, doUpdateGame updates moves, avoid peaces disappearing and invalidmovie exception
		// vm: actually we have to invoke checkAndReplayMoves() here, because we reset a board on pause/resume everytime.
		// As for doUpdateGame() - that method updates moves only if gameLivePaused=false, so should be safe.
		// Lets see how synchronized approach is suitable here
		liveService.checkAndReplayMoves();

		liveService.checkFirstTestMove();

		liveService.setGameActivityPausedMode(false); // probably set it above
		liveService.checkGameEvents();

		//liveService.executePausedActivityGameEvents();
	}

	@Override
	public void setWhitePlayerTimer(final String timeString) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				boolean whiteToMove = getBoardFace().isWhiteToMove();

				if (getBoardFace().isReside()) { // if white at top
					topPanelView.showTimeLeftIcon(whiteToMove);
					bottomPanelView.showTimeLeftIcon(!whiteToMove);

					topPanelView.setTimeRemain(timeString);
				} else {
					topPanelView.showTimeLeftIcon(!whiteToMove);
					bottomPanelView.showTimeLeftIcon(whiteToMove);

					bottomPanelView.setTimeRemain(timeString);
				}
			}
		});
	}

	@Override
	public void setBlackPlayerTimer(final String timeString) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() { // TODO add check
			@Override
			public void run() {

				boolean blackToMove = !getBoardFace().isWhiteToMove();

				if (getBoardFace().isReside()) {
					topPanelView.showTimeLeftIcon(!blackToMove);
					bottomPanelView.showTimeLeftIcon(blackToMove);

					bottomPanelView.setTimeRemain(timeString);
				} else {
					topPanelView.showTimeLeftIcon(blackToMove);
					bottomPanelView.showTimeLeftIcon(!blackToMove);

					topPanelView.setTimeRemain(timeString);
				}
			}
		});
	}

	private void indicateCurrentMove(boolean userMove) {  // TODO adjust or remove
		if (getBoardFace().isReside()) {
			topPanelView.showTimeLeftIcon(userMove);
			bottomPanelView.showTimeLeftIcon(!userMove);
		} else {
			topPanelView.showTimeLeftIcon(!userMove);
			bottomPanelView.showTimeLeftIcon(userMove);
		}
	}

	// ----------------------Lcc Events ---------------------------------------------

	@Override
	public void startGameFromService() {

		logLiveTest("startGameFromService");

		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dismissProgressDialog();
					dismissDialogs(); // hide game end popup

					try {
						synchronized (LccHelper.LOCK) {
							onGameStarted();
						}
					} catch (DataNotValidException e) {
						logTest(e.getMessage());
					}
				}
			});
		}
	}

	@Override
	public void createSeek() {
		// shouldn't be used here. Use in WaitFragment instead
	}

	@Override
	public void onGameRefresh(GameLiveItem gameItem) {
		logLiveTest("onGameRefresh");
		Activity activity = getActivity();
		if (activity == null) {
			logLiveTest("activity = null, quit");
			return;
		}
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}

		if (getBoardFace().isAnalysis() && gameItem.getGameId() == gameId) {
			return;
		} else {
			getBoardFace().setAnalysis(false);
			getBoardFace().setFinished(false);
			gameId = gameItem.getGameId();
		}

		String[] actualMoves = gameItem.getMoveList().trim().split(" ");
		int actualMovesSize = actualMoves.length;

		int[] moveFT;
		Move move;
		boolean playSound;

		for (int i = getBoardFace().getMovesCount(); i < actualMovesSize; i++) {

			moveFT = getBoardFace().parseCoordinate(actualMoves[i]);

			move = getBoardFace().convertMove(moveFT);
			playSound = i == actualMovesSize - 1;

			boardView.setMoveAnimator(move, true);

			getBoardFace().makeMove(move, playSound);
		}

		boardView.resetValidMoves();

		getBoardFace().setMovesCount(actualMovesSize);

		if (!liveService.isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boardView.setGameFace(GameLiveFragment.this);
				boardView.invalidate();
				invalidateGameScreen();
			}
		});

		liveService.checkTestMove();

		if (gameItem.hasNewMessage()) {
			controlsLiveView.haveNewMessage(true);
		}
	}

	public void onConnectionBlocked(boolean blocked) {
		blockGame(blocked);
	}

	@Override
	public void onMessageReceived() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				controlsLiveView.haveNewMessage(true);
				boardView.invalidate();
			}
		});
	}

	@Override
	public void onInform(String title, String message) {
		showSinglePopupDialog(title, message);
	}

	@Override
	public void onDrawOffered(String drawOfferUsername) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				topPanelView.showDrawOfferedView(true);
			}
		});
	}

	@Override
	public void onGameEnd(final String gameEndMessage) {
		final Activity activity = getActivity();
		if (activity == null) {
			return;
		}

		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}
		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		final Game game = liveService.getLastGame();
		final List<Integer> ratings = game.getRatings();
		// Get side result
		List<GameResult> gameResults = game.getResults();
		final GameResult whitePlayerResult = gameResults.get(0);
		final GameResult blackPlayerResult = gameResults.get(1);

		gameEndTitleId = R.string.black_wins;
		if (whitePlayerResult == WIN) {
			gameEndTitleId = R.string.white_wins;
		} else if (blackPlayerResult != WIN) {
			gameEndTitleId = R.string.game_drawn;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final View layout;
				if (!AppUtils.isNeedToUpgrade(activity)) {
					layout = inflater.inflate(R.layout.popup_end_game, null, false);
				} else {
					layout = inflater.inflate(R.layout.popup_end_game_free, null, false);
				}

				int newWhiteRating = ratings.get(0);
				int newBlackRating = ratings.get(1);

				if (getBoardFace().isReside()) {
					topPanelView.setPlayerRating(String.valueOf(newWhiteRating));
					bottomPanelView.setPlayerRating(String.valueOf(newBlackRating));
				} else {
					topPanelView.setPlayerRating(String.valueOf(newBlackRating));
					bottomPanelView.setPlayerRating(String.valueOf(newWhiteRating));
				}

				updatePlayerLabels(game, newWhiteRating, newBlackRating);
				showGameEndPopup(layout, getString(gameEndTitleId), gameEndMessage);

				setBoardToFinishedState();

				controlsLiveView.showAfterMatch();
			}
		});

	}

	private void showGameEndPopup(View layout, String title, String message) {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}
		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView ratingTitleTxt = (TextView) layout.findViewById(R.id.ratingTitleTxt);
		TextView resultRatingTxt = (TextView) layout.findViewById(R.id.resultRatingTxt);
		TextView resultRatingChangeTxt = (TextView) layout.findViewById(R.id.resultRatingChangeTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating = liveService.getLastGame().getRatingForPlayer(liveService.getUsername());
		int ratingChange = liveService.getLastGame().getRatingChangeForPlayer(liveService.getUsername());

		GameRatingClass gameRatingClass = liveService.getLastGame().getGameRatingClass();
		String newRatingStr = getString(R.string.live);
		if (gameRatingClass == GameRatingClass.Standard) {
			newRatingStr += Symbol.SPACE + getString(R.string.standard);
		} else if (gameRatingClass == GameRatingClass.Blitz) {
			newRatingStr += Symbol.SPACE + getString(R.string.blitz);
		} else /*if (gameRatingClass == GameRatingClass.Lightning)*/ {
			newRatingStr += Symbol.SPACE + getString(R.string.lightning);
		}

		String ratingChangeString = Symbol.wrapInPars(ratingChange > 0 ? "+" + ratingChange : "" + ratingChange);

		resultRatingTxt.setText(String.valueOf(currentPlayerNewRating));
		resultRatingChangeTxt.setText(ratingChangeString);
		ratingTitleTxt.setText(getString(R.string.new_game_rating_arg, newRatingStr));

//		inneractiveRectangleAd = (InneractiveAd) layout.findViewById(R.id.inneractiveRectangleAd);
//		InneractiveAdHelper.showRectangleAd(inneractiveRectangleAd, this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupGameEndFragment endPopupFragment = PopupGameEndFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.analyzePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.sharePopupBtn).setOnClickListener(this);


//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
//		}
	}


	@Override
	protected void setBoardToFinishedState() { // TODO implement state conditions logic for board
		super.setBoardToFinishedState();
		showSubmitButtonsLay(false);
	}

	// -----------------------------------------------------------------------------------

	private void blockGame(final boolean block) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (block) {
					fadeLay.setVisibility(View.VISIBLE);
				} else {
					fadeLay.setVisibility(View.INVISIBLE);
				}

				boardView.lockBoard(block);
			}
		});
	}

	protected void sendMove() throws DataNotValidException {
		LiveChessService liveService = getLiveService();


		String debugString = " no debug log";
		getBoardFace().setSubmit(false);
		showSubmitButtonsLay(false);

		String move = getBoardFace().convertMoveLive();
		Log.i(TAG, "LCC make move: " + move);

		String stackTrace;
		try {
			throw new Exception();
		} catch (Exception e) {
			stackTrace = Log.getStackTraceString(e);
		}

		String temporaryDebugInfo =
				"username=" + liveService.getUsername() +
						" lccInitiated=" + lccInitiated +
//						", " + boardDebug +
						", gameSeq=" + liveService.getCurrentGame().getMoves().size() +
						", boardHply=" + getBoardFace().getPly() +
						", moveLive=" + getBoardFace().convertMoveLive() +
						", gamesC=" + liveService.getGamesCount() +
						", gameId=" + getGameId() +
//						", analysisPanel=" + gamePanelView.isAnalysisEnabled() +
						", analysisBoard=" + getBoardFace().isAnalysis() +
						", latestMoveNumber=" + liveService.getLatestMoveNumber() +
						", debugString=" + debugString +
						", submit=" + preferences.getBoolean(getAppData().getUsername() + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false) +
						", movesLive=" + liveService.getCurrentGame().getMoves() +
						", moves=" + getBoardFace().getMoveListSAN() +
						", trace=" + stackTrace;
		temporaryDebugInfo = temporaryDebugInfo.replaceAll("\n", " ");
		//Log.d("TESTTEST", temporaryDebugInfo);

		liveService.makeMove(move, temporaryDebugInfo);
	}

	@Override
	public void switch2Analysis() {
//		super.switch2Analysis(isAnalysis);
//		Log.d("live", "switch2Analysis analysis = " + isAnalysis); // TODO restore
//		if (isAnalysis) {
//			liveService.setLatestMoveNumber(0);
//			ChessBoardLive.resetInstance();
//		}
//		controlsLiveView.enableControlButtons(isAnalysis);
	}

	@Override
	public void switch2Chat() {
//		LiveChessService liveService;
//		try {
//			liveService = getLiveService();
//		} catch (DataNotValidException e) {
//			logLiveTest(e.getMessage());
//			return;
//		}
//		if(!liveService.isCurrentGameExist()) return;
//			liveService.getCurrentGame();
//		currentGame.setHasNewMessage(false);
		controlsLiveView.haveNewMessage(false);

		getActivityFace().openFragment(new LiveChatFragment());
	}

	@Override
	public void playMove() {
		try {
			sendMove();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}
	}

	@Override
	public void cancelMove() {
		showSubmitButtonsLay(false);
		boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
		boardView.resetValidMoves();
		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();
		boardView.invalidate();
	}

	@Override
	public void goHome() {
		getActivityFace().showPreviousFragment();
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
	}

	@Override
	public void updateAfterMove() {
		if (!getBoardFace().isAnalysis()) {
			try {
				sendMove();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
			}
		}
	}

	@Override
	public void toggleSides() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		}
		BoardAvatarDrawable tempDrawable = labelsConfig.topAvatar;
		labelsConfig.topAvatar = labelsConfig.bottomAvatar;
		labelsConfig.bottomAvatar = tempDrawable;

		String tempLabel = labelsConfig.topPlayerName;
		labelsConfig.topPlayerName = labelsConfig.bottomPlayerName;
		labelsConfig.bottomPlayerName = tempLabel;

		String tempScore = labelsConfig.topPlayerRating;
		labelsConfig.topPlayerRating = labelsConfig.bottomPlayerRating;
		labelsConfig.bottomPlayerRating = tempScore;
	}

	@Override
	public void invalidateGameScreen() {
		if (isLCSBound) {
			showSubmitButtonsLay(getBoardFace().isSubmit());

			if (labelsConfig.bottomAvatar != null) {
				labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
				bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
			}

			if (labelsConfig.topAvatar != null) {
				labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
				topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
			}

			topPanelView.showTimeRemain(true);
			bottomPanelView.showTimeRemain(true);

			topPanelView.setSide(labelsConfig.getOpponentSide());
			bottomPanelView.setSide(labelsConfig.userSide);

			topPanelView.setPlayerName(labelsConfig.topPlayerName);
			topPanelView.setPlayerRating(labelsConfig.topPlayerRating);
			bottomPanelView.setPlayerName(labelsConfig.bottomPlayerName);
			bottomPanelView.setPlayerRating(labelsConfig.bottomPlayerRating);

			boardView.updateNotations(getBoardFace().getNotationArray());
			try {
				getLiveService().paintClocks();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
			}
		}
	}

	@Override
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}

		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}

		boolean isGameOver = liveService.getCurrentGame() != null && liveService.getCurrentGame().isGameOver();

		if (isGameOver) {
			optionsMap.put(ID_REMATCH, getString(R.string.rematch));
			optionsMap.remove(ID_OFFER_DRAW);
			optionsMap.remove(ID_ABORT_RESIGN);

		} else {

			if (getBoardFace().getPly() < 1 && isUserMove()) {
				optionsMap.put(ID_ABORT_RESIGN, getString(R.string.abort));
				optionsMap.remove(ID_OFFER_DRAW);
			} else {
				optionsMap.put(ID_ABORT_RESIGN, getString(R.string.resign));
				optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
			}

			if (!isUserMove()) {
				optionsMap.remove(ID_OFFER_DRAW);
			}

			optionsMap.remove(ID_REMATCH);
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsMap);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			getActivityFace().changeRightFragment(new LiveGameOptionsFragment());
			getActivityFace().toggleRightMenu();
		} else if (code == ID_ABORT_RESIGN) {
			if (getBoardFace().getPly() < 1 && isUserMove()) {
				showPopupDialog(R.string.abort_game_, ABORT_GAME_TAG);
			} else {
				showPopupDialog(R.string.resign_game_, ABORT_GAME_TAG);
			}
		} else if (code == ID_OFFER_DRAW) {
			showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
		} else if (code == ID_REMATCH) {
			try {
				getLiveService().rematch();
			} catch (DataNotValidException e) {
				e.printStackTrace();
			}
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

	private boolean isUserMove() {
		Boolean userColorWhite = isUserColorWhite();
		return userColorWhite != null && (userColorWhite ? getBoardFace().isWhiteToMove() : !getBoardFace().isWhiteToMove());
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {  // TODO remove arg and get state from boardFace
		controlsLiveView.showSubmitButtons(show);

		if (!show) {
			getBoardFace().setSubmit(false);
		}
	}

	@Override
	public Boolean isUserColorWhite() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return null;
		}
		return liveService.isUserColorWhite();
	}

	@Override
	public Long getGameId() {
//		LiveChessService liveService;
//		try {
//			liveService = getLiveService();
//		} catch (DataNotValidException e) {
//			logLiveTest(e.getMessage());
//			return null;
//		}
//		return liveService.getCurrentGameId(); // currentGame initialized in init() method
		return gameId;
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}

		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			if (isLCSBound) {
				Log.i(TAG, "Request draw: " + liveService.getCurrentGame());
				liveService.runMakeDrawTask();
			}
		} else if (tag.equals(WARNING_TAG)) {
			if (isLCSBound) {
				liveService.getPendingWarnings().remove(warningMessage);
			}
			Log.d("live", "positive clicked");
			// TODO find a real cause of analysis block
			// restore game to normal state
//			switch2Analysis(false);
			getBoardFace().setAnalysis(false);

			synchronized (LccHelper.LOCK) {
				try {
					onGameStarted();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}

				if (!isUserColorWhite()) {
					getBoardFace().setReside(true);
				}
			}
		} else if (tag.equals(ABORT_GAME_TAG)) {
			if (isLCSBound) {

				Game game = liveService.getCurrentGame();

				if (liveService.isFairPlayRestriction()) {
					Log.i("LCCLOG", ": resign game by fair play restriction: " + game);
					Log.i(TAG, "Resign game: " + game);
					liveService.runMakeResignTask();
				} else if (liveService.isAbortableBySeq()) {
					Log.i(TAG, "abort game: " + game);
					liveService.runAbortGameTask();
				} else {
					Log.i(TAG, "resign game: " + game);
					liveService.runMakeResignTask();
				}
			}
		}
		super.onPositiveBtnClick(fragment);
	}

	// ---------------- Players names and labels -----------------------------------------------------------------

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}
		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			if (isLCSBound) {
				Log.i(TAG, "Decline draw: " + liveService.getCurrentGame());
				liveService.runRejectDrawTask();
			}
		} else if (tag.equals(WARNING_TAG)) {
			if (isLCSBound) {
				liveService.getPendingWarnings().remove(warningMessage);
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.fair_play_policy_url))));
		}
		super.onNegativeBtnClick(fragment);
	}

	// ---------------- Players names and labels -----------------------------------------------------------------

	@Override
	public String getWhitePlayerName() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return Symbol.EMPTY;
		}
		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null)
			return Symbol.EMPTY;
		else
			return currentGame.getWhiteUsername();
	}

	@Override
	public String getBlackPlayerName() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return Symbol.EMPTY;
		}
		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null)
			return Symbol.EMPTY;
		else
			return currentGame.getBlackUsername();
	}

	@Override
	public boolean currentGameExist() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return false;
		}
		return liveService.getCurrentGame() != null;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardLive.getInstance(this);
	}

	private void updatePlayerLabels(Game game, int newWhiteRating, int newBlackRating) {

		if (getBoardFace().isReside()) {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
			labelsConfig.topPlayerName = game.getWhitePlayer().getUsername();
			labelsConfig.topPlayerRating = String.valueOf(newWhiteRating);
			labelsConfig.bottomPlayerName = game.getBlackPlayer().getUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(newBlackRating);
		} else {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = getBlackPlayerName();
			labelsConfig.topPlayerRating = String.valueOf(newBlackRating);
			labelsConfig.bottomPlayerName = game.getWhitePlayer().getUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(newWhiteRating);
		}
	}

	@Override
	protected void restoreGame() {
		if (isLCSBound) {
//			ChessBoardLive.resetInstance();		 // moved to onGameStarted
//			boardView.setGameFace(this);

			synchronized (LccHelper.LOCK) {
				try {
					onGameStarted();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
					isLCSBound = false;
					return;
				}
				getBoardFace().setJustInitialized(false);
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.cancelBtn) {
			showSubmitButtonsLay(false);
			boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
			getBoardFace().takeBack();
			boardView.resetValidMoves();
			getBoardFace().decreaseMovesCount();
			boardView.invalidate();
		} else if (view.getId() == R.id.newGamePopupBtn) {
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
		} else if (view.getId() == R.id.analyzePopupBtn) {
			GameAnalysisItem analysisItem = new GameAnalysisItem();  // TODO reuse later
			analysisItem.setGameType(RestHelper.V_GAME_CHESS);
			analysisItem.setFen(getBoardFace().generateFullFen());
			analysisItem.setMovesList(getBoardFace().getMoveListSAN());
			String opponentName;
			int userColor;
			Boolean userColorWhite = isUserColorWhite();
			if (userColorWhite != null && userColorWhite) {
				opponentName = getBlackPlayerName();
				userColor = ChessBoard.WHITE_SIDE;
			} else {
				opponentName = getWhitePlayerName();
				userColor = ChessBoard.BLACK_SIDE;
			}
			analysisItem.setOpponent(opponentName);
			analysisItem.setUserColor(userColor);

			getActivityFace().openFragment(GameAnalyzeFragment.createInstance(analysisItem));
		} else if (view.getId() == R.id.sharePopupBtn) {
			LiveChessService liveService;
			try {
				liveService = getLiveService();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				return;
			}

			GameLiveItem currentGame = liveService.getGameItem();
			ShareItem shareItem = new ShareItem(currentGame, currentGame.getGameId(), getString(R.string.live));

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
		} else if (view.getId() == PanelInfoLiveView.DRAW_ACCEPT_ID) { // TODO restore logic from controlsView
			if (isLCSBound) {
				try {
					LiveChessService liveService = getLiveService();
					Log.i(TAG, "Request draw: " + liveService.getCurrentGame());
					liveService.runMakeDrawTask();
					topPanelView.showDrawOfferedView(false);
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}
			}
		} else if (view.getId() == PanelInfoLiveView.DRAW_DECLINE_ID) {
			if (isLCSBound) {
				try {
					LiveChessService liveService = getLiveService();
					Log.i(TAG, "Decline draw: " + liveService.getCurrentGame());
					liveService.runRejectDrawTask();
					topPanelView.showDrawOfferedView(false);
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}
			}
		} else if (view.getId() == R.id.rematchPopupBtn) {
			if (isLCSBound) {
				LiveChessService liveService;
				try {
					liveService = getLiveService();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
					return;
				}
				liveService.rematch();
			}
			dismissDialogs();
		}
	}

	private void init() throws DataNotValidException {
		LiveChessService liveService = getLiveService();

		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null) {
			throw new DataNotValidException(DataNotValidException.GAME_NOT_EXIST);
		}

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		notationsView.resetNotations();
		enableScreenLockTimer();

		if (!liveService.isCurrentGameExist()) {
			controlsLiveView.enableAnalysisMode(true);
			getBoardFace().setFinished(true);
		}

		liveService.setLccEventListener(this);
		liveService.setLccChatMessageListener(this);
		liveService.setGameTaskListener(gameTaskListener);

		{// fill labels
			labelsConfig = new LabelsConfig();
			if (isUserColorWhite()) {
				labelsConfig.userSide = ChessBoard.WHITE_SIDE;
				labelsConfig.topPlayerName = currentGame.getBlackUsername();
				labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
				labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
				labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
			} else {
				labelsConfig.userSide = ChessBoard.BLACK_SIDE;
				labelsConfig.topPlayerName = currentGame.getWhiteUsername();
				labelsConfig.topPlayerRating = String.valueOf(currentGame.getWhiteRating());
				labelsConfig.bottomPlayerName = currentGame.getBlackUsername();
				labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getBlackRating());
			}
		}

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String opponentName;
			if (isUserColorWhite()) {
				opponentName = currentGame.getBlackUsername();
			} else {
				opponentName = currentGame.getWhiteUsername();
			}

			String opponentAvatarUrl = liveService.getCurrentGame().getOpponentForPlayer(opponentName).getAvatarUrl(); // TODO test
			imageDownloader.download(opponentAvatarUrl, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		}



		int resignTitleId = liveService.getResignTitle();
		{// options list setup
			optionsMap = new SparseArray<String>();
			optionsMap.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
			optionsMap.put(ID_ABORT_RESIGN, getString(resignTitleId));
			optionsMap.put(ID_REMATCH, getString(R.string.rematch));
			optionsMap.put(ID_SETTINGS, getString(R.string.settings));
		}

		lccInitiated = true;
	}

	private void widgetsInit(View view) {
		fadeLay = view.findViewById(R.id.fadeLay);

		controlsLiveView = (ControlsLiveView) view.findViewById(R.id.controlsLiveView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoLiveView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoLiveView) view.findViewById(R.id.bottomPanelView);

		boardView = (ChessBoardLiveView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsLiveView);
		boardView.setNotationsView(notationsView);
		setBoardView(boardView);
		boardView.setGameFace(this);
		controlsLiveView.setBoardViewFace(boardView);
		topPanelView.setClickHandler(this);
	}

	private class LabelsConfig {
		BoardAvatarDrawable topAvatar;
		BoardAvatarDrawable bottomAvatar;
		String topPlayerName;
		String bottomPlayerName;
		String topPlayerRating;
		String bottomPlayerRating;
		int userSide;

		int getOpponentSide() {
			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}
	}

	private class ImageUpdateListener extends ImageReadyListenerLight {

		private static final int TOP_AVATAR = 0;
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
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();

					String userAvatarUrl = getAppData().getUserAvatar();
					imageDownloader.download(userAvatarUrl, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);

					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}

	protected void logLiveTest(String messageToLog) {
		Log.d(TAG, "LIVE GAME FRAGMENT: " + messageToLog);
	}
}
