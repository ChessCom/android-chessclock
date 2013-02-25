package com.chess.ui.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.entity.new_api.DailyGameByIdItem;
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
import com.chess.ui.activities.PreferencesScreenActivity;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameNetworkActivityFace;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.views.*;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.13
 * Time: 13:45
 */
public class GameDailyFragment extends GameBaseFragment implements GameNetworkActivityFace {

	public static final String DOUBLE_SPACE = "  ";
	private static final String DRAW_OFFER_TAG = "offer draw";
	private static final String ERROR_TAG = "send request failed popup";

	private static final int SEND_MOVE_UPDATE = 1;
	private static final int CREATE_CHALLENGE_UPDATE = 2;
	private static final int DRAW_OFFER_UPDATE = 3;
	private static final int ABORT_GAME_UPDATE = 4;
	private static final int CURRENT_GAME = 0;
	private static final int GAMES_LIST = 1;

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private GameOnlineUpdatesListener abortGameUpdateListener;
	private GameOnlineUpdatesListener drawOfferedUpdateListener;

	private GameStateUpdateListener gameStateUpdateListener;
	//	private StartGameUpdateListener startGameUpdateListener;
//	private GetGameUpdateListener getGameUpdateListener;
	private GameOnlineUpdatesListener sendMoveUpdateListener;
	//	private GameOnlineUpdatesListener gamesListUpdateListener;
	private GameOnlineUpdatesListener createChallengeUpdateListener;

	private ChessBoardNetworkView boardView;

	//	private GameOnlineItem currentGame;
	private DailyGameByIdItem.Data currentGame;
	private long gameId;
	/**
	 * Use local Db instead, and pass game id through intent directly
	 */
//	@Deprecated
//	private GameListCurrentItem gameInfoItem;
//	private String timeRemains;

	private IntentFilter boardUpdateFilter;
	private BroadcastReceiver moveUpdateReceiver;

	protected boolean userPlayWhite = true;
	private LoadFromDbUpdateListener loadFromDbUpdateListener;
	private LoadFromDbUpdateListener currentGamesCursorUpdateListener;
	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsNetworkView controlsNetworkView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private BoardAvatarDrawable opponentAvatarDrawable;
	private BoardAvatarDrawable userAvatarDrawable;
	private LabelsConfig labelsConfig;
	private boolean chat;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_daily, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		widgetsInit(view);
	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);
		controlsNetworkView = (ControlsNetworkView) view.findViewById(R.id.controlsNetworkView);

		setTitle(R.string.daily_chess);

		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		{// set avatars
			Bitmap src = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();
			opponentAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);
			userAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			labelsConfig.topAvatar = opponentAvatarDrawable;
			labelsConfig.bottomAvatar = userAvatarDrawable;
		}

		controlsNetworkView.enableGameControls(false);

		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsNetworkView);
		boardView.setNotationsView(notationsView);

		setBoardView(boardView);

//		if (extras.getBoolean(AppConstants.NOTIFICATION, false)) { // TODO restore, replace with arguments
////			ChessBoardOnline.resetInstance();
//		}

//		boardView.setBoardFace(ChessBoardOnline.getInstance(this));
		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);

		boardUpdateFilter = new IntentFilter(IntentConstants.BOARD_UPDATE);
	}

	@Override
	public void onStart() {
		init();
		super.onStart();
		moveUpdateReceiver = new MoveUpdateReceiver();
		registerReceiver(moveUpdateReceiver, boardUpdateFilter);

		DataHolder.getInstance().setInOnlineGame(gameId, true);
		loadGameAndUpdate();
	}

	public void init() {
		gameId = getArguments().getLong(BaseGameItem.GAME_ID, 0);

		menuOptionsDialogListener = new MenuOptionsDialogListener();
		abortGameUpdateListener = new GameOnlineUpdatesListener(ABORT_GAME_UPDATE);
		drawOfferedUpdateListener = new GameOnlineUpdatesListener(DRAW_OFFER_UPDATE);

		gameStateUpdateListener = new GameStateUpdateListener();
//		startGameUpdateListener = new StartGameUpdateListener();
//		getGameUpdateListener = new GetGameUpdateListener();
		sendMoveUpdateListener = new GameOnlineUpdatesListener(SEND_MOVE_UPDATE);
//		gamesListUpdateListener = new GameOnlineUpdatesListener(NEXT_GAME_UPDATE);
		createChallengeUpdateListener = new GameOnlineUpdatesListener(CREATE_CHALLENGE_UPDATE);
		loadFromDbUpdateListener = new LoadFromDbUpdateListener(CURRENT_GAME);

		currentGamesCursorUpdateListener = new LoadFromDbUpdateListener(GAMES_LIST);
//		showActionRefresh = true;  // TODO restore
	}

/*
	@Override
	protected void onNewIntent(Intent intent) {   // TODO restore, recheck logic
		super.onNewIntent(intent);

		if (intent.getExtras() != null) {
			Long gameIdReceived = intent.getLongExtra(BaseGameItem.GAME_ID, 0);

			if (gameIdReceived != null){
				gameId = gameIdReceived;

	//			ChessBoardOnline.resetInstance();

				showSubmitButtonsLay(false);
	//			boardView.setBoardFace(getBoardFace());
				boardView.setGameActivityFace(GameOnlineScreenActivity.this);

				getBoardFace().setAnalysis(false);

				loadGameAndUpdate();
			}
		}
	}
*/

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

					userPlayWhite = currentGame.getWhiteUsername().toLowerCase().equals(AppData.getUserName(getActivity()));

					labelsConfig.topAvatar = opponentAvatarDrawable;
					labelsConfig.bottomAvatar = userAvatarDrawable;

					if (userPlayWhite) {
						labelsConfig.userSide = AppConstants.WHITE_SIDE;
						labelsConfig.topPlayerLabel = getBlackPlayerName();
						labelsConfig.bottomPlayerLabel = getWhitePlayerName();
					} else {
						labelsConfig.userSide = AppConstants.BLACK_SIDE;
						labelsConfig.topPlayerLabel = getWhitePlayerName();
						labelsConfig.bottomPlayerLabel = getBlackPlayerName();
					}

					DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

					controlsNetworkView.enableGameControls(true);
					boardView.lockBoard(false);

					checkMessages();

					adjustBoardForGame();

					getBoardFace().setJustInitialized(false);
					updateGameState(currentGame.getGameId());

					break;
				case GAMES_LIST:
					// iterate through all loaded items in cursor
					do {
						long localDbGameId = DBDataManager.getLong(returnedObj, DBConstants.V_GAME_ID);
						if (localDbGameId != gameId) {
							gameId = localDbGameId;
							showSubmitButtonsLay(false);
							boardView.setGameActivityFace(GameDailyFragment.this);

							getBoardFace().setAnalysis(false);

//							gameInfoItem.setGameId(gameId);

							loadGameAndUpdate();
							// same new gameId
							// TODO restore, if needed
//							Intent intent = getIntent();              // TODO remove gameInfoItem
//							intent.putExtra(BaseGameItem.GAME_ID, gameId);
//							getIntent().replaceExtras(intent);
							return;
						}
					} while (returnedObj.moveToNext());

//					finish();
					backToHomeFragment(); // TODO restore, recheck logic

					break;
			}

		}
	}

	protected void updateGameState(long gameId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAME_BY_ID(gameId));
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

		new RequestJsonTask<DailyGameByIdItem>(gameStateUpdateListener).executeTask(loadItem);
	}

	//	private class GamesListUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			switchToNextGame(returnedObj);
//		}
//	}

//	private void switchToNextGame(String returnedObj){
//		ArrayList<GameListCurrentItem> currentGames = new ArrayList<GameListCurrentItem>();
//
//		for (GameListCurrentItem gameListItem : ChessComApiParser.getCurrentOnlineGames(returnedObj)) {
//			if (gameListItem.isMyTurn()) {
//				currentGames.add(gameListItem);
//			}
//		}
//
//		for (GameListCurrentItem currentGame : currentGames) {
//			if (currentGame.getGameId() != gameId) {
//				gameId = currentGame.getGameId();
//				showSubmitButtonsLay(false);
////				boardView.setBoardFace(ChessBoardOnline.getInstance(GameOnlineScreenActivity.this));
//				boardView.setGameActivityFace(GameOnlineScreenActivity.this);
//
//				getBoardFace().setAnalysis(false);
//
//				gameInfoItem.setGameId(gameId);
//
////				updateGameState(gameId); // if next game
//				loadGameAndUpdate();
//				// same new gameId
//				Intent intent = getIntent();
//				intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameInfoItem);
//				getIntent().replaceExtras(intent);
//				return;
//			}
//		}
//		finish();
//	}

//	private class StartGameUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			showSubmitButtonsLay(false);
//			getSoundPlayer().playGameStart();
//
//			currentGame = ChessComApiParser.getGameParseV3(returnedObj);
//
//			DBDataManager.updateOnlineGame(getContentResolver(), currentGame, AppData.getUserName(getContext()));
//
//			DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);
//
//			controlsNetworkView.enableGameControls(true);
//			boardView.lockBoard(false);
//
//			checkMessages();
//
//			adjustBoardForGame();
//		}
//	}

	private void adjustBoardForGame() {
//		boardView.setFinished(false);
		getBoardFace().setFinished(false);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

//		timeRemains = gameInfoItem.getTimeRemaining() + gameInfoItem.getTimeRemainingUnits();

		long timeRemains = currentGame.getSecondsRemain();

		String seconds = AppUtils.getTimeLeftFromSeconds(timeRemains, getActivity());


		if (isUserMove()) {

//			infoLabelTxt.setText(timeRemains); // TODO restore
			topPanelView.setPlayerTimeLeft(seconds);
			updatePlayerDots(userPlayWhite);
		} else {
			updatePlayerDots(!userPlayWhite);
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

//	private class GameStateUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			currentGame = ChessComApiParser.getGameParseV3(returnedObj);
//
//			DBDataManager.updateOnlineGame(getContentResolver(), currentGame, AppData.getUserName(getContext()));
//
//			controlsNetworkView.enableGameControls(true);
//			boardView.lockBoard(false);
//
//			if (getBoardFace().isAnalysis()) {
//				boardView.enableAnalysis();
//				return;
//			}
//
//			onGameRefresh();
//			checkMessages();
//		}
//	}

	public void onGameRefresh() {

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

//		timeRemains = gameInfoItem.getTimeRemaining() + gameInfoItem.getTimeRemainingUnits();

		if (isUserMove()) {

//			infoLabelTxt.setText(timeRemains); // TODO restore
			updatePlayerDots(userPlayWhite);
		} else {
			updatePlayerDots(!userPlayWhite);
		}

		if (currentGame.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			String[] moves = currentGame.getMoveList()
					.replaceAll(AppConstants.MOVE_NUMBERS_PATTERN, StaticData.SYMBOL_EMPTY)
					.replaceAll(DOUBLE_SPACE, StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE);    // Start after "+" sign

			if (moves.length - getBoardFace().getMovesCount() == 1) {
				getBoardFace().updateMoves(moves[moves.length - 1], false);

				getBoardFace().setMovesCount(moves.length);
				boardView.invalidate();
			}
			invalidateGameScreen();
		}
	}

	public void invalidateGameScreen() {
		showSubmitButtonsLay(getBoardFace().isSubmit());

		userAvatarDrawable.setSide(labelsConfig.userSide);
		opponentAvatarDrawable.setSide(labelsConfig.getOpponentSide());

		topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
		bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);

		topPanelView.setPlayerLabel(labelsConfig.topPlayerLabel);
		bottomPanelView.setPlayerLabel(labelsConfig.bottomPlayerLabel);

//		whitePlayerLabel.setText(getWhitePlayerName());
//		blackPlayerLabel.setText(getBlackPlayerName());

//		boardView.updateNotations(getBoardFace().getMoveListSAN());
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
		showSubmitButtonsLay(false);

		if (currentGame == null) { // TODO fix inappropriate state, current game can't be null here // if we don't have Game entity
			// get game entity
			throw new IllegalStateException("Current game became NULL");
//			updateGameState(gameId);
//			LoadItem loadItem = new LoadItem();
//			loadItem.setLoadPath(RestHelper.GET_GAME_V5);
//			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//			loadItem.addRequestParams(RestHelper.P_GID, gameId);
//
//			new GetStringObjTask(getGameUpdateListener).executeTask(loadItem);
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


//	private class GetGameUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			currentGame = ChessComApiParser.getGameParseV3(returnedObj);
//
////			DBDataManager.updateOnlineGame(getContext(), currentGame);
//
//			controlsNetworkView.enableGameControls(true);
//			boardView.lockBoard(false);
//
//			sendMove();
//		}
//	}

//	private class SendMoveUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void showProgress(boolean show) {
//			super.showProgress(show);
//			if (isPaused)
//				return;
//
//			if (show) {
//				showPopupHardProgressDialog(R.string.sendinggameinfo);
//			} else
//				dismissProgressDialog();
//		}
//
//		@Override
//		public void updateData(String returnedObj) {
//			moveWasSent();
//
//			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//			mNotificationManager.cancel((int) gameId);
//			mNotificationManager.cancel(R.id.notification_message);
//		}
//	}

	private void moveWasSent() {
		showSubmitButtonsLay(false);
//		if(boardView.isFinished()){
		if(getBoardFace().isFinished()){
			showGameEndPopup(endGamePopupView, endGameMessage);
		} else {
			int action = AppData.getAfterMoveAction(getContext());
			if (action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
				backToHomeFragment();
//				finish();
			else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
				loadGamesList();
			}
		}
	}

	private void loadGamesList() {
		// replace with db update
		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getDailyCurrentMyListGamesParams(getContext()), // TODO adjust
				getContentResolver()).executeTask();

//		LoadItem listLoadItem = new LoadItem();
//		listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
//		listLoadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//		listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);
//
//		new GetStringObjTask(gamesListUpdateListener).executeTask(listLoadItem);
	}

	private void openChatActivity() {
		if(currentGame == null)
			return;

		preferencesEditor.putString(AppConstants.OPPONENT, userPlayWhite
				? currentGame.getBlackUsername() : currentGame.getWhiteUsername());
		preferencesEditor.commit();

		currentGame.setHasNewMessage(false);
		controlsNetworkView.haveNewMessage(false);

		// TODO restore, open ChatFragment

//		Intent intent = new Intent(this, ChatOnlineActivity.class);
//		intent.putExtra(BaseGameItem.GAME_ID, gameId);
//		startActivity(intent);

		chat = false;
	}


	private void checkMessages() {
		if (currentGame.hasNewMessage()) {
			controlsNetworkView.haveNewMessage(true);
		}
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.newInstance(gameId));
//		super.switch2Analysis(isAnalysis);
	}

	@Override
	public void switch2Chat() {
		openChatActivity();
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

	public Boolean isUserColorWhite() {
		if (currentGame != null)
			return currentGame.getWhiteUsername().toLowerCase().equals(AppData.getUserName(getActivity()));
		else
			return null;
	}

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
	public void showOptions() {
/*
		Offer draw should be able only after the first move was made.
		Also Abort should change to Resign after that.
*/
		userPlayWhite = currentGame.getWhiteUsername().toLowerCase()
				.equals(AppData.getUserName(getActivity()));

		boolean userMove =  isUserMove();

		if (getBoardFace().getHply() < 1 && userMove) {
			menuOptionsItems = new CharSequence[]{
					getString(R.string.settings),
					getString(R.string.messages),
					getString(R.string.email_game),
					getString(R.string.reside),
					getString(R.string.abort)};
		} else {
			menuOptionsItems = new CharSequence[]{
					getString(R.string.settings),
					getString(R.string.messages),
					getString(R.string.email_game),
					getString(R.string.reside),
					getString(R.string.offer_draw),
					getString(R.string.resign)};
		}

		new AlertDialog.Builder(getActivity()) // TODO change with FragmentDialog
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		controlsNetworkView.showSubmitButtons(show);
		if (!show) {
			getBoardFace().setSubmit(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.game_echess, menu); // TODO restore, recheck
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
//				loadGameAndUpdate();
				updateGameState(gameId);
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
					showPopupDialog(R.string.offer_draw, R.string.are_you_sure_q, DRAW_OFFER_RECEIVED_TAG);
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
//		boolean finished = boardView.isFinished();
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
				getContentResolver().update(DBConstants.uriArray[DBConstants.ECHESS_ONLINE_GAMES],
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
//			backToLoginActivity();
			backToLoginFragment();
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
	public void onPrepareOptionsMenu(Menu menu) { // TODO restore, recheck
		if (currentGame != null) {
			changeChatIcon(menu);
		}
		super.onPrepareOptionsMenu(menu);
	}

	//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		if (currentGame != null) {
//			changeChatIcon(menu);
//		}
//		return super.onPrepareOptionsMenu(menu);
//	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		if(currentGame == null) {
			throw new IllegalStateException("showGameEndPopup starts with currentGame = null");
//			return;
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
		layout.findViewById(R.id.homePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.reviewPopupBtn).setOnClickListener(this);
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
		getBoardFace().setJustInitialized(false);
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.submitBtn) {
			if(currentGame == null) { // TODO remove or restore after debug
				throw new IllegalStateException("onClick Submit Button got currentGame = NULL");
//				return;
			}

			sendMove();
		} else if (view.getId() == R.id.newGamePopupBtn) {
			dismissDialogs();
			getActivityFace().changeRightFragment(NewGamesFragment.newInstance(NewGamesFragment.RIGHT_MENU_MODE));

//			Intent intent = new Intent(this, OnlineNewGameActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
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

			DBDataManager.updateOnlineGame(getContentResolver(), currentGame, AppData.getUserName(getContext()));

			controlsNetworkView.enableGameControls(true);
			boardView.lockBoard(false);

			if (getBoardFace().isAnalysis()) {  // TODO recheck logic
//				boardView.enableAnalysis();
				return;
			}

			if (getBoardFace().isJustInitialized()){
				adjustBoardForGame();
			} else {
				onGameRefresh();
			}
			checkMessages();
		}
	}

	private class GameOnlineUpdatesListener extends ChessUpdateListener<BaseResponseItem> {
		private int listenerCode;

		private GameOnlineUpdatesListener(int listenerCode) {
			super(BaseResponseItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			if (isPaused)
				return;

			if (listenerCode == SEND_MOVE_UPDATE){
				if (show) {
					showPopupHardProgressDialog(R.string.sending_game_info);
				} else {
					dismissProgressDialog();
				}
			}
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

//	private class CreateChallengeUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
//		}
//
//		@Override
//		public void errorHandle(String resultMessage) {
//			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
//		}
//	}

	private class LabelsConfig {
		int topPlayerSide;
		int bottomPlayerSide;
		String topPlayerLabel;
		String bottomPlayerLabel;
		Drawable topAvatar;
		Drawable bottomAvatar;
		int userSide;

		int getOpponentSide(){
			return userSide == AppConstants.WHITE_SIDE? AppConstants.BLACK_SIDE: AppConstants.WHITE_SIDE;
		}
	}


}
