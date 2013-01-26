package com.chess.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccGameTaskRunner;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.ui.activities.PreferencesScreenActivity;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.views.ChessBoardLiveView;
import com.chess.ui.views.GamePanelInfoView;
import com.chess.ui.views.NotationView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.01.13
 * Time: 11:33
 */
public class GameLiveFragment extends GameBaseFragment implements LccEventListener, LccChatMessageListener {


	private static final String TAG = "GameLiveScreenActivity";
	private static final String WARNING_TAG = "warning message popup";

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;

	private MenuOptionsDialogListener menuOptionsDialogListener;

//	protected TextView topPlayerLabel;
//	protected TextView topPlayerClock;

	private View submitButtonsLay;
	private GameLiveItem currentGame;
	private ChessBoardLiveView boardView;
	private int whitePlayerNewRating;
	private int blackPlayerNewRating;
	private int currentPlayerRating;

	private String whiteTimer;
	private String blackTimer;
	private View fadeLay;
	private View gameBoardView;
	private boolean lccInitiated;
	private Button submitBtn;
	private String warningMessage;

	private String boardDebug; // temp
	private NotationView notationsView;
	private GamePanelInfoView topPanelView;
	private GamePanelInfoView bottomPanelView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_live, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
		lccInitiated = init();

		if (!lccInitiated) {
			return;
		}

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		Log.d("Live Game", "GameLiveScreenActivity started ");
		if (getLccHolder().getPendingWarnings().size() > 0) {
			// get last warning
			warningMessage = getLccHolder().getLastWarningMessage();

			Log.d("LCCLOG-WARNING", warningMessage);

			showPopupDialog(R.string.warning, warningMessage, WARNING_TAG); // todo: check
		}
	}

	private boolean init() {
		if (!getLccHolder().isConnected()) {
			showToast(R.string.application_was_killed);
			return false;
		}

		currentGame = getLccHolder().getGameItem();
		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		Game game = getLccHolder().getCurrentGame();
		switch (game.getGameTimeConfig().getGameTimeClass()) {
			case BLITZ:
				currentPlayerRating = getLccHolder().getUser().getBlitzRating();
				break;
			case LIGHTNING:
				currentPlayerRating = getLccHolder().getUser().getQuickRating();
				break;
			case STANDARD:
				currentPlayerRating = getLccHolder().getUser().getStandardRating();
				break;
		}

		if (!getLccHolder().currentGameExist()) {
			gameControlsView.enableAnalysisMode(true);

			boardView.setFinished(true);
//			gameControlsView.showBottomPart(false); // seems to be unused in new design
		}

		getLccHolder().setLccEventListener(this);
		getLccHolder().setLccChatMessageListener(this);

		int resignOrAbort = getLccHolder().getResignTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.offer_draw),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();
		return true;
	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);

		fadeLay = view.findViewById(R.id.fadeLay);
		gameBoardView = view.findViewById(R.id.baseView);

		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (GamePanelInfoView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (GamePanelInfoView) view.findViewById(R.id.bottomPanelView);

		boardView = (ChessBoardLiveView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGameControlsView(gameControlsView);
		setBoardView(boardView);

//		boardView.setBoardFace(getBoardFace());
		boardView.setGameActivityFace(this);

		submitButtonsLay = view.findViewById(R.id.submitButtonsLay);
		submitBtn = (Button) view.findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(this);

		view.findViewById(R.id.cancelBtn).setOnClickListener(this);

		gameControlsView.enableAnalysisMode(false);

//		topPlayerLabel = whitePlayerLabel;
//		topPlayerClock = blackPlayerLabel;

//		topPlayerLabel.setMaxWidth(getResources().getDisplayMetrics().widthPixels);  // TODO restore
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!lccInitiated) {
			getActivityFace().showPreviousFragment();
			return;
		}

		getLccHolder().setActivityPausedMode(false);
		getLccHolder().setLccChatMessageListener(this);
		updateGameState();
	}

	@Override
	public void onPause() {
		dismissDialogs();


		super.onPause();
		getLccHolder().setActivityPausedMode(true);

		handler.removeCallbacks(blinkSubmitButton);
	}

	private void updateGameState() {
		if (getBoardFace().isJustInitialized()) {
			onGameStarted();
			getBoardFace().setJustInitialized(false);
		}

		getLccHolder().executePausedActivityGameEvents();
	}

	private void onGameStarted() {
		showSubmitButtonsLay(false);
		getSoundPlayer().playGameStart();

		currentGame = getLccHolder().getGameItem();

		checkMessages();

		blockGame(false);
		getLccHolder().checkAndReplayMoves();

		// temporary disable playLastMoveAnimation feature, because it can be one of the illegal move reasons potentially
		// todo: probably could be enabled with new LCC
		/*invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();
		playLastMoveAnimation();*/

		getLccHolder().checkFirstTestMove();
	}

	public void setWhitePlayerTimer(String timeString) {
		whiteTimer = timeString;
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getBoardFace().isReside()) {
					topPanelView.setPlayerTimeLeft(whiteTimer);
//					topPlayerClock.setText(whiteTimer);
				} else {
//					gameControlsView.setBottomPlayerTimer(whiteTimer);
					bottomPanelView.setPlayerTimeLeft(whiteTimer);
				}
			}
		});
	}

	public void setBlackPlayerTimer(String timeString) {
		blackTimer = timeString;
		getActivity().runOnUiThread(new Runnable() { // TODO add check
			@Override
			public void run() {
				if (getBoardFace().isReside()) {
//					gameControlsView.setBottomPlayerTimer(blackTimer);
					bottomPanelView.setPlayerTimeLeft(blackTimer);
				} else {
//					topPlayerClock.setText(blackTimer);
					topPanelView.setPlayerTimeLeft(blackTimer);
				}
			}
		});
	}

	private void changePlayersLabelColors() {
		int hintColor = getResources().getColor(R.color.hint_text);
		int whiteColor = getResources().getColor(R.color.white);

		int topPlayerTextColor;
		int bottomPlayerTextColor;

		if (getBoardFace().isWhiteToMove()) {
			topPlayerTextColor = getBoardFace().isReside() ? whiteColor : hintColor;
			bottomPlayerTextColor = getBoardFace().isReside() ? hintColor : whiteColor;
		} else {
			topPlayerTextColor = getBoardFace().isReside() ? hintColor : whiteColor;
			bottomPlayerTextColor = getBoardFace().isReside() ? whiteColor : hintColor;
		}

//		topPlayerLabel.setTextColor(topPlayerTextColor); // there will be no
//		topPlayerClock.setTextColor(topPlayerTextColor);
//		gameControlsView.setBottomPlayerTextColor(bottomPlayerTextColor);


		int topPlayerDotId;
		int bottomPlayerDotId;

		if (getBoardFace().isReside()) {
			topPlayerDotId = R.drawable.player_indicator_white;
			bottomPlayerDotId = R.drawable.player_indicator_black;
		} else {
			topPlayerDotId = R.drawable.player_indicator_black;
			bottomPlayerDotId = R.drawable.player_indicator_white;
		}

//		if (topPlayerTextColor == hintColor) { // not active // TODO set active side
//			topPlayerClock.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//			gameControlsView.setBottomIndicator(bottomPlayerDotId);
//		} else {
//			topPlayerClock.setCompoundDrawablesWithIntrinsicBounds(topPlayerDotId, 0, 0, 0);
//			gameControlsView.setBottomIndicator(0);
//		}
	}

	// ----------------------Lcc Events ---------------------------------------------

	public void onGameRecreate() {
		getActivityFace().showPreviousFragment();
	}

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

		boardDebug = "lastHply=" + getBoardFace().getHply() + ", lastMoves=" + actualMovesSize;

		getBoardFace().setMovesCount(actualMovesSize);

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boardView.invalidate();
				invalidateGameScreen();
			}
		});
		getLccHolder().checkTestMove();

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
				gameControlsView.haveNewMessage(true);
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
		String message = drawOfferUsername + StaticData.SYMBOL_SPACE + getString(R.string.has_offered_draw);

		popupItem.setPositiveBtnId(R.string.accept);
		popupItem.setNegativeBtnId(R.string.decline);
		showPopupDialog(message, DRAW_OFFER_RECEIVED_TAG);
		getLastPopupFragment().setCancelable(false);
	}

	@Override
	public void onGameEnd(final String gameEndMessage) {
		final Game game = getLccHolder().getLastGame();
		switch (game.getGameTimeConfig().getGameTimeClass()) {
			case BLITZ: {
				whitePlayerNewRating = game.getWhitePlayer().getBlitzRating();
				blackPlayerNewRating = game.getBlackPlayer().getBlitzRating();
				break;
			}
			case LIGHTNING: {
				whitePlayerNewRating = game.getWhitePlayer().getQuickRating();
				blackPlayerNewRating = game.getBlackPlayer().getQuickRating();
				break;
			}
			case STANDARD: {
				whitePlayerNewRating = game.getWhitePlayer().getStandardRating();
				blackPlayerNewRating = game.getBlackPlayer().getStandardRating();
				break;
			}
		}

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View layout;
		if (!AppUtils.isNeedToUpgrade(getActivity())) {
			layout = inflater.inflate(R.layout.popup_end_game, null, false);
		} else {
			layout = inflater.inflate(R.layout.popup_end_game_free, null, false);
		}

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updatePlayerLabels(game, whitePlayerNewRating, blackPlayerNewRating);
				showGameEndPopup(layout, getString(R.string.game_over), gameEndMessage);

				setBoardToFinishedState();
			}
		});

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

	protected void sendMove(String debugString) {

		getBoardFace().setSubmit(false);
		showSubmitButtonsLay(false);

		String move = getBoardFace().convertMoveLive();
		Log.i(TAG, "LCC make move: " + move);

//		GameRules gameRules = ChessRules.getInstance();
//		GameSetup gameSetup = gameRules.createDefaultGameSetup();
//		boolean legalMove;

		/*boolean legalMove;
		try{
			String testMove = "";
			if (move.length() > 2)
				testMove = Notation.coord2live(move);

			final GameMove gameMove = StandardChessMoveEncoder.decodeMove(testMove, gameSetup);
			legalMove = gameRules.isMoveLegal(gameMove, gameSetup);

		} catch (Exception ex) {
			legalMove = false;
			BugSenseHandler.sendException(ex);
		}*/

		String stackTrace;
		try {
			throw new Exception();
		} catch (Exception e) {
			stackTrace = Log.getStackTraceString(e);
		}

		String temporaryDebugInfo =
				"lccInitiated=" + lccInitiated +
						", " + boardDebug +
						", gameSeq=" + getLccHolder().getCurrentGame().getSeq() +
						", boardHply=" + getBoardFace().getHply() +
						", moveLive=" + getBoardFace().convertMoveLive() +
						", gamesC=" + getLccHolder().getGamesCount() +
						", gameId=" + getGameId() +
						", analysisPanel=" + gameControlsView.isAnalysisEnabled() +
						", analysisBoard=" + getBoardFace().isAnalysis() +
						", latestMoveNumber=" + getLccHolder().getLatestMoveNumber() +
						", debugString=" + debugString +
						", submit=" + preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false) +
						", movesLive=" + getLccHolder().getCurrentGame().getMoves() +
						", moves=" + getBoardFace().getMoveListSAN() +
						", trace=" + stackTrace;
		temporaryDebugInfo = temporaryDebugInfo.replaceAll("\n", " ");
		//Log.d("TESTTEST", temporaryDebugInfo);

		LccGameTaskRunner gameTaskRunner = new LccGameTaskRunner(new GameTaskListener());
		getLccHolder().makeMove(move, gameTaskRunner, temporaryDebugInfo);
	}

	private void updatePlayerLabels() {
		if (getBoardFace().isReside()) {
//			topPlayerLabel.setText(getWhitePlayerName());
//			gameControlsView.setBottomPlayerLabel(getBlackPlayerName());
			topPanelView.setPlayerLabel(getWhitePlayerName());
			bottomPanelView.setPlayerLabel(getBlackPlayerName());
		} else {
//			topPlayerLabel.setText(getBlackPlayerName());
//			gameControlsView.setBottomPlayerLabel(getWhitePlayerName());
			topPanelView.setPlayerLabel(getBlackPlayerName());
			bottomPanelView.setPlayerLabel(getWhitePlayerName());
		}
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		super.switch2Analysis(isAnalysis);
		if (isAnalysis) {
			getLccHolder().setLatestMoveNumber(0);
			ChessBoardLive.resetInstance();
		}
		gameControlsView.enableControlButtons(isAnalysis);
	}

	@Override
	public void switch2Chat() {
		openChatActivity();
	}

	private void openChatActivity() {
		preferencesEditor.putString(AppConstants.OPPONENT, isUserColorWhite()
				? currentGame.getBlackUsername() : currentGame.getWhiteUsername());
		preferencesEditor.commit();

		currentGame.setHasNewMessage(false);
		gameControlsView.haveNewMessage(false);


		// TODO open ChatFragment
//		Intent intent = new Intent(this, ChatLiveActivity.class);
//		intent.putExtra(BaseGameItem.TIMESTAMP, currentGame.getTimestamp());
//		startActivity(intent);
	}

	private void checkMessages() {
		if (currentGame.hasNewMessage()) {
			gameControlsView.haveNewMessage(true);
		}
	}

	@Override
	public void newGame() {
//		startActivity(new Intent(this, LiveNewGameActivity.class));
		getActivityFace().changeRightFragment(new NewGamesFragment());

	}

	@Override
	public void updateAfterMove() {
		if (!getBoardFace().isAnalysis())
			sendMove("update");
	}

	@Override
	public void invalidateGameScreen() {
		showSubmitButtonsLay(getBoardFace().isSubmit());

//		topPlayerLabel.setVisibility(View.VISIBLE); // TODO restore - recheck
//		topPlayerClock.setVisibility(View.VISIBLE);

		updatePlayerLabels();
		getLccHolder().paintClocks();
		changePlayersLabelColors();

//		boardView.updateNotations(getBoardFace().getMoveListSAN());
		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(getActivity()) // TODO replace with fragmentDialog
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {  // TODO remove arg and get state from boardFace
		submitButtonsLay.setVisibility(show ? View.VISIBLE : View.GONE);

		if (show) {
			blinkSubmitBtn();
		} else {
			getBoardFace().setSubmit(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		menuInflater.inflate(R.menu.game_live, menu);
		super.onCreateOptionsMenu(menu, menuInflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_chat:
				openChatActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Boolean isUserColorWhite() {
		return getLccHolder().isUserColorWhite();
	}

	@Override
	public Long getGameId() {
		return getLccHolder().getCurrentGameId(); // currentGame initialized in init() method
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		private final int LIVE_SETTINGS = 0;
		private final int LIVE_RESIDE = 1;
		private final int LIVE_DRAW_OFFER = 2;
		private final int LIVE_RESIGN_OR_ABORT = 3;
		private final int LIVE_MESSAGES = 4;

		@Override
		public void onClick(DialogInterface dialogInterface, int pos) {
			switch (pos) {
				case LIVE_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case LIVE_RESIDE:
					getBoardFace().setReside(!getBoardFace().isReside());
					boardView.invalidate();
					break;
				case LIVE_DRAW_OFFER:
					showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
					break;
				case LIVE_RESIGN_OR_ABORT:
					showPopupDialog(R.string.abort_resign_game, R.string.are_you_sure_q, ABORT_GAME_TAG);
					break;
				case LIVE_MESSAGES:
					openChatActivity();
					break;
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

		LccGameTaskRunner gameTaskRunner = new LccGameTaskRunner(new GameTaskListener());
		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			Log.i(TAG, "Request draw: " + getLccHolder().getCurrentGame());
			gameTaskRunner.runMakeDrawTask();
		} else if (tag.equals(WARNING_TAG)) {
			getLccHolder().getPendingWarnings().remove(warningMessage);
		} else if (tag.equals(ABORT_GAME_TAG)) {
			Game game = getLccHolder().getCurrentGame();

			if (getLccHolder().isFairPlayRestriction()) {
				Log.i("LCCLOG", ": resign game by fair play restriction: " + game);
				Log.i(TAG, "Resign game: " + game);
				gameTaskRunner.runMakeResignTask();
			} else if (getLccHolder().isAbortableBySeq()) {
				Log.i(TAG, "LCCLOG: abort game: " + game);
				gameTaskRunner.runAbortGameTask();
			} else {
				Log.i(TAG, "LCCLOG: resign game: " + game);
				gameTaskRunner.runMakeResignTask();
			}
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

		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			Log.i(TAG, "Decline draw: " + getLccHolder().getCurrentGame());
			new LccGameTaskRunner(new GameTaskListener()).runRejectDrawTask();
		}
		super.onNegativeBtnClick(fragment);
	}

	protected void changeChatIcon(Menu menu) {
		MenuItem menuItem = menu.findItem(R.id.menu_chat);
		if (menuItem == null)
			return;

		if (currentGame.hasNewMessage()) {
			menuItem.setIcon(R.drawable.chat_nm);
		} else {
			menuItem.setIcon(R.drawable.chat);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (currentGame != null) {
			changeChatIcon(menu);
		}
		super.onPrepareOptionsMenu(menu);
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
		return getLccHolder().getCurrentGame() != null;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardLive.getInstance(this);
	}

	private void updatePlayerLabels(Game game, int newWhiteRating, int newBlackRating) {
		String whitePlayerLabel = game.getWhitePlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR + newWhiteRating + StaticData.SYMBOL_RIGHT_PAR;
		String blackPlayerLabel = game.getBlackPlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR + newBlackRating + StaticData.SYMBOL_RIGHT_PAR;

		if (getBoardFace().isReside()) {

//			topPlayerLabel.setText(whitePlayerLabel);
//			gameControlsView.setBottomPlayerLabel(blackPlayerLabel);

			topPanelView.setPlayerLabel(whitePlayerLabel);
			bottomPanelView.setPlayerLabel(blackPlayerLabel);
		} else {
//			topPlayerLabel.setText(blackPlayerLabel);
//			gameControlsView.setBottomPlayerLabel(whitePlayerLabel); // always at the bottom
			topPanelView.setPlayerLabel(blackPlayerLabel);
			bottomPanelView.setPlayerLabel(whitePlayerLabel);
		}
	}

	private void blinkSubmitBtn() {
		handler.removeCallbacks(blinkSubmitButton);
		handler.postDelayed(blinkSubmitButton, BLINK_DELAY);
	}

	private Runnable blinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			submitBtn.setBackgroundResource(R.drawable.button_grey_selector);
			submitBtn.invalidate();
			handler.removeCallbacks(unBlinkSubmitButton);
			handler.postDelayed(unBlinkSubmitButton, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkSubmitButton = new Runnable() {
		@Override
		public void run() {
			submitBtn.setBackgroundResource(R.drawable.button_orange_selector);
			submitBtn.invalidate();
			blinkSubmitBtn();
		}
	};


	private void showGameEndPopup(View layout, String title, String message) {

		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating;
		if (isUserColorWhite()) {
			currentPlayerNewRating = whitePlayerNewRating;
		} else {
			currentPlayerNewRating = blackPlayerNewRating;
		}

		int ratingDiff;
		String sign;
		if (currentPlayerRating < currentPlayerNewRating) { // 800 1200
			ratingDiff = currentPlayerNewRating - currentPlayerRating;
			sign = StaticData.SYMBOL_PLUS;
		} else { // 800 700
			ratingDiff = currentPlayerRating - currentPlayerNewRating;
			sign = StaticData.SYMBOL_MINUS;
		}

		String rating = getString(R.string.your_end_game_rating, sign + ratingDiff, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		MopubHelper.showRectangleAd(adViewWrapper, getActivity());

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(getActivity())) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
	}

	@Override
	protected void restoreGame() {
		ChessBoardLive.resetInstance();
//		boardView.setBoardFace(getBoardFace());
		boardView.setGameActivityFace(this);
		onGameStarted();
		getBoardFace().setJustInitialized(false);
		getLccHolder().executePausedActivityGameEvents();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.cancelBtn) {
			showSubmitButtonsLay(false);
			getBoardFace().takeBack();
			getBoardFace().decreaseMovesCount();
			boardView.invalidate();
		} else if (view.getId() == R.id.submitBtn) {
			sendMove("submit click");
		} else if (view.getId() == R.id.newGamePopupBtn) {
//			Intent intent = new Intent(this, LiveNewGameActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
			getActivityFace().changeRightFragment(new NewGamesFragment());

		} else if (view.getId() == R.id.rematchPopupBtn) {
			getLccHolder().rematch();
			dismissDialogs();
		}
	}

	private class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}
}
