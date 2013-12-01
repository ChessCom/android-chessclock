package com.chess.ui.interfaces.boards;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 17:53
 */
public interface BoardViewAnalysisFace extends BoardViewFace {

	void restart();

	void flipBoard();

	void vsComputer();

	void closeBoard();

	void showExplorer();
}
