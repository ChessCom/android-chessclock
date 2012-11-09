package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.RestHelper;

public class GameListFinishedItem extends BaseGameOnlineItem{

	/*
	game_type: 1 = standard chess , 2 = chess 960
	time_remaining_units:  h = hours , d = days
	is_opponent_online: 1 = yes , 0 = no
	game_result: 1 = win, 0 = loss, 2 = draw
	color: 1 = white , 2 = black
	 */

	private int gameResults;

	public GameListFinishedItem() {
	}

	public GameListFinishedItem(String[] values) {
//		super(values);
		gameId = Long.parseLong(values[0]);
//		color = Integer.parseInt(values[1]);
		gameType = Integer.parseInt(values[2]);
//		userNameStrLength = Integer.parseInt(values[3]);
		opponentName = values[4];
		opponentRating = Integer.parseInt(values[5]);
		timeRemainingAmount = Integer.parseInt(values[6]);
		timeRemainingUnits = values[7];
//		fenStrLength = Integer.parseInt(values[8]);
//		fen = values[9];
		timestamp = Long.parseLong(values[10]);
//		lastMoveFromSquare =  values[11];
//		lastMoveToSquare = values[12];
//		isDrawOfferPending = values[13].equals("p");
		isOpponentOnline = values[14].equals(RestHelper.V_ONE);

		gameResults = Integer.parseInt(values[14]);
	}

	public void setGameResults(int gameResults) {
		this.gameResults = gameResults;
	}

	public int getGameResult() {
		return gameResults;
	}


	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);

		writeBaseGameOnlineParcel(parcel);
		parcel.writeInt(gameResults);
	}



	public static final Parcelable.Creator<GameListFinishedItem> CREATOR = new Parcelable.Creator<GameListFinishedItem>() {
		public GameListFinishedItem createFromParcel(Parcel in) {
			return new GameListFinishedItem(in);
		}

		public GameListFinishedItem[] newArray(int size) {
			return new GameListFinishedItem[size];
		}
	};

	private GameListFinishedItem(Parcel in) {
		readBaseGameParcel(in);

		readBaseGameOnlineParcel(in);
		gameResults = in.readInt();
	}



}
