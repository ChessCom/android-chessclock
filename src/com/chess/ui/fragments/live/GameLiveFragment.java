package com.chess.ui.fragments.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.live.client.Game;
import com.chess.live.rules.GameResult;
import com.chess.live.util.GameRatingClass;
import com.chess.model.DataHolder;
import com.chess.model.GameAnalysisItem;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.statics.AppConstants;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.game.GameAnalyzeFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsLiveChessFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.PanelInfoLiveView;
import com.chess.ui.views.chess_boards.ChessBoardLiveView;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsLiveView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;

import java.util.List;

import static com.chess.live.rules.GameResult.WIN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.01.13
 * Time: 11:33
 */
public class GameLiveFragment extends GameBaseFragment implements GameNetworkFace, LccChatMessageListener,
		PopupListSelectionFace {

	private static final String TAG = "LccLog-GameLiveFragment";
	private static final String WARNING_TAG = "warning message popup";
	private static final String GAME_EXPIRED_TAG = "game expired popup";

	// Options ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_OFFER_DRAW = 1;
	private static final int ID_ABORT_RESIGN = 2;
	private static final int ID_REMATCH = 3;
	private static final int ID_SETTINGS = 4;

	protected ChessBoardLiveView boardView;

	private View fadeLay;
	protected boolean lccInitiated;
	private String warningMessage;

	private NotationFace notationsFace;
	protected PanelInfoLiveView topPanelView;
	protected PanelInfoLiveView bottomPanelView;
	protected ControlsLiveView controlsView;
	private PopupOptionsMenuFragment optionsSelectFragment;

	private int gameEndTitleId;
	protected SparseArray<String> optionsMap;
	private String[] countryNames;
	private int[] countryCodes;
	private boolean userSawGameEndPopup;
	private GameBaseFragment.ImageUpdateListener topImageUpdateListener;
	private GameBaseFragment.ImageUpdateListener bottomImageUpdateListener;

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
		} else if (savedInstanceState != null) {
			gameId = savedInstanceState.getLong(GAME_ID);
		}

		topImageUpdateListener = new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR);
		bottomImageUpdateListener = new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR);

		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
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
			showToast(e.getMessage());
			logTest(e.getMessage());
		}
		enableSlideMenus(false);
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			Long currentGameId = getLiveService().getCurrentGameId();
			if (isLCSBound && currentGameId != null && currentGameId != 0) {
				onGameStarted(); // we don't need synchronized block here because it's UI thread, all calls are synchronizid
			}
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			logTest(e.getMessage());
			isLCSBound = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		logLiveTest("onPause");

		dismissEndGameDialog();
		if (isLCSBound) {
			try {
				getLiveService().setGameActivityPausedMode(true);
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				isLCSBound = false;
			}
		}
	}

	// ----------------------Lcc Events ---------------------------------------------

	protected void onGameStarted() throws DataNotValidException {
		logLiveTest("onGameStarted");

		LiveChessService liveService = getLiveService();
		GameLiveItem currentGame = liveService.getGameItem();
		gameId = currentGame.getGameId();

		ChessBoardLive.resetInstance();
		BoardFace boardFace = getBoardFace();

		Boolean isUserColorWhite = liveService.isUserColorWhite(); // should throw exception if null
		userPlayWhite = isUserColorWhite;

		boardFace.setReside(isUserColorWhite != null && !isUserColorWhite);

		getNotationsFace().resetNotations();
		boardView.resetValidMoves();

		if (liveService.getPendingWarnings().size() > 0) {
			warningMessage = liveService.getLastWarningMessage();
			popupItem.setNegativeBtnId(R.string.fair_play_policy);
			showPopupDialog(R.string.warning, warningMessage, WARNING_TAG);
		}

		showSubmitButtonsLay(false);
		getControlsView().enableAnalysisMode(false);
		getControlsView().showDefault();
		getControlsView().showHome(false);

		boardFace.setFinished(false);
		if (need2update) {
			getSoundPlayer().playGameStart();
		}

		getControlsView().haveNewMessage(currentGame.hasNewMessage());

		// avoid races on update moves logic for active game, doUpdateGame updates moves, avoid peaces disappearing and invalidmovie exception
		// vm: actually we have to invoke checkAndReplayMoves() here, because we reset a board on pause/resume everytime.
		// As for doUpdateGame() - that method updates moves only if gameLivePaused=false, so should be safe.
		// Lets see how synchronized approach is suitable here
		liveService.checkAndReplayMoves();

		liveService.checkFirstTestMove();

		liveService.setGameActivityPausedMode(false);
		liveService.checkGameEvents();

		{// fill labels
			userPlayWhite = liveService.isUserColorWhite();
			if (userPlayWhite) {
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

		invalidateGameScreen();

		boardView.updatePlayerNames(currentGame.getWhiteUsername(), currentGame.getBlackUsername());

		{// set avatars

			labelsConfig.topPlayerAvatar = liveService.getCurrentGame().
					getOpponentForPlayer(labelsConfig.bottomPlayerName).getAvatarUrl();
			if (labelsConfig.topPlayerAvatar != null && !labelsConfig.topPlayerAvatar.contains(StaticData.GIF)) {
				imageDownloader.download(labelsConfig.topPlayerAvatar, topImageUpdateListener, AVATAR_SIZE);
			}

			labelsConfig.bottomPlayerAvatar = liveService.getCurrentGame().
					getOpponentForPlayer(labelsConfig.topPlayerName).getAvatarUrl();
			if (labelsConfig.bottomPlayerAvatar != null && !labelsConfig.bottomPlayerAvatar.contains(StaticData.GIF)) {
				imageDownloader.download(labelsConfig.bottomPlayerAvatar, bottomImageUpdateListener, AVATAR_SIZE);
			}

			{ // get opponent info
				LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.topPlayerName);
				new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.TOP_PLAYER)).executeTask(loadItem);
			}
			{ // get users info
				LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.bottomPlayerName);
				new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.BOTTOM_PLAYER)).executeTask(loadItem);
			}
		}

		need2update = false;
		userSawGameEndPopup = false;
	}

	@Override
	public void onGameRefresh(final GameLiveItem gameItem) {
		logLiveTest("onGameRefresh");
		Activity activity = getActivity();
		if (activity == null) {
			logLiveTest("activity = null, quit");
			return;
		}
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (final DataNotValidException e) {
			logLiveTest(e.getMessage());
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showToast(e.getMessage());
				}
			});
			return;
		}
		BoardFace boardFace = getBoardFace();
		if (boardFace.isAnalysis() && gameItem.getGameId() == gameId) {
			return;
		} else {
			boardFace.setAnalysis(false);
			boardFace.setFinished(false);
			gameId = gameItem.getGameId();
		}


		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boardView.resetValidMoves();
			}
		});

		boardView.goToLatestMove();

		String[] actualMoves = gameItem.getMoveList().trim().split(Symbol.SPACE);
		int actualMovesSize = actualMoves.length;

		for (int i = boardFace.getMovesCount(); i < actualMovesSize; i++) {
			int[] moveFT = boardFace.parseCoordinate(actualMoves[i]);
			Move move = boardFace.convertMove(moveFT);
			// we play sound and animate only for the last move
			boolean playSound;
			if (i == actualMovesSize - 1) {
				playSound = true;
				boardView.setMoveAnimator(move, true);
			} else {
				playSound = false;
			}

			boardFace.makeMove(move, playSound);
		}

		boardFace.setMovesCount(actualMovesSize);

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				invalidateGameScreen();
				getControlsView().haveNewMessage(gameItem.hasNewMessage());
			}
		});

		liveService.checkTestMove();
	}

	@Override
	public void onGameEnd(final Game game, final String gameEndMessage) {
		final Activity activity = getActivity();
		if (activity == null || userSawGameEndPopup) {
			return;
		}
		userSawGameEndPopup = true;

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

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

				getControlsView().showAfterMatch();
			}
		});
	}

	@Override
	public void setWhitePlayerTimer(final String timeString) {
		final FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (getActivity() == null) {
					return;
				}

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

				if (getActivity() == null) {
					return;
				}

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
					dismissEndGameDialog(); // hide game end popup

					try {
						onGameStarted();
					} catch (DataNotValidException e) {
						logTest(e.getMessage());
					}
				}
			});
		}
	}

	@Override
	public void updateOpponentOnlineStatus(final boolean online) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				topPanelView.setReconnecting(online);
			}
		});
	}

	@Override
	public void expireGame() {
		showSinglePopupDialog(R.string.error, getString(R.string.game_expired), GAME_EXPIRED_TAG);
		getLastPopupFragment().setCancelable(false);
	}


	public void onConnectionBlocked(boolean blocked) {
		blockGame(blocked);
	}

	@Override
	public void onMessageReceived() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getControlsView().haveNewMessage(true);
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


	protected void showGameEndPopup(View layout, String title, String message) {
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

		String liveUsername = liveService.getUsername();
		int currentPlayerNewRating = liveService.getLastGame().getRatingForPlayer(liveUsername);
		int ratingChange = liveService.getLastGame().getRatingChangeForPlayer(liveUsername);

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
		popupItem.setCustomView(layout);

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

	protected void blockGame(final boolean block) {
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
		//LogMe.dl("TESTTEST", temporaryDebugInfo);

		liveService.makeMove(move, temporaryDebugInfo);
	}

	@Override
	public void switch2Analysis() {
	}

	@Override
	public void switch2Chat() {
		getControlsView().haveNewMessage(false);

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
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActivityFace().showPreviousFragment();
			}
		});
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
	}

	@Override
	public void invalidateGameScreen() {
		logLiveTest("GameLive invalidateGameScreen = " );
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

			topPanelView.setLabelsTextColor(themeFontColorStateList.getDefaultColor());
			bottomPanelView.setLabelsTextColor(themeFontColorStateList.getDefaultColor());
			try {
				getLiveService().paintClocks();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
			}

			boardView.updateNotations(getBoardFace().getNotationArray());
			boardView.invalidate();
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
			getActivityFace().openFragment(SettingsLiveChessFragment.createInstance(true));
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private boolean isUserMove() {
		return isUserColorWhite() ? getBoardFace().isWhiteToMove() : !getBoardFace().isWhiteToMove();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {  // TODO remove arg and get state from boardFace
		getControlsView().showSubmitButtons(show);

		if (!show) {
			getBoardFace().setSubmit(false);
		}
	}

	@Override
	public Long getGameId() {
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

		} else if (tag.equals(ABORT_GAME_TAG)) {
			if (isLCSBound) {

				Game game = liveService.getCurrentGame();

				if (liveService.isFairPlayRestriction()) {
					Log.i(TAG, "resign game by fair play restriction: " + game);
				} else {
					Log.i(TAG, "resign game: " + game);
				}
				liveService.runMakeResignTask();
			}
		} else if (tag.equals(GAME_EXPIRED_TAG)) {
			goHome();
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
			try {
				onGameStarted();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				isLCSBound = false;
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
			getActivityFace().toggleRightMenu();
			dismissEndGameDialog();
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
			dismissEndGameDialog();

			getActivityFace().openFragment(new LiveGameWaitFragment());
			DataHolder.getInstance().setLiveGameOpened(true);
		} else if (view.getId() == R.id.analyzePopupBtn) {
			GameAnalysisItem analysisItem = new GameAnalysisItem();  // TODO reuse later
			analysisItem.setGameType(RestHelper.V_GAME_CHESS);
			analysisItem.setFen(getBoardFace().generateFullFen());
			analysisItem.setMovesList(getBoardFace().getMoveListSAN());
			analysisItem.copyLabelConfig(labelsConfig);

			getActivityFace().openFragment(GameAnalyzeFragment.createInstance(analysisItem));
			dismissEndGameDialog();

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
		}
	}

	protected void init() throws DataNotValidException {
		LiveChessService liveService = getLiveService();

		if (!liveService.isActiveGamePresent()) {
			getControlsView().enableAnalysisMode(true);
			getBoardFace().setFinished(true);
		}

		liveService.setLccChatMessageListener(this);

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

	protected class GetUserUpdateListener extends ChessUpdateListener<UserItem> {

		static final int BOTTOM_PLAYER = 0;
		static final int TOP_PLAYER = 1;

		private int itemCode;

		public GetUserUpdateListener(int itemCode) {
			super(UserItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);
			UserItem.Data userInfo = returnedObj.getData();
			if (itemCode == BOTTOM_PLAYER) {
				labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, userInfo.getCountryId());

				bottomPanelView.setPlayerFlag(labelsConfig.bottomPlayerCountry);
				bottomPanelView.setPlayerPremiumIcon(userInfo.getPremiumStatus());
			} else if (itemCode == TOP_PLAYER) {
				labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, userInfo.getCountryId());

				topPanelView.setPlayerFlag(labelsConfig.topPlayerCountry);
				topPanelView.setPlayerPremiumIcon(userInfo.getPremiumStatus());
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {

		}
	}

	protected ControlsLiveView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsLiveView) controlsView;
	}

	public void setNotationsFace(View notationsView) {
		this.notationsFace = (NotationFace) notationsView;
	}

	public NotationFace getNotationsFace() {
		return notationsFace;
	}

	@Override
	protected View getTopPanelView() {
		return topPanelView;
	}

	@Override
	protected View getBottomPanelView() {
		return bottomPanelView;
	}

	protected void widgetsInit(View view) {
		fadeLay = view.findViewById(R.id.fadeLay);

		setControlsView(view.findViewById(R.id.controlsView));
		if (inPortrait()) {
			setNotationsFace(view.findViewById(R.id.notationsView));
		} else {
			setNotationsFace(view.findViewById(R.id.notationsViewTablet));
		}

		topPanelView = (PanelInfoLiveView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoLiveView) view.findViewById(R.id.bottomPanelView);

		boardView = (ChessBoardLiveView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(getControlsView());
		boardView.setNotationsFace(getNotationsFace());
		setBoardView(boardView);
		boardView.setGameFace(this);
		getControlsView().setBoardViewFace(boardView);
		getControlsView().showHome(false);
		topPanelView.setClickHandler(this);

		{ // set avatars views
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			{ // set stubs while avatars are loading
				Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
						R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);

				labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);

				labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
				topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
				topPanelView.invalidate();

				labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), src);

				labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
				bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
				bottomPanelView.invalidate();
			}
		}
	}

	protected void logLiveTest(String messageToLog) {
		LogMe.dl(TAG, "LIVE GAME FRAGMENT: " + messageToLog);
	}
}
