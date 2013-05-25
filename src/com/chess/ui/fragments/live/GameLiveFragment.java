package com.chess.ui.fragments.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.live.rules.GameResult;
import com.chess.live.util.GameRatingClass;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.NewGamesFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.settings.SettingsFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameNetworkActivityFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardLiveView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsLiveView;
import com.chess.utilities.AppUtils;
import quickaction.ActionItem;
import quickaction.QuickAction;

import java.util.List;

import static com.chess.live.rules.GameResult.WIN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.01.13
 * Time: 11:33
 */
public class GameLiveFragment extends GameBaseFragment implements GameNetworkActivityFace, LccEventListener, LccChatMessageListener, QuickAction.OnActionItemClickListener {


	private static final String TAG = "GameLiveScreenActivity";
	private static final String WARNING_TAG = "warning message popup";

	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	//	private static final int ID_FLIP_BOARD = 1;
	private static final int ID_OFFER_DRAW = 2;
	private static final int ID_RESIGN_ABORT = 3;
	private static final int ID_SETTINGS = 4;
	private static final String GAME_ID = "game_id";

	//	private GameLiveItem currentGame;
	private ChessBoardLiveView boardView;

	private String whiteTimer;
	private String blackTimer;
	private View fadeLay;
	private View gameBoardView;
	private boolean lccInitiated;
	private String warningMessage;
	private ChessUpdateListener<Game> gameTaskListener;


	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsLiveView controlsLiveView;
	private QuickAction quickAction;
	private long gameId;
	private LabelsConfig labelsConfig;
	//	private UserInfoUpdateListener userInfoUpdateListener;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ImageDownloaderToListener imageDownloader;
	private int gameEndTitleId;

	public GameLiveFragment() {
	}

	public static GameLiveFragment newInstance(long id) {
		GameLiveFragment fragment = new GameLiveFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameTaskListener = new ChessUpdateListener<Game>();
//		userInfoUpdateListener = new UserInfoUpdateListener();
		imageDownloader = new ImageDownloaderToListener(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_live, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live_chess);

		widgetsInit(view);
		try {
			init();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isLCSBound) {
			try {
				onGameStarted();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				isLCSBound = false;
			}
		}
	}

	@Override
	public void onPause() {
		dismissDialogs();

		super.onPause();
		if (isLCSBound) {
			try {
				getLiveService().setGameActivityPausedMode(true);
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				isLCSBound = false;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(GAME_ID, gameId);
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
		logTest("onGameStarted");

		LiveChessService liveService = getLiveService();
		GameLiveItem currentGame = liveService.getGameItem();

		ChessBoardLive.resetInstance();
		boardView.setGameActivityFace(this);

		if (!liveService.isUserColorWhite()) {
			getBoardFace().setReside(true);
		}
		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		notationsView.resetNotations();
//		boardView.invalidate();

		invalidateGameScreen();
		if (liveService.getPendingWarnings().size() > 0) {
			warningMessage = liveService.getLastWarningMessage();
			Log.d("LCCLOG-WARNING", warningMessage);
			popupItem.setNegativeBtnId(R.string.fair_play_policy);
			showPopupDialog(R.string.warning, warningMessage, WARNING_TAG);
		}

		showSubmitButtonsLay(false);
		controlsLiveView.enableAnalysisMode(false);
		getBoardFace().setFinished(false);
		getSoundPlayer().playGameStart();

		if (currentGame.hasNewMessage()) {
			controlsLiveView.haveNewMessage(true);
		}

		if (liveService.getCurrentGame().isGameOver()) { // avoid races on update moves logic for active game, doUpdateGame updates moves, avoid peaces disappearing and invalidmovie exception
			liveService.checkAndReplayMoves();
		}

		// temporary disable playLastMoveAnimation feature, because it can be one of the illegalmove reasons potentially
		// todo: probably could be enabled with new LCC
		/*invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();
		playLastMoveAnimation();*/

		liveService.checkFirstTestMove();
		liveService.executePausedActivityGameEvents();

	}

	@Override
	public void setWhitePlayerTimer(String timeString) {
		whiteTimer = timeString;
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getBoardFace().isReside()) {
					topPanelView.setTimeLeft(whiteTimer);
				} else {
					bottomPanelView.setTimeLeft(whiteTimer);
				}
			}
		});
	}

	@Override
	public void setBlackPlayerTimer(String timeString) {
		blackTimer = timeString;
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() { // TODO add check
			@Override
			public void run() {
				if (getBoardFace().isReside()) {
					bottomPanelView.setTimeLeft(blackTimer);
				} else {
					topPanelView.setTimeLeft(blackTimer);
				}
			}
		});
	}

	// ----------------------Lcc Events ---------------------------------------------

//	@Override
//	public void onGameRecreate() {
//		getActivityFace().showPreviousFragment();
//	}

	@Override
	public void startGameFromService() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dismissProgressDialog();
					dismissDialogs(); // hide game end popup

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
	public void createSeek() {
		// shouldn't be used here. Use in WaitFragment instead
	}

	@Override
	public void onGameRefresh(GameLiveItem gameItem) {
		logTest("onGameRefresh");
		Activity activity = getActivity();
		if (activity == null) {
			logTest("activity = null, quit");
			return;
		}
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
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

			moveFT = MoveParser.parseCoordinate(getBoardFace(), actualMoves[i]);

			if (moveFT.length == 4) {
				if (moveFT[3] == 2) {
					move = new Move(moveFT[0], moveFT[1], 0, 2);
				} else {
					move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
				}
			} else {
				move = new Move(moveFT[0], moveFT[1], 0, 0);
			}
			playSound = i == actualMovesSize - 1;
			getBoardFace().makeMove(move, playSound);
		}

		getBoardFace().setMovesCount(actualMovesSize);

		if (!liveService.isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				boardView.setGameActivityFace(GameLiveFragment.this);
				boardView.invalidate();
				invalidateGameScreen();
			}
		});

		liveService.checkTestMove();

		if (gameItem.hasNewMessage()) {
			controlsLiveView.haveNewMessage(true);
		}
	}

//	@Override            // TODO restore
//	public void onConnectionBlocked(boolean blocked) {
//		super.onConnectionBlocked(blocked);
//		if (blocked) {
//			blockGame(blocked);
//		}
//	}

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
		// TODO show button at top panel view
//		drawButtonsLay = getView().findViewById(R.id.drawButtonsLay);
//		drawTitleTxt = (TextView) getView().findViewById(R.id.drawTitleTxt);
//		getView().findViewById(R.id.acceptDrawBtn).setOnClickListener(GameLiveFragment.this);
//		getView().findViewById(R.id.declineDrawBtn).setOnClickListener(GameLiveFragment.this);
//		drawButtonsLay.setVisibility(View.VISIBLE);
//
//		String message = drawOfferUsername + StaticData.SYMBOL_SPACE + getString(R.string.has_offered_draw);
//		drawTitleTxt.setText(message);
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
			logTest(e.getMessage());
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

				updatePlayerLabels(game, ratings.get(0), ratings.get(1));
				showGameEndPopup(layout, getString(gameEndTitleId), gameEndMessage);

				setBoardToFinishedState();
			}
		});

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
				gameBoardView.invalidate();
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
						"lccInitiated=" + lccInitiated +
//						", " + boardDebug +
						", gameSeq=" + liveService.getCurrentGame().getMoves().size() +
						", boardHply=" + getBoardFace().getHply() +
						", moveLive=" + getBoardFace().convertMoveLive() +
						", gamesC=" + liveService.getGamesCount() +
						", gameId=" + getGameId() +
//						", analysisPanel=" + gamePanelView.isAnalysisEnabled() +
						", analysisBoard=" + getBoardFace().isAnalysis() +
						", latestMoveNumber=" + liveService.getLatestMoveNumber() +
						", debugString=" + debugString +
						", submit=" + preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false) +
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
//			logTest(e.getMessage());
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
			logTest(e.getMessage());
		}
	}

	@Override
	public void cancelMove() {
		showSubmitButtonsLay(false);
		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();
		boardView.invalidate();
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(NewGamesFragment.newInstance(NewGamesFragment.RIGHT_MENU_MODE));
	}

	@Override
	public void updateAfterMove() {
		if (!getBoardFace().isAnalysis()) {
			try {
				sendMove();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
			}
		}
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

			topPanelView.activateTimer(true);
			bottomPanelView.activateTimer(true);

			topPanelView.setSide(labelsConfig.getOpponentSide());
			bottomPanelView.setSide(labelsConfig.userSide);

			topPanelView.setPlayerLabel(labelsConfig.topPlayerLabel);
			bottomPanelView.setPlayerLabel(labelsConfig.bottomPlayerLabel);

			boardView.updateNotations(getBoardFace().getNotationArray());
			try {
				getLiveService().paintClocks();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
			}
		}
	}

	@Override
	public void showOptions(View view) {
		quickAction.show(view);
		quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
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
			logTest(e.getMessage());
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
//			logTest(e.getMessage());
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
			logTest(e.getMessage());
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

			try {
				onGameStarted();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
			}

			if (!isUserColorWhite()) {
				getBoardFace().setReside(true);
			}
		} else if (tag.equals(ABORT_GAME_TAG)) {
			if (isLCSBound) {

				Game game = liveService.getCurrentGame();

				if (liveService.isFairPlayRestriction()) {
					Log.i("LCCLOG", ": resign game by fair play restriction: " + game);
					Log.i(TAG, "Resign game: " + game);
					liveService.runMakeResignTask();
				} else if (liveService.isAbortableBySeq()) {
					Log.i(TAG, "LCCLOG: abort game: " + game);
					liveService.runAbortGameTask();
				} else {
					Log.i(TAG, "LCCLOG: resign game: " + game);
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
			logTest(e.getMessage());
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
			logTest(e.getMessage());
			return null;
		}
		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getWhiteUsername() + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.getWhiteRating() + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public String getBlackPlayerName() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
			return null;
		}
		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getBlackUsername() + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.getBlackRating() + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public boolean currentGameExist() {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
			return false;
		}
		return liveService.getCurrentGame() != null;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardLive.getInstance(this);
	}

	private void updatePlayerLabels(Game game, int newWhiteRating, int newBlackRating) {
		String whitePlayerLabel = game.getWhitePlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR + newWhiteRating + StaticData.SYMBOL_RIGHT_PAR;
		String blackPlayerLabel = game.getBlackPlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR + newBlackRating + StaticData.SYMBOL_RIGHT_PAR;

		if (getBoardFace().isReside()) {
			topPanelView.setPlayerLabel(whitePlayerLabel);
			bottomPanelView.setPlayerLabel(blackPlayerLabel);
		} else {
			topPanelView.setPlayerLabel(blackPlayerLabel);
			bottomPanelView.setPlayerLabel(whitePlayerLabel);
		}
	}

	private void showGameEndPopup(View layout, String title, String message) {
		LiveChessService liveService;
		try {
			liveService = getLiveService();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
			return;
		}
		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView ratingTitleTxt = (TextView) layout.findViewById(R.id.ratingTitleTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
		TextView rulesLinkTxt = (TextView) layout.findViewById(R.id.rulesLinkTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating = liveService.getLastGame().getRatingForPlayer(liveService.getUsername());
		int ratingChange = liveService.getLastGame().getRatingChangeForPlayer(liveService.getUsername());

		GameRatingClass gameRatingClass = liveService.getLastGame().getGameRatingClass();
		String newRatingStr = getString(R.string.live);
		if (gameRatingClass == GameRatingClass.Standard) {
			newRatingStr += StaticData.SYMBOL_SPACE + getString(R.string.standard);
		} else if (gameRatingClass == GameRatingClass.Blitz) {
			newRatingStr += StaticData.SYMBOL_SPACE + getString(R.string.blitz);
		} else /*if (gameRatingClass == GameRatingClass.Lightning)*/ {
			newRatingStr += StaticData.SYMBOL_SPACE + getString(R.string.lightning);
		}

		String ratingChangeString = ratingChange > 0 ? "+" + ratingChange : "" + ratingChange;
		String rating = currentPlayerNewRating + StaticData.SYMBOL_SPACE
				+ StaticData.SYMBOL_LEFT_PAR + ratingChangeString + StaticData.SYMBOL_RIGHT_PAR;
		yourRatingTxt.setText(rating);
		ratingTitleTxt.setText(getString(R.string.new_game_rating_arg, newRatingStr));

//		inneractiveRectangleAd = (InneractiveAd) layout.findViewById(R.id.inneractiveRectangleAd);
//		InneractiveAdHelper.showRectangleAd(inneractiveRectangleAd, this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.shareBtn).setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(getActivity())) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
			if (message.contains(getString(R.string.won_game_abandoned))) {
				layout.findViewById(R.id.upgradeBtn).setVisibility(View.GONE);
				showFairPolicyLink(rulesLinkTxt);
			}
		} else if (message.contains(getString(R.string.won_game_abandoned))) {
			showFairPolicyLink(rulesLinkTxt);
		}
	}

	private void showFairPolicyLink(TextView rulesLinkTxt) {
		rulesLinkTxt.setVisibility(View.VISIBLE);
		rulesLinkTxt.setOnClickListener(this);
	}

	@Override
	protected void restoreGame() {
		if (isLCSBound) {
//			ChessBoardLive.resetInstance();		 // moved to onGameStarted
//			boardView.setGameActivityFace(this);
			try {
				onGameStarted();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				isLCSBound = false;
				return;
			}
			getBoardFace().setJustInitialized(false);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.cancelBtn) {
			showSubmitButtonsLay(false);
			getBoardFace().takeBack();
			getBoardFace().decreaseMovesCount();
			boardView.invalidate();
//		} else if (view.getId() == R.id.submitBtn) {
//			sendMove("submit click");
		} else if (view.getId() == R.id.newGamePopupBtn) {
//			Intent intent = new Intent(this, LiveNewGameActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
			getActivityFace().changeRightFragment(NewGamesFragment.newInstance(NewGamesFragment.RIGHT_MENU_MODE));
		} else if (view.getId() == R.id.rulesLinkTxt) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.fair_play_policy_url))));
		} else if (view.getId() == R.id.shareBtn) {
			LiveChessService liveService;
			try {
				liveService = getLiveService();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				return;
			}

			GameLiveItem currentGame = liveService.getGameItem();
			ShareItem shareItem = new ShareItem(currentGame, currentGame.getGameId(), getString(R.string.live));

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
//		} else if (view.getId() == R.id.acceptDrawBtn) { // TODO restore logic from controlsView
//			if (isLCSBound) {
//				Log.i(TAG, "Request draw: " + liveService.getCurrentGame());
//				liveService.runMakeDrawTask();
//			}
//			drawButtonsLay.setVisibility(View.GONE);
//		} else if (view.getId() == R.id.declineDrawBtn) {
//			if (isLCSBound) {
//				Log.i(TAG, "Decline draw: " + liveService.getCurrentGame());
//				liveService.runRejectDrawTask();
//			}
//			drawButtonsLay.setVisibility(View.GONE);
		} else if (view.getId() == R.id.rematchPopupBtn) {
			if (isLCSBound) {
				LiveChessService liveService;
				try {
					liveService = getLiveService();
				} catch (DataNotValidException e) {
					logTest(e.getMessage());
					return;
				}
				liveService.rematch();
			}
			dismissDialogs();
		}
	}

	@Override
	public void onItemClick(QuickAction source, int pos, int actionId) {
		if (actionId == ID_NEW_GAME) {
			getActivityFace().changeRightFragment(new LiveGameOptionsFragment());
			getActivityFace().toggleRightMenu();
		} else if (actionId == ID_OFFER_DRAW) {
			showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
		} else if (actionId == ID_RESIGN_ABORT) {
			showPopupDialog(R.string.abort_resign_game, R.string.are_you_sure_q, ABORT_GAME_TAG);
		} else if (actionId == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsFragment());
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

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String opponentName;
			if (isUserColorWhite()) {
				opponentName = currentGame.getBlackUsername();
			} else {
				opponentName = currentGame.getWhiteUsername();
			}
			// request opponent avatar
//			LoadItem loadItem = new LoadItem();
//			loadItem.setLoadPath(RestHelper.CMD_USERS);
//			loadItem.addRequestParams(RestHelper.P_USERNAME, opponentName);
//			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
//			new RequestJsonTask<UserItem>(userInfoUpdateListener).executeTask(loadItem);

			String opponentAvatarUrl = liveService.getCurrentGame().getOpponentForPlayer(opponentName).getAvatarUrl(); // TODO test
			imageDownloader.download(opponentAvatarUrl, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);

		}

		{// fill labels
			labelsConfig = new LabelsConfig();
			if (isUserColorWhite()) {
				labelsConfig.userSide = ChessBoard.WHITE_SIDE;

				labelsConfig.topPlayerLabel = getBlackPlayerName();
				labelsConfig.bottomPlayerLabel = getWhitePlayerName();
			} else {
				labelsConfig.userSide = ChessBoard.BLACK_SIDE;

				labelsConfig.topPlayerLabel = getWhitePlayerName();
				labelsConfig.bottomPlayerLabel = getBlackPlayerName();
			}
		}

		int resignTitleId = liveService.getResignTitle();
		{// Quick action setup
			quickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
			quickAction.addActionItem(new ActionItem(ID_NEW_GAME, getString(R.string.new_game)));
			quickAction.addActionItem(new ActionItem(ID_OFFER_DRAW, getString(R.string.offer_draw)));
			quickAction.addActionItem(new ActionItem(ID_RESIGN_ABORT, getString(resignTitleId)));
			quickAction.addActionItem(new ActionItem(ID_SETTINGS, getString(R.string.settings)));
			quickAction.setOnActionItemClickListener(this);
		}
		lccInitiated = true;
	}

	private void widgetsInit(View view) {
		fadeLay = view.findViewById(R.id.fadeLay);
		gameBoardView = view.findViewById(R.id.baseView);

		controlsLiveView = (ControlsLiveView) view.findViewById(R.id.controlsLiveView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		boardView = (ChessBoardLiveView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsLiveView);
		boardView.setNotationsView(notationsView);
		setBoardView(boardView);
		boardView.setGameActivityFace(this);
		controlsLiveView.setBoardViewFace(boardView);
	}

	private class LabelsConfig {
		BoardAvatarDrawable topAvatar;
		BoardAvatarDrawable bottomAvatar;
		String topPlayerLabel;
		String bottomPlayerLabel;
		int userSide;

		int getOpponentSide() {
			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}
	}

//	private class UserInfoUpdateListener extends ChessUpdateListener<UserItem> {
//
//		public UserInfoUpdateListener() {
//			super(UserItem.class);
//		}
//
//		@Override
//		public void updateData(UserItem returnedObj) {
//			super.updateData(returnedObj);
//
//			String opponentAvatarUrl = returnedObj.getData().getAvatar();
//			imageDownloader.download(opponentAvatarUrl, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
//		}
//	}

	private class ImageUpdateListener implements ImageReadyListener {

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

					String userAvatarUrl = AppData.getUserAvatar(activity);
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
}
