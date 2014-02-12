package com.chess.ui.fragments.daily;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.entity.api.YourTurnItem;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.*;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.RightPlayFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsDailyChessFragment;
import com.chess.ui.fragments.settings.SettingsFragmentTablet;
import com.chess.ui.fragments.settings.SettingsThemeFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameDailyFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;
import com.chess.widgets.ProfileImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.13
 * Time: 13:45
 */
public class GameDailyFragment extends GameBaseFragment implements GameDailyFace, PopupListSelectionFace {

	private static final String DRAW_OFFER_TAG = "offer draw";
	private static final String ERROR_TAG = "send request failed popup";
	private static final String END_VACATION_TAG = "end vacation popup";
	protected static final String FORCE_UPDATE = "force_update";

	private static final int SEND_MOVE_UPDATE = 1;
	private static final int CREATE_CHALLENGE_UPDATE = 2;
	private static final int DRAW_OFFER_UPDATE = 3;
	private static final int ABORT_GAME_UPDATE = 4;

	// Options ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_SKIP_GAME = 1;
	private static final int ID_OFFER_DRAW = 2;
	private static final int ID_ABORT_RESIGN = 3;
	private static final int ID_FLIP_BOARD = 4;
	private static final int ID_SHARE_PGN = 5;
	private static final int ID_SETTINGS = 6;
	private static final int ID_THEME = 7;

	private GameDailyUpdatesListener abortGameUpdateListener;
	private GameDailyUpdatesListener drawOfferedUpdateListener;

	private GameStateUpdateListener gameStateUpdateListener;
	private GameDailyUpdatesListener submitMoveUpdateListener;
	private GameDailyUpdatesListener createChallengeUpdateListener;

	private ArrayList<Long> loadedNextGameIds;
	private HashMap<Long, Boolean> viewedGamesMap;
	protected DailyCurrentGameData currentGame;

	private IntentFilter boardUpdateFilter;
	private IntentFilter newChatUpdateFilter;
	private BroadcastReceiver moveUpdateReceiver;
	private NewChatUpdateReceiver newChatUpdateReceiver;

	private ChessBoardDailyView boardView;
	private ControlsDailyView controlsView;
	private SparseArray<String> optionsMap;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private String[] countryNames;
	private int[] countryCodes;
	protected String username;
	private NotationFace notationsFace;
	private boolean forceUpdate;
	private int dailyRating;
	private boolean skipPreviousGames;

	public GameDailyFragment() {
	}

	public static GameDailyFragment createInstance(long gameId, String username) {
		GameDailyFragment fragment = new GameDailyFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}

	public static GameDailyFragment createInstance(long gameId, boolean forceUpdate) {
		GameDailyFragment fragment = new GameDailyFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putBoolean(FORCE_UPDATE, forceUpdate);
		fragment.setArguments(arguments);

		return fragment;
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
			username = getArguments().getString(USERNAME);
			forceUpdate = getArguments().getBoolean(FORCE_UPDATE);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
			username = savedInstanceState.getString(USERNAME);
			forceUpdate = savedInstanceState.getBoolean(FORCE_UPDATE);
		}
		if (TextUtils.isEmpty(username)) {
			username = getUsername();
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

		getActivityFace().setCustomActionBarViewId(R.layout.new_home_actionbar);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		moveUpdateReceiver = new MoveUpdateReceiver();
		newChatUpdateReceiver = new NewChatUpdateReceiver();
		registerReceiver(moveUpdateReceiver, boardUpdateFilter);
		registerReceiver(newChatUpdateReceiver, newChatUpdateFilter);

		DataHolder.getInstance().setInDailyGame(gameId, true);
		loadGameAndUpdate();
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterMyReceiver(moveUpdateReceiver);
		unRegisterMyReceiver(newChatUpdateReceiver);

		DataHolder.getInstance().setInDailyGame(gameId, false);
		if (HONEYCOMB_PLUS_API) {
			dismissEndGameDialog();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			getActivityFace().changeRightFragment(new DailyGameOptionsFragment());
			getActivityFace().toggleRightMenu();
		} else if (code == ID_SKIP_GAME) { // get next game where it's my turn
			loadNextMyTurnGame();
		} else if (code == ID_ABORT_RESIGN) {
			if (!username.equals(getUsername())) { // don't let unAuth users to make action
				showToast("=)");
				return;
			}
			if (getBoardFace().getPly() < 1 && isUserMove()) {
				showPopupDialog(R.string.abort_game_, ABORT_GAME_TAG);
			} else {
				showPopupDialog(R.string.resign_game_, ABORT_GAME_TAG);
			}
		} else if (code == ID_OFFER_DRAW) {
			if (!username.equals(getUsername())) { // don't let unAuth users to make action
				showToast("=)");
				return;
			}
			showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_SHARE_PGN) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(SettingsDailyChessFragment.createInstance(true));
		} else if (code == ID_THEME) {
			if (!isTablet) {
				getActivityFace().openFragment(new SettingsThemeFragment());
			} else {
				getActivityFace().openFragment(new SettingsFragmentTablet());
			}
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	private class MoveUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long gameId = intent.getLongExtra(BaseGameItem.GAME_ID, 0);

			updateGameState(gameId);
		}
	}

	private class NewChatUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			getControlsView().haveNewMessage(true);
		}
	}

	private void loadGameAndUpdate() {
		// load game from DB. After load update
		Cursor cursor = DbDataManager.query(getContentResolver(),
				DbHelper.getDailyGame(gameId, username));

		if (cursor.moveToFirst() && !forceUpdate) {
			showSubmitButtonsLay(false);

			currentGame = DbDataManager.getDailyCurrentGameFromCursor(cursor);
			cursor.close();

			adjustBoardForGame();

			// clear notification for this game
			List<YourTurnItem> moveNotifications = DbDataManager.getAllPlayMoveNotifications(getContentResolver(), username);
			if (moveNotifications.size() == 1) {
				if (moveNotifications.get(0).getGameId() == gameId) {
					((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE))
							.cancel(R.id.notification_id);
				}
			}
		} else {
			updateGameState(gameId);
		}

		// clear badge
		DbDataManager.deletePlayMoveNotification(getContentResolver(), username, gameId);
		updateNotificationBadges();
	}

	protected void updateGameState(long gameId) {
		LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameId);
		new RequestJsonTask<DailyCurrentGameItem>(gameStateUpdateListener).executeTask(loadItem);
	}

	private void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().equals(username);

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

		DataHolder.getInstance().setInDailyGame(currentGame.getGameId(), true);

		getControlsView().enableGameControls(true);
		boardView.lockBoard(false);

		getControlsView().haveNewMessage(currentGame.hasNewMessage());

		getBoardFace().setFinished(false);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		long secondsRemain = currentGame.getTimeRemaining();
		String timeRemains;
		if (secondsRemain == 0) {
			timeRemains = getString(R.string.less_than_60_sec);
		} else {
			timeRemains = AppUtils.getTimeLeftFromSeconds(secondsRemain, getActivity());
		}

		String defaultTime = AppUtils.getDaysString(currentGame.getDaysPerMove(), getActivity());
		boolean userMove = isUserMove();
		if (userMove) {
			labelsConfig.topPlayerTime = defaultTime;
			labelsConfig.bottomPlayerTime = timeRemains;
		} else {
			labelsConfig.topPlayerTime = timeRemains;
			labelsConfig.bottomPlayerTime = defaultTime;
		}

		if (currentGame.isOpponentOnVacation()) {
			labelsConfig.topPlayerTime = getString(R.string.vacation_on);
		}

		topPanelView.showTimeLeftIcon(!userMove);
		bottomPanelView.showTimeLeftIcon(userMove);

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == RestHelper.V_GAME_CHESS_960) {
			boardFace.setChess960(true);
		} else {
			boardFace.setChess960(false);
		}

		if (boardFace.isChess960()) {// we need to setup only position not made moves.
			// Daily games tournaments already include those moves in movesList
			boardFace.setupBoard(currentGame.getStartingFenPosition());
		}

		boardFace.setReside(!userPlayWhite);

		boardFace.checkAndParseMovesList(currentGame.getMoveList());

		boardView.resetValidMoves();

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null || getNotationsFace() == null) {
					return;
				}

				getNotationsFace().rewindForward();

			}
		}, NOTATION_REWIND_DELAY);

		getControlsView().showConditional(true);
		getControlsView().enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null || getNotationsFace() == null) {
					return;
				}
				getControlsView().enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);

		{ // set stubs while avatars are loading
			Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);

			labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);

			labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
			topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
			topPanelView.invalidate();

			labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), src);

			labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
			bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
			bottomPanelView.invalidate();
		}

		// load avatars for players
		imageDownloader.download(labelsConfig.topPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		imageDownloader.download(labelsConfig.bottomPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);

		controlsView.enableChatButton(username.equals(getUsername()));
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

		String playerCountry = labelsConfig.topPlayerCountry;
		labelsConfig.topPlayerCountry = labelsConfig.bottomPlayerCountry;
		labelsConfig.bottomPlayerCountry = playerCountry;
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
		return currentGame != null && getActivity() != null;
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
			submitMove();
		}
	}

	private void submitMove() {
		logTest(" last move = " +  getBoardFace().getLastMoveForDaily());
		if (username.equals(getUsername())) { // allow only authenticated user to send move in his own games
			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId, RestHelper.V_SUBMIT, currentGame.getTimestamp());
			loadItem.addRequestParams(RestHelper.P_NEW_MOVE, getBoardFace().getLastMoveForDaily());
			new RequestJsonTask<BaseResponseItem>(submitMoveUpdateListener).executeTask(loadItem);
		}
	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);

		// update DB
		currentGame.setMyTurn(false);
		currentGame.setFen(getBoardFace().generateFullFen());
		currentGame.setMoveList(getBoardFace().getMoveListSAN());
		DbDataManager.saveDailyGame(getContentResolver(), currentGame, username);

		// update right side fragment
		getActivity().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));

		if (getBoardFace().isFinished()) {
			View endGamePopupView;
			if (!isNeedToUpgrade()) {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game, null, false);
			} else {
				endGamePopupView = inflater.inflate(R.layout.popup_end_game_free, null, false);
			}
			showGameEndPopup(endGamePopupView, endGameTitle, endGameReason);
		} else {
			int action = getAppData().getAfterMoveAction();
			if (action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
				backToHomeFragment();
			else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
				loadNextMyTurnGame();
			}
		}

		updateNotificationBadges();
	}

	private void loadNextMyTurnGame() {
		if (loadedNextGameIds.size() == 0) {
			// if we didn't load yet, then load from DB

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getDailyCurrentMyListGames(username));
			if (cursor.moveToFirst()) {
				// iterate through all loaded items in cursor, but load after current game if it wasn't loaded
				do {
					long localDbGameId = DbDataManager.getLong(cursor, DbScheme.V_ID);
					loadedNextGameIds.add(localDbGameId);
				} while (cursor.moveToNext());
				cursor.close();
			}
		}

		if (!loadedNextGameIds.contains(gameId)) {
			skipPreviousGames = false;
		}

		for (Long nextGameId : loadedNextGameIds) {
			// iterate until we find current game
			if (nextGameId != gameId && skipPreviousGames) {
				continue;
			} else {
				skipPreviousGames = false;
			}

			if (nextGameId != gameId) {
				if (viewedGamesMap.containsKey(nextGameId)) {
					continue;
				}
				// mark this one as viewed
				viewedGamesMap.put(nextGameId, true);

				gameId = nextGameId;

				showSubmitButtonsLay(false);
				boardView.setGameFace(GameDailyFragment.this);

				getBoardFace().setAnalysis(false);

				loadGameAndUpdate();
				return;
			}
		}
		getActivityFace().showPreviousFragment();
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.createInstance(gameId, username, false));
	}

	@Override
	public void switch2Chat() {
		if (currentGame == null) {
			return;
		}

		// update game state in DB
		currentGame.setHasNewMessage(false);
		DbDataManager.saveDailyGame(getContentResolver(), currentGame, username);

		currentGame.setHasNewMessage(false);
		getControlsView().haveNewMessage(false);

		getActivityFace().openFragment(DailyChatFragment.createInstance(gameId, labelsConfig.topPlayerAvatar)); // TODO check when flip
	}

	@Override
	public void openConditions() {
		getActivityFace().openFragment(GameDailyConditionsFragment.createInstance(gameId, username, false));
	}

	@Override
	public void showConditionsBtn(boolean show) {
		getControlsView().showConditional(show);
	}

	@Override
	public void playMove() {
		submitMove();
	}

	@Override
	public void cancelMove() {
		showSubmitButtonsLay(false);

		boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
		boardView.resetValidMoves();

		getBoardFace().takeBack();
		getBoardFace().decreaseMovesCount();

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public void goHome() {
		// not used in daily
	}

	@Override
	public void newGame() {
		loadNextMyTurnGame();
	}

	@Override
	public boolean isUserColorWhite() {
		return labelsConfig.userSide == ChessBoard.WHITE_SIDE;
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	private boolean isUserMove() {
		userPlayWhite = currentGame.getWhiteUsername()
				.equals(username);

		return currentGame.isMyTurn();
	}

	@Override
	public boolean isUserAbleToMove(int color) {
		return super.isUserAbleToMove(color) && username.equals(getUsername());
	}

	@Override
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}

		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getDailyCurrentMyListGamesCnt(getUsername()));
		if (cursor != null && cursor.getCount() > 0) { // show skip button if there are more games, where is my turn
			optionsMap.put(ID_SKIP_GAME, getString(R.string.next_game));
		} else {
			optionsMap.remove(ID_SKIP_GAME);
		}

		if (getBoardFace().getPly() < 1 && isUserMove()) {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.abort));
			optionsMap.remove(ID_OFFER_DRAW);
		} else {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.resign));
			optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
		}

		// tournaments games are not abortable
		if (currentGame.isTournamentGame()) {
			optionsMap.put(ID_ABORT_RESIGN, getString(R.string.resign));
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsMap);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		getControlsView().showSubmitButtons(show);
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
			builder.append("\n [Termination \"").append(endGameReason).append("\"]");
		}
		builder.append("\n ").append(moves).append(Symbol.SPACE).append(result)
				.append("\n \n Sent from my Android");

		PgnItem pgnItem = new PgnItem(whitePlayerName, blackPlayerName);
		pgnItem.setStartDate(date);
		pgnItem.setPgn(builder.toString());

		sendPGN(pgnItem);
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
	protected void showGameEndPopup(View layout, String title, String reason) {
		if (currentGame == null) {
			throw new IllegalStateException("showGameEndPopup starts with currentGame = null");
		}

		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView resultRatingTxt = (TextView) layout.findViewById(R.id.resultRatingTxt);
		TextView ratingTitleTxt = (TextView) layout.findViewById(R.id.ratingTitleTxt);
		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(reason);

		String gameType = getString(R.string.standard);
		if (currentGame.getGameType() == RestHelper.V_GAME_CHESS_960) {
			gameType = getString(R.string.chess_960);
		}

		ratingTitleTxt.setText(getString(R.string.new_arg_rating_, gameType));
		resultRatingTxt.setText(String.valueOf(getCurrentPlayerRating()));

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		PopupGameEndFragment endPopupFragment = PopupGameEndFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);

		if (isNeedToUpgrade()) {
			initPopupAdWidget(layout);
			MopubHelper.showRectangleAd(getMopubRectangleAd(), getActivity());
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
		boardView.setGameFace(this);

		adjustBoardForGame();
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			dismissEndGameDialog();
			getActivityFace().changeRightFragment(RightPlayFragment.createInstance(RIGHT_MENU_MODE));
		} else if (view.getId() == R.id.sharePopupBtn) {
			GameDailyItem gameDailyItem = new GameDailyItem();
			gameDailyItem.setWhiteUsername(getWhitePlayerName());
			gameDailyItem.setBlackUsername(getBlackPlayerName());
			ShareItem shareItem = new ShareItem(gameDailyItem, gameId, ShareItem.DAILY);

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
		} else if (view.getId() == R.id.rematchPopupBtn) {
			sendRematch();
			dismissEndGameDialog();
		}
	}

	private void sendRematch() {
		String opponent;
		if (userPlayWhite) {
			opponent = currentGame.getBlackUsername();
		} else {
			opponent = currentGame.getWhiteUsername();
		}

		int minRating = dailyRating - LiveGameConfig.RATING_STEP;
		int maxRating = dailyRating + LiveGameConfig.RATING_STEP;
		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), currentGame.getDaysPerMove(),
				currentGame.isRated() ? 1 : 0, currentGame.getGameType(), opponent, minRating, maxRating);
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

			boolean userPlayWhite = currentGame.getWhiteUsername().equals(username);
			boolean whiteToMove = currentGame.getUserToMove() == RestHelper.P_WHITE;
			currentGame.setMyTurn(userPlayWhite ? whiteToMove : !whiteToMove);

			DbDataManager.updateDailyGame(getContentResolver(), currentGame, username);

			adjustBoardForGame();
		}
	}

	@Override
	protected void afterLogin() {
		super.afterLogin();

		// after we were forced to re-login we should re-load game
		loadGameAndUpdate();
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
					break;
				case CREATE_CHALLENGE_UPDATE:
					showSinglePopupDialog(R.string.challenge_created, R.string.you_will_notified_when_game_starts);
					break;
				case DRAW_OFFER_UPDATE:
					showToast(R.string.draw_offered);
					break;
				case ABORT_GAME_UPDATE:
					String title;
					String opponentName;
					if (isUserColorWhite()) {
						title = getString(R.string.black_wins);
						opponentName = getBlackPlayerName();
					} else {
						title = getString(R.string.white_wins);
						opponentName = getWhitePlayerName();
					}

					String reason = getString(R.string.won_by_resignation, opponentName);
					onGameOver(title, reason);
					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_ARE_ON_VACATAION) {
					showPopupDialog(R.string.leave_vacation_q, END_VACATION_TAG);
					return;
				} else if (serverCode == ServerErrorCodes.PLEASE_REFRESH_GAME) {
					updateGameState(gameId);
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId, RestHelper.V_SUBMIT, currentGame.getTimestamp());
			loadItem.addRequestParams(RestHelper.P_NEW_MOVE, getBoardFace().getLastMoveForDaily());
			new RequestJsonTask<BaseResponseItem>(submitMoveUpdateListener).executeTask(loadItem);
		}
	}

	protected ControlsDailyView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsDailyView) controlsView;
	}

	protected void setNotationsFace(View notationsView) {
		this.notationsFace = (NotationFace) notationsView;
	}

	protected NotationFace getNotationsFace() {
		return notationsFace;
	}


	public void init() {
		viewedGamesMap = new HashMap<Long, Boolean>();
		loadedNextGameIds = new ArrayList<Long>();
		skipPreviousGames = true;

		gameId = getArguments().getLong(BaseGameItem.GAME_ID, 0);

		boardUpdateFilter = new IntentFilter(IntentConstants.BOARD_UPDATE);
		newChatUpdateFilter = new IntentFilter(IntentConstants.NOTIFICATIONS_UPDATE);

		labelsConfig = new LabelsConfig();

		abortGameUpdateListener = new GameDailyUpdatesListener(ABORT_GAME_UPDATE);
		drawOfferedUpdateListener = new GameDailyUpdatesListener(DRAW_OFFER_UPDATE);

		gameStateUpdateListener = new GameStateUpdateListener();
		submitMoveUpdateListener = new GameDailyUpdatesListener(SEND_MOVE_UPDATE);
		createChallengeUpdateListener = new GameDailyUpdatesListener(CREATE_CHALLENGE_UPDATE);

		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);

		// get current user rating
		dailyRating = getAppData().getUserDailyRating();
		if (dailyRating == 0) {
			dailyRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal(), getUsername());
		}
	}

	protected void widgetsInit(View view) {
		setControlsView(view.findViewById(R.id.controlsView));
		if (inPortrait()) {
			setNotationsFace(view.findViewById(R.id.notationsView));
		} else {
			setNotationsFace(view.findViewById(R.id.notationsViewTablet));
		}

		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		getControlsView().enableGameControls(false);

		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(getControlsView());
		boardView.setNotationsFace(getNotationsFace());

		setBoardView(boardView);

		boardView.setGameFace(this);
		boardView.lockBoard(true);

		{// options list setup
			optionsMap = new SparseArray<String>();
			optionsMap.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsMap.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsMap.put(ID_SHARE_PGN, getString(R.string.share_pgn));
			optionsMap.put(ID_SETTINGS, getString(R.string.settings));
			optionsMap.put(ID_THEME, getString(R.string.theme));
		}
	}

}
