package com.chess.ui.interfaces;

import com.chess.backend.entity.SoundPlayer;

/**
 * BoardToGameActivityFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:25
 */
public interface BoardToGameActivityFace {
	SoundPlayer getSoundPlayer();

	Boolean isUserColorWhite();
}
