package com.chess.ui.interfaces;

import org.petero.droidfish.gamelogic.Move;
import org.petero.droidfish.gamelogic.Position;

import java.util.ArrayList;

/**
 * GameActivityFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameCompActivityFace extends GameActivityFace {

	void onPlayerMove();

	void onCompMove();

	void updateEngineMove(Position sourcePos, Move engineMove);

	void onEngineThinkingInfo(String engineThinkingInfo, String variantStr, ArrayList<ArrayList<Move>> pvMoves, ArrayList<Move> variantMoves, ArrayList<Move> bookMoves);

	void run(Runnable runnable);

}
