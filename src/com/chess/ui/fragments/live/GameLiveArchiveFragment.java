package com.chess.ui.fragments.live;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.LiveArchiveGameData;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.DataHolder;
import com.chess.model.PgnItem;
import com.chess.model.PopupItem;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.daily.DailyChatFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsLiveChessFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.chess_boards.ChessBoardNetworkView;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.utilities.AppUtils;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.09.13
 * Time: 8:27
 */
public class GameLiveArchiveFragment  extends GameBaseFragment implements GameNetworkFace, PopupListSelectionFace {

	private static final String ERROR_TAG = "send request failed popup";

	private static final int CURRENT_GAME = 0;
	private static final int GAMES_LIST = 1;

	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_FLIP_BOARD = 1;
	private static final int ID_SHARE_PGN = 2;
	private static final int ID_SETTINGS = 3;

	private ChessBoardNetworkView boardView;

	private LiveArchiveGameData currentGame;

	protected boolean userPlayWhite = true;
	private LoadFromDbUpdateListener currentGamesCursorUpdateListener;
	private ControlsDailyView controlsView;
	private LabelsConfig labelsConfig;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private ImageDownloaderToListener imageDownloader;
	private String[] countryNames;
	private int[] countryCodes;
	private NotationFace notationsFace;

	public GameLiveArchiveFragment() { }

	public static GameLiveArchiveFragment createInstance(long gameId) {
		GameLiveArchiveFragment fragment = new GameLiveArchiveFragment();
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

		loadGameAndUpdate();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (HONEYCOMB_PLUS_API) {
			dismissEndGameDialog();
		}
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			getActivityFace().changeRightFragment(new LiveGameOptionsFragment());
			getActivityFace().toggleRightMenu();
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_SHARE_PGN) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(SettingsLiveChessFragment.createInstance(true));
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private void loadGameAndUpdate() {
		Cursor cursor = DbDataManager.query(getContentResolver(),
				DbHelper.getLiveArchiveGame(gameId, getUsername()));

		if (cursor.moveToFirst()) {
			showSubmitButtonsLay(false);

			currentGame = DbDataManager.getLiveArchiveGameFromCursor(cursor);
			cursor.close();

			adjustBoardForGame();
		}
	}

	private class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		private int listenerCode;

		public LoadFromDbUpdateListener(int listenerCode) {
			super(getContext(), GameLiveArchiveFragment.this);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (listenerCode) {
				case CURRENT_GAME:
					showSubmitButtonsLay(false);

					currentGame = DbDataManager.getLiveArchiveGameFromCursor(returnedObj);
					returnedObj.close();

					adjustBoardForGame();

					break;
				case GAMES_LIST:
					// iterate through all loaded items in cursor
					do {
						long localDbGameId = DbDataManager.getLong(returnedObj, DbScheme.V_ID);
						if (localDbGameId != gameId) {
							gameId = localDbGameId;
							showSubmitButtonsLay(false);
							boardView.setGameFace(GameLiveArchiveFragment.this);

							getBoardFace().setAnalysis(false);

							loadGameAndUpdate();
							return;
						}
					} while (returnedObj.moveToNext());

					getActivityFace().showPreviousFragment();

					break;
			}
		}
	}

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

		DataHolder.getInstance().setInDailyGame(currentGame.getGameId(), true);

		getControlsView().enableGameControls(true);
		boardView.lockBoard(false);

		if (currentGame.hasNewMessage()) {
			getControlsView().haveNewMessage(true);
		}

		getBoardFace().setFinished(false);

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
//		boardFace.setChess960(currentGame.getGameType() != RestHelper.V_GAME_CHESS); / not used in live
//
//		if (boardFace.isChess960()) {// we need to setup only position not made moves.
//			// Daily games tournaments already include those moves in movesList
//			boardFace.setupBoard(currentGame.getStartingFenPosition());
//		}

		boardFace.setReside(!userPlayWhite);

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

		boardView.updateNotations(getBoardFace().getNotationArray());

		getControlsView().enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				getControlsView().enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);
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
	}


	private void loadGamesList() {
		new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getLiveArchiveListGames(getUsername()),
				getContentResolver()).executeTask();
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameLiveArchiveAnalysisFragment.createInstance(gameId));
	}

	@Override
	public void switch2Chat() {
		if (currentGame == null) {
			return;
		}

		currentGame.setHasNewMessage(false);
		getControlsView().haveNewMessage(false);

		getActivityFace().openFragment(DailyChatFragment.createInstance(gameId, labelsConfig.topPlayerAvatar)); // TODO check when flip
	}

	@Override
	public void playMove() {

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
		// not used here
	}

	@Override
	public void newGame() {
		loadGamesList();
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	@Override
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}
		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
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
		builder.append("\n ").append(moves)
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

		if (tag.equals(ERROR_TAG)) {
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

		String currentPlayerNewRating = Symbol.SPACE +  getCurrentPlayerRating();

		ratingTitleTxt.setText(getString(R.string.new_) + Symbol.SPACE + getString(R.string.rating_));
		String rating = getString(R.string.rating_, currentPlayerNewRating);
		resultRatingTxt.setText(rating);

//		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
//		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

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
		boardView.setGameFace(this);

		adjustBoardForGame();
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			dismissEndGameDialog();
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
		} else if (view.getId() == R.id.rematchPopupBtn) { // TODO fix, shouldn't be here
			sendRematch();
			dismissEndGameDialog();
		}
	}

	private void sendRematch() {
	}


	protected ControlsDailyView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsDailyView) controlsView;
	}

	public void setNotationsFace(View notationsView) {
		this.notationsFace = (NotationFace) notationsView;
	}

	public NotationFace getNotationsFace() {
		return notationsFace;
	}

	@Override
	protected View getTopPanelView() {
		return topPanelView;
	}

	@Override
	protected View getBottomPanelView() {
		return bottomPanelView;
	}

	public void init() {
		labelsConfig = new LabelsConfig();

		currentGamesCursorUpdateListener = new LoadFromDbUpdateListener(GAMES_LIST);

		imageDownloader = new ImageDownloaderToListener(getActivity());

		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
	}

	private void widgetsInit(View view) {

		setControlsView(view.findViewById(R.id.controlsView));
		if (inPortrait()) {
			setNotationsFace(view.findViewById(R.id.notationsView));
		} else {
			setNotationsFace(view.findViewById(R.id.notationsViewTablet));
		}

		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

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
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsArray.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsArray.put(ID_SHARE_PGN, getString(R.string.share_pgn));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

}

