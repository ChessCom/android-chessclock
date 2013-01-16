package com.chess.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.chess.backend.entity.new_api.*;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.model.*;
import com.chess.ui.activities.GameOnlineScreenActivity;

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

	public static String[] sArguments1 = new String[1];
	public static String[] sArguments2 = new String[2];
	public static String[] sArguments3 = new String[3];

	public static String SELECTION_TACTIC_ID_AND_USER = concatArguments(
			DBConstants.V_TACTIC_ID,
			DBConstants.V_USER);

	public static String SELECTION_USER = concatArguments(DBConstants.V_USER);

	public static String SELECTION_ID = concatArguments(DBConstants._ID);

	public static String SELECTION_GAME_ID = concatArguments(
			DBConstants.V_USER,
			DBConstants.V_GAME_ID);

	public static String SELECTION_USER_OFFERED_DRAW = concatArguments(
			DBConstants.V_USER,
			DBConstants.V_GAME_ID,
			DBConstants.V_USER_OFFERED_DRAW);

	// -------------- PROJECTIONS DEFINITIONS ---------------------------

	public static final String[] PROJECTION_ID = new String[] {
			DBConstants._ID
	};

	public static final String[] PROJECTION_TACTIC_ITEM_ID_AND_USER = new String[] {
			DBConstants._ID,
			DBConstants.V_TACTIC_ID,
			DBConstants.V_USER
	};

	public static final String[] PROJECTION_USER = new String[] {
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
			DBConstants.V_TIMESTAMP,
			DBConstants.V_OPPONENT_OFFERED_DRAW,
			DBConstants.V_HAS_NEW_MESSAGE,
			DBConstants.V_TIME_REMAINING
	};

	public static final String[] PROJECTION_FINISHED_LIST_GAMES = new String[] {
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_GAME_ID,
			DBConstants.V_GAME_TYPE,
			DBConstants.V_GAME_RESULT,
			DBConstants.V_OPPONENT_NAME
	};

	public static final String[] PROJECTION_ECHESS_DRAW_OFFERED = new String[] {
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_GAME_ID,
			DBConstants.V_USER_OFFERED_DRAW
	};

	public static String concatArguments(String... arguments){
		StringBuilder selection = new StringBuilder();

		String separator = StaticData.SYMBOL_EMPTY;
		for (String argument : arguments) {
			selection.append(separator);
			separator = AND_;
			selection.append(argument);
			selection.append(EQUALS_ARG_);
		}
		return selection.toString();
	}

	/**
	 * Check if we have saved games for current user
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedOnlineCurrentGame(Context context) {
		String userName = getUserName(context);

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Cursor cursor = contentResolver.query(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI,
				PROJECTION_USER, SELECTION_USER, arguments1, null);
		boolean exist = cursor.moveToFirst();
		cursor.close();

		return exist;
	}

//	public static void updateOnlineGame(ContentResolver contentResolver, GameOnlineItem currentGame, String userName) {
	public static void updateOnlineGame(ContentResolver contentResolver, DailyGameByIdItem.Data currentGame, String userName) {

        final String[] arguments2 = sArguments2;
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

    public static String getUserName(Context context){
        final boolean userIsGuest = AppData.isGuest(context);

        String userName = AppData.GUEST_USER_NAME;
        if (!userIsGuest){
            userName = AppData.getUserName(context);
        }
        return userName;
    }

    /**
     * Check if we have saved games for current user
     * @param context
     * @return true if cursor can be positioned to first
     */
    public static boolean haveSavedTacticGame(Context context) {
        String userName = getUserName(context);

        ContentResolver contentResolver = context.getContentResolver();
        final String[] arguments1 = sArguments1;
        arguments1[0] = userName;

        Cursor cursor = contentResolver.query(DBConstants.TACTICS_BATCH_CONTENT_URI,
				PROJECTION_USER, SELECTION_USER, arguments1, null);
		boolean exist = cursor.moveToFirst();
		cursor.close();

        return exist;
    }

    public static int saveTacticItemToDb(Context context, TacticItem.Data tacticItem){
        String userName = getUserName(context);
        ContentResolver contentResolver = context.getContentResolver();

        final String[] arguments2 = sArguments2;
        arguments2[0] = String.valueOf(tacticItem.getId());
        arguments2[1] = userName;

        Uri uri = DBConstants.TACTICS_BATCH_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, PROJECTION_TACTIC_ITEM_ID_AND_USER,
                SELECTION_TACTIC_ID_AND_USER, arguments2, null);
        if (cursor.moveToFirst()) {
            contentResolver.update(Uri.parse(uri.toString() + SLASH_ + getId(cursor)),
                    putTacticItemToValues(tacticItem), null, null);
        } else {
            contentResolver.insert(uri, putTacticItemToValues(tacticItem));
        }

        cursor.close();

        if (tacticItem.getResultItem() != null) {
            saveTacticResultItemToDb(context, tacticItem.getResultItem());
        }

        return StaticData.RESULT_OK;
    }

    public static TacticItem.Data getLastTacticItemFromDb(Context context) {
        String userName = getUserName(context);
        ContentResolver contentResolver = context.getContentResolver();

        final String[] arguments1 = sArguments1;
        arguments1[0] = userName;

        Cursor cursor = contentResolver.query(DBConstants.TACTICS_BATCH_CONTENT_URI,
                null, SELECTION_USER, arguments1, null);

        cursor.moveToFirst();
        TacticItem.Data tacticItem = getTacticItemFromCursor(cursor);
		cursor.close();

        // set result item
		TacticRatingData resultItem = getTacticResultItemFromDb(context, tacticItem.getId());
        tacticItem.setResultItem(resultItem);

        return tacticItem;
    }

    private static void saveTacticResultItemToDb(Context context, TacticRatingData resultItem){
        ContentResolver contentResolver = context.getContentResolver();

        final String[] arguments2 = sArguments2;
        arguments2[0] = String.valueOf(resultItem.getId());
        arguments2[1] = resultItem.getUser();

        Uri uri = DBConstants.TACTICS_RESULTS_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,null, SELECTION_TACTIC_ID_AND_USER, arguments2, null);

        if (cursor.moveToFirst()) {
            contentResolver.update(Uri.parse(uri.toString() + SLASH_ + getId(cursor)),
                    putTacticResultItemToValues(resultItem), null, null);
        } else {
            contentResolver.insert(uri, putTacticResultItemToValues(resultItem));
        }

        cursor.close();
    }

    private static TacticRatingData getTacticResultItemFromDb(Context context, long id){
        String userName = getUserName(context);
        ContentResolver contentResolver = context.getContentResolver();

        final String[] arguments2 = sArguments2;
        arguments2[0] = String.valueOf(id);
        arguments2[1] = userName;
        Cursor cursor = contentResolver.query(DBConstants.TACTICS_RESULTS_CONTENT_URI,
                null, SELECTION_TACTIC_ID_AND_USER, arguments2, null);

        if (cursor.moveToFirst()){
			TacticRatingData resultItem = getTacticResultItemFromCursor(cursor);
            cursor.close();

            return resultItem;
        } else {
			cursor.close();
			return null;
		}
    }

    public static ContentValues putTacticItemToValues(TacticItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_TACTIC_ID, dataObj.getId());
		values.put(DBConstants.V_FEN, dataObj.getInitialFen());
		values.put(DBConstants.V_MOVE_LIST, dataObj.getCleanMoveString());
		values.put(DBConstants.V_ATTEMPT_CNT, dataObj.getAttemptCnt());
		values.put(DBConstants.V_PASSED_CNT, dataObj.getPassedCnt());
		values.put(DBConstants.V_RATING, dataObj.getRating());
		values.put(DBConstants.V_AVG_SECONDS, dataObj.getAvgSeconds());
		values.put(DBConstants.V_SECONDS_SPENT, dataObj.getSecondsSpent());
		values.put(DBConstants.V_STOP, dataObj.isStop()? 1 :0);
		values.put(DBConstants.V_WAS_SHOWED, dataObj.isWasShowed()? 1 :0);
		values.put(DBConstants.V_IS_RETRY, dataObj.isRetry()? 1 :0);

		return values;
	}


	public static TacticItem.Data getTacticItemFromCursor(Cursor cursor) {
		TacticItem.Data dataObj = new TacticItem.Data();

		dataObj.setUser(getString(cursor, DBConstants.V_USER));
		dataObj.setId(getLong(cursor, DBConstants.V_TACTIC_ID));
		dataObj.setFen(getString(cursor, DBConstants.V_FEN));
		dataObj.setMoveList(getString(cursor, DBConstants.V_MOVE_LIST));
		dataObj.setAttemptCnt(getInt(cursor, DBConstants.V_ATTEMPT_CNT));
		dataObj.setPassedCnt(getInt(cursor, DBConstants.V_PASSED_CNT));
		dataObj.setRating(getInt(cursor, DBConstants.V_RATING));
		dataObj.setAvgSeconds(getInt(cursor, DBConstants.V_AVG_SECONDS));
		dataObj.setSecondsSpent(getInt(cursor, DBConstants.V_SECONDS_SPENT));
		dataObj.setStop(getInt(cursor, DBConstants.V_STOP) > 0);
        dataObj.setWasShowed(getInt(cursor, DBConstants.V_WAS_SHOWED) > 0);
        dataObj.setRetry(getInt(cursor, DBConstants.V_IS_RETRY) > 0);

        return dataObj;
	}

    public static ContentValues putTacticResultItemToValues(TacticRatingData dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_TACTIC_ID, dataObj.getId());
		values.put(DBConstants.V_SCORE, dataObj.getScoreStr());
		values.put(DBConstants.V_USER_RATING_CHANGE, dataObj.getUserRatingChange());
		values.put(DBConstants.V_USER_RATING, dataObj.getUserRating());
		values.put(DBConstants.V_PROBLEM_RATING_CHANGE, dataObj.getProblemRatingChange());
		values.put(DBConstants.V_PROBLEM_RATING, dataObj.getProblemRating());

		return values;
	}


	public static TacticRatingData getTacticResultItemFromCursor(Cursor cursor) {
		TacticRatingData dataObj = new TacticRatingData();

		dataObj.setScore(getString(cursor, DBConstants.V_SCORE));
		dataObj.setUserRatingChange(getInt(cursor, DBConstants.V_USER_RATING_CHANGE));
		dataObj.setUserRating(getInt(cursor, DBConstants.V_USER_RATING));
		dataObj.setProblemRatingChange(getInt(cursor, DBConstants.V_PROBLEM_RATING_CHANGE));
		dataObj.setProblemRating(getInt(cursor, DBConstants.V_PROBLEM_RATING));

		return dataObj;
	}


	public static ContentValues putEchessFinishedListGameToValues(DailyFinishedGameData dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, userName);
		setValuesFromListGameItem(values, dataObj);
		values.put(DBConstants.V_GAME_RESULT, dataObj.getGameScore());
		return values;
	}


	public static GameListFinishedItem getEchessFinishedListGameFromCursor(Cursor cursor) {
		GameListFinishedItem dataObj = new GameListFinishedItem();

		/*

		if (getInt(cursor, DBConstants.V_GAME_TYPE) == BaseGameItem.CHESS_960) {
		if (getInt(cursor, DBConstants.V_GAME_RESULT) == BaseGameItem.GAME_WON) {
		, DBConstants.V_OPPONENT_NAME) +
		 */
//		setListGameItemFromCursor(dataObj, cursor);

		dataObj.setGameId(getLong(cursor, DBConstants.V_GAME_ID));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setOpponentName(getString(cursor, DBConstants.V_OPPONENT_NAME));
//		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
		dataObj.setGameResult(getInt(cursor, DBConstants.V_GAME_RESULT));

		return dataObj;
	}

	public static ContentValues putEchessGameListCurrentItemToValues(DailyCurrentGameData dataObj, String userName) {
		ContentValues values = new ContentValues();

		setValuesFromListGameItem(values, dataObj);
		values.put(DBConstants.V_USER, userName);
		values.put(DBConstants.V_OPPONENT_OFFERED_DRAW, dataObj.isDrawOfferPending()? 1 : 0);
		values.put(DBConstants.V_IS_MY_TURN, dataObj.isMyTurn()? 1 : 0);
		values.put(DBConstants.V_HAS_NEW_MESSAGE, dataObj.hasNewMessage()? 1 : 0);
//		Log.d("TEST", "echess values = " + values);
		return values;
	}


	public static DailyCurrentGameData getEchessGameListCurrentItemFromCursor(Cursor cursor) {
		DailyCurrentGameData dataObj = new DailyCurrentGameData();

		dataObj.setGameId(getLong(cursor, DBConstants.V_GAME_ID));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setOpponentUsername(getString(cursor, DBConstants.V_OPPONENT_NAME));
		dataObj.setTimeRemaining(getInt(cursor, DBConstants.V_TIME_REMAINING));
		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));

//		setListGameItemFromCursor(dataObj, cursor);
		dataObj.setDrawOfferPending(getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0);
		dataObj.setMyTurn(getInt(cursor, DBConstants.V_IS_MY_TURN) > 0);
		dataObj.setHasNewMessage(getInt(cursor, DBConstants.V_HAS_NEW_MESSAGE) > 0);

		return dataObj;
	}

//	private static void setListGameItemFromCursor(BaseGameOnlineItem dataObj, Cursor cursor){
//		dataObj.setGameId(getLong(cursor, DBConstants.V_GAME_ID));
//		dataObj.setMyColor(getInt(cursor, DBConstants.V_COLOR));
//		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
//		dataObj.setUserNameStrLength(getInt(cursor, DBConstants.V_USER_NAME_STR_LENGTH));
//		dataObj.setOpponentName(getString(cursor, DBConstants.V_OPPONENT_NAME));
//		dataObj.setOpponentRating(getInt(cursor, DBConstants.V_OPPONENT_RATING));
//		dataObj.setTimeRemainingAmount(getInt(cursor, DBConstants.V_TIME_REMAINING));
//		dataObj.setTimeRemainingUnits(getString(cursor, DBConstants.V_TIME_REMAINING_UNITS));
//		dataObj.setFenStrLength(getInt(cursor, DBConstants.V_FEN_STR_LENGTH));
//		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
//		dataObj.setLastMoveFromSquare(getString(cursor, DBConstants.V_LAST_MOVE_FROM_SQUARE));
//		dataObj.setLastMoveToSquare(getString(cursor, DBConstants.V_LAST_MOVE_TO_SQUARE));
//		dataObj.setOpponentOnline(getInt(cursor, DBConstants.V_IS_OPPONENT_ONLINE) > 0);
//	}

	private static void setValuesFromListGameItem(ContentValues values, DailyGameBaseData dataObj){
		values.put(DBConstants.V_GAME_ID, dataObj.getGameId());
		values.put(DBConstants.V_COLOR, dataObj.getMyColor());
		values.put(DBConstants.V_GAME_TYPE, dataObj.getGameType());
		values.put(DBConstants.V_OPPONENT_NAME, dataObj.getOpponentUsername());
		values.put(DBConstants.V_OPPONENT_RATING, dataObj.getOpponentRating());
		values.put(DBConstants.V_TIME_REMAINING, dataObj.getTimeRemaining());
		values.put(DBConstants.V_TIMESTAMP, dataObj.getTimestamp());
		String moveTo = dataObj.getLastMoveToSquare();
		String moveFrom = dataObj.getLastMoveFromSquare();
		values.put(DBConstants.V_LAST_MOVE_FROM_SQUARE, moveFrom == null? StaticData.SYMBOL_EMPTY: moveFrom);
		values.put(DBConstants.V_LAST_MOVE_TO_SQUARE, moveTo == null? StaticData.SYMBOL_EMPTY: moveTo);
	}

	public static ContentValues putGameOnlineItemToValues(DailyGameByIdItem.Data dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_FINISHED, 0);
		values.put(DBConstants.V_USER, userName);
		setValuesFromOnlineGame(values, dataObj);
		values.put(DBConstants.V_USER_OFFERED_DRAW, dataObj.isDrawOffered());

		return values;
	}

	private static void setValuesFromOnlineGame(ContentValues values, DailyGameByIdItem.Data dataObj){
		values.put(DBConstants.V_GAME_ID, dataObj.getGameId());
		values.put(DBConstants.V_GAME_TYPE, dataObj.getGameType());
		values.put(DBConstants.V_TIMESTAMP, dataObj.getTimestamp());
		values.put(DBConstants.V_GAME_NAME, dataObj.getGameName());
		values.put(DBConstants.V_WHITE_USER_NAME, dataObj.getWhiteUsername());
		values.put(DBConstants.V_BLACK_USER_NAME, dataObj.getBlackUsername());
		String feStartPosition = dataObj.getFenStartPosition();
		values.put(DBConstants.V_FEN_START_POSITION, feStartPosition == null? "" : feStartPosition);
		values.put(DBConstants.V_MOVE_LIST, dataObj.getMoveList());
		values.put(DBConstants.V_WHITE_USER_MOVE, dataObj.isWhiteMove()? 1: 0);
		values.put(DBConstants.V_WHITE_RATING, dataObj.getWhiteRating());
		values.put(DBConstants.V_BLACK_RATING, dataObj.getBlackRating());
		values.put(DBConstants.V_ENCODED_MOVE_STR, dataObj.getEncodedMoveString());
		values.put(DBConstants.V_HAS_NEW_MESSAGE, dataObj.hasNewMessage()? 1: 0);
		values.put(DBConstants.V_SECONDS_REMAIN, dataObj.getSecondsRemain());
		values.put(DBConstants.V_RATED, dataObj.isRated()? 1: 0);
		values.put(DBConstants.V_DAYS_PER_MOVE, dataObj.getDaysPerMove());
	}

	private static void setOnlineGameFromCursor(DailyGameByIdItem.Data dataObj, Cursor cursor){
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
		dataObj.setEncodedMoveString(getString(cursor, DBConstants.V_ENCODED_MOVE_STR));
		dataObj.setHasNewMessage(getInt(cursor, DBConstants.V_HAS_NEW_MESSAGE) > 0);
		dataObj.setSecondsRemain(getLong(cursor, DBConstants.V_SECONDS_REMAIN));
		dataObj.setRated(getInt(cursor, DBConstants.V_RATED) > 0);
		dataObj.setDaysPerMove(getInt(cursor, DBConstants.V_DAYS_PER_MOVE));
	}

	public static DailyGameByIdItem.Data getGameOnlineItemFromCursor(Cursor cursor) {
		DailyGameByIdItem.Data dataObj = new DailyGameByIdItem.Data();
		setOnlineGameFromCursor(dataObj, cursor);
		dataObj.setDrawOffered(getInt(cursor, DBConstants.V_USER_OFFERED_DRAW)/* > 0*/);

		return dataObj;
	}

	public static ContentValues putGameFinishedItemToValues(DailyGameByIdItem.Data dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_FINISHED, 1);
		values.put(DBConstants.V_USER, userName);
		setValuesFromOnlineGame(values, dataObj);

		return values;
	}

	public static DailyGameByIdItem.Data getGameFinishedItemFromCursor(Cursor cursor) {
		DailyGameByIdItem.Data dataObj = new DailyGameByIdItem.Data();

		setOnlineGameFromCursor(dataObj, cursor);

		return dataObj;
	}

	public static boolean checkIfDrawOffered(ContentResolver resolver, String userName, long gameId) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = userName;
		arguments2[1] = String.valueOf(gameId);

		Cursor cursor = resolver.query(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI,
				PROJECTION_ECHESS_DRAW_OFFERED, SELECTION_USER_OFFERED_DRAW, arguments2, null);
		return cursor.moveToFirst() && getInt(cursor, DBConstants.V_USER_OFFERED_DRAW) > 0;
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
