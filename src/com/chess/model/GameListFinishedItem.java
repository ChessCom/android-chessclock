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

	private int gameResult;

	public GameListFinishedItem() {
	}

	public GameListFinishedItem(String[] values) {
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
//		isDrawOffered = values[13].equals("p");
		isOpponentOnline = values[14].equals(RestHelper.V_TRUE);

		gameResult = Integer.parseInt(values[14]);
	}

	public void setGameResult(int gameResult) {
		this.gameResult = gameResult;
	}

	public int getGameResult() {
		return gameResult;
	}


	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);

		writeBaseGameOnlineParcel(parcel);
		parcel.writeInt(gameResult);
	}



	public static final Parcelable.Creator<GameListFinishedItem> CREATOR = new Parcelable.Creator<GameListFinishedItem>() {
		@Override
		public GameListFinishedItem createFromParcel(Parcel in) {
			return new GameListFinishedItem(in);
		}

		@Override
		public GameListFinishedItem[] newArray(int size) {
			return new GameListFinishedItem[size];
		}
	};

	private GameListFinishedItem(Parcel in) {
		readBaseGameParcel(in);

		readBaseGameOnlineParcel(in);
		gameResult = in.readInt();
	}



}
