package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.RestHelper;

/**
 * @author alien_roger
 * @created 31.07.12
 * @modified 31.07.12
 */
public class GameOnlineItem extends BaseGameItem {

	public final static int CURRENT_TYPE = 0;
	public final static int CHALLENGES_TYPE = 1;
	public final static int FINISHED_TYPE = 2;

	private boolean whiteUserMove;
	private String encodedMoveStr;

	protected String gameName;
	protected int gameType;
	protected String fenStartPosition;

	private boolean rated;
	private int daysPerMove;
	private boolean userOfferedDraw;

	public GameOnlineItem() {
	}

	public GameOnlineItem(String[] values) {
		gameId = Long.valueOf(values[0].split("[+]")[1]);
		gameType = Integer.parseInt(values[1]);
		timestamp = Long.valueOf(values[2]);
		gameName =  values[3];
		whiteUsername = values[4].trim();
		blackUsername = values[5].trim();
		fenStartPosition =  values[6];
		moveList = values[7];
		whiteUserMove = values[8].equals(RestHelper.V_TRUE);
		whiteRating = Integer.parseInt(values[9]);
		blackRating = Integer.parseInt(values[10]);
		encodedMoveStr = values[11];
		hasNewMessage = values[12].equals(RestHelper.V_TRUE);
		secondsRemain = Long.parseLong(values[13]);
		rated = values[16].equals(RestHelper.V_TRUE);
		daysPerMove = Integer.parseInt(values[17]);
	}

	public String getEncodedMoveStr() {
		return encodedMoveStr;
	}

	public String getFenStartPosition() {
		return fenStartPosition;
	}

	public String getGameName() {
		return gameName;
	}

	public int getGameType() {
		return gameType;
	}

	public boolean isWhiteMove(){
		return whiteUserMove;
	}

	public boolean getRated() {
		return rated;
	}

	public int getDaysPerMove() {
		return daysPerMove;
	}

	public void setDaysPerMove(int daysPerMove) {
		this.daysPerMove = daysPerMove;
	}

	public void setEncodedMoveStr(String encodedMoveStr) {
		this.encodedMoveStr = encodedMoveStr;
	}

	public void setFenStartPosition(String fenStartPosition) {
		this.fenStartPosition = fenStartPosition;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public void setRated(boolean rated) {
		this.rated = rated;
	}

	public void setWhiteUserMove(boolean whiteUserMove) {
		this.whiteUserMove = whiteUserMove;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		writeBaseGameParcel(out);

		// own write
        out.writeByte((byte) (whiteUserMove ? 1 : 0));
        out.writeByte((byte) (userOfferedDraw ? 1 : 0));
        out.writeByte((byte) (rated ? 1 : 0));
		out.writeString(encodedMoveStr);
		out.writeString(gameName);
		out.writeInt(gameType);
		out.writeString(fenStartPosition);
		out.writeInt(daysPerMove);
	}


	public static final Parcelable.Creator<GameOnlineItem> CREATOR = new Parcelable.Creator<GameOnlineItem>() {
		@Override
		public GameOnlineItem createFromParcel(Parcel in) {
			return new GameOnlineItem(in);
		}

		@Override
		public GameOnlineItem[] newArray(int size) {
			return new GameOnlineItem[size];
		}
	};

	private GameOnlineItem(Parcel in) {
		readBaseGameParcel(in);
		// own read
        whiteUserMove = in.readByte() == 1;
        userOfferedDraw = in.readByte() == 1;
        rated = in.readByte() == 1;

		encodedMoveStr = in.readString();
		gameName = in.readString();
		gameType = in.readInt();
		fenStartPosition = in.readString();
		daysPerMove = in.readInt();
	}

	public void setUserOfferedDraw(boolean userOfferedDraw){
		this.userOfferedDraw = userOfferedDraw;
	}

	public boolean isUserOfferedDraw() {
		return userOfferedDraw;
	}
}
