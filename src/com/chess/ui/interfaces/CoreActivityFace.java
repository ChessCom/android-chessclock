package com.chess.ui.interfaces;

import com.chess.model.Game;
import com.chess.utilities.SoundPlayer;

/**
 * CoreActivityFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:25
 */
public interface CoreActivityFace {
	Boolean isUserColorWhite();

	SoundPlayer getSoundPlayer();

	Game getCurrentGame();
}
