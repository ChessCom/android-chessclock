package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListCurrentItem;
import com.chess.model.GameOnlineItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.views.ChessBoardNetworkView;
import com.chess.ui.views.ChessBoardOnlineView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameOnlineScreenActivity extends GameBaseActivity {

	private static final String DRAW_OFFER_TAG = "offer draw";
	private static final String END_GAME_TAG = "end game popup";
	private static final String ERROR_TAG = "send request failed popup";

	private int UPDATE_DELAY = 120000;
	private View submitButtonsLay;

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private AbortGameUpdateListener abortGameUpdateListener;
	private DrawOfferedUpdateListener drawOfferedUpdateListener;

	private GameStateUpdateListener gameStateUpdateListener;
	private StartGameUpdateListener startGameUpdateListener;
	private GetGameUpdateListener getGameUpdateListener;
	private SendMoveUpdateListener sendMoveUpdateListener;
	private GamesListUpdateListener gamesListUpdateListener;
	private CreateChallengeUpdateListener createChallengeUpdateListener;

	private AsyncTask<LoadItem, Void, Integer> updateGameStateTask;
	private ChessBoardNetworkView boardView;

	private GameOnlineItem currentGame;
	private long gameId;
//	private int currentPlayerRating;
	private GameListCurrentItem gameInfoItem;
	private String timeRemains;
	private TextView infoLabelTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_online);
		init();
		widgetsInit();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		infoLabelTxt = (TextView) findViewById(R.id.thinking);

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submitBtn).setOnClickListener(this);
		findViewById(R.id.cancelBtn).setOnClickListener(this);

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);
		gamePanelView.enableGameControls(false);

		boardView = (ChessBoardOnlineView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		setBoardView(boardView);

		ChessBoard chessBoard = (ChessBoard) getLastCustomNonConfigurationInstance();
		if (chessBoard != null) {
			boardView.setBoardFace(chessBoard);
		} else {
			boardView.setBoardFace(new ChessBoard(this));
			getBoardFace().setInit(true);
			getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}
		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);
	}

	public void init() {
		gameInfoItem = (GameListCurrentItem) extras.getSerializable(BaseGameItem.GAME_INFO_ITEM);
		gameId = gameInfoItem.getGameId();
		timeRemains = gameInfoItem.getTimeRemainingAmount() + gameInfoItem.getTimeRemainingUnits();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.messages),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(R.string.resignorabort)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		abortGameUpdateListener = new AbortGameUpdateListener();
		drawOfferedUpdateListener = new DrawOfferedUpdateListener();

		gameStateUpdateListener = new GameStateUpdateListener();
		startGameUpdateListener = new StartGameUpdateListener();
		getGameUpdateListener = new GetGameUpdateListener();
		sendMoveUpdateListener = new SendMoveUpdateListener();
		gamesListUpdateListener = new GamesListUpdateListener();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();

		showActionRefresh = true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateGameState();
		handler.postDelayed(updateGameStateOrder, UPDATE_DELAY);  // run repeatable task
	}

	private Runnable updateGameStateOrder = new Runnable() {
		@Override
		public void run() {
			updateGameState();
			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_DELAY);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(updateGameStateOrder);
	}

	private void updateGameState() {
		if (getBoardFace().isInit()) {
			getOnlineGame(gameId);
			getBoardFace().setInit(false);
		} else {
			// TODO don't check updates if it' our move
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V5);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(gameId));

			updateGameStateTask = new GetStringObjTask(gameStateUpdateListener).executeTask(loadItem);
		}
	}

	protected void getOnlineGame(long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_GAME_V5);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(gameId));

		new GetStringObjTask(startGameUpdateListener).executeTask(loadItem);
	}

	private class StartGameUpdateListener extends ChessUpdateListener {
		public StartGameUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				onGameStarted(returnedObj);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	private void onGameStarted(String returnedObj) {
		showSubmitButtonsLay(false);
		getSoundPlayer().playGameStart();

		currentGame = ChessComApiParser.GetGameParseV3(returnedObj);
		gamePanelView.enableGameControls(true);
		boardView.lockBoard(false);

		checkMessages();

		adjustBoardForGame();
	}

	private void adjustBoardForGame() {
		boardView.setFinished(false);

		if (isUserMove()) {
			infoLabelTxt.setText(timeRemains);
			setWhitePlayerDot(userPlayWhite);
		} else {
			setWhitePlayerDot(!userPlayWhite);
		}

		if (currentGame.getGameType().equals("2"))
			getBoardFace().setChess960(true);

		if (!userPlayWhite) {
			getBoardFace().setReside(true);
		}

		String FEN = currentGame.getFenStartPosition();
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			getBoardFace().genCastlePos(FEN);
			MoveParser.fenParse(FEN, getBoardFace());
		}
		if (currentGame.getMoveList().contains("1.")) {
			int beginIndex = 1;
			String[] moves = currentGame.getMoveList()
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", " ").substring(beginIndex).split(" ");

			getBoardFace().setMovesCount(moves.length);
			for (int i = 0, cnt = getBoardFace().getMovesCount(); i < cnt; i++) {
				getBoardFace().updateMoves(moves[i], false);
			}
		} else {
			getBoardFace().setMovesCount(0);
		}


		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();

		playLastMoveAnimation();
	}

	private class GameStateUpdateListener extends ChessUpdateListener {
		public GameStateUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				if (getBoardFace().isAnalysis())
					return;

				GameOnlineItem newGame = ChessComApiParser.GetGameParseV3(returnedObj);

				onGameRefresh(newGame);

				checkMessages();
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	public void onGameRefresh(GameOnlineItem newGame) {
		currentGame = newGame;
		gamePanelView.enableGameControls(true);
		boardView.lockBoard(false);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		if (isUserMove()) {
			infoLabelTxt.setText(timeRemains);
			setWhitePlayerDot(userPlayWhite);
		} else {
			setWhitePlayerDot(!userPlayWhite);
		}

		if (currentGame.getMoveList().contains("1.")) {
			int beginIndex = 1;

			String[] moves = currentGame.getMoveList()
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", " ").substring(beginIndex).split(" ");

			if (moves.length - getBoardFace().getMovesCount() == 1) {
				getBoardFace().updateMoves(moves[moves.length - 1], false);

				getBoardFace().setMovesCount(moves.length);
				boardView.invalidate();
			}
			invalidateGameScreen();
		}
	}

	public void invalidateGameScreen() {
		showSubmitButtonsLay(getBoardFace().isSubmit());

		whitePlayerlabel.setText(getWhitePlayerName());
		blackPlayerLabel.setText(getBlackPlayerName());

		boardView.setMovesLog(getBoardFace().getMoveListSAN());
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
		return currentGame != null;
	}

	@Override
	public void updateAfterMove() {
		showSubmitButtonsLay(false);

		if (currentGame == null) { // if we don't have Game entity
			// get game entity
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V5);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(gameId));

			new GetStringObjTask(getGameUpdateListener).executeTask(loadItem);
		} else {
			sendMove();
		}
	}

	private void sendMove() {
		//save rating
//		currentPlayerRating = getCurrentPlayerRating();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_SUBMIT);
		loadItem.addRequestParams(RestHelper.P_NEWMOVE, getBoardFace().convertMoveEchess());
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestampStr());

		new GetStringObjTask(sendMoveUpdateListener).executeTask(loadItem);
	}


	private class GetGameUpdateListener extends ChessUpdateListener {
		public GetGameUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				currentGame = ChessComApiParser.GetGameParseV3(returnedObj);
				gamePanelView.enableGameControls(true);
				boardView.lockBoard(false);

				sendMove();
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	private class SendMoveUpdateListener extends ChessUpdateListener {
		public SendMoveUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			if (isPaused)
				return;

			if (show) {
				showPopupHardProgressDialog(R.string.sendinggameinfo);
			} else
				dismissProgressDialog();
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				moveWasSent();

				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel((int) gameId);
				mNotificationManager.cancel(R.id.notification_message);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.substring(RestHelper.R_ERROR.length()));
			}
		}
	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);
		if(boardView.isFinished()){
			showGameEndPopup(endGamePopupView, endGameMessage);
		} else {
			int action = AppData.getAfterMoveAction(getContext());
			if (action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
				finish();
			else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
				getGamesList();
			}
		}
	}


	private void getGamesList() {
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

		new GetStringObjTask(gamesListUpdateListener).executeTask(listLoadItem);
	}

	private class GamesListUpdateListener extends ChessUpdateListener {
		public GamesListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {

				ArrayList<GameListCurrentItem> currentGames = new ArrayList<GameListCurrentItem>();

				for (GameListCurrentItem gameListItem : ChessComApiParser.getCurrentOnlineGames(returnedObj)) {
					if (gameListItem.getIsMyTurn()) {
						currentGames.add(gameListItem);
					}
				}

				for (GameListCurrentItem currentGame : currentGames) {
					if (currentGame.getGameId() != gameId) {
						gameId = currentGame.getGameId();
						showSubmitButtonsLay(false);
						boardView.setBoardFace(new ChessBoard(GameOnlineScreenActivity.this));
						getBoardFace().setAnalysis(false);

						getOnlineGame(gameId); // if next game
						// same new gameId
						Intent intent = getIntent();
						intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameInfoItem);
						getIntent().replaceExtras(intent);
						return;
					}
				}
				finish();

			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	private void openChatActivity() {
		if(currentGame == null)
			return;

		preferencesEditor.putString(AppConstants.OPPONENT, userPlayWhite
				? currentGame.getBlackUsername() : currentGame.getWhiteUsername());
		preferencesEditor.commit();

		currentGame.setHasNewMessage(false);
		gamePanelView.haveNewMessage(false);

		Intent intent = new Intent(this, ChatOnlineActivity.class);
		intent.putExtra(BaseGameItem.GAME_ID, gameId);
		intent.putExtra(BaseGameItem.TIMESTAMP, currentGame.getTimestampStr());
		startActivity(intent);

		chat = false;
	}


	private void checkMessages() {
		if (currentGame.hasNewMessage()) {
			gamePanelView.haveNewMessage(true);
		}
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		super.switch2Analysis(isAnalysis);
		if (getBoardFace().isAnalysis()){
			infoLabelTxt.setVisibility(View.GONE);
		} else {
			infoLabelTxt.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void switch2Chat() {
		openChatActivity();
	}

	@Override
	public void newGame() {
		getGamesList();
	}

	public Boolean isUserColorWhite() {
		return currentGame.getWhiteUsername().toLowerCase()
				.equals(AppData.getUserName(this));
	}

	private boolean isUserMove() {
		if(currentGame == null)  // TODO probably redundant
			return false;

		userPlayWhite = currentGame.getWhiteUsername().toLowerCase()
				.equals(AppData.getUserName(this));

		return (currentGame.isWhiteMove() && userPlayWhite)
				|| (!currentGame.isWhiteMove() && !userPlayWhite);
	}


	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		submitButtonsLay.setVisibility(show ? View.VISIBLE : View.GONE);
		if (!show) {
			getBoardFace().setSubmit(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_echess, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateGameState();
				break;
			case R.id.menu_next_game:
				newGame();
				break;
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_analysis:
				boardView.switchAnalysis();
				break;
			case R.id.menu_chat:
				openChatActivity();
				break;
			case R.id.menu_previous:
				boardView.moveBack();
				break;
			case R.id.menu_next:
				boardView.moveForward();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		final CharSequence[] items;
		private final int ECHESS_SETTINGS = 0;
		private final int ECHESS_MESSAGES = 1;
		private final int ECHESS_RESIDE = 2;
		private final int ECHESS_DRAW_OFFER = 3;
		private final int ECHESS_RESIGN_OR_ABORT = 4;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case ECHESS_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case ECHESS_MESSAGES:
					openChatActivity();
					break;
				case ECHESS_RESIDE:
					getBoardFace().setReside(!getBoardFace().isReside());
					boardView.invalidate();
					break;
				case ECHESS_DRAW_OFFER:
					showPopupDialog(R.string.drawoffer, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
					break;
				case ECHESS_RESIGN_OR_ABORT:
					showPopupDialog(R.string.abort_resign_game, R.string.are_you_sure_q, ABORT_GAME_TAG);
					break;
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_RECEIVED_TAG)) {
			String draw = DataHolder.getInstance().isAcceptDraw()? RestHelper.V_ACCEPTDRAW : RestHelper.V_OFFERDRAW;

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
			loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestampStr());

			new GetStringObjTask(drawOfferedUpdateListener).executeTask(loadItem);
		} else if (fragment.getTag().equals(ABORT_GAME_TAG)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestampStr());

			new GetStringObjTask(abortGameUpdateListener).executeTask(loadItem);
		} else if(fragment.getTag().equals(ERROR_TAG)){
			backToLoginActivity();
		}
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

	private class AbortGameUpdateListener extends ChessUpdateListener {
		public AbortGameUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				onGameOver(getString(R.string.game_over), true);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		if(currentGame == null)
			return;


		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
//		endGameTitleTxt.setText(R.string.game_over); // already set to game over
		endGameReasonTxt.setText(message);


		int currentPlayerNewRating = getCurrentPlayerRating();

//		int ratingDiff; // TODO fill difference in ratings
//		String sign;
//		if(currentPlayerRating < currentPlayerNewRating){ // 800 1200
//			ratingDiff = currentPlayerNewRating - currentPlayerRating;
//			sign = StaticData.SYMBOL_PLUS;
//		} else { // 800 700
//			ratingDiff = currentPlayerRating - currentPlayerNewRating;
//			sign = StaticData.SYMBOL_MINUS;
//		}

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		MopubHelper.showRectangleAd(adViewWrapper, this);
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getSupportFragmentManager(), END_GAME_TAG);
		blockScreenRotation(true);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);
		if (AppUtils.isNeedToUpgrade(this)) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
	}

	private int getCurrentPlayerRating() {
		if (userPlayWhite) {
			return Integer.valueOf(currentGame.getWhiteRating());
		} else {
			return Integer.valueOf(currentGame.getBlackRating());
		}
	}

	private class DrawOfferedUpdateListener extends ChessUpdateListener {
		public DrawOfferedUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				showSinglePopupDialog(R.string.drawoffered, DRAW_OFFER_TAG);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	@Override
	protected void restoreGame() {
		boardView.setBoardFace(new ChessBoard(this));

		adjustBoardForGame();
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
			if(currentGame == null)
				return;

			sendMove();
		} else if (view.getId() == R.id.newGamePopupBtn) {
			endPopupFragment.dismiss();
			Intent intent = new Intent(this, OnlineNewGameActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (view.getId() == R.id.rematchPopupBtn) {
			sendRematch();
            endPopupFragment.dismiss();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (updateGameStateTask != null)
			updateGameStateTask.cancel(true);
	}

	private void sendRematch() {
		String opponent;
		String color; // reversed color
		if (userPlayWhite) {
			opponent = currentGame.getBlackUsername();
			color = "2";
		}
		else {
			opponent = currentGame.getWhiteUsername();
			color = "1";
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_TIMEPERMOVE, currentGame.getDaysPerMove());
		loadItem.addRequestParams(RestHelper.P_IPLAYAS, color);
		loadItem.addRequestParams(RestHelper.P_ISRATED, currentGame.getRated());
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, currentGame.getGameType());
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponent);

		new GetStringObjTask(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener {
		public CreateChallengeUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showPopupDialog(getString(R.string.error), returnedObj.substring(RestHelper.R_ERROR.length()),
						ERROR_TAG);
			}
			blockScreenRotation(false);
		}
	}
}
