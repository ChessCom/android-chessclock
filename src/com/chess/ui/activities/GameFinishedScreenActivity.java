package com.chess.ui.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.ChessBoardNetworkView;
import com.chess.ui.views.ChessBoardOnlineView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.ChessComApiParser;

import java.util.ArrayList;

/**
 * GameFinishedScreenActivity class
 *
 * @author alien_roger
 * @created at: 03.05.12 5:52
 */
public class GameFinishedScreenActivity extends GameBaseActivity {

	private MenuOptionsDialogListener menuOptionsDialogListener;

	private StartGameUpdateListener startGameUpdateListener;
	private GamesListUpdateListener gamesListUpdateListener;
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

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);
		gamePanelView.hideChatButton();

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

		startGameUpdateListener = new StartGameUpdateListener();
		gamesListUpdateListener = new GamesListUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		boardView.setBoardFace(new ChessBoard(this));
		getBoardFace().setMode(AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);

		updateGameState();
	}

	private void updateGameState() {
		getOnlineGame(gameId);
		getBoardFace().setInit(false);
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
		getSoundPlayer().playGameStart();

		currentGame = ChessComApiParser.GetGameParseV3(returnedObj);

		adjustBoardForGame();
	}

	private void adjustBoardForGame() {
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

	public void onGameRefresh(GameItem newGame) {
		currentGame = newGame;
		String[] moves;
		int[] moveFT;

		if (currentGame.values.get(AppConstants.MOVE_LIST).contains("1.")) {

			int beginIndex = 1;

			moves = currentGame.values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]",
					StaticData.SYMBOL_EMPTY).replaceAll("  ", " ").substring(beginIndex).split(" ");

			if (moves.length - boardView.getBoardFace().getMovesCount() == 1) {
				moveFT = MoveParser.parse(boardView.getBoardFace(), moves[moves.length - 1]);

				boolean playSound = false;

				if (moveFT.length == 4) {
					Move move;
					if (moveFT[3] == 2)
						move = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					boardView.getBoardFace().makeMove(move, playSound);
				} else {
					Move move = new Move(moveFT[0], moveFT[1], 0, 0);
					boardView.getBoardFace().makeMove(move, playSound);
				}

				boardView.getBoardFace().setMovesCount(moves.length);
				boardView.invalidate();
			}
			invalidateGameScreen();
		}
	}

	public void invalidateGameScreen() {
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
					if (gameListItem.type == GameListItem.LIST_TYPE_CURRENT && gameListItem.values.get(GameListItem.IS_MY_TURN).equals(GameListItem.V_ONE)) {
						currentGames.add(gameListItem);
					}
				}

				for (GameListItem currentGame : currentGames) {
					if (currentGame.getGameId() != gameId) {
						boardView.setBoardFace(new ChessBoard(GameFinishedScreenActivity.this));
						getBoardFace().setAnalysis(false);
						getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

						getOnlineGame(currentGame.getGameId()); // if next game
						return;
					}
				}
				finish();
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
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
				getOnlineGame(gameId);
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
					getOnlineGame(gameId);
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
	protected void onGameEndMsgReceived() {
	}

	@Override
	protected void restoreGame() {
		boardView.setBoardFace(new ChessBoard(this));
		boardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));

		adjustBoardForGame();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
