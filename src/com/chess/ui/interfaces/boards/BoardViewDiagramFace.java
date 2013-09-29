package com.chess.ui.interfaces.boards;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 19:07
 */
public interface BoardViewDiagramFace {

	void onPlay();

	void onRewindBack();

	void onMoveBack();

	void onMoveForward();

	void onRewindForward();

	void showOptions();
}
