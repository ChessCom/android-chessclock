package com.chess.ui.engine;

import com.chess.ui.interfaces.boards.LessonsBoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 12:45
 */
public class ChessBoardLessons extends ChessBoard implements LessonsBoardFace {

	private static ChessBoardLessons instance;

	private ChessBoardLessons(GameFace gameFace) {
		super(gameFace);
	}

	public static ChessBoardLessons getInstance(GameFace gameFace) {
		final Long gameId = gameFace.getGameId();

		if (instance == null || instance.gameId == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoardLessons(gameFace);
			instance.gameId = gameId;
			instance.justInitialized = true;
		} else {
			instance.justInitialized = false;
		}
		return instance;
	}

	public static void resetInstance() {
		instance = null;
	}

	@Override
	public boolean isLatestMoveMadeUser() {
		return ply > 0 && ply % 2 == 0;
	}

	@Override
	public boolean isLastLessonMoveIsCorrect(String validMove) {

		Move validLessonMove = convertMoveAlgebraic(validMove);
		Move userLastMove = convertMoveAlgebraic(getLastMoveSAN());
		return validLessonMove.equals(userLastMove);
	}

}
