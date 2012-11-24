package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.chess.backend.RestHelper;

public class GameListChallengeItem extends BaseGameOnlineItem {

	private int opponentWinCount;
	private int opponentLossCount;
	private int opponentDrawCount;
	private int playAsColor;
	private int daysPerMove;
//	private String initialSetupFen;
	private boolean isRated;


	public GameListChallengeItem(String[] values) {
		gameId = Long.parseLong(values[0].trim());
		opponentName = values[1];
		String rating = values[2];
		if (rating != null && !TextUtils.isEmpty(rating)) {
			opponentRating = Integer.parseInt(rating);
		}

		String winCount = values[3];
		if (winCount != null && !TextUtils.isEmpty(winCount)) {
			opponentWinCount = Integer.parseInt(values[3]);
		}

		String lossCount = values[4];
		if (lossCount != null && !TextUtils.isEmpty(lossCount)) {
			opponentLossCount = Integer.parseInt(values[4]);
		}
		String drawCount = values[5];
		if (drawCount != null && !TextUtils.isEmpty(drawCount)) {
			opponentDrawCount = Integer.parseInt(values[5]);
		}
		playAsColor = Integer.parseInt(values[6]);

		daysPerMove = Integer.parseInt(values[7]);
		gameType = Integer.parseInt(values[8]);

		String rated = values[9];
		if (rated != null) {
			isRated = rated.equals(RestHelper.V_ONE);
		}
//		initialSetupFen = values[10];
	}

	public int getOpponentWinCount() {
		return opponentWinCount;
	}

	public int getOpponentLossCount() {
		return opponentLossCount;
	}

	public int getOpponentDrawCount() {
		return opponentDrawCount;
	}

	public int getPlayAsColor() {
		return playAsColor;
	}

	public boolean getRated() {
		return isRated;
	}

//	public String getInitialSetupFen() {
//		return initialSetupFen;
//	}

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
	public void writeToParcel(Parcel out, int flags) {
		writeBaseGameParcel(out);

		// own write
		out.writeInt(opponentWinCount);
		out.writeInt(opponentLossCount);
		out.writeInt(opponentDrawCount);
		out.writeInt(playAsColor);
		out.writeInt(daysPerMove);
//		parcel.writeString(initialSetupFen);
        out.writeByte((byte) (isRated ? 1 : 0));
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
//		initialSetupFen = in.readString();
        isRated = in.readByte() == 1;
	}
}
