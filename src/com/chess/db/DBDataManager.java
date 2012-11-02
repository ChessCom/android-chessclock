package com.chess.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.chess.model.GameListFinishedItem;
import com.chess.model.TacticItem;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBDataManager {
	private final static String TAG = "DBDataManager";

	private static final String ORDER_BY = "ORDER BY";
	private static final String GROUP_BY = "GROUP BY";
	public static final String OPERATOR_OR = " OR ";
	public static final String OPERATOR_LIKE = " LIKE ";
	public static final String OPERATOR_AND = " AND ";
	public static final String OPERATOR_MORE = " > ";
	public static final String OPERATOR_EQUALS = " = ";

	public static String SELECTION_ITEM_ID_AND_USER;
	public static String SELECTION_TACTIC_BATCH_USER;
	public static String SELECTION_ID;
	public static String SELECTION_GAME_ID;

	public static final String[] PROJECTION_ID = new String[] {
			DBConstants._ID
	};

	public static final String[] PROJECTION_TACTIC_ITEM_ID_AND_USER = new String[] {
			DBConstants._ID,
			DBConstants.V_TACTIC_ID,
			DBConstants.V_USER
	};

	public static final String[] PROJECTION_TACTIC_BATCH_USER = new String[] {
			DBConstants._ID,
			DBConstants.V_USER
	};

	public static final String[] PROJECTION_GAME_ID = new String[] {
			DBConstants._ID,
			DBConstants.V_GAME_ID
	};

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants._ID);
		selection.append("=?");
		SELECTION_ID = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants.V_TACTIC_ID);
		selection.append("=?");
		selection.append(OPERATOR_AND);
		selection.append(DBConstants.V_USER);
		selection.append("=?");
		SELECTION_ITEM_ID_AND_USER = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants.V_USER);
		selection.append("=?");
		SELECTION_TACTIC_BATCH_USER = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants.V_GAME_ID);
		selection.append("=?");
		SELECTION_GAME_ID = selection.toString();
	}

	public static ContentValues putTacticItemToValues(TacticItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_TACTIC_ID, dataObj.getId());
		values.put(DBConstants.V_FEN, dataObj.getFen());
		values.put(DBConstants.V_MOVE_LIST, dataObj.getMoveList());
		values.put(DBConstants.V_ATTEMPT_CNT, dataObj.getAttemptCnt());
		values.put(DBConstants.V_PASSED_CNT, dataObj.getPassedCnt());
		values.put(DBConstants.V_RATING, dataObj.getRating());
		values.put(DBConstants.V_AVG_SECONDS, dataObj.getAvgSeconds());

		return values;
	}


	public static TacticItem getTacticItemFromCursor(Cursor cursor) {
		TacticItem dataObj = new TacticItem();

		dataObj.setUser(getString(cursor, DBConstants.V_USER));
		dataObj.setId(getString(cursor, DBConstants.V_TACTIC_ID));
		dataObj.setFen(getString(cursor, DBConstants.V_FEN));
		dataObj.setMoveList(getString(cursor, DBConstants.V_MOVE_LIST));
		dataObj.setAttemptCnt(getString(cursor, DBConstants.V_ATTEMPT_CNT));
		dataObj.setPassedCnt(getString(cursor, DBConstants.V_PASSED_CNT));
		dataObj.setRating(getString(cursor, DBConstants.V_RATING));
		dataObj.setAvgSeconds(getString(cursor, DBConstants.V_AVG_SECONDS));

		return dataObj;
	}


	public static ContentValues putEchessFinishedGameToValues(GameListFinishedItem dataObj) {
		ContentValues values = new ContentValues();
/*
			+ V_GAME_ID 				    + _LONG_NOT_NULL + _COMMA
			+ V_COLOR 					    + _TEXT_NOT_NULL + _COMMA
			+ V_GAME_TYPE 				    + _TEXT_NOT_NULL + _COMMA
			+ V_USER_NAME_STR_LENGTH 	    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_NAME 			    + _TEXT_NOT_NULL + _COMMA
			+ V_OPPONENT_RATING 		    + _TEXT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING_AMOUNT 	    + _TEXT_NOT_NULL + _COMMA
			+ V_TIME_REMAINING_UNITS 	    + _TEXT_NOT_NULL + _COMMA
			+ V_FEN_STR_LENGTH 		        + _TEXT_NOT_NULL + _COMMA
			+ V_TIMESTAMP 				    + _LONG_NOT_NULL + _COMMA
			+ V_LAST_MOVE_FROM_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_LAST_MOVE_TO_SQUARE 	    + _TEXT_NOT_NULL + _COMMA
			+ V_IS_OPPONENT_ONLINE 	        + _INT_NOT_NULL + _COMMA
			+ V_GAME_RESULTS 			    + _TEXT_NOT_NULL + _CLOSE;
*/

		values.put(DBConstants.V_GAME_ID, dataObj.getGameId());
		values.put(DBConstants.V_COLOR, dataObj.getColor());
		values.put(DBConstants.V_GAME_TYPE, dataObj.getGameType());
		values.put(DBConstants.V_USER_NAME_STR_LENGTH, dataObj.getUsernameStringLength());
		values.put(DBConstants.V_OPPONENT_NAME, dataObj.getOpponentUsername());
		values.put(DBConstants.V_OPPONENT_RATING, dataObj.getOpponentRating());
		values.put(DBConstants.V_TIME_REMAINING_AMOUNT, dataObj.getTimeRemainingAmount());
		values.put(DBConstants.V_TIME_REMAINING_UNITS, dataObj.getTimeRemainingUnits());
		values.put(DBConstants.V_FEN_STR_LENGTH, dataObj.getFenStringLength());
		values.put(DBConstants.V_TIMESTAMP, dataObj.getTimestamp());
		values.put(DBConstants.V_LAST_MOVE_FROM_SQUARE, dataObj.getLastMoveFromSquare());
		values.put(DBConstants.V_LAST_MOVE_TO_SQUARE, dataObj.getLastMoveToSquare());
		values.put(DBConstants.V_IS_OPPONENT_ONLINE, dataObj.getIsOpponentOnline()? 1 : 0);
		values.put(DBConstants.V_GAME_RESULTS, dataObj.getGameResult());
		return values;
	}


	public static GameListFinishedItem getEchessFinishedGameFromCursor(Cursor cursor) {
		GameListFinishedItem dataObj = new GameListFinishedItem();

		dataObj.setGameId(getLong(cursor, DBConstants.V_GAME_ID));
		dataObj.setColor(getString(cursor, DBConstants.V_COLOR));
		dataObj.setGameType(getString(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setUserNameStrLength(getString(cursor, DBConstants.V_USER_NAME_STR_LENGTH));
		dataObj.setOpponentName(getString(cursor, DBConstants.V_OPPONENT_NAME));
		dataObj.setOpponentRating(getString(cursor, DBConstants.V_OPPONENT_RATING));
		dataObj.setTimeRemainingAmount(getString(cursor, DBConstants.V_TIME_REMAINING_AMOUNT));
		dataObj.setTimeRemainingUnits(getString(cursor, DBConstants.V_TIME_REMAINING_UNITS));
		dataObj.setFenStrLength(getString(cursor, DBConstants.V_FEN_STR_LENGTH));
		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
		dataObj.setLastMoveFromSquare(getString(cursor, DBConstants.V_LAST_MOVE_FROM_SQUARE));
		dataObj.setLastMoveToSquare(getString(cursor, DBConstants.V_LAST_MOVE_TO_SQUARE));
		dataObj.setOpponentOnline(getInt(cursor, DBConstants.V_IS_OPPONENT_ONLINE) > 0);
		dataObj.setGameResults(getString(cursor, DBConstants.V_GAME_RESULTS));
		return dataObj;
	}

	public static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}

	public static int getInt(Cursor cursor, String column) {
		return cursor.getInt(cursor.getColumnIndex(column));
	}

	public static long getLong(Cursor cursor, String column) {
		return cursor.getLong(cursor.getColumnIndex(column));
	}

	public static long getId(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndex(DBConstants._ID));
	}

	public static int getDbVersion() {
		return DBConstants.DATABASE_VERSION;
	}
}
