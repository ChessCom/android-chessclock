package com.chess.model;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 31.07.12
 * @modified 31.07.12
 */
public class GameOnlineItem extends BaseGameItem {

	public static final int STARTING_FEN_POSITION_NUMB = 6;
	public static final int MOVE_LIST_NUMB = 7;


	private String userToMove;
	private String encodedMoveStr;

	protected String gameName;
	protected String gameType;
	protected String fenStartPosition;
	protected boolean hasNewMessage;

	public GameOnlineItem(String[] values) {
		gameId = Long.valueOf(values[0].split("[+]")[1]);
		gameType = values[1];
		timestamp = Long.valueOf(values[2]);
		gameName =  values[3];
		whiteUserName = values[4].trim();
		blackUserName = values[5].trim();
		fenStartPosition =  values[STARTING_FEN_POSITION_NUMB];
		moveList = values[MOVE_LIST_NUMB];
		userToMove = values[8];
		whiteRating = values[9];
		blackRating = values[10];
		encodedMoveStr = values[11];
		hasNewMessage = values[12].equals("1");
		secondsRemain = values[13];
	}
}
