package com.chess.backend.tasks;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.interfaces.BoardFace;

/**
 * CalculateTacticsMoveTask class
 *
 * @author alien_roger
 * @created at: 30.07.12 4:46
 */
public class CalculateTacticsMoveTask extends AbstractUpdateTask<String, Void> {
	private BoardFace boardFace;

	public CalculateTacticsMoveTask(TaskUpdateInterface<String> taskFace, BoardFace boardFace) {
		super(taskFace);
		this.boardFace = boardFace;
	}

	@Override
	protected Integer doTheTask(Void... params) {
		int i;
		for (i = 0; i < boardFace.getTacticMoves().length; i++) {
			int[] moveFT = MoveParser.parse(boardFace, boardFace.getTacticMoves()[i]);
			try {
				Thread.sleep(1500);
			} catch (Exception ignored) {
			}
			if (moveFT.length == 4) {
				Move move;
				if (moveFT[3] == 2)
					move = new Move(moveFT[0], moveFT[1], 0, 2);
				else
					move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

				boardFace.makeMove(move);
			} else {
				Move move = new Move(moveFT[0], moveFT[1], 0, 0);
				boardFace.makeMove(move);
			}
		}
		return result = StaticData.RESULT_OK;
	}
}
