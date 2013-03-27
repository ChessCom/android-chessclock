package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.*;
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
import com.chess.backend.share.facebook.Share2Facebook;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListCurrentItem;
import com.chess.model.GameOnlineItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.TweetPreviewFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.views.ChessBoardNetworkView;
import com.chess.ui.views.ChessBoardOnlineView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.InneractiveAdHelper;
import com.inneractive.api.ads.InneractiveAd;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameOnlineScreenActivity extends GameBaseActivity {

	private static final String DRAW_OFFER_TAG = "offer draw";
	private static final String ERROR_TAG = "send request failed popup";

	private View submitButtonsLay;
	private View shareButtonsLay;

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
	private GameListCurrentItem gameInfoItem;
	private String timeRemains;
	private TextView infoLabelTxt;
	private IntentFilter boardUpdateFilter;
	private BroadcastReceiver moveUpdateReceiver;

	protected boolean userPlayWhite = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_online);
		init();
		widgetsInit();
	}

	@Override
	protected void onStart() {
		super.onStart();

		DataHolder.getInstance().setInOnlineGame(gameId, true);
		updateGameState();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);    //To change body of overridden methods use File | Settings | File Templates.

		if (intent.getExtras() != null) {
			gameInfoItem = (GameListCurrentItem) intent.getParcelableExtra(BaseGameItem.GAME_INFO_ITEM);

			if (gameInfoItem != null){
				gameId = gameInfoItem.getGameId();

				ChessBoardOnline.resetInstance();

				showSubmitButtonsLay(false);
				boardView.setBoardFace(ChessBoardOnline.getInstance(GameOnlineScreenActivity.this));
				getBoardFace().setAnalysis(false);

				updateGameState();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		moveUpdateReceiver = new MoveUpdateReceiver();
		registerReceiver(moveUpdateReceiver, boardUpdateFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unRegisterMyReceiver(moveUpdateReceiver);

		DataHolder.getInstance().setInOnlineGame(gameId, false);
		if (HONEYCOMB_PLUS_API) {
			dismissDialogs();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (updateGameStateTask != null) {
			updateGameStateTask.cancel(true);
		}
	}

	public void init() {
		gameInfoItem = (GameListCurrentItem) extras.getParcelable(BaseGameItem.GAME_INFO_ITEM);

		gameId = gameInfoItem.getGameId();

		menuOptionsDialogListener = new MenuOptionsDialogListener();
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

	private class MoveUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateGameState();
		}
	}

	private void updateGameState() {
		if (getBoardFace().isJustInitialized()) {
			getOnlineGame(gameId);
			getBoardFace().setJustInitialized(false);
		} else {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V5);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, gameId);

			updateGameStateTask = new GetStringObjTask(gameStateUpdateListener).executeTask(loadItem);
		}
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
			showSubmitButtonsLay(false);
			getSoundPlayer().playGameStart();

			currentGame = ChessComApiParser.getGameParseV3(returnedObj);

//			DBDataManager.updateOnlineGame(getContext(), currentGame);

			DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

			gamePanelView.enableGameControls(true);
			boardView.lockBoard(false);

			checkMessages();

			adjustBoardForGame();
		}
	}

	private void adjustBoardForGame() {
		boardView.setFinished(false);
		shareButtonsLay.setVisibility(View.GONE);

		timeRemains = gameInfoItem.getTimeRemainingAmount() + gameInfoItem.getTimeRemainingUnits();

		if (isUserMove()) {
			infoLabelTxt.setText(StaticData.SYMBOL_EMPTY); // disable time as it incorrect when switching to next game

//			infoLabelTxt.setText(timeRemains);
			updatePlayerDots(userPlayWhite);
		} else {
			infoLabelTxt.setText(StaticData.SYMBOL_EMPTY);
			updatePlayerDots(!userPlayWhite);
		}

		BoardFace boardFace = getBoardFace(); 
		if (currentGame.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		if (!userPlayWhite) {
			boardFace.setReside(true);
		}

		String FEN = currentGame.getFenStartPosition();
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			boardFace.genCastlePos(FEN);
			MoveParser.fenParse(FEN, boardFace);
		}

		if (currentGame.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			int beginIndex = 1;
			String[] moves = currentGame.getMoveList()
					.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
					.replaceAll("  ", " ").substring(beginIndex).split(" ");

			boardFace.setMovesCount(moves.length);
			for (int i = 0, cnt = boardFace.getMovesCount(); i < cnt; i++) {
				boardFace.updateMoves(moves[i], false);
			}
		} else {
			boardFace.setMovesCount(0);
		}


		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();
	}

	private class GameStateUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			currentGame = ChessComApiParser.getGameParseV3(returnedObj);

//			DBDataManager.updateOnlineGame(getContext(), currentGame);

			gamePanelView.enableGameControls(true);
			boardView.lockBoard(false);

			if (getBoardFace().isAnalysis()) {
				boardView.enableAnalysis();
				return;
			}

			onGameRefresh();
			checkMessages();
		}
	}

	public void onGameRefresh() {

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		timeRemains = gameInfoItem.getTimeRemainingAmount() + gameInfoItem.getTimeRemainingUnits();

		if (isUserMove()) {
			infoLabelTxt.setText(StaticData.SYMBOL_EMPTY); // disable time as it incorrect when switching to next game

//			infoLabelTxt.setText(timeRemains);
			updatePlayerDots(userPlayWhite);
		} else {
			infoLabelTxt.setText(StaticData.SYMBOL_EMPTY);
			updatePlayerDots(!userPlayWhite);
		}

		if (currentGame.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
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

	@Override
	public void invalidateGameScreen() {
		showSubmitButtonsLay(getBoardFace().isSubmit());

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
		showSubmitButtonsLay(false);

		if (currentGame == null) { // if we don't have Game entity
			// get game entity
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GET_GAME_V5);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_GID, gameId);

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
		loadItem.addRequestParams(RestHelper.P_CHESSID, gameId);
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_SUBMIT);
		loadItem.addRequestParams(RestHelper.P_NEWMOVE, getBoardFace().convertMoveEchess());
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestamp());

		new GetStringObjTask(sendMoveUpdateListener).executeTask(loadItem);
	}


	private class GetGameUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			currentGame = ChessComApiParser.getGameParseV3(returnedObj);

//			DBDataManager.updateOnlineGame(getContext(), currentGame);

			gamePanelView.enableGameControls(true);
			boardView.lockBoard(false);

			sendMove();
		}
	}

	private class SendMoveUpdateListener extends ChessUpdateListener {

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
			mNotificationManager.cancel((int) gameId);
			mNotificationManager.cancel(R.id.notification_message);
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

		gamePanelView.enableGameControls(false);
		boardView.lockBoard(true);

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
					gameId = currentGame.getGameId();
					showSubmitButtonsLay(false);
					boardView.setBoardFace(ChessBoardOnline.getInstance(GameOnlineScreenActivity.this));
					getBoardFace().setJustInitialized(false);
					getBoardFace().setAnalysis(false);

					gameInfoItem.setGameId(gameId);

					getOnlineGame(gameId); // if next game
					// same new gameId
					Intent intent = getIntent();              // TODO update gameInfoItem
					intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameInfoItem);
					getIntent().replaceExtras(intent);
					return;
				}
			}
			finish();
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
/*
		Offer draw should be able only after the first move was made.
		Also Abort should change to Resign after that.
*/
		userPlayWhite = currentGame.getWhiteUsername().toLowerCase()
				.equals(AppData.getUserName(this));

		boolean userMove =  (currentGame.isWhiteMove() && userPlayWhite)
				|| (!currentGame.isWhiteMove() && !userPlayWhite);

		if (getBoardFace().getHply() < 1 && userMove) {
			menuOptionsItems = new CharSequence[]{
					getString(R.string.settings),
					getString(R.string.messages),
					getString(R.string.emailgame),
					getString(R.string.reside),
					getString(R.string.abort)};
		} else {
			menuOptionsItems = new CharSequence[]{
					getString(R.string.settings),
					getString(R.string.messages),
					getString(R.string.emailgame),
					getString(R.string.reside),
					getString(R.string.drawoffer),
					getString(R.string.resign)};
		}

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
		private final int ECHESS_SETTINGS = 0;
		private final int ECHESS_MESSAGES = 1;
		private final int EMAIL_GAME = 2;
		private final int ECHESS_RESIDE = 3;
		private final int ECHESS_DRAW_OFFER = 4;
		private final int ECHESS_RESIGN_OR_ABORT = 5;

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case ECHESS_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case ECHESS_MESSAGES:
					openChatActivity();
					break;
				case EMAIL_GAME:
					sendPGN();
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

	private void sendPGN() {
		CharSequence moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = currentGame.getWhiteUsername();
		String blackPlayerName = currentGame.getBlackUsername();
		String result = GAME_GOES;
		boolean finished = boardView.isFinished();
		if(finished){// means in check state
			if (getBoardFace().getSide() == ChessBoard.LIGHT) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
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
				.append("\n [TimeControl \"").append(timeControl.toString()).append("\"]");
		if(finished){
			builder.append("\n [Termination \"").append(endGameMessage).append("\"]");
		}
		builder.append("\n ").append(moves)
				.append("\n \n Sent from my Android");

		sendPGN(builder.toString());
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(DRAW_OFFER_RECEIVED_TAG)) {
			String draw;

			if (gameInfoItem.isDrawOfferPending()) { // If Draw was already offered by the opponent, we send accept to it.
				draw = RestHelper.V_ACCEPTDRAW;
			} else {
				draw = RestHelper.V_OFFERDRAW;
				// save at this point state to DB
				currentGame.setUserOfferedDraw(true);
//				String[] arguments = new String[]{String.valueOf(currentGame.isUserOfferedDraw())};
//				getContentResolver().update(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI,
//						DBDataManager.putGameOnlineItemToValues(currentGame, AppData.getUserName(this)),
//						DBDataManager.SELECTION_USER_OFFERED_DRAW, arguments);
			}

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, gameId);
			loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestamp());

			new GetStringObjTask(drawOfferedUpdateListener).executeTask(loadItem);
		} else if (tag.equals(ABORT_GAME_TAG)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			loadItem.addRequestParams(RestHelper.P_CHESSID, gameId);
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestamp());

			new GetStringObjTask(abortGameUpdateListener).executeTask(loadItem);
		} else if(tag.equals(ERROR_TAG)){
			backToLoginActivity();
		}
		super.onPositiveBtnClick(fragment);
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

		@Override
		public void updateData(String returnedObj) {
			onGameOver(getString(R.string.game_over), true);
		}
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		if(currentGame == null)
			return;

		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating = getCurrentPlayerRating();

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
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

		if (AppUtils.isNeedToUpgrade(this)) {
			/*LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
        	MopubHelper.showRectangleAd(adViewWrapper, this);*/
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
		// show share buttons
		shareButtonsLay.setVisibility(View.VISIBLE);
	}

	private int getCurrentPlayerRating() {
		if (userPlayWhite) {
			return currentGame.getWhiteRating();
		} else {
			return currentGame.getBlackRating();
		}
	}

	private class DrawOfferedUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			showSinglePopupDialog(R.string.drawoffered, DRAW_OFFER_TAG);
		}
	}

	@Override
	protected void restoreGame() {
		ChessBoardOnline.resetInstance();
		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		adjustBoardForGame();
		getBoardFace().setJustInitialized(false);
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
			dismissDialogs();
			Intent intent = new Intent(this, OnlineNewGameActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (view.getId() == R.id.shareFaceBookBtn) {
			Share2Facebook share2Facebook = new Share2Facebook(this, R.drawable.ic_facebook, "Facebook");
			ShareItem shareItem = new ShareItem(currentGame, gameId, getString(R.string.online));
			share2Facebook.shareMe(shareItem);
		} else if (view.getId() == R.id.shareTwitterBtn) {
			ShareItem shareItem = new ShareItem(currentGame, gameId, getString(R.string.online));

			TweetPreviewFragment previewFragment = TweetPreviewFragment.newInstance(shareItem.composeTwitterMessage());
			previewFragment.show(getSupportFragmentManager(), "tweet preview");
		} else if (view.getId() == R.id.rematchPopupBtn) {
			sendRematch();
			dismissDialogs();
		}
	}

	private void sendRematch() {
		String opponent;
		int color; // reversed color
		if (userPlayWhite) {
			opponent = currentGame.getBlackUsername();
			color = 2;
		} else {
			opponent = currentGame.getWhiteUsername();
			color = 1;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_TIMEPERMOVE, currentGame.getDaysPerMove());
		loadItem.addRequestParams(RestHelper.P_IPLAYAS, color);
		loadItem.addRequestParams(RestHelper.P_ISRATED, currentGame.getRated()? 1 : 0);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, currentGame.getGameType());
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponent);

		new GetStringObjTask(createChallengeUpdateListener).executeTask(loadItem);
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		infoLabelTxt = (TextView) findViewById(R.id.thinking);

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submitBtn).setOnClickListener(this);
		findViewById(R.id.cancelBtn).setOnClickListener(this);

		shareButtonsLay = findViewById(R.id.shareButtonsLay);
		findViewById(R.id.shareFaceBookBtn).setOnClickListener(this);
		findViewById(R.id.shareTwitterBtn).setOnClickListener(this);

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);
		gamePanelView.enableGameControls(false);

		boardView = (ChessBoardOnlineView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);
		setBoardView(boardView);

		if (extras.getBoolean(AppConstants.NOTIFICATION, false)) {
			ChessBoardOnline.resetInstance();
		}

		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);

		boardUpdateFilter = new IntentFilter(IntentConstants.BOARD_UPDATE);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}
}
