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
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LccHelper;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.live.client.Game;
import com.chess.live.rules.GameResult;
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
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.RightPlayFragment;
import com.chess.ui.fragments.game.GameAnalyzeFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
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
import com.chess.utilities.MopubHelper;
import com.chess.widgets.ProfileImageView;

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

	// Options ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_OFFER_DRAW = 1;
	private static final int ID_ABORT_RESIGN = 2;
	private static final int ID_REMATCH = 3;
	private static final int ID_SHARE_GAME = 4;
	private static final int ID_SETTINGS = 5;

	protected ChessBoardLiveView boardView;

	protected View fadeLay;
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
	private ImageUpdateListener topImageUpdateListener;
	private ImageUpdateListener bottomImageUpdateListener;
	private boolean submitClicked;
	private int previousSide;

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
		return inflater.inflate(R.layout.game_live_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getActivityFace().setCustomActionBarViewId(R.layout.home_actionbar);

//		if (gameId != 0 && !isValid()) { // when user returns by back stack // TODO this method might not be called when returning from backstack because view was already initialized
//			goHome();                    // there is nothing related to widgets, so they should be initialized anyway.
//			return;
//		}

		widgetsInit(view);
		try {
			init();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			logTest(e.getMessage());
		}
		enableSlideMenus(false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (gameId != 0 && !isValid()) { // when user returns by back stack
			goHome();
			return;
		}

//		LogMe.dl("lcc", "```````````````````````````````````````````" );
//		LogMe.dl("lcc", " fragments to receive onLiveClientConnected " );
//		for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
//			if (fragment != null) {
//				LogMe.dl("lcc", "onResume fragment = " + fragment.getClass().getSimpleName() );
//			}
//		}
//		LogMe.dl("lcc", "```````````````````````````````````````````" );

		if (isLCSBound) {
			try {
				LiveConnectionHelper liveHelper = getLiveHelper();
				if (getCurrentGame(liveHelper) != null) {
					onGameStarted();
				}
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				setLCSBound(false);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		dismissEndGameDialog();
		if (isLCSBound) {
			try {
				LiveConnectionHelper liveHelper = getLiveHelper();
				liveHelper.setGameActivityPausedMode(true);
				// I think we have to still tick clock even when fragment is in background, because we still have to show
				// actual clock values when user returns to fragment back but it is impossible to get time values from
				// liveHelper that moment
				//liveHelper.stopClocks();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				setLCSBound(false);
			}
		}
	}

	// ----------------------Lcc Events ---------------------------------------------

	protected void onGameStarted() throws DataNotValidException {
		logLiveTest("onGameStarted");
		LiveConnectionHelper liveHelper = getLiveHelper();
		liveHelper.setGameActivityPausedMode(false);

		synchronized (LccHelper.GAME_SYNC_LOCK) {

			GameLiveItem currentGame = getGameItem(liveHelper);

			logLiveTest("onGameStarted currentGame=" + currentGame);

			if (currentGame == null) { // this happens when we resume to fragment via back navigation
				throw new DataNotValidException(DataNotValidException.GAME_NOT_EXIST);
			}
			gameId = currentGame.getGameId();

			optionsMapInit();

			resetBoardInstance();
			BoardFace boardFace = getBoardFace();

			Boolean isUserColorWhite = liveHelper.isUserColorWhite(); // should throw exception if null
			userPlayWhite = isUserColorWhite;

			boardFace.setReside(isUserColorWhite != null && !isUserColorWhite);

			getNotationsFace().resetNotations();

			if (liveHelper.getPendingWarnings().size() > 0) {
				warningMessage = liveHelper.getLastWarningMessage();
				popupItem.setNegativeBtnId(R.string.fair_play_policy);
				showPopupDialog(R.string.warning, warningMessage, WARNING_TAG);
			}

			showSubmitButtonsLay(false);
			getControlsView().showAnalysis(false);
			getControlsView().showDefault();
			getControlsView().showHome(false);

			boardFace.setFinished(false);
			if (need2update) {
				getSoundPlayer().playGameStart();
			}

			getControlsView().haveNewMessage(currentGame.hasNewMessage());
			liveHelper.checkAndReplayMoves();
			liveHelper.checkFirstTestMove();
			liveHelper.checkGameEvents();

			{// fill labels
				userPlayWhite = liveHelper.isUserColorWhite();
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
			liveHelper.initClocks();


			boardView.resetValidMoves();
			invalidateGameScreen();

			boardView.updatePlayerNames(currentGame.getWhiteUsername(), currentGame.getBlackUsername());

			{// set avatars
				Game game = getCurrentGame(liveHelper);
				if (game != null) {
					if (getBoardFace().isReside()) {
						labelsConfig.bottomPlayerAvatar = game.getBlackPlayer().getAvatarUrl();
						labelsConfig.topPlayerAvatar = game.getWhitePlayer().getAvatarUrl();
					} else {
						labelsConfig.bottomPlayerAvatar = game.getWhitePlayer().getAvatarUrl();
						labelsConfig.topPlayerAvatar = game.getBlackPlayer().getAvatarUrl();
					}
				}

				if (labelsConfig.topPlayerAvatar != null && labelsConfig.topPlayerAvatar.contains(StaticData.GIF)) {
					imageDownloader.download(labelsConfig.topPlayerAvatar, topImageUpdateListener, AVATAR_SIZE);
				}

				if (labelsConfig.bottomPlayerAvatar != null && labelsConfig.bottomPlayerAvatar.contains(StaticData.GIF)) {
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
	}

	@Override
	public void onGameRefresh(final GameLiveItem gameItem) {
		logLiveTest("onGameRefresh");
		Activity activity = getActivity();
		if (activity == null) {
			logLiveTest("activity = null, quit");
			return;
		}
		final LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (final DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}
		final BoardFace boardFace = getBoardFace();
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

				if (getActivity() == null) {
					return;
				}

				invalidateGameScreen();
				getControlsView().haveNewMessage(gameItem.hasNewMessage());

				if (boardFace.isWhiteToMove()) {
					if (boardFace.isReside()) { // white on top
						bumpTopTimer();
					} else { // white on bottom
						bumpBottomTimer();
					}
					if (!getBoardFace().isSubmit()) {
						topPanelView.showTimeLeftIcon(true);
						bottomPanelView.showTimeLeftIcon(false);
					}
				} else { // if black to move
					if (boardFace.isReside()) { // black on bottom
						bumpBottomTimer();
					} else { // black on top
						bumpTopTimer();
					}
					if (!getBoardFace().isSubmit()) {
						topPanelView.showTimeLeftIcon(true);
						bottomPanelView.showTimeLeftIcon(false);
					}
				}
				liveHelper.checkTestMove();
			}
		});
	}

	@Override
	public void onGameEnd(final Game game, final String gameEndMessage) {
		final Activity activity = getActivity();
		if (activity == null || userSawGameEndPopup) {
			return;
		}
		userSawGameEndPopup = true;

		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
			liveHelper.stopClocks();

			if (getCurrentGame(liveHelper) == null) {
				return;
			}

		} catch (DataNotValidException e) {
			e.printStackTrace();
		}

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
			gameEndTitleId = R.string.game_end_title_draw;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final View endGamePopupView;

				initShowAdsFlag();

				if (!isNeedToUpgrade()) {
					endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
				} else if (isNeedToUpgrade() && !showAdsForNewMembers) {
					endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
				} else {
					endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
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
				showGameEndPopup(endGamePopupView, getString(gameEndTitleId), gameEndMessage, game);

				setBoardToFinishedState();

				getControlsView().showAfterMatch();
			}
		});
	}

	@Override
	public void onClockFinishing() {
		Activity activity = getActivity();
		if (activity != null && isResumed()) {
			getActivityFace().getSoundPlayer().playTenSeconds();
		}
	}

	@Override
	public void showPiecesMovesAnimation(boolean show) {
		boardView.setShowMovesAnimation(show);
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

				if (getActivity() == null || !isResumed()) {
					return;
				}

				boolean whiteToMove = getBoardFace().isWhiteToMove();

				if (getBoardFace().isReside()) { // if white at top

					if (!getBoardFace().isSubmit()) {
						topPanelView.showTimeLeftIcon(whiteToMove);
						bottomPanelView.showTimeLeftIcon(!whiteToMove);
					}

					topPanelView.setTimeRemain(timeString);
				} else {

					if (!getBoardFace().isSubmit()) {
						topPanelView.showTimeLeftIcon(!whiteToMove);
						bottomPanelView.showTimeLeftIcon(whiteToMove);
					}

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

				if (getActivity() == null || !isResumed()) {
					return;
				}

				boolean blackToMove = !getBoardFace().isWhiteToMove();

				if (getBoardFace().isReside()) {

					if (!getBoardFace().isSubmit()) {
						topPanelView.showTimeLeftIcon(!blackToMove);
						bottomPanelView.showTimeLeftIcon(blackToMove);
					}

					bottomPanelView.setTimeRemain(timeString);
				} else {

					if (!getBoardFace().isSubmit()) {
						topPanelView.showTimeLeftIcon(blackToMove);
						bottomPanelView.showTimeLeftIcon(!blackToMove);
					}

					topPanelView.setTimeRemain(timeString);
				}
			}
		});
	}

	private void bumpBottomTimer() {
		topPanelView.cancelTimerBump();
		bottomPanelView.startTimerBump();
	}

	private void bumpTopTimer() {
		bottomPanelView.cancelTimerBump();
		topPanelView.startTimerBump();
	}

	// ----------------------Lcc Events ---------------------------------------------

	@Override
	public void startGameFromService() {
		logLiveTest("startGameFromService");

		final LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
			showToast(e.getMessage());
			return;
		}

		boolean needToChangeFragment = liveHelper.isCurrentGameObserved();
		checkFragmentAndStartGame(needToChangeFragment, liveHelper);
	}

	protected void checkFragmentAndStartGame(final boolean needToChangeFragment, final LiveConnectionHelper liveHelper) {

		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dismissProgressDialog();
					dismissEndGameDialog(); // hide game end popup

					if (needToChangeFragment) {
						openLiveFragment(liveHelper);
					} else {
						try {
							onGameStarted();
						} catch (DataNotValidException e) {
							logTest(e.getMessage());
						}
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

		Activity activity = getActivity();
		if (activity == null) {
			return;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isAdded()) {
					return;
				}
				showSinglePopupDialog(R.string.error, getString(R.string.game_expired));

				setBoardToFinishedState();
				getControlsView().showAfterMatch();

				try {
					getLiveHelper().stopClocks(); // wait for LCC fix
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}
			}
		});
	}


	public void onConnectionBlocked(boolean blocked) {
		blockGame(blocked);
	}

	@Override
	public void onMessageReceived() {

		Activity activity = getActivity();
		if (activity == null) {
			return;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getControlsView().haveNewMessage(true);
				boardView.invalidateMe();
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


	protected void showGameEndPopup(View layout, String title, String message, Game game) {
		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView ratingTitleTxt = (TextView) layout.findViewById(R.id.ratingTitleTxt);
		TextView resultRatingTxt = (TextView) layout.findViewById(R.id.resultRatingTxt);
		TextView resultRatingChangeTxt = (TextView) layout.findViewById(R.id.resultRatingChangeTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating;
		int ratingChange;
		String liveUsername = getUsername();
		if (game.getWhitePlayer().getUsername().equals(liveUsername)) {
			currentPlayerNewRating = game.getRatingForPlayer(game.getWhitePlayer().getUsername());
			ratingChange = game.getRatingChangeForPlayer(game.getWhitePlayer().getUsername());
		} else {
			currentPlayerNewRating = game.getRatingForPlayer(game.getBlackPlayer().getUsername());
			ratingChange = game.getRatingChangeForPlayer(game.getBlackPlayer().getUsername());
		}

		String ratingChangeString = Symbol.wrapInPars(ratingChange > 0 ? "+" + ratingChange : "" + ratingChange);

		resultRatingTxt.setText(String.valueOf(currentPlayerNewRating));
		resultRatingChangeTxt.setText(ratingChangeString);

		ratingTitleTxt.setVisibility(View.GONE);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		PopupGameEndFragment endPopupFragment = PopupGameEndFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		int mode = getAppData().getDefaultLiveMode();
		// set texts to buttons
		String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
		String newGameStr = getString(R.string.new_arg, AppUtils.getLiveModeButtonLabel(newGameButtonsArray[mode], getContext()));
		Button newGameButton = (Button) layout.findViewById(R.id.newGamePopupBtn);
		newGameButton.setText(newGameStr);
		newGameButton.setOnClickListener(this);

		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.analyzePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.sharePopupBtn).setOnClickListener(this);

		if (isNeedToUpgrade() && showAdsForNewMembers) {
			initPopupAdWidget(layout);
			MopubHelper.showRectangleAd(getMopubRectangleAd(), getActivity());
		}
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
				if (getActivity() == null || fadeLay == null) {
					return;
				}

				if (block && getBoardFace().isSubmit()) {
					cancelMove();
				}

				showLoadingProgress(block);
				fadeLay.setVisibility(block ? View.VISIBLE : View.INVISIBLE);
				boardView.lockBoard(block);
			}
		});
	}

	protected void submitMove() throws DataNotValidException {
		LiveConnectionHelper liveHelper = getLiveHelper();

		String debugString = " no debug log";
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
				"username=" + liveHelper.getUsername() +
						" lccInitiated=" + lccInitiated +
//						", " + boardDebug +
						", gameSeq=" + getCurrentGame(liveHelper).getMoves().size() +
						", boardHply=" + getBoardFace().getPly() +
						", moveLive=" + getBoardFace().convertMoveLive() +
						", gamesC=" + liveHelper.getGamesCount() +
						", gameId=" + getGameId() +
//						", analysisPanel=" + gamePanelView.isAnalysisEnabled() +
						", analysisBoard=" + getBoardFace().isAnalysis() +
						", latestMoveNumber=" + liveHelper.getLatestMoveNumber() +
						", debugString=" + debugString +
						", submit=" + preferences.getBoolean(getAppData().getUsername() + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false) +
						", movesLive=" + getCurrentGame(liveHelper).getMoves() +
						", moves=" + getBoardFace().getMoveListSAN() +
						", trace=" + stackTrace;
		temporaryDebugInfo = temporaryDebugInfo.replaceAll("\n", " ");
		//LogMe.dl("TESTTEST", temporaryDebugInfo);

		liveHelper.makeMove(move, temporaryDebugInfo);
	}

	@Override
	public void switch2Analysis() {
		GameAnalysisItem analysisItem = new GameAnalysisItem();
		analysisItem.setGameType(RestHelper.V_GAME_CHESS);
		analysisItem.setFen(getBoardFace().generateFullFen());
		analysisItem.setMovesList(getBoardFace().getMoveListSAN());
		analysisItem.copyLabelConfig(labelsConfig);

		getActivityFace().openFragment(GameAnalyzeFragment.createInstance(analysisItem));
	}

	@Override
	public void switch2Chat() {
		getControlsView().haveNewMessage(false);

		getActivityFace().openFragment(new LiveChatFragment());
	}

	@Override
	public void playMove() {

		if (!getBoardFace().isSubmit()) {
			return;
		}

		if (!submitClicked) {
			submitClicked = true;

			try {
				submitMove();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
			}
		}

	}

	@Override
	public void cancelMove() {
		// cancelMove() code is the same in Game Live/Daily fragments. Probably could be moved to some common place
		showSubmitButtonsLay(false);
		boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
		boardView.resetValidMoves();
		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();
		boardView.invalidateMe();
	}

	@Override
	public void goHome() {
		if (getActivity() == null) {
			return;
		}
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showPreviousFragmentSafe();
			}
		});
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(RightPlayFragment.createInstance(RIGHT_MENU_MODE));
	}

	@Override
	public void updateAfterMove() {
		if (!getBoardFace().isAnalysis()) {
			try {
				submitMove();
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
				getLiveHelper().updatePlayersClock();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
			}

			boardView.updateNotations(getBoardFace().getNotationsArray());
		}
	}

	@Override
	public void showOptions() {
		if (optionsMap == null) {
			return;
		}

		// hide if we press it again
		if (optionsSelectFragment != null) {
			optionsSelectFragment.dismiss();
			return;
		}

		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}

		boolean isGameOver = !liveHelper.isActiveGamePresent();

		if (isGameOver) {
			optionsMap.put(ID_REMATCH, getString(R.string.rematch));
//			optionsMap.remove(ID_OFFER_DRAW); // re move it from here, but will add a logic to handle it in onClick to see if it help with problem that player are unable to claim draw
//			optionsMap.remove(ID_ABORT_RESIGN);
		} else {
			optionsMap.remove(ID_REMATCH);
		}

		if (getBoardFace().getPly() < 1 && isUserMove()) {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.abort));
			optionsMap.remove(ID_OFFER_DRAW);
		} else {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.resign));
			optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
		}

		if (!isUserMove()) { // user able to offer draw only when it's his turn
			optionsMap.remove(ID_OFFER_DRAW);
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
			try {
				boolean isGameExist = getLiveHelper().isActiveGamePresent();
				if (!isGameExist) {
					optionsSelectFragment.dismiss();
					optionsSelectFragment = null;
					return;
				}
			} catch (DataNotValidException e) {
				e.printStackTrace();
			}

			if (getBoardFace().getPly() < 1 && isUserMove()) {
				showPopupDialog(R.string.abort_game_, ABORT_GAME_TAG);
			} else {
				showPopupDialog(R.string.resign_game_, ABORT_GAME_TAG);
			}
		} else if (code == ID_OFFER_DRAW) {
			try {
				boolean isGameExist = getLiveHelper().isActiveGamePresent();
				if (!isGameExist) {
					optionsSelectFragment.dismiss();
					optionsSelectFragment = null;
					return;
				}
			} catch (DataNotValidException e) {
				e.printStackTrace();
			}
			showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
		} else if (code == ID_REMATCH) {
			try {
				getLiveHelper().rematch();
			} catch (DataNotValidException e) {
				e.printStackTrace();
			}
		} else if (code == ID_SHARE_GAME) {
			shareGame();
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

		if (show) {
			submitClicked = false;
		} else {
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
		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}

		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			if (isLCSBound) {
				Log.i(TAG, "Request draw: " + getCurrentGame(liveHelper));
				liveHelper.runMakeDrawTask();
			}
		} else if (tag.equals(WARNING_TAG)) {
			if (isLCSBound) {
				liveHelper.getPendingWarnings().remove(warningMessage);
			}

		} else if (tag.equals(ABORT_GAME_TAG)) {
			if (isLCSBound) {

				Game game = getCurrentGame(liveHelper);

				if (liveHelper.isFairPlayRestriction()) {
					Log.i(TAG, "resign game by fair play restriction: " + game);
				} else {
					Log.i(TAG, "resign game: " + game);
				}
				liveHelper.runMakeResignTask();
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
		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}
		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			if (isLCSBound) {
				Log.i(TAG, "Decline draw: " + getCurrentGame(liveHelper));
				liveHelper.runRejectDrawTask();
			}
		} else if (tag.equals(WARNING_TAG)) {
			if (isLCSBound) {
				liveHelper.getPendingWarnings().remove(warningMessage);
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.FAIR_POLICY_LINK)));
		}
		super.onNegativeBtnClick(fragment);
	}

	// ---------------- Players names and labels -----------------------------------------------------------------

	@Override
	public String getWhitePlayerName() {
		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return Symbol.EMPTY;
		}
		GameLiveItem currentGame = getGameItem(liveHelper);
		if (currentGame == null)
			return Symbol.EMPTY;
		else
			return currentGame.getWhiteUsername();
	}

	@Override
	public String getBlackPlayerName() {
		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return Symbol.EMPTY;
		}
		GameLiveItem currentGame = getGameItem(liveHelper);
		if (currentGame == null)
			return Symbol.EMPTY;
		else
			return currentGame.getBlackUsername();
	}

	@Override
	public boolean currentGameExist() {
		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return false;
		}
		return getCurrentGame(liveHelper) != null;
	}

	@Override
	public BoardFace getBoardFace() {
		if (chessBoard == null) {
			chessBoard = new ChessBoardLive(this);
		}
		return chessBoard;
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
				setLCSBound(false);
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			LiveGameConfig gameConfig = getAppData().getLiveGameConfigBuilder().build();
			getActivityFace().openFragment(LiveGameWaitFragment.createInstance(gameConfig));

			dismissEndGameDialog();
		} else if (view.getId() == R.id.rematchPopupBtn) {
			if (isLCSBound) {
				LiveConnectionHelper liveHelper;
				try {
					liveHelper = getLiveHelper();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
					return;
				}
				liveHelper.rematch();
			}
			dismissEndGameDialog();

			getActivityFace().openFragment(new LiveGameWaitFragment());
			DataHolder.getInstance().setLiveGameOpened(true);
		} else if (view.getId() == R.id.analyzePopupBtn) {
			switch2Analysis();
			dismissEndGameDialog();
		} else if (view.getId() == R.id.sharePopupBtn) {
			shareGame();
		} else if (view.getId() == PanelInfoLiveView.DRAW_ACCEPT_ID) {
			if (isLCSBound) {
				try {
					LiveConnectionHelper liveHelper = getLiveHelper();
					Log.i(TAG, "Request draw: " + liveHelper.getCurrentGame());
					liveHelper.runMakeDrawTask();
					topPanelView.showDrawOfferedView(false);
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}
			}
		} else if (view.getId() == PanelInfoLiveView.DRAW_DECLINE_ID) {
			if (isLCSBound) {
				try {
					LiveConnectionHelper liveHelper = getLiveHelper();
					Log.i(TAG, "Decline draw: " + liveHelper.getCurrentGame());
					liveHelper.runRejectDrawTask();
					topPanelView.showDrawOfferedView(false);
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}
			}
		}
	}

	private void shareGame() {
		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			return;
		}

		GameLiveItem currentGame = getGameItem(liveHelper);
		ShareItem shareItem = new ShareItem(currentGame, currentGame.getGameId(), ShareItem.LIVE);

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
	}

	protected void init() throws DataNotValidException {
		LiveConnectionHelper liveHelper = getLiveHelper();

		if (!liveHelper.isActiveGamePresent()) {
			getControlsView().showAnalysis(true);
			getBoardFace().setFinished(true);
		}

		liveHelper.setLccChatMessageListener(this);

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
			// don't show error here
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.RESOURCE_NOT_FOUND) {
					return;
				}
			}
			super.errorHandle(resultCode);
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
			topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			{ // set stubs while avatars are loading
				Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
						R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);

				labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);

				labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
				topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
				topPanelView.invalidateMe();

				labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), src);

				labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
				bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
				bottomPanelView.invalidateMe();
			}
		}
	}

	@Override
	public void onLiveClientConnected() {
		super.onLiveClientConnected();

		if (!isAdded()) {
			return;
		}

		if (!isValid()) {
			goHome();
			return;
		}

		try {
			init();
			LiveConnectionHelper liveHelper = getLiveHelper();
			if (isLCSBound && getCurrentGame(liveHelper) != null) {
				// screen rotated case
				onGameStarted();
			}
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
			logTest(e.getMessage());
			setLCSBound(false);
		}
	}

	@Override
	public void onLiveServiceConnected() {
		super.onLiveServiceConnected();

		if (!isValid()) {
			goHome();
		}
	}

	protected void logLiveTest(String messageToLog) {
		LogMe.dl(TAG, "LIVE GAME FRAGMENT: " + messageToLog);
	}

	protected void optionsMapInit() throws DataNotValidException {
		LiveConnectionHelper liveHelper = getLiveHelper();
		int resignTitleId = liveHelper.getResignTitle();

		optionsMap = new SparseArray<String>();
		optionsMap.put(ID_NEW_GAME, getString(R.string.new_game));
		optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
		optionsMap.put(ID_ABORT_RESIGN, getString(resignTitleId));
		optionsMap.put(ID_REMATCH, getString(R.string.rematch));
		optionsMap.put(ID_SHARE_GAME, getString(R.string.share_game));
		optionsMap.put(ID_SETTINGS, getString(R.string.settings));
	}

	public boolean isValid() {

		LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			e.printStackTrace();
			return false;
		}

		boolean isGameValid = getCurrentGame(liveHelper) != null;

		return isGameValid;
	}

	protected GameLiveItem getGameItem(LiveConnectionHelper liveHelper) {
		return liveHelper.getGameItem();
	}

	protected Game getCurrentGame(LiveConnectionHelper liveHelper) {
		return liveHelper.getCurrentGame();
	}
}
