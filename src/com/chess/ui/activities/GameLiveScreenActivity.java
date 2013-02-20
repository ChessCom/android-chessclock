package com.chess.ui.activities;

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
import com.chess.model.BaseGameItem;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoardLive;
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

	private static final String TAG = "GameLiveScreenActivity";
	private static final String WARNING_TAG = "warning message popup";

	private static final long BLINK_DELAY = 5 * 1000;
	private static final long UNBLINK_DELAY = 400;

	private MenuOptionsDialogListener menuOptionsDialogListener;

	protected TextView topPlayerLabel;
	protected TextView topPlayerClock;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_live);

		widgetsInit();
		/*lccInitiated = init();

		if(!lccInitiated){
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
		}*/
	}

	private void init() {

		/*if (!getLccHolder().isConnected()) {
			showToast(R.string.application_was_killed);
			return false;
		}*/

		currentGame = getLccHolder().getGameItem();
		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		/*Game game = getLccHolder().getCurrentGame();
		switch (game.getGameTimeConfig().getGameTimeClass()) {
			case BLITZ:
				currentPlayerRating = getLccHolder().getUser().getRatingFor(GameRatingClass.Blitz);
				break;
			case LIGHTNING:
				currentPlayerRating = getLccHolder().getUser().getRatingFor(GameRatingClass.Lightning);
				break;
			case STANDARD:
				currentPlayerRating = getLccHolder().getUser().getRatingFor(GameRatingClass.Standard);
				break;
		}*/

		if (!getLccHolder().currentGameExist()) {
			gamePanelView.enableAnalysisMode(true);

			boardView.setFinished(true);
			gamePanelView.showBottomPart(false);
		}

		getLccHolder().setLccEventListener(this);
		getLccHolder().setLccChatMessageListener(this);

		int resignOrAbort = getLccHolder().getResignTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();

		lccInitiated = true;

		//return true;
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		fadeLay = findViewById(R.id.fadeLay);
		gameBoardView = findViewById(R.id.baseView);

		boardView = (ChessBoardLiveView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		setBoardView(boardView);

		//boardView.setBoardFace(ChessBoardLive.getInstance(this));
		boardView.setGameActivityFace(this);

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
	protected void onResume() {
		super.onResume();

		if (getLccHolder() != null) {
			if (!lccInitiated) { // probably redundant
				finish();
				return;
			}

			//getLccHolder().setGameActivityPausedMode(false); // moved to execute paused events
			getLccHolder().setLccChatMessageListener(this);
			updateGameState();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (getLccHolder() != null) {
			gameTaskRunner = new LccGameTaskRunner(gameTaskListener, getLccHolder());
		}
	}

	@Override
	protected void onLiveServiceConnected() {
		super.onLiveServiceConnected();

		gameTaskRunner = new LccGameTaskRunner(gameTaskListener, getLccHolder());

		// todo: duplicate in onStart with lccholder check
		boardView.setBoardFace(ChessBoardLive.getInstance(this));

		init();

		/*if (!lccInitiated) {
			return;
		}*/

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		invalidateGameScreen(); //

		Log.d("Live Game", "GameLiveScreenActivity started ");
		if (getLccHolder().getPendingWarnings().size() > 0) {
			// get last warning
			warningMessage = getLccHolder().getLastWarningMessage();

			Log.d("LCCLOG-WARNING", warningMessage);

			showPopupDialog(R.string.warning, warningMessage, WARNING_TAG); // todo: check
		}

		if (!lccInitiated) {
			finish();
			return;
		}

		//getLccHolder().setGameActivityPausedMode(false); // moved to execute paused events
		getLccHolder().setLccChatMessageListener(this);
		updateGameState();
	}

	@Override
	protected void onPause() {
		dismissDialogs();

		super.onPause();

		if (getLccHolder() != null) { // when user goes to Pause mode too fast, even before Live service init
		getLccHolder().setGameActivityPausedMode(true);
		}

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

		getLccHolder().checkAndReplayMoves();

		// temporary disable playLastMoveAnimation feature, because it can be one of the illegalmove reasons potentially
		// todo: probably could be enabled with new LCC
		/*invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();
		playLastMoveAnimation();*/

		getLccHolder().checkFirstTestMove();
	}

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

	// ----------------------Lcc Events ---------------------------------------------

	public void onGameRecreate() {
		finish();
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

		blockGame(false);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				invalidateGameScreen();
			}
		});
		getLccHolder().checkTestMove();

		checkMessages();
	}

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

	/*@Override
	public void onInform(String title, String message){
		showSinglePopupDialog(title, message);
	}*/

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

		final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		final Game game = getLccHolder().getLastGame();
		final List<Integer> ratings = game.getRatings();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				final View layout;
				if (!AppUtils.isNeedToUpgrade(GameLiveScreenActivity.this, getLccHolder())) {
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

	// -----------------------------------------------------------------------------------

	private void blockGame(final boolean block){
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
				"username=" + getLccHolder().getUsername() +
						"lccInitiated=" + lccInitiated +
						", " + boardDebug +
						", gameSeq=" + getLccHolder().getCurrentGame().getMoves().size() +
						", boardHply=" + getBoardFace().getHply() +
						", moveLive=" + getBoardFace().convertMoveLive() +
						", gamesC=" + getLccHolder().getGamesCount() +
						", gameId=" + getGameId() +
						", analysisPanel=" + gamePanelView.isAnalysisEnabled() +
						", analysisBoard=" + getBoardFace().isAnalysis() +
						", latestMoveNumber=" + getLccHolder().getLatestMoveNumber() +
						", debugString=" + debugString +
						", submit=" + preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false) +
						", movesLive=" + getLccHolder().getCurrentGame().getMoves() +
						", moves=" + getBoardFace().getMoveListSAN() +
						", trace=" + stackTrace;
		temporaryDebugInfo = temporaryDebugInfo.replaceAll("\n", " ");
		//Log.d("TESTTEST", temporaryDebugInfo);

		LccGameTaskRunner gameTaskRunner = new LccGameTaskRunner(new GameTaskListener(), getLccHolder());
		getLccHolder().makeMove(move, gameTaskRunner, temporaryDebugInfo);
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
		if (isAnalysis) {
			getLccHolder().setLatestMoveNumber(0);
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
		if(!getBoardFace().isAnalysis())
			sendMove("update");
	}

	@Override
	public void invalidateGameScreen() {
		if (getLccHolder() != null) {
			showSubmitButtonsLay(getBoardFace().isSubmit());

			topPlayerLabel.setVisibility(View.VISIBLE);
			topPlayerClock.setVisibility(View.VISIBLE);

			updatePlayerLabels();
			getLccHolder().paintClocks();
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

		if(show){
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

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}
		LccGameTaskRunner gameTaskRunner = new LccGameTaskRunner(new GameTaskListener(), getLccHolder());
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
				Log.i(TAG,"LCCLOG: resign game: " + game);
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
			new LccGameTaskRunner(new GameTaskListener(), getLccHolder()).runRejectDrawTask();
		}
		super.onNegativeBtnClick(fragment);
	}

	protected void changeChatIcon(Menu menu) {
		MenuItem menuItem = menu.findItem(R.id.menu_chat);
		if(menuItem == null)
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

	private void blinkSubmitBtn(){
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

		int currentPlayerNewRating = getLccHolder().getLastGame().getRatingForPlayer(getLccHolder().getUsername());
		/*if (userPlayWhite) {
			currentPlayerNewRating = whitePlayerNewRating;
		} else {
			currentPlayerNewRating = blackPlayerNewRating;
		}*/

		int ratingChange = getLccHolder().getLastGame().getRatingChangeForPlayer(getLccHolder().getUsername());
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


		if (AppUtils.isNeedToUpgrade(this, getLccHolder())) {
			/*LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		    MopubHelper.showRectangleAd(adViewWrapper, this);*/
			inneractiveRectangleAd = (InneractiveAd) layout.findViewById(R.id.inneractiveRectangleAd);
			InneractiveAdHelper.showRectangleAd(inneractiveRectangleAd, this);
		}

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getSupportFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this, getLccHolder())) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
	}

	@Override
	protected void restoreGame() {
		ChessBoardLive.resetInstance();
		boardView.setBoardFace(ChessBoardLive.getInstance(this));
		boardView.setGameActivityFace(this);
		onGameStarted();
		getBoardFace().setJustInitialized(false);
		//getLccHolder().executePausedActivityGameEvents();
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

