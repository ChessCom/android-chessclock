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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.entity.new_api.DailyGameByIdItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.CompGameSetupFragment;
import com.chess.ui.fragments.NewGamesFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameNetworkActivityFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.chess_boards.ChessBoardNetworkView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.13
 * Time: 13:45
 */
public class GameDailyFragment extends GameBaseFragment implements GameNetworkActivityFace, PopupListSelectionFace {

	public static final String DOUBLE_SPACE = "  ";
	private static final String DRAW_OFFER_TAG = "offer draw";
	private static final String ERROR_TAG = "send request failed popup";

	private static final int SEND_MOVE_UPDATE = 1;
	private static final int CREATE_CHALLENGE_UPDATE = 2;
	private static final int DRAW_OFFER_UPDATE = 3;
	private static final int ABORT_GAME_UPDATE = 4;
	private static final int CURRENT_GAME = 0;
	private static final int GAMES_LIST = 1;

	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_OFFER_DRAW = 1;
	private static final int ID_EMAIL_GAME = 2;
	private static final int ID_FLIP_BOARD = 3;
	private static final int ID_SETTINGS = 4;
	private static final String OPTION_SELECTION = "option select popup";

	private GameOnlineUpdatesListener abortGameUpdateListener;
	private GameOnlineUpdatesListener drawOfferedUpdateListener;

	private GameStateUpdateListener gameStateUpdateListener;
	private GameOnlineUpdatesListener sendMoveUpdateListener;
	private GameOnlineUpdatesListener createChallengeUpdateListener;

	private ChessBoardNetworkView boardView;

	//	private GameOnlineItem currentGame;
	private DailyGameByIdItem.Data currentGame;
	private long gameId;

//	private String timeRemains;

	private IntentFilter boardUpdateFilter;
	private BroadcastReceiver moveUpdateReceiver;

	protected boolean userPlayWhite = true;
	private LoadFromDbUpdateListener loadFromDbUpdateListener;
	private LoadFromDbUpdateListener currentGamesCursorUpdateListener;
	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsDailyView controlsDailyView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private BoardAvatarDrawable opponentAvatarDrawable;
	private BoardAvatarDrawable userAvatarDrawable;
	private LabelsConfig labelsConfig;
	private ArrayList<String> optionsList;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private ImageDownloaderToListener imageDownloader;

	public static GameDailyFragment newInstance(long gameId) {
		GameDailyFragment fragment = new GameDailyFragment();
		fragment.gameId = gameId;
		Bundle arguments = new Bundle();
		arguments.putLong(BaseGameItem.GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		labelsConfig = new LabelsConfig();
		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_daily, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.daily_chess);

		widgetsInit(view);
	}

	@Override
	public void onStart() {
		super.onStart();
		moveUpdateReceiver = new MoveUpdateReceiver();
		registerReceiver(moveUpdateReceiver, boardUpdateFilter);

		DataHolder.getInstance().setInOnlineGame(gameId, true);
//		loadGameAndUpdate();
		updateGameState(gameId);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (HONEYCOMB_PLUS_API) {
			dismissDialogs();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		unRegisterMyReceiver(moveUpdateReceiver);

		DataHolder.getInstance().setInOnlineGame(gameId, false);
	}

	@Override
	public void valueSelected(int code) {
		if (code == ID_NEW_GAME) {
			getActivityFace().openFragment(new CompGameSetupFragment());
		} else if (code == ID_OFFER_DRAW) {
			showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_EMAIL_GAME) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void dialogCanceled() {
		optionsSelectFragment = null;
	}

	private class MoveUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long gameId = intent.getLongExtra(BaseGameItem.GAME_ID, 0);

			updateGameState(gameId);
//			loadGameAndUpdate();
		}
	}

	private void loadGameAndUpdate() {
		// load game from DB. After load update
		new LoadDataFromDbTask(loadFromDbUpdateListener, DbHelper.getEchessGameParams(getActivity(), gameId),
				getContentResolver()).executeTask();
	}

	private class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		private int listenerCode;

		public LoadFromDbUpdateListener(int listenerCode) {
			super(getContext());
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (listenerCode) {
				case CURRENT_GAME:
					showSubmitButtonsLay(false);
					getSoundPlayer().playGameStart();

					currentGame = DBDataManager.getGameOnlineItemFromCursor(returnedObj);
					returnedObj.close();

					adjustBoardForGame();

					updateGameState(currentGame.getGameId());

					break;
				case GAMES_LIST:
					// iterate through all loaded items in cursor
					do {
						long localDbGameId = DBDataManager.getLong(returnedObj, DBConstants.V_ID);
						if (localDbGameId != gameId) {
							gameId = localDbGameId;
							showSubmitButtonsLay(false);
							boardView.setGameActivityFace(GameDailyFragment.this);

							getBoardFace().setAnalysis(false);

							updateGameState(gameId);
//							loadGameAndUpdate();
							return;
						}
					} while (returnedObj.moveToNext());

					getActivityFace().showPreviousFragment();

					break;
			}
		}
	}

	protected void updateGameState(long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_GAME_ID, gameId);

		new RequestJsonTask<DailyGameByIdItem>(gameStateUpdateListener).executeTask(loadItem);
	}

	private void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().toLowerCase().equals(AppData.getUserName(getActivity()));

		labelsConfig.topAvatar = opponentAvatarDrawable;
		labelsConfig.bottomAvatar = userAvatarDrawable;

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = currentGame.getBlackUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
		} else {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
			labelsConfig.topPlayerName = currentGame.getWhiteUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getWhiteRating());
			labelsConfig.bottomPlayerName = currentGame.getBlackUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getBlackRating());
		}

		DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

		controlsDailyView.enableGameControls(true);
		boardView.lockBoard(false);

		if (currentGame.hasNewMessage()) {
			controlsDailyView.haveNewMessage(true);
		}

		getBoardFace().setFinished(false);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

//		timeRemains = gameInfoItem.getTimeRemaining() + gameInfoItem.getTimeRemainingUnits();

		long timeRemains = currentGame.getSecondsRemain();

		String seconds = AppUtils.getTimeLeftFromSeconds(timeRemains, getActivity());


		if (isUserMove()) {
			topPanelView.setTimeLeft(seconds);
		} else {
			// TODO set greyed timeLeft
//			topPanelView.setTimeLeft(seconds);
		}

		ChessBoardOnline.resetInstance();
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
			String[] moves = currentGame.getMoveList()
					.replaceAll(AppConstants.MOVE_NUMBERS_PATTERN, StaticData.SYMBOL_EMPTY)
					.replaceAll(DOUBLE_SPACE, StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE);   // Start after "+" sign

			boardFace.setMovesCount(moves.length);
			for (int i = 0, cnt = boardFace.getMovesCount(); i < cnt; i++) {
				boardFace.updateMoves(moves[i], false);
			}
		} else {
			boardFace.setMovesCount(0);
		}

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		boardFace.setJustInitialized(false);
	}


	@Override
	public void invalidateGameScreen() {
		showSubmitButtonsLay(getBoardFace().isSubmit());

//		if (labelsConfig.bottomAvatar != null) {
//			labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
//			bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
//		}
//
//		if (labelsConfig.topAvatar != null) {
//			labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
//			topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
//		}

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);

		topPanelView.setPlayerName(labelsConfig.topPlayerName);
		topPanelView.setPlayerRating(labelsConfig.topPlayerRating);
		bottomPanelView.setPlayerName(labelsConfig.bottomPlayerName);
		bottomPanelView.setPlayerRating(labelsConfig.bottomPlayerRating);

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	protected void setBoardToFinishedState(){ // TODO implement state conditions logic for board
		super.setBoardToFinishedState();
		showSubmitButtonsLay(false);
	}

	@Override
	public String getWhitePlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getWhiteUsername();
	}

	@Override
	public String getBlackPlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
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

	private void sendMove() { // TODO check dot's update after move
		//save rating
//		currentPlayerRating = getCurrentPlayerRating();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameId));
		loadItem.setRequestMethod(RestHelper.PUT);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_SUBMIT);
		loadItem.addRequestParams(RestHelper.P_NEWMOVE, getBoardFace().convertMoveEchess());
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestamp());

		new RequestJsonTask<BaseResponseItem>(sendMoveUpdateListener).executeTask(loadItem);
	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);

		if(getBoardFace().isFinished()){
			showGameEndPopup(endGamePopupView, endGameMessage);
		} else {
			int action = AppData.getAfterMoveAction(getContext());
			if (action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
				backToHomeFragment();
			else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
				loadGamesList();
			}
		}
	}

	private void loadGamesList() {
		// replace with db update
//		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getDailyCurrentMyListGamesParams(getContext()), // TODO adjust
		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getDailyCurrentListGamesParams(getActivity()), // TODO adjust
				getContentResolver()).executeTask();

//		LoadItem listLoadItem = new LoadItem();
//		listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
//		listLoadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//		listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);
//
//		new GetStringObjTask(gamesListUpdateListener).executeTask(listLoadItem);
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.newInstance(gameId));
	}

	@Override
	public void switch2Chat() {
		if(currentGame == null)
			return;

		preferencesEditor.putString(AppConstants.OPPONENT, userPlayWhite
				? currentGame.getBlackUsername() : currentGame.getWhiteUsername());
		preferencesEditor.commit();

		currentGame.setHasNewMessage(false);
		controlsDailyView.haveNewMessage(false);

		getActivityFace().openFragment(new DailyChatFragment());
	}

	@Override
	public void playMove() {
		sendMove();
	}

	@Override
	public void cancelMove() {
		showSubmitButtonsLay(false);

		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();
		boardView.invalidate();	}

	@Override
	public void newGame() {
		loadGamesList();
	}

	@Override
	public Boolean isUserColorWhite() {
		if (currentGame != null && getActivity() != null)
			return currentGame.getWhiteUsername().toLowerCase().equals(AppData.getUserName(getActivity()));
		else
			return null;
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	private boolean isUserMove() {

		userPlayWhite = currentGame.getWhiteUsername().toLowerCase()
				.equals(AppData.getUserName(getActivity()));

		return (currentGame.isWhiteMove() && userPlayWhite)
				|| (!currentGame.isWhiteMove() && !userPlayWhite);
	}

	@Override
	public void showOptions(View view) {
		if (optionsSelectFragment != null) {
			return;
		}
		optionsSelectFragment = PopupOptionsMenuFragment.newInstance(this, optionsList);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION);
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		controlsDailyView.showSubmitButtons(show);
		if (!show) {
			getBoardFace().setSubmit(false);
		}
	}

	private void sendPGN() {
		CharSequence moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = currentGame.getWhiteUsername();
		String blackPlayerName = currentGame.getBlackUsername();
		String result = GAME_GOES;

		boolean finished = getBoardFace().isFinished();
		if(finished){// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
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

			String userName = AppData.getUserName(getActivity());
			boolean drawWasOffered = DBDataManager.checkIfDrawOffered(getContentResolver(), userName, gameId);


			if (drawWasOffered) { // If Draw was already offered by the opponent, we send accept to it.
				draw = RestHelper.V_ACCEPTDRAW;
			} else {
				draw = RestHelper.V_OFFERDRAW;
				// save at this point state to DB
				currentGame.setDrawOffered(1);
				String[] arguments = new String[]{String.valueOf(currentGame.isDrawOffered())};
				getContentResolver().update(DBConstants.uriArray[DBConstants.DAILY_ONLINE_GAMES],
						DBDataManager.putGameOnlineItemToValues(currentGame, userName),
						DBDataManager.SELECTION_USER_OFFERED_DRAW, arguments);
			}

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameId));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestamp());

			new RequestJsonTask<BaseResponseItem>(drawOfferedUpdateListener).executeTask(loadItem);
		} else if (tag.equals(ABORT_GAME_TAG)) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameId));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, currentGame.getTimestamp());

			new RequestJsonTask<BaseResponseItem>(abortGameUpdateListener).executeTask(loadItem);
		} else if(tag.equals(ERROR_TAG)){
			backToLoginFragment();
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		if(currentGame == null) {
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
//			sign = StaticData.SYMBOL_PLUS;
//		} else { // 800 700
//			ratingDiff = currentPlayerRating - currentPlayerNewRating;
//			sign = StaticData.SYMBOL_MINUS;
//		}

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
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
		boardView.setGameActivityFace(this);

		adjustBoardForGame();
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			dismissDialogs();
			getActivityFace().changeRightFragment(NewGamesFragment.newInstance(NewGamesFragment.RIGHT_MENU_MODE));
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
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, currentGame.getDaysPerMove());
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, currentGame.isRated()? 1 : 0);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, currentGame.getGameType());
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponent);

		new RequestJsonTask<BaseResponseItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class GameStateUpdateListener extends ChessUpdateListener<DailyGameByIdItem> {

		private GameStateUpdateListener() {
			super(DailyGameByIdItem.class);
		}

		@Override
		public void updateData(DailyGameByIdItem returnedObj) {
			super.updateData(returnedObj);

			currentGame = returnedObj.getData();

//			imageDownloader.download(currentGame.get, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
//			imageDownloader.download("https://s3.amazonaws.com/chess-7/images_users/avatars/rest_large.10.jpeg", new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
			imageDownloader.download("http://www.tutorialspoint.com/images/java-mini-logo.png", new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);


			DBDataManager.updateOnlineGame(getContentResolver(), currentGame, AppData.getUserName(getContext()));

			adjustBoardForGame();
		}
	}

	private class GameOnlineUpdatesListener extends ChessUpdateListener<BaseResponseItem> {
		private int listenerCode;

		private GameOnlineUpdatesListener(int listenerCode) {
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
					showSinglePopupDialog(R.string.congratulations, R.string.online_game_created);
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
		public void errorHandle(String resultMessage) {
			super.errorHandle(resultMessage);
			switch (listenerCode) {
				case CREATE_CHALLENGE_UPDATE:
					showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);

					break;
			}
		}
	}

	public void init() {
		gameId = getArguments().getLong(BaseGameItem.GAME_ID, 0);

		abortGameUpdateListener = new GameOnlineUpdatesListener(ABORT_GAME_UPDATE);
		drawOfferedUpdateListener = new GameOnlineUpdatesListener(DRAW_OFFER_UPDATE);

		gameStateUpdateListener = new GameStateUpdateListener();
		sendMoveUpdateListener = new GameOnlineUpdatesListener(SEND_MOVE_UPDATE);
		createChallengeUpdateListener = new GameOnlineUpdatesListener(CREATE_CHALLENGE_UPDATE);
		loadFromDbUpdateListener = new LoadFromDbUpdateListener(CURRENT_GAME);

		currentGamesCursorUpdateListener = new LoadFromDbUpdateListener(GAMES_LIST);

		imageDownloader = new ImageDownloaderToListener(getActivity());
	}

	private void widgetsInit(View view) {

		controlsDailyView = (ControlsDailyView) view.findViewById(R.id.controlsNetworkView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			labelsConfig.topAvatar = opponentAvatarDrawable;
			labelsConfig.bottomAvatar = userAvatarDrawable;
		}

		controlsDailyView.enableGameControls(false);

		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsDailyView);
		boardView.setNotationsView(notationsView);

		setBoardView(boardView);

		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);

		boardUpdateFilter = new IntentFilter(IntentConstants.BOARD_UPDATE);

		{// options list setup
			optionsList = new ArrayList<String>();
			optionsList.add( getString(R.string.new_game));
			optionsList.add( getString(R.string.email_game));
			optionsList.add(getString(R.string.settings));
		}
	}

	private class LabelsConfig {
		BoardAvatarDrawable topAvatar;
		BoardAvatarDrawable bottomAvatar;
		String topPlayerName;
		String bottomPlayerName;
		String topPlayerRating;
		String bottomPlayerRating;
		int userSide;

		int getOpponentSide(){
			return userSide == ChessBoard.WHITE_SIDE? ChessBoard.BLACK_SIDE: ChessBoard.WHITE_SIDE;
		}
	}


	private class ImageUpdateListener implements ImageReadyListener {

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

					String userAvatarUrl = AppData.getUserAvatar(activity);
					imageDownloader.download(userAvatarUrl, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);

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
