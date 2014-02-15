package com.chess.ui.interfaces.game_ui;

import org.petero.droidfish.gamelogic.Move;

import java.util.ArrayList;

/**
 * GameFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameCompFace extends GameFace {

	/**
	 * Called when comp made move and it's player's turn to move
	 */
	void onPlayerMove();

	/**
	 * Called when player made move and it's comp turn to move
	 */
	void onCompMove();

	void computer();

	void onGameStarted(int currentMovePosition);

	void updateEngineMove(Move engineMove);

	void onEngineThinkingInfo(String engineThinkingInfo, String statStr, String variantStr, ArrayList<ArrayList<Move>> pvMoves,
							  ArrayList<Move> variantMoves, ArrayList<Move> bookMoves);

	void run(Runnable runnable);

}
