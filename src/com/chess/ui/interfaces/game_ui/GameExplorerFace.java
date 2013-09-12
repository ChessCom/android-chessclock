package com.chess.ui.interfaces.game_ui;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.09.13
 * Time: 10:29
 */
public interface GameExplorerFace extends GameFace {

	void nextPosition(String move);

	void showNextMoves();
}
