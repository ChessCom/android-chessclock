package com.chess.ui.interfaces;

/**
 * GameActivityFace class
 *
 * @author alien_roger
 * @created at: 13.03.12 7:08
 */
public interface GameCompActivityFace extends GameActivityFace {

	void onPlayerMove();

	void onCompMove();

	void updateCompMove(String engineMove);

	void onEngineThinkingInfo(String engineThinkingInfo, String variantStr);

	void run(Runnable runnable);

}
