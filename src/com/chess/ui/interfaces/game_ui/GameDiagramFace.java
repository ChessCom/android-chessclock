package com.chess.ui.interfaces.game_ui;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 19:25
 */
public interface GameDiagramFace extends GameFace {

	void onPlay();

	void onRewindBack();

	void onMoveBack();

	void onMoveForward();

	void onRewindForward();

}