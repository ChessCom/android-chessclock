package com.chess.ui.interfaces;

import com.chess.backend.entity.SoundPlayer;
import com.chess.model.GameItem;

/**
 * BoardToGameActivityFace class
 *
 * @author alien_roger
 * @created at: 05.03.12 5:25
 */
public interface BoardToGameActivityFace {
	Boolean isUserColorWhite();
	SoundPlayer getSoundPlayer();
	GameItem getCurrentGame();
}
