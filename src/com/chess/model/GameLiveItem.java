package com.chess.model;

import android.os.Parcel;
import com.chess.live.client.Game;
import com.chess.live.util.GameRatingClass;
import com.chess.statics.Symbol;

import java.util.Iterator;

/**
 * @author alien_roger
 * @created 31.07.12
 * @modified 31.07.12
 */
public class GameLiveItem extends BaseGameItem {

	public GameLiveItem(Game lccGame, int moveIndex) {

		gameId = lccGame.getId();
		timestamp = System.currentTimeMillis();
		whiteUsername = lccGame.getWhitePlayer().getUsername().trim();
		blackUsername = lccGame.getBlackPlayer().getUsername().trim();

		moveList = Symbol.EMPTY;

		final Iterator movesIterator = lccGame.getMoves().iterator();
		for (int i = 0; i <= moveIndex; i++) {
			moveList += movesIterator.next() + Symbol.SPACE;
		}

		Integer whiteRating = 0;
		Integer blackRating = 0;
		switch (lccGame.getGameTimeConfig().getGameTimeClass()) {
			case BLITZ: {
				whiteRating = lccGame.getWhitePlayer().getRatingFor(GameRatingClass.Blitz);
				blackRating = lccGame.getBlackPlayer().getRatingFor(GameRatingClass.Blitz);
				break;
			}
			case LIGHTNING: {
				whiteRating = lccGame.getWhitePlayer().getRatingFor(GameRatingClass.Lightning);
				blackRating = lccGame.getBlackPlayer().getRatingFor(GameRatingClass.Lightning);
				break;
			}
			case STANDARD: {
				whiteRating = lccGame.getWhitePlayer().getRatingFor(GameRatingClass.Standard);
				blackRating = lccGame.getBlackPlayer().getRatingFor(GameRatingClass.Standard);
				break;
			}
		}
		if (whiteRating == null) {
			whiteRating = 0;
		}
		if (blackRating == null) {
			blackRating = 0;
		}

		this.whiteRating = whiteRating;
		this.blackRating = blackRating;

		secondsRemain = (lccGame.getGameTimeConfig().getBaseTime() / 10);
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);
	}


	public static final Creator<GameLiveItem> CREATOR = new Creator<GameLiveItem>() {
		@Override
		public GameLiveItem createFromParcel(Parcel in) {
			return new GameLiveItem(in);
		}

		@Override
		public GameLiveItem[] newArray(int size) {
			return new GameLiveItem[size];
		}
	};

	private GameLiveItem(Parcel in) {
		readBaseGameParcel(in);
	}

}
