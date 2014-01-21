package com.chess.ui.interfaces.game_ui;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 18:25
 */
public interface GameAnalysisFace extends GameFace {

	void restart();

	void openNotes();

	void closeBoard();

	void showExplorer();

	void vsComputer();

}
