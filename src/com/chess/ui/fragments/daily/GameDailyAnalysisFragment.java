package com.chess.ui.fragments.daily;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.model.PopupItem;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.explorer.GameExplorerFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameAnalysisFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardAnalysisView;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsAnalysisView;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 7:26
 */
public class GameDailyAnalysisFragment extends GameBaseFragment implements GameAnalysisFace {

	private static final String ERROR_TAG = "send request failed popup";

	protected ChessBoardAnalysisView boardView;

	private DailyCurrentGameData currentGame;

	protected boolean userPlayWhite = true;
	protected LoadFromDbUpdateListener loadFromDbUpdateListener;
	protected PanelInfoGameView topPanelView;
	protected PanelInfoGameView bottomPanelView;
	protected ControlsAnalysisView controlsView;
	protected NotationFace notationsFace;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	protected BoardAvatarDrawable opponentAvatarDrawable;
	protected BoardAvatarDrawable userAvatarDrawable;
	protected LabelsConfig labelsConfig;
	protected String[] countryNames;
	protected int[] countryCodes;
	protected String username;

	public GameDailyAnalysisFragment(){}

	public static GameDailyAnalysisFragment createInstance(long gameId, String username) {
		GameDailyAnalysisFragment fragment = new GameDailyAnalysisFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
			username = getArguments().getString(USERNAME);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
			username = savedInstanceState.getString(USERNAME);
		}
		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}
		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_analysis_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.analysis);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		DataHolder.getInstance().setInOnlineGame(gameId, true);
		loadGame();
	}

	@Override
	public void onPause() {
		super.onPause();

		DataHolder.getInstance().setInOnlineGame(gameId, false);
		if (HONEYCOMB_PLUS_API) {
			dismissEndGameDialog();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	protected void loadGame() {
		// load game from DB. After load update
		new LoadDataFromDbTask(loadFromDbUpdateListener, DbHelper.getDailyGame(gameId, username),
				getContentResolver()).executeTask();
	}

	@Override
	public void restart() {
		adjustBoardForGame();
	}

	@Override
	public void closeBoard() {
		getActivityFace().showPreviousFragment();
	}

	@Override
	public void showExplorer() {
		getActivityFace().openFragment(GameExplorerFragment.createInstance(getBoardFace().generateFullFen()));
	}

	protected class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		public LoadFromDbUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			currentGame = DbDataManager.getDailyCurrentGameFromCursor(returnedObj);
			returnedObj.close();

			adjustBoardForGame();
		}
	}

	protected void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().equals(username);

		labelsConfig.topAvatar = opponentAvatarDrawable;
		labelsConfig.bottomAvatar = userAvatarDrawable;

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

		getControlsView().enableGameControls(true);
		boardView.lockBoard(false);

		getBoardFace().setFinished(false);

		long secondsRemain = currentGame.getTimeRemaining();
		String timeRemains;
		if (secondsRemain == 0) {
			timeRemains = getString(R.string.less_than_60_sec);
		} else {
			timeRemains = AppUtils.getTimeLeftFromSeconds(secondsRemain, getActivity());
		}

		String defaultTime = getDaysString(currentGame.getDaysPerMove());
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

		// boardFace.setupBoard(currentGame.getStartingFenPosition());
		// if we pass FEN like this rn1qkbnr/pp2pppp/2p5/5b2/3PN3/8/PPP2PPP/R1BQKBNR, and them moveslist that lead to this position, it fails to load properly
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

		boardFace.setAnalysis(true);
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
		if (currentGame == null) { // TODO fix inappropriate state, current game can't be null here // if we don't have Game entity
			// get game entity
			throw new IllegalStateException("Current game became NULL");
		}
	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {
	}

	@Override
	public Boolean isUserColorWhite() {
		if (currentGame != null)
			return currentGame.getWhiteUsername().equals(username);
		else
			return null;
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	@Override
	public void showOptions() {
	}

	private boolean isUserMove() {
		userPlayWhite = currentGame.getWhiteUsername().equals(username);

		return currentGame.isMyTurn();
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

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

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
//		ChessBoardOnline.resetInstance();
		boardView.setGameActivityFace(this);

		adjustBoardForGame();
		getBoardFace().setJustInitialized(false);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			dismissEndGameDialog();
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
		}
	}

	protected ControlsAnalysisView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsAnalysisView) controlsView;
	}

	public void setNotationsFace(View notationsView) {
		this.notationsFace = (NotationFace) notationsView;
	}

	public NotationFace getNotationsFace() {
		return notationsFace;
	}

	protected void init() {
		labelsConfig = new LabelsConfig();

		loadFromDbUpdateListener = new LoadFromDbUpdateListener();

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

		{// set avatars
			Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			opponentAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);
			userAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			labelsConfig.topAvatar = opponentAvatarDrawable;
			labelsConfig.bottomAvatar = userAvatarDrawable;
		}

		getControlsView().enableGameControls(false);

		boardView = (ChessBoardAnalysisView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(getControlsView());
		boardView.setNotationsFace(getNotationsFace());

		setBoardView(boardView);

		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);

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
}
