package com.chess.ui.fragments.daily;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.entity.api.DailyCurrentGameItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.model.PopupItem;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.chess_boards.ChessBoardNetworkView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.utilities.AppUtils;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.13
 * Time: 13:45
 */
public class GameDailyFragment extends GameBaseFragment implements GameNetworkFace, PopupListSelectionFace {

	private static final String DRAW_OFFER_TAG = "offer draw";
	private static final String ERROR_TAG = "send request failed popup";

	private static final int SEND_MOVE_UPDATE = 1;
	private static final int CREATE_CHALLENGE_UPDATE = 2;
	private static final int DRAW_OFFER_UPDATE = 3;
	private static final int ABORT_GAME_UPDATE = 4;

	// Options ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_OFFER_DRAW = 1;
	private static final int ID_ABORT_RESIGN = 2;
	private static final int ID_FLIP_BOARD = 3;
	private static final int ID_EMAIL_GAME = 4;
	private static final int ID_SETTINGS = 5;
	private static final String END_VACATION_TAG = "end vacation popup";

	private GameDailyUpdatesListener abortGameUpdateListener;
	private GameDailyUpdatesListener drawOfferedUpdateListener;

	private GameStateUpdateListener gameStateUpdateListener;
	private GameDailyUpdatesListener sendMoveUpdateListener;
	private GameDailyUpdatesListener createChallengeUpdateListener;
	private LoadFromDbUpdateListener currentGamesCursorUpdateListener;
	private ImageDownloaderToListener imageDownloader;

	private DailyCurrentGameData currentGame;
	protected boolean userPlayWhite = true;

	private IntentFilter boardUpdateFilter;
	private IntentFilter newChatUpdateFilter;
	private BroadcastReceiver moveUpdateReceiver;
	private NewChatUpdateReceiver newChatUpdateReceiver;

	private ChessBoardNetworkView boardView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsDailyView controlsDailyView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private LabelsConfig labelsConfig;
	private SparseArray<String> optionsMap;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private String[] countryNames;
	private int[] countryCodes;

	public GameDailyFragment() {
	}

	public static GameDailyFragment createInstance(long gameId) {
		GameDailyFragment fragment = new GameDailyFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
		}
		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_daily_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.daily);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		moveUpdateReceiver = new MoveUpdateReceiver();
		newChatUpdateReceiver = new NewChatUpdateReceiver();
		registerReceiver(moveUpdateReceiver, boardUpdateFilter);
		registerReceiver(newChatUpdateReceiver, newChatUpdateFilter);

		DataHolder.getInstance().setInOnlineGame(gameId, true);
		loadGameAndUpdate();
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterMyReceiver(moveUpdateReceiver);
		unRegisterMyReceiver(newChatUpdateReceiver);

		DataHolder.getInstance().setInOnlineGame(gameId, false);
		if (HONEYCOMB_PLUS_API) {
			dismissDialogs();
		}
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			getActivityFace().openFragment(new DailyNewGameFragment());
		} else if (code == ID_ABORT_RESIGN) {
			if (getBoardFace().getPly() < 1 && isUserMove()) {
				showPopupDialog(R.string.abort_game_, ABORT_GAME_TAG);
			} else {
				showPopupDialog(R.string.resign_game_, ABORT_GAME_TAG);
			}
		} else if (code == ID_OFFER_DRAW) {
			showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_EMAIL_GAME) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsBoardFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private class MoveUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long gameId = intent.getLongExtra(BaseGameItem.GAME_ID, 0);

			LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameId);
			new RequestJsonTask<DailyCurrentGameItem>(gameStateUpdateListener).executeTask(loadItem);
//			updateGameState(gameId);
		}
	}

	private class NewChatUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			controlsDailyView.haveNewMessage(true);
		}
	}

	private void loadGameAndUpdate() {
		// load game from DB. After load update
		Cursor cursor = DbDataManager.query(getContentResolver(),
				DbHelper.getDailyGame(gameId, getUsername()));

		if (cursor.moveToFirst()) {
			showSubmitButtonsLay(false);

			currentGame = DbDataManager.getDailyCurrentGameFromCursor(cursor);
			cursor.close();

			adjustBoardForGame();

			// clear badge
			DbDataManager.deletePlayMoveNotification(getContentResolver(), getUsername(), gameId);
			updateNotificationBadges();
		} /*else { // TODO should not get here, because we always have saved game in DB
			updateGameState(gameId);
		}*/
	}

	private class LoadFromDbUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			// iterate through all loaded items in cursor
			do {
				// check if it's user's move in this game
				long localDbGameId = DbDataManager.getLong(returnedObj, DbScheme.V_ID);
				boolean isMyTurn = DbDataManager.getInt(returnedObj, DbScheme.V_IS_MY_TURN) > 0;
				if (localDbGameId != gameId && isMyTurn) {
					gameId = localDbGameId;
					showSubmitButtonsLay(false);
					boardView.setGameFace(GameDailyFragment.this);

					getBoardFace().setAnalysis(false);

					loadGameAndUpdate();
					return;
				}
			} while (returnedObj.moveToNext());

			getActivityFace().showPreviousFragment();
		}
	}

//	protected void updateGameState(long gameId) {
//		LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameId);
//		new RequestJsonTask<DailyCurrentGameItem>(gameStateUpdateListener).executeTask(loadItem);
//	}

	private void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().equals(getAppData().getUsername());

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = currentGame.getBlackUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
			labelsConfig.topPlayerAvatar = currentGame.getBlackAvatar();
			labelsConfig.bottomPlayerAvatar = currentGame.getWhiteAvatar();
			labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getBlackUserCountry());
			labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getWhiteUserCountry());
			labelsConfig.topPlayerPremiumStatus = currentGame.getBlackPremiumStatus();
			labelsConfig.bottomPlayerPremiumStatus = currentGame.getWhitePremiumStatus();
		} else {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
			labelsConfig.topPlayerName = currentGame.getWhiteUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getWhiteRating());
			labelsConfig.bottomPlayerName = currentGame.getBlackUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.topPlayerAvatar = currentGame.getWhiteAvatar();
			labelsConfig.bottomPlayerAvatar = currentGame.getBlackAvatar();
			labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getWhiteUserCountry());
			labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getBlackUserCountry());
			labelsConfig.topPlayerPremiumStatus = currentGame.getWhitePremiumStatus();
			labelsConfig.bottomPlayerPremiumStatus = currentGame.getBlackPremiumStatus();
		}

		DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

		controlsDailyView.enableGameControls(true);
		boardView.lockBoard(false);

		if (currentGame.hasNewMessage()) {
			controlsDailyView.haveNewMessage(true);
		}

		getBoardFace().setFinished(false);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		long secondsRemain = currentGame.getTimeRemaining();
		String timeRemains;
		if (secondsRemain == 0) {
			timeRemains = getString(R.string.less_than_60_sec);
		} else {
			timeRemains = AppUtils.getTimeLeftFromSeconds(secondsRemain, getActivity());
		}

		String defaultTime = getString(R.string.days_arg, currentGame.getDaysPerMove());
		boolean userMove = isUserMove();
		if (userMove) {
			labelsConfig.topPlayerTime = defaultTime;
			labelsConfig.bottomPlayerTime = timeRemains;
		} else {
			labelsConfig.topPlayerTime = timeRemains;
			labelsConfig.bottomPlayerTime = defaultTime;
		}

		topPanelView.showTimeLeftIcon(!userMove);
		bottomPanelView.showTimeLeftIcon(userMove);

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		boardFace.setupBoard(currentGame.getStartingFenPosition());
		if (!userPlayWhite) {
			boardFace.setReside(true);
		}

		boardFace.checkAndParseMovesList(currentGame.getMoveList());

		boardView.resetValidMoves();

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		boardFace.setJustInitialized(false);

		imageDownloader.download(labelsConfig.topPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		imageDownloader.download(labelsConfig.bottomPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);
	}

	@Override
	public void toggleSides() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		}
		BoardAvatarDrawable tempDrawable = labelsConfig.topAvatar;
		labelsConfig.topAvatar = labelsConfig.bottomAvatar;
		labelsConfig.bottomAvatar = tempDrawable;

		String tempLabel = labelsConfig.topPlayerName;
		labelsConfig.topPlayerName = labelsConfig.bottomPlayerName;
		labelsConfig.bottomPlayerName = tempLabel;

		String tempScore = labelsConfig.topPlayerRating;
		labelsConfig.topPlayerRating = labelsConfig.bottomPlayerRating;
		labelsConfig.bottomPlayerRating = tempScore;

		String playerTime = labelsConfig.topPlayerTime;
		labelsConfig.topPlayerTime = labelsConfig.bottomPlayerTime;
		labelsConfig.bottomPlayerTime = playerTime;

		int playerPremiumStatus = labelsConfig.topPlayerPremiumStatus;
		labelsConfig.topPlayerPremiumStatus = labelsConfig.bottomPlayerPremiumStatus;
		labelsConfig.bottomPlayerPremiumStatus = playerPremiumStatus;
	}

	@Override
	public void invalidateGameScreen() {
		showSubmitButtonsLay(getBoardFace().isSubmit());

		if (labelsConfig.bottomAvatar != null) {
			labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
			bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
		}

		if (labelsConfig.topAvatar != null) {
			labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
			topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
		}

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);

		topPanelView.setPlayerName(labelsConfig.topPlayerName);
		topPanelView.setPlayerRating(labelsConfig.topPlayerRating);
		bottomPanelView.setPlayerName(labelsConfig.bottomPlayerName);
		bottomPanelView.setPlayerRating(labelsConfig.bottomPlayerRating);

		topPanelView.setPlayerFlag(labelsConfig.topPlayerCountry);
		bottomPanelView.setPlayerFlag(labelsConfig.bottomPlayerCountry);

		topPanelView.setPlayerPremiumIcon(labelsConfig.topPlayerPremiumStatus);
		bottomPanelView.setPlayerPremiumIcon(labelsConfig.bottomPlayerPremiumStatus);

		if (currentGameExist()) {
			topPanelView.setTimeRemain(labelsConfig.topPlayerTime);
			bottomPanelView.setTimeRemain(labelsConfig.bottomPlayerTime);

			boolean userMove = isUserMove();
			topPanelView.showTimeLeftIcon(!userMove);
			bottomPanelView.showTimeLeftIcon(userMove);
		}

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	protected void setBoardToFinishedState() { // TODO implement state conditions logic for board
		super.setBoardToFinishedState();
		showSubmitButtonsLay(false);
	}

	@Override
	public String getWhitePlayerName() {
		if (currentGame == null)
			return Symbol.EMPTY;
		else
			return currentGame.getWhiteUsername();
	}

	@Override
	public String getBlackPlayerName() {
		if (currentGame == null)
			return Symbol.EMPTY;
		else
			return currentGame.getBlackUsername();
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
		showSubmitButtonsLay(false);

		if (currentGame == null) { // TODO fix inappropriate state, current game can't be null here // if we don't have Game entity
			// get game entity
			throw new IllegalStateException("Current game became NULL");
//			updateGameState(gameId);
		} else {
			sendMove();
		}
	}

	private void sendMove() {
		LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId, RestHelper.V_SUBMIT, currentGame.getTimestamp());
		loadItem.addRequestParams(RestHelper.P_NEW_MOVE, getBoardFace().getLastMoveForDaily());
		new RequestJsonTask<BaseResponseItem>(sendMoveUpdateListener).executeTask(loadItem);
	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);

		// update DB
		currentGame.setMyTurn(false);
		currentGame.setFen(getBoardFace().generateFullFen());
		currentGame.setMoveList(getBoardFace().getMoveListSAN());
		DbDataManager.saveDailyGame(getContentResolver(), currentGame, getUsername());

		if (getBoardFace().isFinished()) {
			showGameEndPopup(endGamePopupView, endGameMessage);
		} else {
			int action = getAppData().getAfterMoveAction();
			if (action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
				backToHomeFragment();
			else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
				loadGamesList();
			}
		}
	}

	private void loadGamesList() {
		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getDailyCurrentListGames(getUsername()),
				getContentResolver()).executeTask();
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.createInstance(gameId));
	}

	@Override
	public void switch2Chat() {
		if (currentGame == null) {
			return;
		}

		currentGame.setHasNewMessage(false);
		controlsDailyView.haveNewMessage(false);

		getActivityFace().openFragment(DailyChatFragment.createInstance(gameId, labelsConfig.topPlayerAvatar)); // TODO check when flip
	}

	@Override
	public void playMove() {
		sendMove();
	}

	@Override
	public void cancelMove() {
		showSubmitButtonsLay(false);

		boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
		boardView.resetValidMoves();
		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();
		boardView.invalidate();
	}

	@Override
	public void goHome() {
		// not used in daily
	}

	@Override
	public void newGame() {
		loadGamesList();
	}

	@Override
	public Boolean isUserColorWhite() {
		if (currentGame != null && getActivity() != null)
			return currentGame.getWhiteUsername().equals(getAppData().getUsername());
		else
			return null;
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	private boolean isUserMove() {
		userPlayWhite = currentGame.getWhiteUsername()
				.equals(getAppData().getUsername());

		return /*(*/currentGame.isMyTurn()/* && userPlayWhite)
				|| (!currentGame.isWhiteMove() && !userPlayWhite)*/;
	}

	@Override
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}

		if (getBoardFace().getPly() < 1 && isUserMove()) {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.abort));
			optionsMap.remove(ID_OFFER_DRAW);
		} else {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.resign));
			optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
		}

		if (!isUserMove()) {
			optionsMap.remove(ID_OFFER_DRAW);
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsMap);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		controlsDailyView.showSubmitButtons(show);
		if (!show) {
			getBoardFace().setSubmit(false);
		}
	}

	private void sendPGN() {
		String moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = currentGame.getWhiteUsername();
		String blackPlayerName = currentGame.getBlackUsername();
		String result = GAME_GOES;

		boolean finished = getBoardFace().isFinished();
		if (finished) {// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
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
		builder.append("[Event \"").append(currentGame.getName()).append("\"]")
				.append("\n [Site \" Chess.com\"]")
				.append("\n [Date \"").append(date).append("\"]")
				.append("\n [White \"").append(whitePlayerName).append("\"]")
				.append("\n [Black \"").append(blackPlayerName).append("\"]")
				.append("\n [Result \"").append(result).append("\"]")
				.append("\n [WhiteElo \"").append(currentGame.getWhiteRating()).append("\"]")
				.append("\n [BlackElo \"").append(currentGame.getBlackRating()).append("\"]")
				.append("\n [TimeControl \"").append(timeControl.toString()).append("\"]");
		if (finished) {
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

			String username = getAppData().getUsername();
			boolean drawWasOffered = DbDataManager.checkIfDrawOffered(getContentResolver(), username, gameId);

			if (drawWasOffered) { // If Draw was already offered by the opponent, we send accept to it.
				draw = RestHelper.V_ACCEPTDRAW;
			} else {
				draw = RestHelper.V_OFFERDRAW;
			}

			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId, draw, currentGame.getTimestamp());
			new RequestJsonTask<BaseResponseItem>(drawOfferedUpdateListener).executeTask(loadItem);
		} else if (tag.equals(ABORT_GAME_TAG)) {

			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId, RestHelper.V_RESIGN, currentGame.getTimestamp());
			new RequestJsonTask<BaseResponseItem>(abortGameUpdateListener).executeTask(loadItem);
		} else if (tag.equals(END_VACATION_TAG)) {
			LoadItem loadItem = LoadHelper.deleteOnVacation(getUserToken());
			new RequestJsonTask<VacationItem>(new VacationUpdateListener()).executeTask(loadItem);

		} else if (tag.equals(ERROR_TAG)) {
			backToLoginFragment();
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		if (currentGame == null) {
			throw new IllegalStateException("showGameEndPopup starts with currentGame = null");
		}

//		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
//		endGameTitleTxt.setText(R.string.game_over); // already set to game over
		endGameReasonTxt.setText(message);


		int currentPlayerNewRating = getCurrentPlayerRating();

//		int ratingDiff; // TODO fill difference in ratings
//		String sign;
//		if(currentPlayerRating < currentPlayerNewRating){ // 800 1200
//			ratingDiff = currentPlayerNewRating - currentPlayerRating;
//			sign = StaticData.PLUS;
//		} else { // 800 700
//			ratingDiff = currentPlayerRating - currentPlayerNewRating;
//			sign = StaticData.MINUS;
//		}

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

//		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
//		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		PopupGameEndFragment endPopupFragment = PopupGameEndFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);

//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
//		}
	}

	private int getCurrentPlayerRating() {
		if (userPlayWhite) {
			return currentGame.getWhiteRating();
		} else {
			return currentGame.getBlackRating();
		}
	}

	@Override
	protected void restoreGame() {
//		ChessBoardOnline.resetInstance();
		boardView.setGameFace(this);

		adjustBoardForGame();
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			dismissDialogs();
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
		} else if (view.getId() == R.id.rematchPopupBtn) {
			sendRematch();
			dismissDialogs();
		}
	}

	private void sendRematch() {
		String opponent;
		if (userPlayWhite) {
			opponent = currentGame.getBlackUsername();
		} else {
			opponent = currentGame.getWhiteUsername();
		}

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), currentGame.getDaysPerMove(),
				currentGame.isRated() ? 1 : 0, currentGame.getGameType(), opponent);
		new RequestJsonTask<BaseResponseItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class GameStateUpdateListener extends ChessLoadUpdateListener<DailyCurrentGameItem> {

		private GameStateUpdateListener() {
			super(DailyCurrentGameItem.class);
		}

		@Override
		public void updateData(DailyCurrentGameItem returnedObj) {
			super.updateData(returnedObj);

			currentGame = returnedObj.getData();

			DbDataManager.updateDailyGame(getContentResolver(), currentGame, getAppData().getUsername());

			adjustBoardForGame();
		}
	}

	private class GameDailyUpdatesListener extends ChessLoadUpdateListener<BaseResponseItem> {
		private int listenerCode;

		private GameDailyUpdatesListener(int listenerCode) {
			super(BaseResponseItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			switch (listenerCode) {
				case SEND_MOVE_UPDATE:
					moveWasSent();

					NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel((int) gameId);
					mNotificationManager.cancel(R.id.notification_message);
					break;
//				case NEXT_GAME_UPDATE:
//					switchToNextGame(returnedObj);
//					break;
				case CREATE_CHALLENGE_UPDATE:
					showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
					break;
				case DRAW_OFFER_UPDATE:
					showSinglePopupDialog(R.string.draw_offered, DRAW_OFFER_TAG);
					break;
				case ABORT_GAME_UPDATE:
					onGameOver(getString(R.string.game_over), true);
					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_ARE_ON_VACATAION) {

					showPopupDialog(R.string.leave_vacation_to_submit_move_q, END_VACATION_TAG);
				} else {
					super.errorHandle(resultCode);
				}
			}
		}
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			showToast(R.string.vacation_off);
		}
	}

	public void init() {
		gameId = getArguments().getLong(BaseGameItem.GAME_ID, 0);

		boardUpdateFilter = new IntentFilter(IntentConstants.BOARD_UPDATE);
		newChatUpdateFilter = new IntentFilter(IntentConstants.NOTIFICATIONS_UPDATE);

		labelsConfig = new LabelsConfig();

		abortGameUpdateListener = new GameDailyUpdatesListener(ABORT_GAME_UPDATE);
		drawOfferedUpdateListener = new GameDailyUpdatesListener(DRAW_OFFER_UPDATE);

		gameStateUpdateListener = new GameStateUpdateListener();
		sendMoveUpdateListener = new GameDailyUpdatesListener(SEND_MOVE_UPDATE);
		createChallengeUpdateListener = new GameDailyUpdatesListener(CREATE_CHALLENGE_UPDATE);

		currentGamesCursorUpdateListener = new LoadFromDbUpdateListener();

		imageDownloader = new ImageDownloaderToListener(getActivity());

		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
	}

	private void widgetsInit(View view) {
		controlsDailyView = (ControlsDailyView) view.findViewById(R.id.controlsNetworkView);
		NotationView notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		controlsDailyView.enableGameControls(false);

		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsDailyView);
		boardView.setNotationsView(notationsView);

		setBoardView(boardView);

		boardView.setGameFace(this);
		boardView.lockBoard(true);

		{// options list setup
			optionsMap = new SparseArray<String>();
			optionsMap.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsMap.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsMap.put(ID_EMAIL_GAME, getString(R.string.email_game));
			optionsMap.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

	private class ImageUpdateListener extends ImageReadyListenerLight {

		private static final int TOP_AVATAR = 0;
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null/* || bitmap == null*/) {
				Log.e("TEST", "ImageLoader bitmap == null");
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();

					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}

}
