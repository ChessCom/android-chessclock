package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GameListChallengeItem extends BaseGameOnlineItem{

	private String opponentWinCount;
	private String opponentLossCount;
	private String opponentDrawCount;
	private String playAsColor;
	private String daysPerMove;
	private String initialSetupFen;
	private String isRated;


	public GameListChallengeItem(String[] values) {
		gameId = Long.parseLong(values[0].trim());
		opponentName = values[1];
		opponentRating = values[2];

		opponentWinCount = values[3];
		opponentLossCount = values[4];
		opponentDrawCount = values[5];

		playAsColor = values[6];
		daysPerMove = values[7];
		gameType = values[8];
		isRated = values[9];
		initialSetupFen = values[10];
	}
	
	public String getOpponentUsername(){
		return opponentName;
	}
	
	public String getOpponentRating(){
		return opponentRating;
	}

	public String getOpponentWinCount(){
		return opponentWinCount;
	}

	public String getOpponentLossCount(){
		return opponentLossCount;
	}

	public String getOpponentDrawCount() {
		return opponentDrawCount;
	}

	public String getPlayAsColor() {
		return playAsColor;
	}

	public String getGameType() {
		return gameType;
	}

	public String getRated() {
		return isRated;
	}

	public String getInitialSetupFen() {
		return initialSetupFen;
	}

	public String getDaysPerMove() {
		return daysPerMove;
	}

//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		for (String string : values.keySet()) {
//			builder.append(" key = ").append(string).append(" value = ").append(values.get(string)).append("\n");
//		}
//		return builder.toString();
//	}



//	The eches challenges response looks like the following:
//	<
//	<game_seek_id>: The game id
//	<game_name>: The seek name - can be null
//	<opponent_username>: The opponent username
//	<opponent_rating>: The opponent rating
//	<opponent_win_count>: The opponent win count
//	<opponent_loss_count>: The opponent loss count
//	<opponent_draw_count>: The opponent Draw count
//	<player_color>:  The users color he/she will play as, 1 = white, 2 = black, 0 = random
//	<days_per_move>: The days per move for the seek
//	<game_type>: The chess game type.  1 = chess, 2 = chess960
//	<is_rated>: Is the seek rated or unrated?, 1 for rated, 2 for not rated
//	<initial_setup_fen>: The initial starting position.  This field can be null
//	>
//

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);

		// own write
		parcel.writeString(opponentWinCount);
		parcel.writeString(opponentLossCount);
		parcel.writeString(opponentDrawCount);
		parcel.writeString(playAsColor);
		parcel.writeString(daysPerMove);
		parcel.writeString(initialSetupFen);
		parcel.writeString(isRated);
	}


	public static final Parcelable.Creator<GameListChallengeItem> CREATOR = new Parcelable.Creator<GameListChallengeItem>() {
		public GameListChallengeItem createFromParcel(Parcel in) {
			return new GameListChallengeItem(in);
		}

		public GameListChallengeItem[] newArray(int size) {
			return new GameListChallengeItem[size];
		}
	};

	private GameListChallengeItem(Parcel in) {
		readBaseGameParcel(in);

		opponentWinCount = in.readString();
		opponentLossCount = in.readString();
		opponentDrawCount = in.readString();
		playAsColor = in.readString();
		daysPerMove = in.readString();
		initialSetupFen = in.readString();
		isRated = in.readString();
	}
}
