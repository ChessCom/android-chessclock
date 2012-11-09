package com.chess.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.model.*;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBDataManager {
	private final static String TAG = "DBDataManager";

	private static final String ORDER_BY = "ORDER BY";
	private static final String GROUP_BY = "GROUP BY";
	public static final String SLASH_ = "/";
	public static final String OR_ = " OR ";
	public static final String LIKE_ = " LIKE ";
	public static final String AND_ = " AND ";
	public static final String MORE_ = " > ";
	public static final String EQUALS_ = " = ";
	public static final String EQUALS_ARG_ = "=?";

	public static String[] arguments1 = new String[1];
	public static String[] arguments2 = new String[2];
	public static String[] arguments3 = new String[3];

	public static String SELECTION_TACTIC_ID_AND_USER;
	public static String SELECTION_TACTIC_BATCH_USER;
	public static String SELECTION_ID;
	public static String SELECTION_GAME_ID;
	public static String SELECTION_USER_OFFERED_DRAW;
	public static String SELECTION_USER;

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
			DBConstants.V_USER,
			DBConstants.V_GAME_ID
	};

	public static final String[] PROJECTION_CURRENT_LIST_GAMES = new String[] {
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_GAME_ID,
			DBConstants.V_OPPONENT_NAME,
			DBConstants.V_GAME_TYPE,
			DBConstants.V_IS_MY_TURN,
			DBConstants.V_OPPONENT_OFFERED_DRAW,
			DBConstants.V_TIME_REMAINING_UNITS,
			DBConstants.V_TIME_REMAINING_AMOUNT
	};

	public static final String[] PROJECTION_FINISHED_LIST_GAMES = new String[] {
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_GAME_ID,
			DBConstants.V_GAME_TYPE,
			DBConstants.V_GAME_RESULTS,
			DBConstants.V_OPPONENT_NAME
	};

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants._ID).append(EQUALS_ARG_);
		SELECTION_ID = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		concatArguments(selection,
				DBConstants.V_TACTIC_ID,
				DBConstants.V_USER);
//		selection.append(DBConstants.V_TACTIC_ID);
//		selection.append(EQUALS_ARG_);
//		selection.append(AND_);
//		selection.append(DBConstants.V_USER);
//		selection.append(EQUALS_ARG_);
		SELECTION_TACTIC_ID_AND_USER = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants.V_USER).append(EQUALS_ARG_);
		SELECTION_TACTIC_BATCH_USER = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		concatArguments(selection,
				DBConstants.V_USER,
				DBConstants.V_GAME_ID);
//		selection.append(DBConstants.V_USER);
//		selection.append(EQUALS_ARG_);
//		selection.append(AND_);
//		selection.append(DBConstants.V_GAME_ID);
//		selection.append(EQUALS_ARG_);
		SELECTION_GAME_ID = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		concatArguments(selection,
				DBConstants.V_USER,
				DBConstants.V_GAME_ID,
				DBConstants.V_USER_OFFERED_DRAW);
//		selection.append(DBConstants.V_USER);
//		selection.append(EQUALS_ARG_);
//		selection.append(AND_);
//		selection.append(DBConstants.V_GAME_ID);
//		selection.append(EQUALS_ARG_);
//		selection.append(AND_);
//		selection.append(DBConstants.V_USER_OFFERED_DRAW);
//		selection.append(EQUALS_ARG_);
		SELECTION_USER_OFFERED_DRAW = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
/*
		DBConstants.V_USER,
		DBConstants.V_GAME_ID,
		DBConstants.V_OPPONENT_NAME,
		DBConstants.,
		DBConstants.,
		DBConstants.,
		DBConstants.,
		DBConstants.
*/
		selection.append(DBConstants.V_USER).append(EQUALS_ARG_);

		SELECTION_USER = selection.toString();
	}

	public static void concatArguments(StringBuilder selection, String... arguments){
		String separator = StaticData.SYMBOL_EMPTY;
		for (String argument : arguments) {
			selection.append(separator);
			separator = AND_;
			selection.append(argument);
			selection.append(EQUALS_ARG_);
		}
	}

	public static void updateOnlineGame(Context context, GameOnlineItem currentGame) {
		String userName = AppData.getUserName(context);
		ContentResolver contentResolver = context.getContentResolver();

		arguments2[0] = userName;
		arguments2[1] = String.valueOf(currentGame.getGameId());

		Cursor cursor = contentResolver.query(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI, PROJECTION_GAME_ID, SELECTION_GAME_ID,
				arguments2, null);

		if (cursor.moveToFirst()) {
			contentResolver.update(Uri.parse(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI.toString() + SLASH_ + DBDataManager.getId(cursor)),
					putGameOnlineItemToValues(currentGame, userName), null, null);
		} else {
			contentResolver.insert(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI, putGameOnlineItemToValues(currentGame, userName));
		}

		cursor.close();
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
		dataObj.setId(getLong(cursor, DBConstants.V_TACTIC_ID));
		dataObj.setFen(getString(cursor, DBConstants.V_FEN));
		dataObj.setMoveList(getString(cursor, DBConstants.V_MOVE_LIST));
		dataObj.setAttemptCnt(getInt(cursor, DBConstants.V_ATTEMPT_CNT));
		dataObj.setPassedCnt(getInt(cursor, DBConstants.V_PASSED_CNT));
		dataObj.setRating(getInt(cursor, DBConstants.V_RATING));
		dataObj.setAvgSeconds(getInt(cursor, DBConstants.V_AVG_SECONDS));

		return dataObj;
	}


	public static ContentValues putEchessFinishedListGameToValues(GameListFinishedItem dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, userName);
		fillValuesFromListGameItem(values, dataObj);
		values.put(DBConstants.V_GAME_RESULTS, dataObj.getGameResult());
		return values;
	}


	public static GameListFinishedItem getEchessFinishedListGameFromCursor(Cursor cursor) {
		GameListFinishedItem dataObj = new GameListFinishedItem();

		fillListGameItemFromCursor(dataObj, cursor);
		dataObj.setGameResults(getInt(cursor, DBConstants.V_GAME_RESULTS));
		return dataObj;
	}

	public static ContentValues putEchessGameListCurrentItemToValues(GameListCurrentItem dataObj, String userName) {
		ContentValues values = new ContentValues();

		fillValuesFromListGameItem(values, dataObj);
		values.put(DBConstants.V_USER, userName);
		return values;
	}


	public static GameListCurrentItem getEchessGameListCurrentItemFromCursor(Cursor cursor) {
		GameListCurrentItem dataObj = new GameListCurrentItem();
		fillListGameItemFromCursor(dataObj, cursor);
		dataObj.setDrawOfferPending(getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0);

		return dataObj;
	}

	private static void fillListGameItemFromCursor(BaseGameOnlineItem dataObj, Cursor cursor){
		dataObj.setGameId(getLong(cursor, DBConstants.V_GAME_ID));
		dataObj.setColor(getInt(cursor, DBConstants.V_COLOR));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setUserNameStrLength(getInt(cursor, DBConstants.V_USER_NAME_STR_LENGTH));
		dataObj.setOpponentName(getString(cursor, DBConstants.V_OPPONENT_NAME));
		dataObj.setOpponentRating(getInt(cursor, DBConstants.V_OPPONENT_RATING));
		dataObj.setTimeRemainingAmount(getInt(cursor, DBConstants.V_TIME_REMAINING_AMOUNT));
		dataObj.setTimeRemainingUnits(getString(cursor, DBConstants.V_TIME_REMAINING_UNITS));
		dataObj.setFenStrLength(getInt(cursor, DBConstants.V_FEN_STR_LENGTH));
		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
		dataObj.setLastMoveFromSquare(getString(cursor, DBConstants.V_LAST_MOVE_FROM_SQUARE));
		dataObj.setLastMoveToSquare(getString(cursor, DBConstants.V_LAST_MOVE_TO_SQUARE));
		dataObj.setOpponentOnline(getInt(cursor, DBConstants.V_IS_OPPONENT_ONLINE) > 0);
	}

	private static void fillValuesFromListGameItem(ContentValues values, BaseGameOnlineItem dataObj){
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
	}

	public static ContentValues putGameOnlineItemToValues(GameOnlineItem dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_FINISHED, 0);
		values.put(DBConstants.V_USER, userName);
		fillValuesFromOnlineGame(values, dataObj);
		values.put(DBConstants.V_USER_OFFERED_DRAW, dataObj.isUserOfferedDraw());

		return values;
	}

	private static void fillValuesFromOnlineGame(ContentValues values, GameOnlineItem dataObj){
		values.put(DBConstants.V_GAME_ID, dataObj.getGameId());
		values.put(DBConstants.V_GAME_TYPE, dataObj.getGameType());
		values.put(DBConstants.V_TIMESTAMP, dataObj.getTimestamp());
		values.put(DBConstants.V_GAME_NAME, dataObj.getGameName());
		values.put(DBConstants.V_WHITE_USER_NAME, dataObj.getWhiteUsername());
		values.put(DBConstants.V_BLACK_USER_NAME, dataObj.getBlackUsername());
		values.put(DBConstants.V_FEN_START_POSITION, dataObj.getFenStartPosition());
		values.put(DBConstants.V_WHITE_USER_MOVE, dataObj.isWhiteMove()? 1: 0);
		values.put(DBConstants.V_WHITE_RATING, dataObj.getWhiteRating());
		values.put(DBConstants.V_BLACK_RATING, dataObj.getBlackRating());
		values.put(DBConstants.V_ENCODED_MOVE_STR, dataObj.getEncodedMoveStr());
		values.put(DBConstants.V_HAS_NEW_MESSAGE, dataObj.hasNewMessage()? 1: 0);
		values.put(DBConstants.V_SECONDS_REMAIN, dataObj.getSecondsRemain());
		values.put(DBConstants.V_RATED, dataObj.getRated()? 1: 0);
		values.put(DBConstants.V_DAYS_PER_MOVE, dataObj.getDaysPerMove());
	}

	private static void fillOnlineGameFromCursor(GameOnlineItem dataObj, Cursor cursor){
		dataObj.setGameId(getLong(cursor, DBConstants.V_GAME_ID));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
		dataObj.setGameName(getString(cursor, DBConstants.V_GAME_NAME));
		dataObj.setWhiteUsername(getString(cursor, DBConstants.V_WHITE_USER_NAME));
		dataObj.setBlackUsername(getString(cursor, DBConstants.V_BLACK_USER_NAME));
		dataObj.setFenStartPosition(getString(cursor, DBConstants.V_FEN_START_POSITION));
		dataObj.setMoveList(getString(cursor, DBConstants.V_MOVE_LIST));
		dataObj.setWhiteUserMove(getInt(cursor, DBConstants.V_WHITE_USER_MOVE) > 0);
		dataObj.setWhiteRating(getInt(cursor, DBConstants.V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, DBConstants.V_BLACK_RATING));
		dataObj.setEncodedMoveStr(getString(cursor, DBConstants.V_ENCODED_MOVE_STR));
		dataObj.setHasNewMessage(getInt(cursor, DBConstants.V_HAS_NEW_MESSAGE) > 0);
		dataObj.setSecondsRemain(getLong(cursor, DBConstants.V_SECONDS_REMAIN));
		dataObj.setRated(getInt(cursor, DBConstants.V_RATED) > 0);
		dataObj.setDaysPerMove(getInt(cursor, DBConstants.V_DAYS_PER_MOVE));
	}

	public static GameOnlineItem getGameOnlineItemFromCursor(Cursor cursor) {
		GameOnlineItem dataObj = new GameOnlineItem();
		fillOnlineGameFromCursor(dataObj, cursor);
		dataObj.setUserOfferedDraw(getInt(cursor, DBConstants.V_USER_OFFERED_DRAW) > 0);

		return dataObj;
	}

	public static ContentValues putGameFinishedItemToValues(GameOnlineItem dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_FINISHED, 1);
		values.put(DBConstants.V_USER, userName);
		fillValuesFromOnlineGame(values, dataObj);

		return values;
	}

	public static GameOnlineItem getGameFinishedItemFromCursor(Cursor cursor) {
		GameOnlineItem dataObj = new GameOnlineItem();

		fillOnlineGameFromCursor(dataObj, cursor);

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
