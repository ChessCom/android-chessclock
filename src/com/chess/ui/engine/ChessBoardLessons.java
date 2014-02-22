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

	public ChessBoardLessons(GameFace gameFace) {
		super(gameFace);
		gameId = gameFace.getGameId();
	}

	@Override
	public boolean isLatestMoveMadeUser() {
		return ply > 0 && ply % 2 == 0;
	}

	@Override
	public boolean isLastLessonMoveIsCorrect(String lessonMove) {

		String lastUserMove = getLastMoveSAN();
		if (lastUserMove == null) {
			return false;
		}

		// take back so the board parsing logic will understand that we converting previous move
		takeBack();
		Move userLastMove = convertMoveAlgebraic(lastUserMove);
		Move validMove = convertMoveAlgebraic(lessonMove);

		if (userLastMove == null || validMove == null) {
			return false;
		}
		// step forward to the normal, current state
		takeNext(false);
		return validMove.equals(userLastMove);
	}

}
