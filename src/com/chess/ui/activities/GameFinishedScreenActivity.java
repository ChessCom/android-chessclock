package com.chess.ui.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListCurrentItem;
import com.chess.model.GameOnlineItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.ChessBoardNetworkView;
import com.chess.ui.views.ChessBoardOnlineView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.ChessComApiParser;

import java.util.ArrayList;
import java.util.Calendar;

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

	private GameOnlineItem currentGame;
	private long gameId;
	private View shareBtn;

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

		shareBtn = findViewById(R.id.shareBtn);
		shareBtn.setOnClickListener(this);
		shareBtn.setVisibility(View.VISIBLE);

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);
		gamePanelView.hideChatButton();
		gamePanelView.enableGameControls(false);

		boardView = (ChessBoardOnlineView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		setBoardView(boardView);

		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		boardView.setGameActivityFace(this);
	}

	public void init() {
		gameId = extras.getLong(BaseGameItem.GAME_ID);

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.emailgame),
				getString(R.string.share_game)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();

		startGameUpdateListener = new StartGameUpdateListener();
		gamesListUpdateListener = new GamesListUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		ChessBoardOnline.resetInstance();
		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		getBoardFace().setMode(AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);

		getOnlineGame(gameId);
		setBoardToFinishedState();
	}

	protected void getOnlineGame(long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_GAME_V5);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_GID, gameId);

		new GetStringObjTask(startGameUpdateListener).executeTask(loadItem);
	}

	private class StartGameUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			getSoundPlayer().playGameStart();

			currentGame = ChessComApiParser.getGameParseV3(returnedObj);
			gamePanelView.enableGameControls(true);

			adjustBoardForGame();
		}
	}

	private void adjustBoardForGame() {
		if (currentGame == null)
			return;

		if (currentGame.getGameType() == BaseGameItem.CHESS_960)
			getBoardFace().setChess960(true);

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}

		String[] moves = {};

		if (currentGame.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			int beginIndex = 1;

			moves = currentGame.getMoveList()
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", StaticData.SYMBOL_SPACE).substring(beginIndex)
					.split(StaticData.SYMBOL_SPACE);

			getBoardFace().setMovesCount(moves.length);
		} else {
			getBoardFace().setMovesCount(0);
		}

		String FEN = currentGame.getFenStartPosition();
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			getBoardFace().genCastlePos(FEN);
			MoveParser.fenParse(FEN, getBoardFace());
		}

		for (int i = 0, cnt = getBoardFace().getMovesCount(); i < cnt; i++) {
			getBoardFace().updateMoves(moves[i], false);
		}

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		invalidateGameScreen();
		getBoardFace().takeBack();
		boardView.invalidate();

		playLastMoveAnimation();
	}

	@Override
	public void invalidateGameScreen() {
		whitePlayerLabel.setText(getWhitePlayerName());
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
	}

	private void getGamesList() {
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

		new GetStringObjTask(gamesListUpdateListener).executeTask(listLoadItem);
	}

	private class GamesListUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			ArrayList<GameListCurrentItem> currentGames = new ArrayList<GameListCurrentItem>();

			for (GameListCurrentItem gameListItem : ChessComApiParser.getCurrentOnlineGames(returnedObj)) {
				if (gameListItem.isMyTurn()) {
					currentGames.add(gameListItem);
				}
			}

			for (GameListCurrentItem currentGame : currentGames) {
				if (currentGame.getGameId() != gameId) {
					boardView.setBoardFace(ChessBoardOnline.getInstance(GameFinishedScreenActivity.this));
					getBoardFace().setAnalysis(false);
					getOnlineGame(currentGame.getGameId()); // if next game
					return;
				}
			}
			finish();
		}
	}

	@Override
	public void newGame() {
		getGamesList();
	}

	@Override
	public Boolean isUserColorWhite() {
		if (currentGame != null)
			return currentGame.getWhiteUsername().toLowerCase().equals(AppData.getUserName(this));
		else
			return null;
	}

	@Override
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
	public void showSubmitButtonsLay(boolean show) {
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
		private final int SHARE = 2;

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case ECHESS_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case EMAIL_GAME:
					sendPGN();
					break;
				case SHARE:
					shareBtn.setVisibility(View.GONE);
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
		if (daysPerMove > 1) {
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
		ChessBoardOnline.resetInstance();
		boardView.setBoardFace(ChessBoardOnline.getInstance(this));

		adjustBoardForGame();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.shareBtn) {
			ShareItem shareItem = new ShareItem(currentGame, gameId, getString(R.string.online));

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
		}
	}

}
