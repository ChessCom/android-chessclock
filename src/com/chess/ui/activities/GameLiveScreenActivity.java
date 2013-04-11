package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.Game;
import com.chess.model.BaseGameItem;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.views.ChessBoardLiveView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.InneractiveAdHelper;
import com.inneractive.api.ads.InneractiveAd;

import java.util.List;

/**
 * GameLiveScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameLiveScreenActivity extends GameBaseActivity implements LccEventListener, LccChatMessageListener {

	private static final String TAG = "LccLog-GameLiveScreenActivity";
	private static final String WARNING_TAG = "warning message popup";
	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;

	protected TextView topPlayerLabel;
	protected TextView topPlayerClock;
	private MenuOptionsDialogListener menuOptionsDialogListener;
	private View submitButtonsLay;
	private GameLiveItem currentGame;
	private ChessBoardLiveView boardView;
	/*private int whitePlayerNewRating;
	private int blackPlayerNewRating;
	private int currentPlayerRating;*/
	private String whiteTimer;
	private String blackTimer;
	private View fadeLay;
	private View gameBoardView;
	private boolean lccInitiated;
	private Button submitBtn;
	private String warningMessage;
	private String boardDebug; // temp
	private View drawButtonsLay;
	private TextView drawTitleTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Log.d(TAG, "GAME ACTIVITY CREATE");

		setContentView(R.layout.boardview_live);

		widgetsInit();
	}

	private void init() {
		currentGame = liveService.getGameItem();
		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		enableScreenLockTimer();

		if (!liveService.currentGameExist()) {
			gamePanelView.enableAnalysisMode(true);

			boardView.setFinished(true);
			gamePanelView.showBottomPart(false);
		}

		//Log.d(TAG, "GAME ACTIVITY onLiveServiceConnected");
		//Log.d(TAG, "GAME ACTIVITY onLiveServiceConnected " + liveService.getLastGame());

		/*enableScreenLockTimer();

		if (liveService.currentGameExist()) {
			currentGame = liveService.getGameItem();
			boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
			boardView.updateBoardAndPiecesImgs();
		} else {
			gamePanelView.enableAnalysisMode(true);
			boardView.setFinished(true);
			gamePanelView.showBottomPart(false);
		}*/

		liveService.setLccEventListener(this);
		liveService.setLccChatMessageListener(this);
		liveService.setGameTaskListener(gameTaskListener);

		int resignOrAbort = liveService.getResignTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();

		lccInitiated = true;
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		fadeLay = findViewById(R.id.fadeLay);
		gameBoardView = findViewById(R.id.baseView);

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		submitBtn = (Button) findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(this);
		findViewById(R.id.cancelBtn).setOnClickListener(this);

		gamePanelView.enableAnalysisMode(false);

		topPlayerLabel = whitePlayerLabel;
		topPlayerClock = blackPlayerLabel;

		topPlayerLabel.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.d("live", "new intent");
		ChessBoardLive.resetInstance();

		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		getBoardFace().setAnalysis(false);
		switch2Analysis(false);

		updateGameState();

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		invalidateGameScreen();

		checkPendingWarnings();
	}

	@Override
	protected void onLiveServiceConnected() {

		super.onLiveServiceConnected();

		boardView = (ChessBoardLiveView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		setBoardView(boardView);

		boardView.setBoardFace(ChessBoardLive.getInstance(this));
		boardView.setGameActivityFace(this);

		init();

		updateGameState();

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		invalidateGameScreen();

		checkPendingWarnings();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isLCSBound) {  // check if this is correct? When Fair Policy popup appears it returns game to the analysis mode
			updateGameState();
		}
	}

	@Override
	protected void onPause() {
		dismissDialogs();

		super.onPause();

		if (isLCSBound) {
			liveService.setGameActivityPausedMode(true);
		}

		handler.removeCallbacks(blinkSubmitButton);
	}

	private void updateGameState() {
		if (getBoardFace().isJustInitialized()) {
			onGameStarted();
			getBoardFace().setJustInitialized(false);
		}

		liveService.executePausedActivityGameEvents();
	}

	// ----------------------Lcc Events ---------------------------------------------

	private void onGameStarted() {
		showSubmitButtonsLay(false);

		getSoundPlayer().playGameStart();

		currentGame = liveService.getGameItem();

		checkMessages();

		liveService.checkAndReplayMoves();

		// temporary disable playLastMoveAnimation feature, because it can be one of the illegalmove reasons potentially
		// todo: probably could be enabled with new LCC
		/*invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();
		playLastMoveAnimation();*/

		liveService.checkFirstTestMove();
	}

	@Override
	public void setWhitePlayerTimer(String timeString) {
		whiteTimer = timeString;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getBoardFace().isReside()) {
					topPlayerClock.setText(whiteTimer);
				} else {
					gamePanelView.setBottomPlayerTimer(whiteTimer);
				}
			}
		});
	}

	@Override
	public void setBlackPlayerTimer(String timeString) {
		blackTimer = timeString;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getBoardFace().isReside()) {
					gamePanelView.setBottomPlayerTimer(blackTimer);
				} else {
					topPlayerClock.setText(blackTimer);
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

		topPlayerLabel.setTextColor(topPlayerTextColor);
		topPlayerClock.setTextColor(topPlayerTextColor);
		gamePanelView.setBottomPlayerTextColor(bottomPlayerTextColor);

		int topPlayerDotId;
		int bottomPlayerDotId;

		if (getBoardFace().isReside()) {
			topPlayerDotId = R.drawable.player_indicator_white;
			bottomPlayerDotId = R.drawable.player_indicator_black;
		} else {
			topPlayerDotId = R.drawable.player_indicator_black;
			bottomPlayerDotId = R.drawable.player_indicator_white;
		}

		if (topPlayerTextColor == hintColor) { // not active
			topPlayerClock.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			gamePanelView.setBottomIndicator(bottomPlayerDotId);
		} else {
			topPlayerClock.setCompoundDrawablesWithIntrinsicBounds(topPlayerDotId, 0, 0, 0);
			gamePanelView.setBottomIndicator(0);
		}
	}

	/*@Override
	public void onInform(String title, String message){
		showSinglePopupDialog(title, message);
	}*/

	@Override
	public void onGameRefresh(GameLiveItem gameItem) {

		if (getBoardFace().isAnalysis()) {
			return;
		}

		String[] actualMoves = gameItem.getMoveList().trim().split(StaticData.SYMBOL_SPACE);
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

		blockGame(false);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				invalidateGameScreen();
			}
		});
		liveService.checkTestMove();

		checkMessages();
	}

	// -----------------------------------------------------------------------------------

	@Override
	public void onConnectionBlocked(boolean blocked) {
		super.onConnectionBlocked(blocked);
		if (blocked) {
			blockGame(blocked);
		}
	}

	@Override
	public void onMessageReceived() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gamePanelView.haveNewMessage(true);
				boardView.invalidate();
			}
		});
	}

	@Override
	public void onDrawOffered(final String drawOfferUsername) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				drawButtonsLay = findViewById(R.id.drawButtonsLay);
				drawTitleTxt = (TextView) findViewById(R.id.drawTitleTxt);
				findViewById(R.id.acceptDrawBtn).setOnClickListener(GameLiveScreenActivity.this);
				findViewById(R.id.declineDrawBtn).setOnClickListener(GameLiveScreenActivity.this);
				drawButtonsLay.setVisibility(View.VISIBLE);

				String message = drawOfferUsername + StaticData.SYMBOL_SPACE + getString(R.string.has_offered_draw);
				drawTitleTxt.setText(message);
			}
		});
	}

	@Override
	public void onGameEnd(final String gameEndMessage) {

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		final Game game = liveService.getLastGame();
		final List<Integer> ratings = game.getRatings();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				final View layout;
				if (!AppUtils.isNeedToUpgrade(GameLiveScreenActivity.this)) {
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

	private void blockGame(final boolean block) {
		runOnUiThread(new Runnable() {
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

		String stackTrace;
		try {
			throw new Exception();
		} catch (Exception e) {
			stackTrace = Log.getStackTraceString(e);
		}

		String temporaryDebugInfo =
				"username=" + liveService.getUsername() +
						"lccInitiated=" + lccInitiated +
						", " + boardDebug +
						", gameSeq=" + liveService.getCurrentGame().getMoves().size() +
						", boardHply=" + getBoardFace().getHply() +
						", moveLive=" + getBoardFace().convertMoveLive() +
						", gamesC=" + liveService.getGamesCount() +
						", gameId=" + getGameId() +
						", analysisPanel=" + gamePanelView.isAnalysisEnabled() +
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

	private void updatePlayerLabels() {
		if (getBoardFace().isReside()) {
			topPlayerLabel.setText(getWhitePlayerName());
			gamePanelView.setBottomPlayerLabel(getBlackPlayerName());
		} else {
			topPlayerLabel.setText(getBlackPlayerName());
			gamePanelView.setBottomPlayerLabel(getWhitePlayerName());
		}
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		super.switch2Analysis(isAnalysis);
		Log.d("live", "switch2Analysis analysis = " + isAnalysis);
		if (isAnalysis) {
			liveService.setLatestMoveNumber(0);
			ChessBoardLive.resetInstance();
		}
		gamePanelView.enableControlButtons(isAnalysis);
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
		gamePanelView.haveNewMessage(false);

		Intent intent = new Intent(this, ChatLiveActivity.class);
		intent.putExtra(BaseGameItem.TIMESTAMP, currentGame.getTimestamp());
		startActivity(intent);
	}

	private void checkMessages() {
		if (currentGame.hasNewMessage()) {
			gamePanelView.haveNewMessage(true);
		}
	}

	@Override
	public void newGame() {
		startActivity(new Intent(this, LiveNewGameActivity.class));
	}

	@Override
	public void updateAfterMove() {
		if (!getBoardFace().isAnalysis())
			sendMove("update");
	}

	@Override
	public void invalidateGameScreen() {
		if (isLCSBound) {
			showSubmitButtonsLay(getBoardFace().isSubmit());

			topPlayerLabel.setVisibility(View.VISIBLE);
			topPlayerClock.setVisibility(View.VISIBLE);

			updatePlayerLabels();
			liveService.paintClocks();
			changePlayersLabelColors();

			boardView.setMovesLog(getBoardFace().getMoveListSAN());
		}
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_live, menu);
		return super.onCreateOptionsMenu(menu);
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
			switch2Analysis(false);
			getBoardFace().setAnalysis(false);

			updateGameState();

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (currentGame != null) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

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

	private void updatePlayerLabels(Game game, int newWhiteRating, int newBlackRating) {
		if (getBoardFace().isReside()) {

			topPlayerLabel.setText(game.getWhitePlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR
					+ newWhiteRating + StaticData.SYMBOL_RIGHT_PAR);
			gamePanelView.setBottomPlayerLabel(game.getBlackPlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR
					+ newBlackRating + StaticData.SYMBOL_RIGHT_PAR);
		} else {
			topPlayerLabel.setText(game.getBlackPlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR
					+ newBlackRating + StaticData.SYMBOL_RIGHT_PAR);
			gamePanelView.setBottomPlayerLabel(game.getWhitePlayer().getUsername() + StaticData.SYMBOL_LEFT_PAR
					+ newWhiteRating + StaticData.SYMBOL_RIGHT_PAR); // always at the bottom
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
		TextView rulesLinkTxt = (TextView) layout.findViewById(R.id.rulesLinkTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating = liveService.getLastGame().getRatingForPlayer(liveService.getUsername());
		/*if (userPlayWhite) {
			currentPlayerNewRating = whitePlayerNewRating;
		} else {
			currentPlayerNewRating = blackPlayerNewRating;
		}*/

		int ratingChange = liveService.getLastGame().getRatingChangeForPlayer(liveService.getUsername());
		/*String sign;
		if(currentPlayerRating < currentPlayerNewRating){ // 800 1200
			ratingDiff = currentPlayerNewRating - currentPlayerRating;
			sign = StaticData.SYMBOL_PLUS;
		} else { // 800 700
			ratingDiff = currentPlayerRating - currentPlayerNewRating;
			sign = StaticData.SYMBOL_MINUS;
		}*/

		String ratingChangeString = ratingChange > 0 ? "+" + ratingChange : "" + ratingChange;

		String rating = getString(R.string.your_end_game_rating, ratingChangeString, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

		inneractiveRectangleAd = (InneractiveAd) layout.findViewById(R.id.inneractiveRectangleAd);
		InneractiveAdHelper.showRectangleAd(inneractiveRectangleAd, this);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getSupportFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.shareBtn).setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this)) {
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
			boardView.setBoardFace(ChessBoardLive.getInstance(this));
			boardView.setGameActivityFace(this);
			onGameStarted();
			getBoardFace().setJustInitialized(false);
			//liveService.executePausedActivityGameEvents();
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
		} else if (view.getId() == R.id.submitBtn) {
			sendMove("submit click");
		} else if (view.getId() == R.id.newGamePopupBtn) {
			Intent intent = new Intent(this, LiveNewGameActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (view.getId() == R.id.rulesLinkTxt) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.fair_play_policy_url))));
		} else if (view.getId() == R.id.shareBtn) {
			ShareItem shareItem = new ShareItem(currentGame, currentGame.getGameId(), getString(R.string.live));

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
		} else if (view.getId() == R.id.acceptDrawBtn) {
			if (isLCSBound) {
				Log.i(TAG, "Request draw: " + liveService.getCurrentGame());
				liveService.runMakeDrawTask();
			}
			drawButtonsLay.setVisibility(View.GONE);
		} else if (view.getId() == R.id.declineDrawBtn) {
			if (isLCSBound) {
				Log.i(TAG, "Decline draw: " + liveService.getCurrentGame());
				liveService.runRejectDrawTask();
			}
			drawButtonsLay.setVisibility(View.GONE);
		} else if (view.getId() == R.id.rematchPopupBtn) {
			liveService.rematch();
			dismissDialogs();
		}
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
					showPopupDialog(R.string.drawoffer, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
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

}

