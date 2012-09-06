package com.chess.model;

import com.chess.backend.statics.StaticData;
import com.chess.live.client.Game;

import java.util.Iterator;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 31.07.12
 * @modified 31.07.12
 */
public class GameLiveItem extends BaseGameItem {

	private static final long serialVersionUID = -1595434515333793544L;

	public GameLiveItem(Game lccGame, int moveIndex) {

		gameId = lccGame.getId();
		timestamp = System.currentTimeMillis();
		whiteUsername = lccGame.getWhitePlayer().getUsername().trim();
		blackUsername = lccGame.getBlackPlayer().getUsername().trim();

		String moves = StaticData.SYMBOL_EMPTY;

		final Iterator movesIterator = lccGame.getMoves().iterator();
		for (int i = 0; i <= moveIndex; i++) {
			moves += movesIterator.next() + StaticData.SYMBOL_SPACE;
		}
		if (moveIndex == -1) {
			moves = StaticData.SYMBOL_EMPTY;
		}

		moveList = moves;


		Integer whiteRating = 0;
		Integer blackRating = 0;
		switch (lccGame.getGameTimeConfig().getGameTimeClass()) {
			case BLITZ: {
				whiteRating = lccGame.getWhitePlayer().getBlitzRating();
				blackRating = lccGame.getBlackPlayer().getBlitzRating();
				break;
			}
			case LIGHTNING: {
				whiteRating = lccGame.getWhitePlayer().getQuickRating();
				blackRating = lccGame.getBlackPlayer().getQuickRating();
				break;
			}
			case STANDARD: {
				whiteRating = lccGame.getWhitePlayer().getStandardRating();
				blackRating = lccGame.getBlackPlayer().getStandardRating();
				break;
			}
		}
		if (whiteRating == null) {
			whiteRating = 0;
		}
		if (blackRating == null) {
			blackRating = 0;
		}

		this.whiteRating = String.valueOf(whiteRating);
		this.blackRating = String.valueOf(blackRating);

		secondsRemain = String.valueOf(lccGame.getGameTimeConfig().getBaseTime() / 10);
	}

}
