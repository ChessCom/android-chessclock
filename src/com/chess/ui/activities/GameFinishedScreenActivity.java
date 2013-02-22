package com.chess.ui.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.chess.R;
import com.chess.backend.entity.new_api.DailyGameByIdItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.views.ChessBoardDailyView;
import com.chess.ui.views.ChessBoardNetworkView;
import com.chess.ui.views.ControlsNetworkView;

import java.util.Calendar;

/**
 * GameFinishedScreenActivity class
 *
 * @author alien_roger
 * @created at: 03.05.12 5:52
 */
public class GameFinishedScreenActivity extends GameBaseActivity {

	public static final String DOUBLE_SPACE = "  ";
	private static final int FINISHED_GAME = 0;
	private static final int GAMES_LIST = 1;

	private MenuOptionsDialogListener menuOptionsDialogListener;

//	private StartGameUpdateListener startGameUpdateListener;
	private LoadFromDbUpdateListener finishedGamesCursorUpdateListener;
	private ChessBoardNetworkView boardView;

	private DailyGameByIdItem.Data currentGame;
	private long gameId;
	private LoadFromDbUpdateListener loadFromDbUpdateListener;
	private ControlsNetworkView controlsNetworkView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_daily);
//		init();
		widgetsInit();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		controlsNetworkView = (ControlsNetworkView) findViewById(R.id.controlsNetworkView);

//		controlsNetworkView.changeGameButton(ControlsBaseView.B_NEW_GAME_ID, R.drawable.ic_next_game);
		controlsNetworkView.enableGameControls(false);

		boardView = (ChessBoardDailyView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsNetworkView);
		setBoardView(boardView);

//		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		boardView.setGameActivityFace(this);
	}

	public void init() {
		gameId = extras.getLong(BaseGameItem.GAME_ID);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.email_game)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();

//		startGameUpdateListener = new StartGameUpdateListener();
		finishedGamesCursorUpdateListener = new LoadFromDbUpdateListener(FINISHED_GAME);
		loadFromDbUpdateListener = new LoadFromDbUpdateListener(GAMES_LIST);
	}

	@Override
	protected void onStart() {
		init();
		super.onStart();
		ChessBoardOnline.resetInstance();
//		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		boardView.setGameActivityFace(GameFinishedScreenActivity.this);

		getBoardFace().setMode(AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);

		loadGame(gameId);
		setBoardToFinishedState();
	}

	protected void loadGame(long gameId) {
		new LoadDataFromDbTask(loadFromDbUpdateListener, DbHelper.getEchessGameParams(this, gameId),
				getContentResolver()).executeTask();

//		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.GET_GAME_V5);
//		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//		loadItem.addRequestParams(RestHelper.P_GID, gameId);
//
//		new GetStringObjTask(startGameUpdateListener).executeTask(loadItem);
	}

	private class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		private int listenerCode;

		public LoadFromDbUpdateListener(int listenerCode) {
			super(getContext());
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			switch (listenerCode) {
				case FINISHED_GAME:
					currentGame = DBDataManager.getGameFinishedItemFromCursor(returnedObj);
					returnedObj.close();

					controlsNetworkView.enableGameControls(true);

					adjustBoardForGame();
					break;
				case GAMES_LIST:
//					ArrayList<GameListCurrentItem> currentGames = new ArrayList<GameListCurrentItem>();
//
//					for (GameListCurrentItem gameListItem : ChessComApiParser.getCurrentOnlineGames(returnedObj)) {
//						if (gameListItem.isMyTurn()) {
//							currentGames.add(gameListItem);
//						}
//					}
//
//					for (GameListCurrentItem currentGame : currentGames) {
//						if (currentGame.getGameId() != gameId) {
//		//					boardView.setBoardFace(ChessBoardOnline.getInstance(GameFinishedScreenActivity.this));
//							boardView.setGameActivityFace(GameFinishedScreenActivity.this);
//
//							getBoardFace().setAnalysis(false);
//							loadGame(currentGame.getGameId()); // if next game
//							return;
//						}
//					}

					// iterate through all loaded items in cursor
					do {
						long localDbGameId = DBDataManager.getLong(returnedObj, DBConstants.V_GAME_ID);
						if (localDbGameId != gameId) {
							gameId = localDbGameId;
//							showSubmitButtonsLay(false);
							boardView.setGameActivityFace(GameFinishedScreenActivity.this);

							getBoardFace().setAnalysis(true);
							loadGame(gameId);
							return;
						}
					} while (returnedObj.moveToNext());

					finish();
					break;
			}
		}
	}

//	private class StartGameUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			getSoundPlayer().playGameStart();
//
//			currentGame = ChessComApiParser.getGameParseV3(returnedObj);
//			controlsNetworkView.enableGameControls(true);
//
//			adjustBoardForGame();
//		}
//	}

	private void adjustBoardForGame() {
		if(currentGame == null) {
			throw new IllegalStateException("adjustBoardForGame got null current game ");
		}

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == BaseGameItem.CHESS_960)
			boardFace.setChess960(true);

		if (!isUserColorWhite()) {
			boardFace.setReside(true);
		}

		String[] moves = {};

		if (currentGame.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			int beginIndex = 1;

			moves = currentGame.getMoveList()
					.replaceAll(AppConstants.MOVE_NUMBERS_PATTERN, StaticData.SYMBOL_EMPTY)
					.replaceAll(DOUBLE_SPACE, StaticData.SYMBOL_SPACE).substring(beginIndex)
					.split(StaticData.SYMBOL_SPACE);

			boardFace.setMovesCount(moves.length);
		} else {
			boardFace.setMovesCount(0);
		}

		String FEN = currentGame.getFenStartPosition();
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			boardFace.genCastlePos(FEN);
			MoveParser.fenParse(FEN, boardFace);
		}

		for (int i = 0, cnt = boardFace.getMovesCount(); i < cnt; i++) {
			boardFace.updateMoves(moves[i], false);
		}

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();
	}

	public void invalidateGameScreen() {
        whitePlayerLabel.setText(getWhitePlayerName());
        blackPlayerLabel.setText(getBlackPlayerName());

//		boardView.updateNotations(getBoardFace().getMoveListSAN());
		boardView.updateNotations(getBoardFace().getNotationArray());
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
	public BoardFace getBoardFace() {
		return ChessBoardOnline.getInstance(this);
	}

	@Override
	public void updateAfterMove() {
	}

	private void getGamesList() {
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getDailyCurrentMyListGamesParams(getContext()),
				getContentResolver()).executeTask();

//		LoadItem listLoadItem = new LoadItem();
//		listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
//		listLoadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//		listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_TRUE);
//
//		new GetStringObjTask(finishedGamesCursorUpdateListener).executeTask(listLoadItem);
	}

	@Override
	public void newGame() {
		getGamesList();
	}

	@Override
	public Boolean isUserColorWhite() {
		if (currentGame != null )
			return currentGame.getWhiteUsername().toLowerCase().equals(AppData.getUserName(this));
		else
			return null;
	}

	public Long getGameId() {
		return gameId;
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_echess_finished, menu);
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
		private final int ECHESS_SETTINGS = 0;
		private final int EMAIL_GAME = 1;

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case ECHESS_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case EMAIL_GAME:
					sendPGN();
					break;
			}
		}
	}

	private void sendPGN() {
		CharSequence moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = currentGame.getWhiteUsername();
		String blackPlayerName = currentGame.getBlackUsername();
		String result;

		if (getBoardFace().getSide() == ChessBoard.LIGHT) {
			result = BLACK_WINS;
		} else {
			result = WHITE_WINS;
		}

		int daysPerMove = currentGame.getDaysPerMove();

		StringBuilder timeControl = new StringBuilder();
		timeControl.append("1 in ").append(daysPerMove);
		if (daysPerMove > 1){
			timeControl.append(" days");
		} else {
			timeControl.append(" day");
		}

		String date = datePgnFormat.format(Calendar.getInstance().getTime());

		StringBuilder builder = new StringBuilder();
		builder.append("[Event \"").append(currentGame.getGameName()).append("\"]")
				.append("\n [Site \" Chess.com\"]")
				.append("\n [Date \"").append(date).append("\"]")
				.append("\n [White \"").append(whitePlayerName).append("\"]")
				.append("\n [Black \"").append(blackPlayerName).append("\"]")
				.append("\n [Result \"").append(result).append("\"]")
				.append("\n [WhiteElo \"").append(currentGame.getWhiteRating()).append("\"]")
				.append("\n [BlackElo \"").append(currentGame.getBlackRating()).append("\"]")
				.append("\n [TimeControl \"").append(timeControl.toString()).append("\"]")
				.append("\n ").append(moves)
				.append("\n \n Sent from my Android");

		sendPGN(builder.toString());
	}

	@Override
	protected void restoreGame() {
//		ChessBoardOnline.resetInstance();
//		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		boardView.setGameActivityFace(GameFinishedScreenActivity.this);


		adjustBoardForGame();
	}

}
