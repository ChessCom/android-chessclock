package com.chess.ui.interfaces.boards;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 19:07
 */
public interface BoardViewDiagramFace extends BoardViewFace {

	void onPlay();

	void onRewindBack();

	void onRewindForward();

	void showHint();

	void showSolution();

	void restart();
}
