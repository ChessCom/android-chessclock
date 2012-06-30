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
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.ChessBoardNetworkView;
import com.chess.ui.views.ChessBoardOnlineView;
import com.chess.ui.views.GamePanelView;
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

    private AsyncTask<LoadItem, Void, Integer> updateGameStateTask;
	private ChessBoardNetworkView boardView;

	private GameItem currentGame;
	private long gameId;

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

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);

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
			getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
			getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}
		boardView.setGameActivityFace(this);
	}

	@Override
	public void init() {
		super.init();
		gameId = extras.getLong(GameListItem.GAME_ID);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.backtogamelist),
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

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V3);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(gameId));

			updateGameStateTask = new GetStringObjTask(gameStateUpdateListener).executeTask(loadItem);
		}
	}

	protected void getOnlineGame(long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_GAME_V3);
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

		checkMessages();

		adjustBoardForGame();
	}

	private void adjustBoardForGame() {
		boardView.setFinished(false);

		if (currentGame.values.get(GameListItem.GAME_TYPE).equals("2"))
			getBoardFace().setChess960(true);

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}
		String[] moves = {};

		if (currentGame.values.get(AppConstants.MOVE_LIST).contains("1.")) {
			int beginIndex = 1;

			moves = currentGame.values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", " ").substring(beginIndex).split(" ");

			getBoardFace().setMovesCount(moves.length);
		} else {
			getBoardFace().setMovesCount(0);
		}

		String FEN = currentGame.values.get(GameItem.STARTING_FEN_POSITION);
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			getBoardFace().genCastlePos(FEN);
			MoveParser.fenParse(FEN, getBoardFace());
		}

		for (int i = 0, cnt = getBoardFace().getMovesCount(); i < cnt; i++) {
			boardView.updateMoves(moves[i]);
		}

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

				newGame = ChessComApiParser.GetGameParseV3(returnedObj);

				onGameRefresh();

				checkMessages();
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	@Override
	public void onGameRefresh() {
		currentGame = newGame;
		String[] moves;

		if (currentGame.values.get(AppConstants.MOVE_LIST).contains("1.")) {
			int beginIndex = 1;

			moves = currentGame.values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", " ").substring(beginIndex).split(" ");

			if (moves.length - getBoardFace().getMovesCount() == 1) {
				boardView.updateMoves(moves[moves.length - 1]);

				getBoardFace().setMovesCount(moves.length);
				boardView.invalidate();
			}
			invalidateGameScreen();
		}
	}

	public void invalidateGameScreen() {
		if (getBoardFace().isSubmit())
			showSubmitButtonsLay(true);

        whitePlayerLabel.setText(getWhitePlayerName());
        blackPlayerLabel.setText(getBlackPlayerName());

		boardView.addMove2Log(getBoardFace().getMoveListSAN());
	}

	@Override
	public String getWhitePlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.values.get(AppConstants.WHITE_USERNAME) + StaticData.SYMBOL_LEFT_PAR + currentGame.values.get(GameItem.WHITE_RATING) + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public String getBlackPlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.values.get(AppConstants.BLACK_USERNAME) + StaticData.SYMBOL_LEFT_PAR + currentGame.values.get(GameItem.BLACK_RATING) + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public void updateAfterMove() {
		showSubmitButtonsLay(false);

		if (currentGame == null) { // if we don't have Game entity
			// get game entity
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V3);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(gameId));

			new GetStringObjTask(getGameUpdateListener).executeTask(loadItem);
		} else {
			sendMove();
		}
	}

	private void sendMove() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_SUBMIT);
		loadItem.addRequestParams(RestHelper.P_NEWMOVE, getBoardFace().convertMoveEchess());
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.values.get(GameListItem.TIMESTAMP));

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
			moveWasSent();

			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(R.id.notification_message);
		}
	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);
		int action = AppData.getAfterMoveAction(getContext());
		if (action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
			finish();
		else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
			getGamesList();
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

				ArrayList<GameListItem> currentGames = new ArrayList<GameListItem>();

				for (GameListItem gameListItem : ChessComApiParser.getCurrentOnlineGames(returnedObj)) {
					if (gameListItem.type == GameListItem.LIST_TYPE_CURRENT
							&& gameListItem.values.get(GameListItem.IS_MY_TURN).equals(GameListItem.V_ONE)) {
						currentGames.add(gameListItem);
					}
				}

				for (GameListItem currentGame : currentGames) {
					if (currentGame.getGameId() != gameId) {
						showSubmitButtonsLay(false);
						boardView.setBoardFace(new ChessBoard(GameOnlineScreenActivity.this));
						getBoardFace().setAnalysis(false);
						getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

						getOnlineGame(currentGame.getGameId()); // if next game
						// same new gameId
						Intent intent = getIntent();
						intent.putExtra(GameListItem.GAME_ID, currentGame.getGameId());
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

	private boolean openChatActivity() {
		preferencesEditor.putString(AppConstants.OPPONENT, currentGame.values.get(
				isUserColorWhite() ? AppConstants.BLACK_USERNAME : AppConstants.WHITE_USERNAME));
		preferencesEditor.commit();

		currentGame.values.put(GameItem.HAS_NEW_MESSAGE, "0");
		gamePanelView.haveNewMessage(false);

		Intent intent = new Intent(this, ChatOnlineActivity.class);
		intent.putExtra(GameListItem.GAME_ID, gameId);
		intent.putExtra(GameListItem.TIMESTAMP, currentGame.values.get(GameListItem.TIMESTAMP));
		startActivity(intent);

		chat = false;
		return true;
	}


	private void checkMessages() {
		if (currentGame.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			gamePanelView.haveNewMessage(true);
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
		return currentGame.values.get(AppConstants.WHITE_USERNAME).toLowerCase()
				.equals(AppData.getUserName(this));
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
		getBoardFace().setSubmit(show);
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
				isMoveNav = true;
				break;
			case R.id.menu_next:
				boardView.moveForward();
				isMoveNav = true;
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		final CharSequence[] items;
		private final int ECHESS_SETTINGS = 0;
		private final int ECHESS_BACK_TO_GAME_LIST = 1;
		private final int ECHESS_MESSAGES = 2;
		private final int ECHESS_RESIDE = 3;
		private final int ECHESS_DRAW_OFFER = 4;
		private final int ECHESS_RESIGN_OR_ABORT = 5;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case ECHESS_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case ECHESS_BACK_TO_GAME_LIST:
					onBackPressed();
					break;
				case ECHESS_MESSAGES:
					openChatActivity();
					break;
				case ECHESS_RESIDE:
					getBoardFace().setReside(!getBoardFace().isReside());
					boardView.invalidate();
					break;
				case ECHESS_DRAW_OFFER:
					popupItem.setTitle(R.string.drawoffer);
					popupItem.setMessage(R.string.are_you_sure_q);
					popupDialogFragment.show(getSupportFragmentManager(), DRAW_OFFER_RECEIVED_TAG);
					break;
				case ECHESS_RESIGN_OR_ABORT:
					popupItem.setTitle(R.string.abort_resign_game);
					popupItem.setMessage(R.string.are_you_sure_q);
					popupDialogFragment.show(getSupportFragmentManager(), ABORT_GAME_TAG);
					break;
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_RECEIVED_TAG)) {
			String draw = DataHolder.getInstance().isAcceptdraw() ? AppConstants.ACCEPTDRAW : AppConstants.OFFERDRAW;

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
			loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.values.get(GameListItem.TIMESTAMP));

			new GetStringObjTask(drawOfferedUpdateListener).executeTask(loadItem);
		} else if (fragment.getTag().equals(ABORT_GAME_TAG)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.values.get(GameListItem.TIMESTAMP));

			new GetStringObjTask(abortGameUpdateListener).executeTask(loadItem);
		}
	}

	protected void changeChatIcon(Menu menu) {
		if (currentGame.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
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
				if (MopubHelper.isShowAds(getContext())) {
					sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
							.putExtra(AppConstants.MESSAGE, "GAME OVER")
							.putExtra(AppConstants.FINISHABLE, true));
				} else {
					finish();
				}
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
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
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		gamePanelView.haveNewMessage(true);
	}

	@Override
	protected void restoreGame() {
		boardView.setBoardFace(new ChessBoard(this));
		boardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));

		adjustBoardForGame();
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			getBoardFace().takeBack();
			getBoardFace().decreaseMovesCount();
			boardView.invalidate();
		} else if (view.getId() == R.id.submit) {
			sendMove();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (updateGameStateTask != null)
			updateGameStateTask.cancel(true);
	}
}
