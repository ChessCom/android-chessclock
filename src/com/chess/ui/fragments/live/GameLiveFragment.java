package com.chess.ui.fragments.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.UserItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
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
import com.chess.ui.views.ChessBoardLiveView;
import com.chess.ui.views.ControlsLiveView;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.utilities.AppUtils;
import quickaction.ActionItem;
import quickaction.QuickAction;

import java.util.List;

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

	private GameLiveItem currentGame;
	private ChessBoardLiveView boardView;

	private String whiteTimer;
	private String blackTimer;
	private View fadeLay;
	private View gameBoardView;
	private boolean lccInitiated;
	private String warningMessage;
	private GameTaskListener gameTaskListener;


	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsLiveView controlsLiveView;
	private QuickAction quickAction;
	private long gameId;
	private LabelsConfig labelsConfig;
	private UserInfoUpdateListener userInfoUpdateListener;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ImageDownloaderToListener imageDownloader;

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

		gameTaskListener = new GameTaskListener();
		userInfoUpdateListener = new UserInfoUpdateListener();
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
		init();
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
			onGameStarted();
		}
	}

	@Override
	public void onPause() {
		dismissDialogs();

		super.onPause();
		if (isLCSBound) {
			liveService.setGameActivityPausedMode(true);
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

	private void onGameStarted() {
		currentGame = liveService.getGameItem();

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}

		invalidateGameScreen();
		checkPendingWarnings();

		showSubmitButtonsLay(false);
		getSoundPlayer().playGameStart();

		checkMessages();

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
		getActivity().runOnUiThread(new Runnable() {
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
		getActivity().runOnUiThread(new Runnable() { // TODO add check
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
		// shouldn't be used here. Use in WaitFragment instead
	}

	@Override
	public void createSeek() {
		// shouldn't be used here. Use in WaitFragment instead
	}

	@Override
	public void onGameRefresh(GameLiveItem gameItem) {

		if (getBoardFace().isAnalysis())
			return;

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

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boardView.invalidate();
				invalidateGameScreen();
			}
		});
		liveService.checkTestMove();

		checkMessages();
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

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		final Game game = liveService.getLastGame();
		final List<Integer> ratings = game.getRatings();
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final View layout;
				if (!AppUtils.isNeedToUpgrade(getActivity())) {
					layout = inflater.inflate(R.layout.popup_end_game, null, false);
				} else {
					layout = inflater.inflate(R.layout.popup_end_game_free, null, false);
				}

				updatePlayerLabels(game, ratings.get(0), ratings.get(1));
				showGameEndPopup(layout, getString(R.string.game_over), gameEndMessage);

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
		getActivity().runOnUiThread(new Runnable() {
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

	protected void sendMove() {
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
		openChat();
	}

	@Override
	public void playMove() {
		sendMove();
	}

	@Override
	public void cancelMove() {
		showSubmitButtonsLay(false);
		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();
		boardView.invalidate();
	}

	private void openChat() {
//		preferencesEditor.putString(AppConstants.OPPONENT, isUserColorWhite()
//				? currentGame.getBlackUsername() : currentGame.getWhiteUsername());
//		preferencesEditor.commit();

		currentGame.setHasNewMessage(false);
		controlsLiveView.haveNewMessage(false);

		getActivityFace().openFragment(new LiveChatFragment());
	}

	private void checkMessages() {
		if (currentGame.hasNewMessage()) {
			controlsLiveView.haveNewMessage(true);
		}
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(NewGamesFragment.newInstance(NewGamesFragment.RIGHT_MENU_MODE));
	}

	@Override
	public void updateAfterMove() {
		if (!getBoardFace().isAnalysis())
			sendMove();
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
			liveService.paintClocks();
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
		return liveService.isUserColorWhite();
	}

	@Override
	public Long getGameId() {
		return liveService.getCurrentGameId(); // currentGame initialized in init() method
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
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

			onGameStarted();

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
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getWhiteUsername() + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.getWhiteRating() + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public String getBlackPlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getBlackUsername() + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.getBlackRating() + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public boolean currentGameExist() {
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

		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
		TextView rulesLinkTxt = (TextView) layout.findViewById(R.id.rulesLinkTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating = liveService.getLastGame().getRatingForPlayer(liveService.getUsername());
		/*if (isUserColorWhite()) {
			currentPlayerNewRating = whitePlayerNewRating;
		} else {
			currentPlayerNewRating = blackPlayerNewRating;
		}*/

		int ratingChange = liveService.getLastGame().getRatingChangeForPlayer(liveService.getUsername());
		/*String sign;
		if (currentPlayerRating < currentPlayerNewRating) { // 800 1200
			ratingDiff = currentPlayerNewRating - currentPlayerRating;
			sign = StaticData.SYMBOL_PLUS;
		} else { // 800 700
			ratingDiff = currentPlayerRating - currentPlayerNewRating;
			sign = StaticData.SYMBOL_MINUS;
		}*/

		String ratingChangeString = ratingChange > 0 ? "+" + ratingChange : "" + ratingChange;

		String rating = getString(R.string.your_end_game_rating, ratingChangeString, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

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
			ChessBoardLive.resetInstance();
			boardView.setGameActivityFace(this);
			onGameStarted();
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
				liveService.rematch();
			}
			dismissDialogs();
		}
	}

	private void checkPendingWarnings() {
		Log.d("live", "checkPendingWarnings");
		Log.d("Live Game", "GameLiveScreenActivity started ");
		if (liveService.getPendingWarnings().size() > 0) {
			// get last warning
			warningMessage = liveService.getLastWarningMessage();

			Log.d("LCCLOG-WARNING", warningMessage);
			popupItem.setNegativeBtnId(R.string.fair_play_policy);
			showPopupDialog(R.string.warning, warningMessage, WARNING_TAG); // it works!
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

	private class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}

	private void init() {
		currentGame = liveService.getGameItem();

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		enableScreenLockTimer();

		if (!liveService.currentGameExist()) {
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
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_USERS);
			loadItem.addRequestParams(RestHelper.P_USER_NAME, opponentName);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
			new RequestJsonTask<UserItem>(userInfoUpdateListener).executeTask(loadItem);
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

		{// Quick action setup
			quickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
			quickAction.addActionItem(new ActionItem(ID_NEW_GAME, getString(R.string.new_game)));
			quickAction.addActionItem(new ActionItem(ID_OFFER_DRAW, getString(R.string.offer_draw)));
			quickAction.addActionItem(new ActionItem(ID_RESIGN_ABORT, getString(liveService.getResignTitle())));
			quickAction.addActionItem(new ActionItem(ID_SETTINGS, getString(R.string.settings)));
			quickAction.setOnActionItemClickListener(this);
		}
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

	private class UserInfoUpdateListener extends ChessUpdateListener<UserItem> {

		public UserInfoUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);

			String opponentAvatarUrl = returnedObj.getData().getAvatar_small_url();
			imageDownloader.download(opponentAvatarUrl, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {

		private static final int TOP_AVATAR = 0;
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(getContext(), bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();

					String userAvatarUrl = AppData.getUserAvatar(getContext());
					imageDownloader.download(userAvatarUrl,new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);

					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(getContext(), bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}
}
