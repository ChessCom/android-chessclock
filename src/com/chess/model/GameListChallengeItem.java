package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GameListChallengeItem extends BaseGameOnlineItem{

	private int opponentWinCount;
	private int opponentLossCount;
	private int opponentDrawCount;
	private int playAsColor;
	private int daysPerMove;
	private String initialSetupFen;
	private boolean isRated;


	public GameListChallengeItem(String[] values) {
		gameId = Long.parseLong(values[0].trim());
		opponentName = values[1];
		opponentRating = Integer.parseInt(values[2]);

		opponentWinCount = Integer.parseInt(values[3]);
		opponentLossCount = Integer.parseInt(values[4]);
		opponentDrawCount = Integer.parseInt(values[5]);

		playAsColor = Integer.parseInt(values[6]);
		daysPerMove = Integer.parseInt(values[7]);
		gameType = Integer.parseInt(values[8]);
		isRated = values[9].equals("1");
		initialSetupFen = values[10];
	}
	
	public String getOpponentUsername(){
		return opponentName;
	}
	
	public int getOpponentRating(){
		return opponentRating;
	}

	public int getOpponentWinCount(){
		return opponentWinCount;
	}

	public int getOpponentLossCount(){
		return opponentLossCount;
	}

	public int getOpponentDrawCount() {
		return opponentDrawCount;
	}

	public int getPlayAsColor() {
		return playAsColor;
	}

	public int getGameType() {
		return gameType;
	}

	public boolean getRated() {
		return isRated;
	}

	public String getInitialSetupFen() {
		return initialSetupFen;
	}

	public int getDaysPerMove() {
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
		parcel.writeInt(opponentWinCount);
		parcel.writeInt(opponentLossCount);
		parcel.writeInt(opponentDrawCount);
		parcel.writeInt(playAsColor);
		parcel.writeInt(daysPerMove);
		parcel.writeString(initialSetupFen);
		boolean[] booleans = new boolean[]{isRated};
		parcel.writeBooleanArray(booleans);
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

		opponentWinCount = in.readInt();
		opponentLossCount = in.readInt();
		opponentDrawCount = in.readInt();
		playAsColor = in.readInt();
		daysPerMove = in.readInt();
		initialSetupFen = in.readString();
		boolean[] booleans = new boolean[1];
		in.readBooleanArray(booleans);
		isRated = booleans[0];
	}
}
