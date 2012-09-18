package com.chess.backend.tasks;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.ui.interfaces.BoardFace;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 18.09.12
 * @modified 18.09.12
 */
public class ComputeMoveTask extends AbstractUpdateTask<String, Void> {

//	private int[] pieces_tmp;
//	private int[] colors_tmp;
//	private BoardFace boardFace;
//	private int time;

	public ComputeMoveTask(BoardFace boardFace, TaskUpdateInterface<String> taskUpdateInterface, int time){
		super(taskUpdateInterface);

//		this.time = time;
	}

	@Override
	protected Integer doTheTask(Void... params) {
//		pieces_tmp = boardFace.getPieces().clone();
//		colors_tmp = boardFace.getColor().clone();
//		Search searcher = new Search(boardFace);
//		searcher.think(0, time, 32);
//		Move best = searcher.getBest();
//		boardFace.makeMove(best);
////		boardFace.set
////		computerMoving = false;
//		boardFace.setMovesCount(boardFace.getHply());
////		update.sendEmptyMessage(0);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

}
