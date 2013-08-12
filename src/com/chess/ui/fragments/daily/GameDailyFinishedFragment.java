package com.chess.ui.fragments.daily;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.model.DataHolder;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbScheme;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
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
 * Date: 08.05.13
 * Time: 18:52
 */
public class GameDailyFinishedFragment extends GameBaseFragment implements GameNetworkFace, PopupListSelectionFace {

	public static final String DOUBLE_SPACE = "  ";
	private static final String ERROR_TAG = "send request failed popup";

	private static final int SEND_MOVE_UPDATE = 1;
	private static final int CREATE_CHALLENGE_UPDATE = 2;
	private static final int CURRENT_GAME = 0;
	private static final int GAMES_LIST = 1;

	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_FLIP_BOARD = 1;
	private static final int ID_EMAIL_GAME = 2;
	private static final int ID_SETTINGS = 3;

	private GameStateUpdateListener gameStateUpdateListener;
	private GameOnlineUpdatesListener sendMoveUpdateListener;
	private GameOnlineUpdatesListener createChallengeUpdateListener;

	private ChessBoardNetworkView boardView;

	//	private GameOnlineItem currentGame;
	private DailyFinishedGameData currentGame;
	private long gameId;

	protected boolean userPlayWhite = true;
	private LoadFromDbUpdateListener loadFromDbUpdateListener;
	private LoadFromDbUpdateListener currentGamesCursorUpdateListener;
	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsDailyView controlsDailyView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private LabelsConfig labelsConfig;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private ImageDownloaderToListener imageDownloader;
	private String[] countryNames;
	private int[] countryCodes;

	public GameDailyFinishedFragment() {

	}

	public static GameDailyFinishedFragment createInstance(long gameId) {
		GameDailyFinishedFragment fragment = new GameDailyFinishedFragment();
		fragment.gameId = gameId;
		Bundle arguments = new Bundle();
		arguments.putLong(BaseGameItem.GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		DataHolder.getInstance().setInOnlineGame(gameId, true);
		loadGameAndUpdate();
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

		DataHolder.getInstance().setInOnlineGame(gameId, false);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			getActivityFace().openFragment(new DailyGameSetupFragment());
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

	private void loadGameAndUpdate() {
		// load game from DB. After load update
//		new LoadDataFromDbTask(loadFromDbUpdateListener, DbHelper.getDailyFinishedGame(getActivity(), gameId),
//				getContentResolver()).executeTask();

		Cursor cursor = DbDataManager.executeQuery(getContentResolver(),
				DbHelper.getDailyFinishedGame(gameId, getUsername()));

		if (cursor.moveToFirst()) {
			showSubmitButtonsLay(false);
			getSoundPlayer().playGameStart();

			currentGame = DbDataManager.getDailyFinishedGameFromCursor(cursor);
			cursor.close();

			adjustBoardForGame();
		}
	}

	private class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		private int listenerCode;

		public LoadFromDbUpdateListener(int listenerCode) {
			super(getContext(), GameDailyFinishedFragment.this);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (listenerCode) {
				case CURRENT_GAME:
					showSubmitButtonsLay(false);
					getSoundPlayer().playGameStart();

					currentGame = DbDataManager.getDailyFinishedGameFromCursor(returnedObj);
					returnedObj.close();

					adjustBoardForGame();

//					updateGameState(currentGame.getGameId());

					break;
				case GAMES_LIST:
					// iterate through all loaded items in cursor
					do {
						long localDbGameId = DbDataManager.getLong(returnedObj, DbScheme.V_ID);
						if (localDbGameId != gameId) {
							gameId = localDbGameId;
							showSubmitButtonsLay(false);
							boardView.setGameFace(GameDailyFinishedFragment.this);

							getBoardFace().setAnalysis(false);

//							updateGameState(gameId);
							loadGameAndUpdate();
							return;
						}
					} while (returnedObj.moveToNext());

					getActivityFace().showPreviousFragment();

					break;
			}
		}
	}

//	protected void updateGameState(long gameId) {
//		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.CMD_GAMES);
//		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken(getContext()));
//		loadItem.addRequestParams(RestHelper.P_GAME_ID, gameId);
//
//		new RequestJsonTask<DailyFinishedGameData>(gameStateUpdateListener).executeTask(loadItem);
//	}

	private void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().equals(getAppData().getUsername());

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = currentGame.getBlackUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
			labelsConfig.topPlayerAvatar = currentGame.getBlackAvatar();
			labelsConfig.bottomPlayerAvatar = currentGame.getWhiteAvatar();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
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


//		if (isUserMove()) {
//			topPanelView.setTimeRemain(seconds);
//		} else {
//			// TODO set greyed timeLeft
////			topPanelView.setTimeRemain(seconds);
//		}

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		boardFace.setupBoard(currentGame.getStartingFenPosition());
		if (!userPlayWhite) {
			boardFace.setReside(true);
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

		boardView.resetValidMoves();

		invalidateGameScreen();

		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		boardFace.setJustInitialized(false);

		imageDownloader.download(labelsConfig.topPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		Log.d("EnhancedImageDownloader", "download(labelsConfig.topPlayerAvatar");
		imageDownloader.download(labelsConfig.bottomPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);
		Log.d("EnhancedImageDownloader", "download(labelsConfig.bottomPlayerAvatar");
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

		LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId,  RestHelper.V_SUBMIT, currentGame.getTimestamp());
		loadItem.addRequestParams(RestHelper.P_NEWMOVE, getBoardFace().convertMoveEchess());
		new RequestJsonTask<BaseResponseItem>(sendMoveUpdateListener).executeTask(loadItem);
	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);

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
		// replace with db update
//		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getDailyCurrentMyListGames(getContext()), // TODO adjust
		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getDailyCurrentListGames(getUsername()), // TODO adjust
				getContentResolver()).executeTask();

	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.createInstance(gameId));
	}

	@Override
	public void switch2Chat() {
		if (currentGame == null)
			return;

		preferencesEditor.putString(AppConstants.OPPONENT, userPlayWhite
				? currentGame.getBlackUsername() : currentGame.getWhiteUsername());
		preferencesEditor.commit();

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

//	private boolean isUserMove() {
//		userPlayWhite = currentGame.getWhiteUsername().equals(getAppData().getUsername(getActivity()));
//
//		return (currentGame.isWhiteMove() && userPlayWhite)
//				|| (!currentGame.isWhiteMove() && !userPlayWhite);
//	}

	@Override
	public void showOptions(View view) {
		if (optionsSelectFragment != null) {
			return;
		}
		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
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
		CharSequence moves = getBoardFace().getMoveListSAN();
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

		if (tag.equals(ERROR_TAG)) {
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
//			sign = StaticData.SYMBOL_PLUS;
//		} else { // 800 700
//			ratingDiff = currentPlayerRating - currentPlayerNewRating;
//			sign = StaticData.SYMBOL_MINUS;
//		}

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

//		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
//		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
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
		int color; // reversed color
		if (userPlayWhite) {
			opponent = currentGame.getBlackUsername();
			color = 2;
		} else {
			opponent = currentGame.getWhiteUsername();
			color = 1;
		}

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), currentGame.getDaysPerMove(), color,
				currentGame.isRated() ? 1 : 0,  currentGame.getGameType(), opponent);
		new RequestJsonTask<BaseResponseItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class GameStateUpdateListener extends ChessLoadUpdateListener<DailyFinishedGameData> {

		private GameStateUpdateListener() {
			super(DailyFinishedGameData.class);
		}

		@Override
		public void updateData(DailyFinishedGameData returnedObj) {
			super.updateData(returnedObj);

			currentGame = returnedObj;

			adjustBoardForGame();
		}
	}

	private class GameOnlineUpdatesListener extends ChessLoadUpdateListener<BaseResponseItem> {
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
					showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
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
		labelsConfig = new LabelsConfig();

		gameStateUpdateListener = new GameStateUpdateListener();
		sendMoveUpdateListener = new GameOnlineUpdatesListener(SEND_MOVE_UPDATE);
		createChallengeUpdateListener = new GameOnlineUpdatesListener(CREATE_CHALLENGE_UPDATE);
		loadFromDbUpdateListener = new LoadFromDbUpdateListener(CURRENT_GAME);

		currentGamesCursorUpdateListener = new LoadFromDbUpdateListener(GAMES_LIST);

		imageDownloader = new ImageDownloaderToListener(getActivity());

		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
	}

	private void widgetsInit(View view) {

		controlsDailyView = (ControlsDailyView) view.findViewById(R.id.controlsNetworkView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
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
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsArray.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsArray.put(ID_EMAIL_GAME, getString(R.string.email_game));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

//	private class LabelsConfig {
//		BoardAvatarDrawable topAvatar;
//		BoardAvatarDrawable bottomAvatar;
//		String topPlayerName;
//		String bottomPlayerName;
//		String topPlayerRating;
//		String bottomPlayerRating;
//		String topPlayerAvatar;
//		String bottomPlayerAvatar;
//		String topPlayerCountry;
//		String bottomPlayerCountry;
//		int userSide;
//
//		int getOpponentSide() {
//			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
//		}
//	}

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
					logTest("top avatar loaded");
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();

					break;
				case BOTTOM_AVATAR:
					logTest("bottom avatar loaded");
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}

}
