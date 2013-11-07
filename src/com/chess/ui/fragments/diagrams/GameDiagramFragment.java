package com.chess.ui.fragments.diagrams;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.BaseGameItem;
import com.chess.model.GameDiagramItem;
import com.chess.model.PopupItem;
import com.chess.statics.Symbol;
import com.chess.ui.engine.*;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.boards.PuzzlesBoardFace;
import com.chess.ui.interfaces.game_ui.GameDiagramFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.chess_boards.ChessBoardDiagramView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.ui.views.game_controls.ControlsDiagramView;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 15:30
 */
public class GameDiagramFragment extends GameBaseFragment implements GameDiagramFace {

	private static final String ERROR_TAG = "send request failed popup";

	protected static final String GAME_ITEM = "game_item";
	public static final int NOTATIONS_SHOW_DELAY = 550;
	private static final long MOVE_RESULT_HIDE_DELAY = 2000;

	private static final long DELAY_BETWEEN_MOVES = 900;
	private static final long START_DELAY = 500;

	private ChessBoardDiagramView boardView;
	private GameDiagramItem diagramItem;
	protected boolean userPlayWhite = true;
	private ControlsDiagramView controlsView;
	private LabelsConfig labelsConfig;
	private NotationView notationsView;
	private HashMap<String, String> commentsMap;
	private TextView notationCommentTxt;
	private boolean isPlaying;
	private int currentPuzzleAnswerCnt;
	private TextView moveResultTxt;
	private int resultIconPadding;
	private boolean isPuzzle;
	private boolean isSmallScreen;
	private boolean nexus4Kind;

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
		super.onViewCreated(view, savedInstanceState);

		if (isPuzzle) {
			setTitle(R.string.puzzle);
		} else {
			setTitle(R.string.chess_game);
		}

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
		handler.removeCallbacks(showNextPuzzleMoveTask);
		boardView.releaseBitmaps();
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
			while (boardFace.takeBack()) {
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
		while (boardFace.takeBack()) {
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
		while (boardFace.takeNext()) {
			notationsView.moveForward(boardFace.getPly());
		}
		notationsView.rewindForward();
		showCommentForMove(boardFace);
		boardView.invalidate();
	}

	/**
	 * get current move and compare to move with comment
	 */
	private void showCommentForMove(BoardFace boardFace) {
		if (commentsMap == null || isSmallScreen) {
			return;
		}

		String lastMove = boardFace.getLastMoveSAN();
		for (String move : commentsMap.keySet()) {
			String move2Compare = move.replaceAll(MovesParser.MOVE_NUMBERS_PATTERN, Symbol.EMPTY).trim();
			if (move2Compare.equals(lastMove)) {
				notationCommentTxt.setVisibility(View.VISIBLE);
				notationCommentTxt.setText(Html.fromHtml(commentsMap.get(move)));

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

	@Override
	public void verifyMove() {

		// skip verification is not in Puzzle mode
		if (controlsView.getState() == ControlsDiagramView.State.DEFAULT) {
			return;
		}

		PuzzlesBoardFace boardFace = getBoardFace();

		if (boardFace.isLastPuzzleMoveCorrect()) { // Correct
			if (boardFace.getMovesCount() < boardFace.getPuzzleMoves().length - 1) { // if it's not last move, make comp move
				final Move move = boardFace.convertMoveAlgebraic(boardFace.getPuzzleMoves()[boardFace.getPly()]);
				boardView.setMoveAnimator(move, true);
				boardView.resetValidMoves();
				boardFace.makeMove(move, true);
				invalidateGameScreen();
			} else {
				showCorrectViews();
			}
		} else { // wrong
			showWrongViews();

			boardFace.takeBack();
			invalidateGameScreen();
			boardView.invalidate();
		}
	}

	private void showCorrectViews() {
		if (isSmallScreen || nexus4Kind) {
			showToast(R.string.correct);
		} else {
			moveResultTxt.setText(R.string.correct);
			setIconToResultView(R.string.ic_check);
		}
		controlsView.showDefault();
		if (!isSmallScreen) {
			notationsView.setVisibility(View.VISIBLE);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					notationsView.rewindForward();
					showCommentForMove(getBoardFace());
					boardView.invalidate();
				}
			}, NOTATION_REWIND_DELAY);
		}
	}

	private void showWrongViews() {
		if (isSmallScreen || nexus4Kind) {
			showToast(R.string.incorrect);
		} else {
			moveResultTxt.setText(R.string.incorrect);
			setIconToResultView(R.string.ic_blocking);

			handler.postDelayed(hideMoveResultTask, MOVE_RESULT_HIDE_DELAY);
		}
	}

	private void setIconToResultView(int iconId) {
		IconDrawable iconDrawable = new IconDrawable(getActivity(), iconId,
				R.color.semitransparent_white_75, R.dimen.glyph_icon_big);
		moveResultTxt.setVisibility(View.VISIBLE);
		moveResultTxt.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
		moveResultTxt.setCompoundDrawablePadding(resultIconPadding);
	}

	private Runnable hideMoveResultTask = new Runnable() {
		@Override
		public void run() {
			if (userPlayWhite) {
				moveResultTxt.setText(R.string.white_to_move);
			} else {
				moveResultTxt.setText(R.string.black_to_move);
			}
			moveResultTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

			handler.removeCallbacks(hideMoveResultTask);
		}
	};

	@Override
	public void showHint() {
		// remember the move before the hint
		final PuzzlesBoardFace boardFace = getBoardFace();

		int hintMoveNumber = boardFace.getPly();
		if (hintMoveNumber == getBoardFace().getPuzzleMoves().length) {
			return;
		}

		// get next valid move
		final Move move = boardFace.convertMoveAlgebraic(boardFace.getPuzzleMoves()[hintMoveNumber]);
		boardFace.setMovesCount(boardFace.getMovesCount() + hintMoveNumber);

		// play move animation
		boardView.setMoveAnimator(move, true);
		boardView.resetValidMoves();
		// make actual move
		boardFace.makeMove(move, true);
		invalidateGameScreen();

		// restore move back
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
				boardView.resetValidMoves();
				getBoardFace().takeBack();
				invalidateGameScreen();
			}
		}, START_DELAY);
	}

	@Override
	public void showAnswer() {

		PuzzlesBoardFace boardFace = getBoardFace();

		currentPuzzleAnswerCnt = boardFace.getPly();

		// show first move immediately
		boolean sizeExceed = currentPuzzleAnswerCnt >= boardFace.getPuzzleMoves().length;

		if (sizeExceed) { // rewind back
			while (boardFace.takeBack()) {
				currentPuzzleAnswerCnt--;
			}
			boardView.invalidate();
		}
		// get next valid move
		final Move move = boardFace.convertMoveAlgebraic(boardFace.getPuzzleMoves()[currentPuzzleAnswerCnt]);
		boardFace.setMovesCount(boardFace.getMovesCount() + currentPuzzleAnswerCnt);

		// play move animation
		boardView.setMoveAnimator(move, true);
		boardView.resetValidMoves();
		// make actual move
		boardFace.makeMove(move, true);
		invalidateGameScreen();

		currentPuzzleAnswerCnt++;

		handler.postDelayed(showNextPuzzleMoveTask, DELAY_BETWEEN_MOVES);
	}

	@Override
	public void restart() {
		adjustBoardForGame();
	}

	private Runnable showNextPuzzleMoveTask = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);

			PuzzlesBoardFace boardFace = getBoardFace();
			boolean sizeExceed = currentPuzzleAnswerCnt >= boardFace.getPuzzleMoves().length;

			if (sizeExceed) {
				showCorrectViews();
				return;
			}

			// get next valid move
			final Move move = boardFace.convertMoveAlgebraic(boardFace.getPuzzleMoves()[currentPuzzleAnswerCnt]);
			boardFace.setMovesCount(boardFace.getMovesCount() + currentPuzzleAnswerCnt);

			// play move animation
			boardView.setMoveAnimator(move, true);
			boardView.resetValidMoves();
			// make actual move
			boardFace.makeMove(move, true);
			invalidateGameScreen();

			currentPuzzleAnswerCnt++;
			handler.postDelayed(this, DELAY_BETWEEN_MOVES);
		}
	};

	private void adjustBoardForGame() {
		ChessBoardDiagram.resetInstance();
		PuzzlesBoardFace boardFace = getBoardFace();

		userPlayWhite = !diagramItem.isFlip();

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			moveResultTxt.setText(R.string.white_to_move);
		} else {
			moveResultTxt.setText(R.string.black_to_move);
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

		String fen = diagramItem.getFen();
		boardFace.setupBoard(fen);

		// revert reside back, because for diagrams white is always at bottom
		if (!TextUtils.isEmpty(fen) && !fen.contains(FenHelper.WHITE_TO_MOVE)) {
			boardFace.setReside(!boardFace.isReside());
		}

		if (diagramItem.isFlip()) {
			boardFace.setReside(true);
		}

		// remove comments from movesList
		String movesList = diagramItem.getMovesList();
		if (movesList != null) {
			commentsMap = boardFace.getCommentsFromMovesList(movesList);

			movesList = boardFace.removeCommentsAndAlternatesFromMovesList(movesList);

			if (isPuzzle) {
				controlsView.showPuzzle();
				notationsView.setVisibility(View.GONE);
				boardFace.setPuzzleMoves(movesList);
			} else {
				moveResultTxt.setVisibility(View.INVISIBLE);
				notationsView.setVisibility(View.VISIBLE);
				controlsView.showDefault();
			}
			boardFace.checkAndParseMovesList(movesList);
		}

		invalidateGameScreen();

		if (diagramItem.isShowAnimation()) {
			boardView.resetValidMoves();

			// rewind all back
			while (boardFace.takeBack()) {
				notationsView.moveBack(boardFace.getPly());
			}
			// now play moves until we reach needed position
			for (int i = 0; i < diagramItem.getFocusMove(); i++) {
				Move move = boardFace.getNextMove();
				if (move != null) {
					boardFace.makeMove(move, false);
				}
			}

			boardView.invalidate();
		}

		if (isSmallScreen || TextUtils.isEmpty(movesList)) {
			notationsView.setVisibility(View.GONE);
			notationCommentTxt.setVisibility(View.GONE);
		}

		if (nexus4Kind) {
			moveResultTxt.setVisibility(View.GONE);
		}

		controlsView.enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				controlsView.enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);

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
	public void onNotationClicked(int pos) {
		showCommentForMove(getBoardFace());
	}

	@Override
	public void invalidateGameScreen() {
		if (!isSmallScreen) {
			boardView.updateNotations(getBoardFace().getNotationArray());
		}
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
	public PuzzlesBoardFace getBoardFace() {
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
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.notationCommentTxt) {
			CharSequence notationCommentTxtText = notationCommentTxt.getText();
			if (notationCommentTxtText != null) {
				String move = getBoardFace().getLastMoveSAN();
				popupItem.setButtonToShow(PopupItem.NEGATIVE_GREEN);
				showSinglePopupDialog(move, notationCommentTxtText.toString());
			}
		}
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

		isSmallScreen = AppUtils.noNeedTitleBar(getActivity());
		nexus4Kind = AppUtils.isNexus4Kind(getActivity());
		isPuzzle = diagramItem.getDiagramType().equals(GameDiagramItem.CHESS_PROBLEM);
	}

	private void widgetsInit(View view) {
		controlsView = (ControlsDiagramView) view.findViewById(R.id.controlsView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		notationCommentTxt = (TextView) view.findViewById(R.id.notationCommentTxt);
		notationCommentTxt.setOnClickListener(this);
		moveResultTxt = (TextView) view.findViewById(R.id.moveResultTxt);
		controlsView.enableGameControls(false);

		boardView = (ChessBoardDiagramView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsView);
		boardView.setNotationsFace(notationsView);

		setBoardView(boardView);

		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);

		addLayoutChangeAnimation(view.findViewById(R.id.boardFrame));

		resultIconPadding = getResources().getDimensionPixelSize(R.dimen.glyph_icon_padding);

		controlsView.enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				controlsView.enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);
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
