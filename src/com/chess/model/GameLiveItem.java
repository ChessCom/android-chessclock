package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.Game;

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

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);
	}


	public static final Parcelable.Creator<GameLiveItem> CREATOR = new Parcelable.Creator<GameLiveItem>() {
		public GameLiveItem createFromParcel(Parcel in) {
			return new GameLiveItem(in);
		}

		public GameLiveItem[] newArray(int size) {
			return new GameLiveItem[size];
		}
	};

	private GameLiveItem(Parcel in) {
		readBaseGameParcel(in);
	}

}
