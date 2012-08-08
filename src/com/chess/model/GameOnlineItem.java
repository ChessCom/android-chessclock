package com.chess.model;

/**
 * @author Alexey Schekin (schekin@azoft.com)
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
	protected String gameType;
	protected String fenStartPosition;

	private String rated;
	private String daysPerMove;

	public GameOnlineItem(String[] values) {
		gameId = Long.valueOf(values[0].split("[+]")[1]);
		gameType = values[1];
		timestamp = Long.valueOf(values[2]);
		gameName =  values[3];
		whiteUsername = values[4].trim();
		blackUsername = values[5].trim();
		fenStartPosition =  values[6];
		moveList = values[7];
		whiteUserMove = values[8].equals("1");
		whiteRating = values[9];
		blackRating = values[10];
		encodedMoveStr = values[11];
		hasNewMessage = values[12].equals("1");
		secondsRemain = values[13];
		rated = values[16];
		daysPerMove = values[17];
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

	public String getGameType() {
		return gameType;
	}

	public boolean isWhiteMove(){
		return whiteUserMove;
	}

	public String getRated() {
		return rated;
	}

	public String getDaysPerMove() {
		return daysPerMove;
	}
}
