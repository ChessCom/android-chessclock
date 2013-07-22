package com.chess.ui.interfaces.boards;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.07.13
 * Time: 13:10
 */
public interface LessonsBoardFace extends BoardFace {

	public boolean isLatestMoveMadeUser();

	String getLastMoveStr();
}
