package com.chess.ui.fragments.game;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.chess.R;
import com.chess.model.BaseGameItem;
import com.chess.model.GameAnalysisItem;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardAnalysis;
import com.chess.ui.fragments.explorer.GameExplorerFragment;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameAnalysisFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardAnalysisView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsAnalysisView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 15:21
 */
public class GameAnalyzeFragment extends GameBaseFragment implements GameAnalysisFace {

	private static final String ERROR_TAG = "send request failed popup";

	private static final String GAME_ITEM = "game_item";

	private ChessBoardAnalysisView boardView;
	private GameAnalysisItem analysisItem;
	protected boolean userPlayWhite = true;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsAnalysisView controlsView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private BoardAvatarDrawable opponentAvatarDrawable;
	private BoardAvatarDrawable userAvatarDrawable;
	private LabelsConfig labelsConfig;

	public static GameAnalyzeFragment createInstance(GameAnalysisItem analysisItem) {
		GameAnalyzeFragment fragment = new GameAnalyzeFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(GAME_ITEM, analysisItem);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			analysisItem = getArguments().getParcelable(GAME_ITEM);
		} else {
			analysisItem = savedInstanceState.getParcelable(GAME_ITEM);
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

		adjustBoardForGame();

	}

	@Override
	public void onPause() {
		super.onPause();

		if (HONEYCOMB_PLUS_API) {
			dismissDialogs();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(GAME_ITEM, analysisItem);
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
		getActivityFace().openFragment(GameExplorerFragment.createInstance(getBoardFace().generateBaseFen()));
	}

	private void adjustBoardForGame() {
		ChessBoardAnalysis.resetInstance();
		userPlayWhite = analysisItem.getUserColor() == ChessBoard.WHITE_SIDE;

		labelsConfig.topAvatar = opponentAvatarDrawable;
		labelsConfig.bottomAvatar = userAvatarDrawable;

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		}

		labelsConfig.topPlayerName = analysisItem.getOpponent();
		labelsConfig.topPlayerRating = null;
		labelsConfig.bottomPlayerName = getUsername();
		labelsConfig.bottomPlayerRating = null;
		labelsConfig.topPlayerAvatar = "";
		labelsConfig.bottomPlayerAvatar = getAppData().getUserAvatar();
		labelsConfig.topPlayerCountry = null;
		labelsConfig.bottomPlayerCountry = getAppData().getUserCountry();
		labelsConfig.topPlayerPremiumStatus = 0;
		labelsConfig.bottomPlayerPremiumStatus = getAppData().getUserPremiumStatus();

		controlsView.enableGameControls(true);
		boardView.lockBoard(false);

		getBoardFace().setFinished(false);


		topPanelView.showTimeLeftIcon(false);
		bottomPanelView.showTimeLeftIcon(false);

		BoardFace boardFace = getBoardFace();
		if (analysisItem.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		if (TextUtils.isEmpty(analysisItem.getMovesList())) {  // don't parse FEN if we have movesList
			boardFace.setupBoard(analysisItem.getFen());
		}

		if (!userPlayWhite) {
			boardFace.setReside(true);
		}

		boardFace.checkAndParseMovesList(analysisItem.getMovesList());

		boardView.resetValidMoves();

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		boardFace.setJustInitialized(false);
		boardFace.setAnalysis(true);
	}


	@Override
	public void toggleSides() { // TODO
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

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public String getWhitePlayerName() {
		if (labelsConfig.userSide == ChessBoard.BLACK_SIDE) {
			return Symbol.EMPTY;
		} else {
			return getUsername();
		}
	}

	@Override
	public String getBlackPlayerName() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			return Symbol.EMPTY;
		} else {
			return getUsername();
		}
	}

	@Override
	public boolean currentGameExist() {
		return true;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardAnalysis.getInstance(this);
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {
	}

	@Override
	public Boolean isUserColorWhite() {
		return labelsConfig.userSide == ChessBoard.WHITE_SIDE;
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	@Override
	public void showOptions() {
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

	}

	@Override
	protected void restoreGame() {
		boardView.setGameActivityFace(this);

		adjustBoardForGame();
		getBoardFace().setJustInitialized(false);
	}

	private void init() {
		labelsConfig = new LabelsConfig();
	}

	private void widgetsInit(View view) {
		controlsView = (ControlsAnalysisView) view.findViewById(R.id.controlsView);
		NotationView notationsView = (NotationView) view.findViewById(R.id.notationsView);
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

		controlsView.enableGameControls(false);

		boardView = (ChessBoardAnalysisView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsView);
		boardView.setNotationsFace(notationsView);

		setBoardView(boardView);

		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);
	}

}
