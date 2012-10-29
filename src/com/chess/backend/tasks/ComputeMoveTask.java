package com.chess.backend.tasks;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.model.ComputeMoveItem;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.Search;
import com.chess.ui.interfaces.BoardFace;

/**
 * @author alien_roger
 * @created 18.09.12
 * @modified 18.09.12
 */
public class ComputeMoveTask extends AbstractUpdateTask<ComputeMoveItem, Void> {

	public ComputeMoveTask(ComputeMoveItem computeMoveItem, TaskUpdateInterface<ComputeMoveItem> taskUpdateInterface){
		super(taskUpdateInterface);
		this.item = computeMoveItem;
	}

	@Override
	protected Integer doTheTask(Void... params) {
		BoardFace boardFace = item.getBoardFace();
		item.setPieces_tmp(boardFace.getPieces().clone());
		item.setColors_tmp(boardFace.getColor().clone());

		Search searcher = new Search(boardFace);
		searcher.think(0, item.getMoveTime(), 32);

		Move best = searcher.getBest();
		boardFace.makeMove(best);

		return StaticData.RESULT_OK;
	}

}
