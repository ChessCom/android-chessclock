package com.chess.ui.activities;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;

import java.util.ArrayList;

/**
 * GameFinishedScreenActivity class
 *
 * @author alien_roger
 * @created at: 03.05.12 5:52
 */
public class GameFinishedScreenActivity extends GameBaseActivity implements View.OnClickListener {

//	private int UPDATE_DELAY = 120000;
	private View submitButtonsLay;


	private MenuOptionsDialogListener menuOptionsDialogListener;
	private AbortGameUpdateListener abortGameUpdateListener;
	private DrawOfferUpdateListener drawOfferUpdateListener;
	private StartGameUpdateListener startGameUpdateListener;
	private GetGameUpdateListener getGameUpdateListener;
	private SendMoveUpdateListener sendMoveUpdateListener;
	private GamesListUpdateListener gamesListUpdateListener;
	private ProgressDialog sendMoveUpdateDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive);
		init();
		widgetsInit();
		onPostCreate();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);
        gamePanelView.hideChatButton();

	}

	@Override
	protected void init() {
		super.init();
		mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));


		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.backtogamelist),
				getString(R.string.messages),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(R.string.resignorabort)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
		abortGameUpdateListener = new AbortGameUpdateListener();
		drawOfferUpdateListener = new DrawOfferUpdateListener();

		startGameUpdateListener = new StartGameUpdateListener();
		getGameUpdateListener = new GetGameUpdateListener();
		sendMoveUpdateListener = new SendMoveUpdateListener();
		gamesListUpdateListener = new GamesListUpdateListener();

		sendMoveUpdateDialog = new ProgressDialog(this);
		sendMoveUpdateDialog.setMessage(getString(R.string.sendinggameinfo));
		sendMoveUpdateDialog.setIndeterminate(true);
		sendMoveUpdateDialog.setCancelable(false);

	}

	@Override
	protected void onResume() {
		super.onResume();

		boardView.setBoardFace(new ChessBoard(this));
		getBoardFace().setMode( AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);

		updateGameSate();
	}

	private void updateGameSate() {
		getOnlineGame(mainApp.getGameId());
		getBoardFace().setInit(false);
	}

	@Override
	protected void getOnlineGame(long game_id) {
		super.getOnlineGame(game_id);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_GAME_V3);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(game_id));

		new GetStringObjTask(startGameUpdateListener).execute(loadItem);
	}
	private class StartGameUpdateListener extends ChessUpdateListener {

		public StartGameUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			onGameStarted(returnedObj);
		}
	}

	private void onGameStarted(String returnedObj) {
		showSubmitButtonsLay(false);
		getSoundPlayer().playGameStart();

		mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(returnedObj));

		adjustBoardForGame();
	}

	private void adjustBoardForGame() {
		if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
			getBoardFace().setChess960(true);

		if (!isUserColorWhite()) {
			getBoardFace().setReside(true);
		}
		String[] moves = {};

		if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
			moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST)
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", " ").substring(1).split(" ");

			getBoardFace().setMovesCount(moves.length);
		} else if (!mainApp.isLiveChess()) {
			getBoardFace().setMovesCount(0);
		}

		Game game = lccHolder.getGame(mainApp.getGameId());
		if (game != null && game.getSeq() > 0) {
			lccHolder.doReplayMoves(game);
		}

		String FEN = mainApp.getCurrentGame().values.get(GameItem.STARTING_FEN_POSITION);
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

	public void invalidateGameScreen() {
		if (getBoardFace().isSubmit())
			showSubmitButtonsLay(true);

		if (mainApp.getCurrentGame() != null) {
			whitePlayerLabel.setText(mainApp.getWhitePlayerName());
			blackPlayerLabel.setText(mainApp.getBlackPlayerName());
		}

		boardView.addMove2Log(getBoardFace().getMoveListSAN());
	}


	@Override
	public void updateAfterMove() {
		showSubmitButtonsLay(false);

		if (mainApp.getCurrentGame() == null) { // if we don't have Game entity
			if (appService.getRepeatableTimer() != null) {
				appService.getRepeatableTimer().cancel();
				appService.setRepeatableTimer(null);
			}

			// get game entity
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V3);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(mainApp.getGameId()));

			new GetStringObjTask(getGameUpdateListener).execute(loadItem);
		} else {
			sendMove();
		}
	}

	private void sendMove() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(mainApp.getCurrentGameId()));
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_SUBMIT);
		loadItem.addRequestParams(RestHelper.P_NEWMOVE, getBoardFace().convertMoveEchess());
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));

		new GetStringObjTask(sendMoveUpdateListener).execute(loadItem);
	}


	private class GetGameUpdateListener extends ChessUpdateListener {
		public GetGameUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(returnedObj));
			sendMove();
		}
	}

	private class SendMoveUpdateListener extends ChessUpdateListener {
		public SendMoveUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);

			if (GameFinishedScreenActivity.this.isFinishing())
				return;

			if (show) {
				sendMoveUpdateDialog.show();
			} else
				sendMoveUpdateDialog.dismiss();
		}


		@Override
		public void updateData(String returnedObj) {
			moveWasSent();

			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(R.id.notification_message);
		}
	}

	private void moveWasSent(){
		showSubmitButtonsLay(false);
		int action = AppData.getAfterMoveAction(getContext());
		if(action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
			finish();
		else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
			getGamesList();
		}
	}


	private void getGamesList(){
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

		new GetStringObjTask(gamesListUpdateListener).execute(listLoadItem);
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
					if (gameListItem.type == GameListItem.LIST_TYPE_CURRENT && gameListItem.values.get(GameListItem.IS_MY_TURN).equals("1")) {
						currentGames.add(gameListItem);
					}
				}
				for (GameListItem currentGame : currentGames) {
					if(currentGame.getGameId() != mainApp.getCurrentGameId()){
						showSubmitButtonsLay(false);
						boardView.setBoardFace(new ChessBoard(GameFinishedScreenActivity.this));
						getBoardFace().setAnalysis(false);
						getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);
						getOnlineGame(currentGame.getGameId()); // if next game
						return;
					}
				}
				finish();

			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				if(!isFinishing())
					mainApp.showDialog(getContext(), AppConstants.ERROR, returnedObj.split("[+]")[1]);
			}
		}
	}

	@Override
	public void update(int code) {
	}

	@Override
	public void newGame() {
		getGamesList();
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
				getOnlineGame(mainApp.getGameId());
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
					getOnlineGame(mainApp.getGameId());
					break;
				case ECHESS_RESIDE:
					getBoardFace().setReside(!getBoardFace().isReside());
					boardView.invalidate();
					break;
				case ECHESS_DRAW_OFFER:
					showDialog(DIALOG_DRAW_OFFER);
					break;
				case ECHESS_RESIGN_OR_ABORT:
					showDialog(DIALOG_ABORT_OR_RESIGN);
					break;
			}
		}
	}

	protected void changeChatIcon(Menu menu) {
		if (mainApp.getCurrentGame().values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainApp.getCurrentGame() != null) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			String draw = AppConstants.OFFERDRAW;
			if (mainApp.acceptdraw)
				draw = AppConstants.ACCEPTDRAW;               // hide to resthelper


			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(mainApp.getCurrentGameId()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));

			new GetStringObjTask(drawOfferUpdateListener).execute(loadItem);
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(mainApp.getCurrentGameId()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));

			new GetStringObjTask(abortGameUpdateListener).execute(loadItem);
		}
	}

	private class AbortGameUpdateListener extends ChessUpdateListener {
		public AbortGameUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				if (MopubHelper.isShowAds(mainApp)) {
					sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
							.putExtra(AppConstants.MESSAGE, "GAME OVER")
							.putExtra(AppConstants.FINISHABLE, true));
				} else {
					finish();
				}
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				if(!isFinishing())
					mainApp.showDialog(getContext(), AppConstants.ERROR, returnedObj.split("[+]")[1]);
			}
		}
	}

	private class DrawOfferUpdateListener extends ChessUpdateListener {
		public DrawOfferUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(isFinishing())
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				mainApp.showDialog(getContext(), StaticData.SYMBOL_EMPTY, getString(R.string.drawoffered));
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				mainApp.showDialog(getContext(), AppConstants.ERROR, returnedObj.split("[+]")[1]);
			}
		}
	}





	@Override
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		gamePanelView.haveNewMessage(true);
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
}
