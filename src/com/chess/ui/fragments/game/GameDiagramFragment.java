package com.chess.ui.fragments.game;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.BaseGameItem;
import com.chess.model.GameDiagramItem;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardDiagram;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MovesParser;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameDiagramFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.chess_boards.ChessBoardDiagramView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsDiagramView;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 15:30
 */
public class GameDiagramFragment extends GameBaseFragment implements GameDiagramFace {

	private static final String ERROR_TAG = "send request failed popup";

	private static final String GAME_ITEM = "game_item";
	public static final int NOTATIONS_SHOW_DELAY = 650;
	private static final long DELAY_BETWEEN_MOVES = 1500;

	private ChessBoardDiagramView boardView;
	private GameDiagramItem diagramItem;
	protected boolean userPlayWhite = true;
	private ControlsDiagramView controlsView;
	private LabelsConfig labelsConfig;
	private NotationView notationsView;
	private HashMap<String, String> commentsMap;
	private TextView notationCommentTxt;
	private boolean isPlaying;

	public static GameDiagramFragment createInstance(GameDiagramItem analysisItem) {
		GameDiagramFragment fragment = new GameDiagramFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(GAME_ITEM, analysisItem);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			diagramItem = getArguments().getParcelable(GAME_ITEM);
		} else {
			diagramItem = savedInstanceState.getParcelable(GAME_ITEM);
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_diagram_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// we are inside of fragment already, don't change action buttons of parent fragment
		setNeedToChangeActionButtons(false);
		super.onViewCreated(view, savedInstanceState);

		if (!diagramItem.isShowAnimation()) {
			enableSlideMenus(true);
		}

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

		handler.removeCallbacks(showNextMoveRunnable);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(GAME_ITEM, diagramItem);
	}

	@Override
	public void onPlay() {
		// if position is final, restart from beginning
		BoardFace boardFace = getBoardFace();
		if (boardFace.getPly() + 1 > boardFace.getMovesCount()) {
			while(boardFace.takeBack()) {
				notationsView.moveBack(boardFace.getPly());
			}
		}

		// play animation from current position
		isPlaying = !isPlaying;
		controlsView.showPlayButton(!isPlaying);
		if (isPlaying) {
			handler.post(showNextMoveRunnable);
		} else {
			handler.removeCallbacks(showNextMoveRunnable);
		}
	}

	@Override
	public void onRewindBack() {
		BoardFace boardFace = getBoardFace();
		while(boardFace.takeBack()) {
			notationsView.moveBack(boardFace.getPly());
		}
		notationsView.rewindBack();
		showCommentForMove(boardFace);
		boardView.invalidate();
	}

	@Override
	public void onMoveBack() {
		BoardFace boardFace = getBoardFace();
		showCommentForMove(boardFace);
	}

	@Override
	public void onMoveForward() {
		BoardFace boardFace = getBoardFace();
		showCommentForMove(boardFace);
	}

	@Override
	public void onRewindForward() {
		BoardFace boardFace = getBoardFace();
		while(boardFace.takeNext()) {
			notationsView.moveForward(boardFace.getPly());
		}
		notationsView.rewindForward();
		showCommentForMove(boardFace);
		boardView.invalidate();
	}

	/**
	 * get current move and compare to move with comment
	 * @param boardFace
	 */
	private void showCommentForMove(BoardFace boardFace) {
		if (commentsMap == null) {
			return;
		}

		String lastMove = boardFace.getLastMoveSAN();
		for (String move : commentsMap.keySet()) {
			String move2Compare = move.replaceAll(MovesParser.MOVE_NUMBERS_PATTERN, Symbol.EMPTY).trim();
			if (move2Compare.equals(lastMove)) {
				notationCommentTxt.setVisibility(View.VISIBLE);
				notationCommentTxt.setText(commentsMap.get(move));

				notationsView.invalidate();
				return;
			}
		}
		notationCommentTxt.setVisibility(View.GONE);
		notationsView.invalidate();
	}

	private Runnable showNextMoveRunnable = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);
			if (getActivity() == null || !isPlaying) {
				return;
			}

			BoardFace boardFace = getBoardFace();
			String[] notations = boardFace.getFullNotationsArray();

			int currentPosition = boardFace.getPly();
			boolean sizeExceed = currentPosition >= notations.length;

			if (sizeExceed) {
				controlsView.enablePlayButton(false);
				return;
			}

			final Move move = boardFace.convertMoveAlgebraic(notations[currentPosition]);
			boardView.setMoveAnimator(move, true);
			boardView.resetValidMoves();
			boardFace.makeMove(move, true);
			invalidateGameScreen();
			showCommentForMove(boardFace);

			handler.postDelayed(this, DELAY_BETWEEN_MOVES);
		}
	};

	private void adjustBoardForGame() {
		ChessBoardDiagram.resetInstance();
		BoardFace boardFace = getBoardFace();
		userPlayWhite = diagramItem.getUserColor() == ChessBoard.WHITE_SIDE;

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		}

		labelsConfig.topPlayerName = diagramItem.getOpponent();
		labelsConfig.topPlayerRating = "----";
		labelsConfig.bottomPlayerName = getUsername();
		labelsConfig.bottomPlayerRating = "----";
		labelsConfig.topPlayerAvatar = "";
		labelsConfig.bottomPlayerAvatar = getAppData().getUserAvatar();
		labelsConfig.topPlayerCountry = "International";
		labelsConfig.bottomPlayerCountry = getAppData().getUserCountry();
		labelsConfig.topPlayerPremiumStatus = 0;
		labelsConfig.bottomPlayerPremiumStatus = getAppData().getUserPremiumStatus();

		controlsView.enableGameControls(true);
		boardView.lockBoard(false);

		boardFace.setFinished(false);

		if (diagramItem.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		boardFace.setupBoard(diagramItem.getFen());

		if (!userPlayWhite) {
			boardFace.setReside(true);
		}

		// remove comments from movesList

		String movesList = diagramItem.getMovesList();
		if (movesList != null) {
			commentsMap = MovesParser.getCommentsFromMovesList(movesList);

			movesList = MovesParser.removeCommentsAndAlternatesFromMovesList(movesList);

			boardFace.checkAndParseMovesList(movesList);
		}

		if (diagramItem.isShowAnimation()) {
			boardView.resetValidMoves();

			invalidateGameScreen();
			boardFace.takeBack();
			boardView.invalidate();

			playLastMoveAnimation();

			showCommentForMove(boardFace);
		}

		boardFace.setJustInitialized(false);
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
	public void onNotationClicked(int pos) {
		showCommentForMove(getBoardFace());
	}

	@Override
	public void invalidateGameScreen() {
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
		return ChessBoardDiagram.getInstance(this);
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
		controlsView = (ControlsDiagramView) view.findViewById(R.id.controlsDiagramView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		notationCommentTxt = (TextView) view.findViewById(R.id.notationCommentTxt);
		controlsView.enableGameControls(false);

		boardView = (ChessBoardDiagramView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsView);
		boardView.setNotationsView(notationsView);

		setBoardView(boardView);

		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);

		addLayoutChangeAnimation(view.findViewById(R.id.baseView));

		// show notationsView with animation
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				notationsView.setVisibility(View.VISIBLE);
			}
		}, NOTATIONS_SHOW_DELAY);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void addLayoutChangeAnimation(View view) {
		if (JELLY_BEAN_PLUS_API) {
			ViewGroup baseView = (ViewGroup) view;
			LayoutTransition layoutTransition = baseView.getLayoutTransition();
			layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		}
	}

}
