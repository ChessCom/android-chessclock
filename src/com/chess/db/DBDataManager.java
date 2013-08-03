package com.chess.db;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.new_api.*;
import com.chess.backend.entity.new_api.stats.*;
import com.chess.backend.statics.StaticData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBDataManager {
	// TODO improve performance by updating only needed fields

	private static final String ORDER_BY = "ORDER BY";
	private static final String GROUP_BY = "GROUP BY";
	public static final String ASCEND = " ASC";
	public static final String DESCEND = " DESC";
	//	public static final String SLASH_ = "/";
	public static final String OR_ = " OR ";
	public static final String LIKE_ = " LIKE ";
	public static final String AND_ = " AND ";
	public static final String MORE_ = " > ";
	public static final String EQUALS_ = " = ";
	public static final String EQUALS_ARG_ = "=?";
	public static final String NOT_EQUALS_ARG_ = "!=?";
	public static final String LIMIT_ = " LIMIT ";
	public static final String LIMIT_1 = DBConstants._ID + " LIMIT 1";

	public static String[] sArguments1 = new String[1];
	public static String[] sArguments2 = new String[2];
	public static String[] sArguments3 = new String[3];

	// -------------- SELECTION DEFINITIONS ---------------------------

	public static String SELECTION_ITEM_ID_AND_USER = concatArguments(
			DBConstants.V_ID,
			DBConstants.V_USER);

	public static String SELECTION_USER = concatArguments(DBConstants.V_USER);

	public static String SELECTION_ID = concatArguments(DBConstants._ID);

	public static String SELECTION_USER_AND_ID = concatArguments(   // TODO remove duplicate
			DBConstants.V_USER,
			DBConstants.V_ID);

	public static String SELECTION_USER_ID = concatArguments(
			DBConstants.V_USER,
			DBConstants.V_USER_ID);

	public static String SELECTION_OPPONENT_OFFERED_DRAW = concatArguments(
			DBConstants.V_USER,
			DBConstants.V_ID,
			DBConstants.V_OPPONENT_OFFERED_DRAW);

	public static String SELECTION_USER_TURN = concatArguments(
			DBConstants.V_USER,
			DBConstants.V_IS_MY_TURN);

	public static String SELECTION_TITLE = concatArguments(DBConstants.V_TITLE);

	public static String SELECTION_CATEGORY_ID = concatArguments(DBConstants.V_CATEGORY_ID);

	public static String SELECTION_CATEGORY_ID_AND_PAGE = concatArguments(DBConstants.V_CATEGORY_ID, DBConstants.V_PAGE);

	public static String SELECTION_CATEGORY = concatArguments(DBConstants.V_CATEGORY);

	public static String SELECTION_ITEM_ID = concatArguments(DBConstants.V_ID);

	public static String SELECTION_ITEM_ID_AND_PAGE = concatArguments(DBConstants.V_ID, DBConstants.V_PAGE);

	public static String SELECTION_ITEM_ID_AND_NUMBER = concatArguments(DBConstants.V_ID, DBConstants.V_NUMBER);

	public static String SELECTION_ITEM_ID_AND_CURRENT_POSITION = concatArguments(
			DBConstants.V_ID,
			DBConstants.V_CURRENT_POSITION);

	public static String SELECTION_ITEM_ID_POSITION_NUMBER = concatArguments(
			DBConstants.V_ID,
			DBConstants.V_CURRENT_POSITION,
			DBConstants.V_NUMBER);

	public static String SELECTION_ITEM_ID_AND_CATEGORY_ID = concatArguments(DBConstants.V_ID, DBConstants.V_CATEGORY_ID);

	public static String SELECTION_CREATE_DATE = concatArguments(DBConstants.V_CREATE_DATE);

	public static String SELECTION_ID_USER_CONVERSATION_ID = concatArguments(
			DBConstants.V_ID,
			DBConstants.V_USER,
			DBConstants.V_CONVERSATION_ID
	);

	public static String SELECTION_USER_CONVERSATION_ID = concatArguments(
			DBConstants.V_USER,
			DBConstants.V_CONVERSATION_ID
	);

	// -------------- PROJECTIONS DEFINITIONS ---------------------------

	public static final String[] PROJECTION_ITEM_ID_AND_USER = new String[]{
			DBConstants._ID,
			DBConstants.V_ID,
			DBConstants.V_USER
	};

	public static final String[] PROJECTION_USER = new String[]{
			DBConstants._ID,
			DBConstants.V_USER
	};

	public static final String[] PROJECTION_GAME_ID = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_ID
	};

	public static final String[] PROJECTION_USER_ID = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_USER_ID
	};

	public static final String[] PROJECTION_CURRENT_GAMES = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_ID,
			DBConstants.V_I_PLAY_AS,
			DBConstants.V_WHITE_USERNAME,
			DBConstants.V_BLACK_USERNAME,
			DBConstants.V_WHITE_AVATAR,
			DBConstants.V_BLACK_AVATAR,
			DBConstants.V_WHITE_PREMIUM_STATUS,
			DBConstants.V_BLACK_PREMIUM_STATUS,
			DBConstants.V_GAME_TYPE,
			DBConstants.V_IS_MY_TURN,
			DBConstants.V_TIMESTAMP,
			DBConstants.V_OPPONENT_OFFERED_DRAW,
			DBConstants.V_HAS_NEW_MESSAGE,
			DBConstants.V_TIME_REMAINING
	};

	public static final String[] PROJECTION_FINISHED_GAMES = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_ID,
			DBConstants.V_I_PLAY_AS,
			DBConstants.V_GAME_TYPE,
			DBConstants.V_GAME_SCORE,
			DBConstants.V_WHITE_USERNAME,
			DBConstants.V_BLACK_USERNAME,
			DBConstants.V_WHITE_AVATAR,
			DBConstants.V_BLACK_AVATAR,
			DBConstants.V_WHITE_RATING,
			DBConstants.V_BLACK_RATING,
			DBConstants.V_WHITE_PREMIUM_STATUS,
			DBConstants.V_BLACK_PREMIUM_STATUS
	};

	public static final String[] PROJECTION_DAILY_PLAYER_NAMES = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_I_PLAY_AS,
			DBConstants.V_WHITE_USERNAME,
			DBConstants.V_BLACK_USERNAME
	};

	public static final String[] PROJECTION_DAILY_DRAW_OFFERED = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_ID,
			DBConstants.V_OPPONENT_OFFERED_DRAW
	};

	public static final String[] PROJECTION_TITLE = new String[]{
			DBConstants._ID,
			DBConstants.V_TITLE
	};

	public static final String[] PROJECTION_USERNAME = new String[]{
			DBConstants._ID,
			DBConstants.V_USERNAME
	};

	public static final String[] PROJECTION_V_CATEGORY_ID = new String[]{
			DBConstants._ID,
			DBConstants.V_CATEGORY_ID
	};

	public static final String[] PROJECTION_ITEM_ID = new String[]{
			DBConstants._ID,
			DBConstants.V_ID
	};

	public static final String[] PROJECTION_CREATE_DATE = new String[]{
			DBConstants._ID,
			DBConstants.V_CREATE_DATE
	};

	public static final String[] PROJECTION_USER_CURRENT_RATING = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_CURRENT
	};

	public static final String[] PROJECTION_VIEWED_VIDEO = new String[]{
			DBConstants._ID,
			DBConstants.V_USER,
			DBConstants.V_ID,
			DBConstants.V_VIDEO_VIEWED
	};

	public static final String[] PROJECTION_ITEM_ID_AND_NUMBER = new String[]{
			DBConstants._ID,
			DBConstants.V_ID,
			DBConstants.V_NUMBER
	};

	public static final String[] PROJECTION_ITEM_ID_POSITION_NUMBER = new String[]{
			DBConstants._ID,
			DBConstants.V_ID,
			DBConstants.V_CURRENT_POSITION,
			DBConstants.V_NUMBER
	};

	public static final String[] PROJECTION_ITEM_ID_AND_CATEGORY_ID = new String[]{
			DBConstants._ID,
			DBConstants.V_ID,
			DBConstants.V_CATEGORY_ID
	};

	public static final String[] PROJECTION_ID_USER_CONVERSATION_ID = new String[]{
			DBConstants._ID,
			DBConstants.V_ID,
			DBConstants.V_USER,
			DBConstants.V_CONVERSATION_ID
	};


	public static String concatArguments(String... arguments) {
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

	public static Cursor executeQuery(ContentResolver contentResolver, QueryParams params) {
		return contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(),
				params.getArguments(), params.getOrder());
	}

	/**
	 * Check if we have saved games for current user
	 *
	 * @param context  to get resources
	 * @param userName
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedDailyGame(Context context, String userName) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()],
				PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
		boolean exist = cursor != null && cursor.moveToFirst();

		if (!exist) { // check finished games list
			cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.DAILY_FINISHED_GAMES.ordinal()],
					PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
			exist = cursor != null && cursor.moveToFirst();
		}
		if (cursor != null) {
			cursor.close();
		}

		return exist;
	}

	public static void updateDailyGame(ContentResolver contentResolver, DailyCurrentGameData currentGame, String userName) {

		final String[] arguments2 = sArguments2;
		arguments2[0] = userName;
		arguments2[1] = String.valueOf(currentGame.getGameId());

		Uri uri = DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER_AND_ID,
				arguments2, null);
		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)),
					putDailyGameCurrentItemToValues(currentGame, userName), null, null);
		} else {
			contentResolver.insert(uri, putDailyGameCurrentItemToValues(currentGame, userName));
		}

		cursor.close();
	}

	/**
	 * @return true if still have current games
	 */
	public static boolean checkAndDeleteNonExistCurrentGames(Context context, List<DailyCurrentGameData> gamesList, String userName) {
		// compare to current list games

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Uri uri = DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()];
		long[] gamesIds;
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER, arguments1, null);
		if (cursor != null && cursor.moveToFirst()) {
			gamesIds = new long[cursor.getCount()];
			int i = 0;
			do {
				gamesIds[i++] = getLong(cursor, DBConstants.V_ID);
			} while (cursor.moveToNext());
		} else if (gamesList.size() == 0) { // means no current games for that user
			arguments1[0] = userName;
			contentResolver.delete(uri, SELECTION_USER, arguments1);

			return false;
		} else { // current games exist, but not saved yet
			return true;
		}

		List<Long> idsToRemove = new ArrayList<Long>();
		for (long gamesId : gamesIds) {
			boolean found = false;
			for (DailyCurrentGameData listCurrentItem : gamesList) {
				if (listCurrentItem.getGameId() == gamesId) {
					found = true;
					break;
				}
			}
			if (!found) {
				idsToRemove.add(gamesId);
			}
		}

		if (idsToRemove.size() > 0) {
			for (Long id : idsToRemove) {
				final String[] arguments2 = sArguments2;
				arguments2[0] = userName;
				arguments2[1] = String.valueOf(id);
				contentResolver.delete(uri, SELECTION_USER_AND_ID, arguments2);
			}
		}

		return gamesIds.length > idsToRemove.size();
	}

	public static boolean checkAndDeleteNonExistFinishedGames(Context context, List<DailyFinishedGameData> gamesList, String userName) {
		// compare to current list games

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Uri uri = DBConstants.uriArray[DBConstants.Tables.DAILY_FINISHED_GAMES.ordinal()];
		long[] gamesIds;
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER, arguments1, null);
		if (cursor != null && cursor.moveToFirst()) {
			gamesIds = new long[cursor.getCount()];
			int i = 0;
			do {
				gamesIds[i++] = getLong(cursor, DBConstants.V_ID);
			} while (cursor.moveToNext());
		} else if (gamesList.size() == 0) { // means no finished games for that user
			arguments1[0] = userName;
			contentResolver.delete(uri, SELECTION_USER, arguments1);

			return false;
		} else { // finished games exist, but not saved yet
			return true;
		}

		List<Long> idsToRemove = new ArrayList<Long>();
		for (long gamesId : gamesIds) {
			boolean found = false;
			for (DailyFinishedGameData listCurrentItem : gamesList) {
				if (listCurrentItem.getGameId() == gamesId) {
					found = true;
					break;
				}
			}
			if (!found) {
				idsToRemove.add(gamesId);
			}
		}

		if (idsToRemove.size() > 0) {
			for (Long id : idsToRemove) {
				final String[] arguments2 = sArguments2;
				arguments2[0] = userName;
				arguments2[1] = String.valueOf(id);
				contentResolver.delete(uri, SELECTION_USER_AND_ID, arguments2);
			}
		}

		return gamesIds.length > idsToRemove.size();
	}

	public static Cursor getRecentOpponentsCursor(Context context, String userName) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments3 = sArguments3;
		arguments3[0] = userName;
		arguments3[1] = userName;
		arguments3[2] = String.valueOf(RestHelper.P_BLACK);

		ContentProviderClient client = contentResolver.acquireContentProviderClient(DBConstants.PROVIDER_NAME);
		SQLiteDatabase dbHandle = ((DBDataProvider) client.getLocalContentProvider()).getDbHandle();
		StringBuilder projection = new StringBuilder();
		String selection = DBConstants.V_USER + EQUALS_ARG_ + AND_ + "(" + DBConstants.V_WHITE_USERNAME + NOT_EQUALS_ARG_
				+ AND_ + DBConstants.V_I_PLAY_AS + EQUALS_ARG_ + ")";

		QueryParams params = new QueryParams();
		params.setDbName(DBConstants.Tables.DAILY_FINISHED_GAMES.name());
		params.setProjection(PROJECTION_DAILY_PLAYER_NAMES);
		params.setSelection(selection);
		params.setArguments(arguments3);
		params.setCommands(GROUP_BY + StaticData.SYMBOL_SPACE + DBConstants.V_WHITE_USERNAME + ", " + DBConstants.V_BLACK_USERNAME);

		for (String projections : params.getProjection()) {
			projection.append(projections).append(StaticData.SYMBOL_COMMA);
		}

		Cursor cursor = dbHandle.rawQuery("SELECT " + projection.toString().substring(0, projection.length() - 1)
				+ " FROM " + params.getDbName() + " WHERE " + params.getSelection() +
				StaticData.SYMBOL_SPACE + params.getCommands(), params.getArguments());
		client.release();

		return cursor;
	}

	/**
	 * Check if we have saved friends for current user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedFriends(Context context, String userName) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.FRIENDS.ordinal()],
				PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
		boolean exist = false;
		if (cursor != null) {
			exist = cursor.moveToFirst();
			cursor.close();
		}

		return exist;
	}

	/**
	 * Check if we have saved games for current user
	 *
	 * @param context  to get resources
	 * @param userName
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedTacticGame(Context context, String userName) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.TACTICS_BATCH.ordinal()],
				PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
		boolean exist = cursor.moveToFirst();
		cursor.close();

		return exist;
	}

	public static int saveTacticItemToDb(Context context, TacticItem.Data tacticItem, String userName) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(tacticItem.getId());
		arguments2[1] = userName;

		Uri uri = DBConstants.uriArray[DBConstants.Tables.TACTICS_BATCH.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID_AND_USER,
				SELECTION_ITEM_ID_AND_USER, arguments2, null);

		ContentValues values = putTacticItemToValues(tacticItem);
		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();

		if (tacticItem.getResultItem() != null) {
			saveTacticResultItemToDb(context, tacticItem.getResultItem());
		}

		return StaticData.RESULT_OK;
	}

	public static TacticItem.Data getLastTacticItemFromDb(Context context, String userName) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.TACTICS_BATCH.ordinal()],
				null, SELECTION_USER, arguments1, null);

		cursor.moveToFirst();
		TacticItem.Data tacticItem = getTacticItemFromCursor(cursor);
		cursor.close();

		// set result item
		TacticRatingData resultItem = getTacticResultItemFromDb(context, tacticItem.getId(), userName);
		tacticItem.setResultItem(resultItem);

		return tacticItem;
	}

	private static void saveTacticResultItemToDb(Context context, TacticRatingData resultItem) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(resultItem.getId());
		arguments2[1] = resultItem.getUser();

		Uri uri = DBConstants.uriArray[DBConstants.Tables.TACTICS_RESULTS.ordinal()];
		Cursor cursor = contentResolver.query(uri, null, SELECTION_ITEM_ID_AND_USER, arguments2, null);

		ContentValues values = putTacticResultItemToValues(resultItem);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	private static TacticRatingData getTacticResultItemFromDb(Context context, long id, String userName) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(id);
		arguments2[1] = userName;
		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.TACTICS_RESULTS.ordinal()],
				null, SELECTION_ITEM_ID_AND_USER, arguments2, null);

		if (cursor.moveToFirst()) {
			TacticRatingData resultItem = getTacticResultItemFromCursor(cursor);
			cursor.close();

			return resultItem;
		} else {
			cursor.close();
			return null;
		}
	}

	public static int getUserCurrentRating(Context context, int dbUriCode, String userName) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;
		Cursor cursor = contentResolver.query(DBConstants.uriArray[dbUriCode],
				PROJECTION_USER_CURRENT_RATING, SELECTION_USER, arguments1, null);

		if (cursor.moveToFirst()) {
			int rating = getInt(cursor, DBConstants.V_CURRENT);
			cursor.close();

			return rating;
		} else {
			cursor.close();
			return 0;
		}
	}


	// ---------------------- Value Setters ------------------------------------------------------------------------
	public static ContentValues putTacticItemToValues(TacticItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_FEN, dataObj.getInitialFen());
		values.put(DBConstants.V_MOVE_LIST, dataObj.getCleanMoveString());
		values.put(DBConstants.V_ATTEMPT_CNT, dataObj.getAttemptCnt());
		values.put(DBConstants.V_PASSED_CNT, dataObj.getPassedCnt());
		values.put(DBConstants.V_RATING, dataObj.getRating());
		values.put(DBConstants.V_AVG_SECONDS, dataObj.getAvgSeconds());
		values.put(DBConstants.V_SECONDS_SPENT, dataObj.getSecondsSpent());
		values.put(DBConstants.V_STOP, dataObj.isStop() ? 1 : 0);
		values.put(DBConstants.V_WAS_SHOWED, dataObj.isWasShowed() ? 1 : 0);
		values.put(DBConstants.V_IS_RETRY, dataObj.isRetry() ? 1 : 0);

		return values;
	}


	public static TacticItem.Data getTacticItemFromCursor(Cursor cursor) {
		TacticItem.Data dataObj = new TacticItem.Data();

		dataObj.setUser(getString(cursor, DBConstants.V_USER));
		dataObj.setId(getLong(cursor, DBConstants.V_ID));
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
		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_SCORE, dataObj.getScoreStr());
		values.put(DBConstants.V_USER_RATING_CHANGE, dataObj.getUserRatingChange());
		values.put(DBConstants.V_USER_RATING, dataObj.getUserRating());
		values.put(DBConstants.V_PROBLEM_RATING_CHANGE, dataObj.getProblemRatingChange());
		values.put(DBConstants.V_PROBLEM_RATING, dataObj.getProblemRating());

		return values;
	}


	public static TacticRatingData getTacticResultItemFromCursor(Cursor cursor) {
		TacticRatingData dataObj = new TacticRatingData();

		dataObj.setUser(getString(cursor, DBConstants.V_USER));
		dataObj.setId(getLong(cursor, DBConstants.V_ID));
		dataObj.setScore(getString(cursor, DBConstants.V_SCORE));
		dataObj.setUserRatingChange(getInt(cursor, DBConstants.V_USER_RATING_CHANGE));
		dataObj.setUserRating(getInt(cursor, DBConstants.V_USER_RATING));
		dataObj.setProblemRatingChange(getInt(cursor, DBConstants.V_PROBLEM_RATING_CHANGE));
		dataObj.setProblemRating(getInt(cursor, DBConstants.V_PROBLEM_RATING));

		return dataObj;
	}

	// ----------------------------------- Daily Games -------------------------------------------------------
	public static ContentValues putDailyFinishedGameToValues(DailyFinishedGameData dataObj, String userName) {
		ContentValues values = new ContentValues();

		setValuesFromGameItem(values, dataObj);
		values.put(DBConstants.V_FINISHED, 1);
		values.put(DBConstants.V_USER, userName);
		values.put(DBConstants.V_GAME_SCORE, dataObj.getGameScore());
		values.put(DBConstants.V_RESULT_MESSAGE, dataObj.getResultMessage());
		return values;
	}

	public static DailyFinishedGameData getDailyFinishedGameFromCursor(Cursor cursor) {
		DailyFinishedGameData dataObj = new DailyFinishedGameData();

		setDailyGameFromCursor(dataObj, cursor);
		dataObj.setGameScore(getInt(cursor, DBConstants.V_GAME_SCORE));
		dataObj.setResultMessage(getString(cursor, DBConstants.V_RESULT_MESSAGE));

		return dataObj;
	}

	public static ContentValues putDailyGameCurrentItemToValues(DailyCurrentGameData dataObj, String userName) {
		ContentValues values = new ContentValues();

		setValuesFromGameItem(values, dataObj);
		values.put(DBConstants.V_FINISHED, 0);
		values.put(DBConstants.V_USER, userName);
		values.put(DBConstants.V_OPPONENT_OFFERED_DRAW, dataObj.isDrawOffered() ? 1 : 0);
		values.put(DBConstants.V_IS_MY_TURN, dataObj.isMyTurn() ? 1 : 0);
		return values;
	}

	private static void setValuesFromGameItem(ContentValues values, DailyGameBaseData dataObj) { // TODO remove
		values.put(DBConstants.V_ID, dataObj.getGameId());
		values.put(DBConstants.V_FEN, dataObj.getFen());
		values.put(DBConstants.V_I_PLAY_AS, dataObj.getMyColor());
		values.put(DBConstants.V_GAME_TYPE, dataObj.getGameType());
		values.put(DBConstants.V_WHITE_USERNAME, dataObj.getWhiteUsername());
		values.put(DBConstants.V_BLACK_USERNAME, dataObj.getBlackUsername());
		values.put(DBConstants.V_WHITE_RATING, dataObj.getWhiteRating());
		values.put(DBConstants.V_BLACK_RATING, dataObj.getBlackRating());
		values.put(DBConstants.V_WHITE_PREMIUM_STATUS, dataObj.getWhitePremiumStatus());
		values.put(DBConstants.V_BLACK_PREMIUM_STATUS, dataObj.getBlackPremiumStatus());
		values.put(DBConstants.V_WHITE_AVATAR, dataObj.getWhiteAvatar());
		values.put(DBConstants.V_BLACK_AVATAR, dataObj.getBlackAvatar());
		values.put(DBConstants.V_TIME_REMAINING, dataObj.getTimeRemaining());
		values.put(DBConstants.V_TIMESTAMP, dataObj.getTimestamp());
		values.put(DBConstants.V_LAST_MOVE_FROM_SQUARE, dataObj.getLastMoveFromSquare());
		values.put(DBConstants.V_LAST_MOVE_TO_SQUARE, dataObj.getLastMoveToSquare());
		values.put(DBConstants.V_GAME_NAME, dataObj.getName());
		values.put(DBConstants.V_FEN_START_POSITION, dataObj.getStartingFenPosition());
		values.put(DBConstants.V_MOVE_LIST, dataObj.getMoveList());
		values.put(DBConstants.V_HAS_NEW_MESSAGE, dataObj.hasNewMessage() ? 1 : 0);
		values.put(DBConstants.V_RATED, dataObj.isRated() ? 1 : 0);
		values.put(DBConstants.V_DAYS_PER_MOVE, dataObj.getDaysPerMove());
		values.put(DBConstants.V_IS_OPPONENT_ONLINE, dataObj.isOpponentOnline());
	}

	public static DailyCurrentGameData getDailyCurrentGameFromCursor(Cursor cursor) {
		DailyCurrentGameData dataObj = new DailyCurrentGameData();

		setDailyGameFromCursor(dataObj, cursor);
		dataObj.setDrawOffered(getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0);
		dataObj.setMyTurn(getInt(cursor, DBConstants.V_IS_MY_TURN) > 0);

		return dataObj;
	}

	/**
	 * Fill data according to PROJECTION_CURRENT_GAMES
	 *
	 * @param cursor to fill from
	 * @return DailyCurrentGameData filled object
	 */
	public static DailyCurrentGameData getDailyCurrentGameListFromCursor(Cursor cursor) {
		DailyCurrentGameData dataObj = new DailyCurrentGameData();

		dataObj.setGameId(getLong(cursor, DBConstants.V_ID));
		dataObj.setWhiteUsername(getString(cursor, DBConstants.V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, DBConstants.V_BLACK_USERNAME));
		dataObj.setWhiteAvatar(getString(cursor, DBConstants.V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, DBConstants.V_BLACK_AVATAR));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setMyTurn(getInt(cursor, DBConstants.V_IS_MY_TURN) > 0);
		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
		dataObj.setDrawOffered(getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0);
		dataObj.setHasNewMessage(getInt(cursor, DBConstants.V_HAS_NEW_MESSAGE) > 0);
		dataObj.setTimeRemaining(getLong(cursor, DBConstants.V_TIME_REMAINING));

		return dataObj;
	}

	/**
	 * Fill data according to PROJECTION_FINISHED_GAMES
	 *
	 * @param cursor to fill from
	 * @return DailyFinishedGameData filled object
	 */
	public static DailyFinishedGameData getDailyFinishedGameListFromCursor(Cursor cursor) {
		DailyFinishedGameData dataObj = new DailyFinishedGameData();

		dataObj.setGameId(getLong(cursor, DBConstants.V_ID));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setGameScore(getInt(cursor, DBConstants.V_GAME_SCORE));
		dataObj.setWhiteUsername(getString(cursor, DBConstants.V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, DBConstants.V_BLACK_USERNAME));
		dataObj.setWhiteAvatar(getString(cursor, DBConstants.V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, DBConstants.V_BLACK_AVATAR));
		dataObj.setWhiteRating(getInt(cursor, DBConstants.V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, DBConstants.V_BLACK_RATING));

		return dataObj;
	}

	private static void setDailyGameFromCursor(DailyGameBaseData dataObj, Cursor cursor) {
		dataObj.setGameId(getLong(cursor, DBConstants.V_ID));
		dataObj.setFen(getString(cursor, DBConstants.V_FEN));
		dataObj.setIPlayAs(getInt(cursor, DBConstants.V_I_PLAY_AS));
		dataObj.setLastMoveFromSquare(getString(cursor, DBConstants.V_LAST_MOVE_FROM_SQUARE));
		dataObj.setLastMoveToSquare(getString(cursor, DBConstants.V_LAST_MOVE_TO_SQUARE));
		dataObj.setGameType(getInt(cursor, DBConstants.V_GAME_TYPE));
		dataObj.setTimestamp(getLong(cursor, DBConstants.V_TIMESTAMP));
		dataObj.setName(getString(cursor, DBConstants.V_GAME_NAME));
		dataObj.setWhiteUsername(getString(cursor, DBConstants.V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, DBConstants.V_BLACK_USERNAME));
		dataObj.setWhitePremiumStatus(getInt(cursor, DBConstants.V_WHITE_PREMIUM_STATUS));
		dataObj.setBlackPremiumStatus(getInt(cursor, DBConstants.V_BLACK_PREMIUM_STATUS));
		dataObj.setStartingFenPosition(getString(cursor, DBConstants.V_FEN_START_POSITION));
		dataObj.setMoveList(getString(cursor, DBConstants.V_MOVE_LIST));
		dataObj.setWhiteRating(getInt(cursor, DBConstants.V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, DBConstants.V_BLACK_RATING));
		dataObj.setWhiteAvatar(getString(cursor, DBConstants.V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, DBConstants.V_BLACK_AVATAR));
		dataObj.setHasNewMessage(getInt(cursor, DBConstants.V_HAS_NEW_MESSAGE) > 0);
		dataObj.setTimeRemaining(getLong(cursor, DBConstants.V_TIME_REMAINING));
		dataObj.setRated(getInt(cursor, DBConstants.V_RATED) > 0);
		dataObj.setDaysPerMove(getInt(cursor, DBConstants.V_DAYS_PER_MOVE));
	}

	// ------------------------------------- Friends ----------------------------------------------
	public static ContentValues putFriendItemToValues(FriendsItem.Data dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, userName);
		values.put(DBConstants.V_USERNAME, dataObj.getUsername());
		values.put(DBConstants.V_USER_ID, dataObj.getUserId());
		values.put(DBConstants.V_LOCATION, dataObj.getLocation());
		values.put(DBConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DBConstants.V_PHOTO_URL, dataObj.getAvatarUrl());
		values.put(DBConstants.V_IS_OPPONENT_ONLINE, dataObj.isOnline() ? 1 : 0);
		values.put(DBConstants.V_PREMIUM_STATUS, dataObj.getPremiumStatus());
		values.put(DBConstants.V_LAST_LOGIN_DATE, dataObj.getLastLoginDate());

		return values;
	}

	public static ContentValues putArticleItemToValues(ArticleItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_TITLE, dataObj.getTitle());
		values.put(DBConstants.V_CREATE_DATE, dataObj.getCreate_date());
		values.put(DBConstants.V_CATEGORY, dataObj.getCategoryName());
		values.put(DBConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DBConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DBConstants.V_USER_ID, dataObj.getUserId());
		values.put(DBConstants.V_USERNAME, dataObj.getUsername());
		values.put(DBConstants.V_FIRST_NAME, dataObj.getFirstName());
		values.put(DBConstants.V_LAST_NAME, dataObj.getLastName());
		values.put(DBConstants.V_CHESS_TITLE, dataObj.getChessTitle());
		values.put(DBConstants.V_USER_AVATAR, dataObj.getAvatar());
		values.put(DBConstants.V_PHOTO_URL, dataObj.getImageUrl());
		values.put(DBConstants.V_THUMB_CONTENT, dataObj.isIsThumbInContent());

		return values;
	}

	public static void updateVideoItem(ContentResolver contentResolver, VideoItem.Data currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getVideoId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DBConstants.uriArray[DBConstants.Tables.VIDEOS.ordinal()];

		Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID,
				DBDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DBDataManager.putVideoItemToValues(currentItem);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}
		cursor.close();
	}

	public static void updateForumTopicItem(ContentResolver contentResolver, ForumTopicItem.Topic currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DBConstants.uriArray[DBConstants.Tables.FORUM_TOPICS.ordinal()];

		Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID,
				DBDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DBDataManager.putForumTopicItemToValues(currentItem);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}
		cursor.close();
	}

	public static void updateForumCategoryItem(ContentResolver contentResolver, ForumCategoryItem.Data currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DBConstants.uriArray[DBConstants.Tables.FORUM_CATEGORIES.ordinal()];

		Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID,
				DBDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DBDataManager.putForumCategoryItemToValues(currentItem);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}
		cursor.close();
	}

	public static void updateForumPostItem(ContentResolver contentResolver, ForumPostItem.Post currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getCreateDate());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DBConstants.uriArray[DBConstants.Tables.FORUM_POSTS.ordinal()];

		Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_CREATE_DATE,
				DBDataManager.SELECTION_CREATE_DATE, arguments1, null);

		ContentValues values = DBDataManager.putForumPostItemToValues(currentItem);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}
		cursor.close();
	}

	/**
	 * Check if we have saved videos for any user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedVideos(Context context) {
		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.VIDEOS.ordinal()],
				PROJECTION_ITEM_ID, null, null, LIMIT_1);
		boolean exist = cursor.moveToFirst();
		cursor.close();

		return exist;
	}

	/**
	 * @param context to get ContnetResolver
	 * @param videoId to search for
	 * @return _id of video if it was saved before
	 */
	public static long haveSavedVideoById(Context context, int videoId) {
		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(videoId);

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.VIDEOS.ordinal()],
				PROJECTION_ITEM_ID, SELECTION_ITEM_ID, arguments1, LIMIT_1);
		if (cursor != null) {
			boolean exist = cursor.moveToFirst();
			cursor.close();
			if (exist) {
				return getId(cursor);
			}
		}
		return -1;
	}

	public static void updateVideoViewedState(ContentResolver contentResolver, VideoViewedItem currentItem) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(currentItem.getUsername());
		arguments2[1] = String.valueOf(currentItem.getVideoId());

		Uri uri = DBConstants.uriArray[DBConstants.Tables.VIDEO_VIEWED.ordinal()];

		Cursor cursor = contentResolver.query(uri, null,
				DBDataManager.SELECTION_USER_AND_ID, arguments2, null);

		ContentValues values = DBDataManager.putVideoViewedItemToValues(currentItem);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	public static Cursor getVideoViewedCursor(Context context, String userName) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = userName;
		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.VIDEO_VIEWED.ordinal()],
				null, SELECTION_USER, arguments1, null);

		if (cursor != null && cursor.moveToFirst()) {
			return cursor;
		} else {
			return null;
		}
	}

	public static boolean isVideoViewed(Context context, String userName, long videoId) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = userName;
		arguments2[1] = String.valueOf(videoId);

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.VIDEO_VIEWED.ordinal()],
				null, SELECTION_USER_AND_ID, arguments2, null);

		return cursor != null && cursor.moveToFirst() && getInt(cursor, DBConstants.V_VIDEO_VIEWED) > 0;
	}

	/**
	 * Check if we have saved articles for any user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedArticles(Context context) {
		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(DBConstants.uriArray[DBConstants.Tables.ARTICLES.ordinal()], null, null, null, LIMIT_1);
		boolean exist = cursor.moveToFirst();
		cursor.close();

		return exist;
	}


	public static ContentValues putVideoItemToValues(VideoItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_TITLE, dataObj.getTitle());
		values.put(DBConstants.V_DESCRIPTION, dataObj.getDescription());
		values.put(DBConstants.V_CATEGORY, dataObj.getCategoryName());
		values.put(DBConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DBConstants.V_ID, dataObj.getVideoId());
		values.put(DBConstants.V_SKILL_LEVEL, dataObj.getSkillLevel());
		values.put(DBConstants.V_USER_AVATAR, dataObj.getUserAvatar());
		values.put(DBConstants.V_MINUTES, dataObj.getMinutes());
		values.put(DBConstants.V_VIEW_COUNT, dataObj.getViewCount());
		values.put(DBConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DBConstants.V_COMMENT_COUNT, dataObj.getCommentCount());
		values.put(DBConstants.V_CREATE_DATE, dataObj.getCreateDate());
		values.put(DBConstants.V_URL, dataObj.getUrl());
		values.put(DBConstants.V_KEY_FEN, dataObj.getKeyFen());
		values.put(DBConstants.V_USERNAME, dataObj.getUsername());
		values.put(DBConstants.V_FIRST_NAME, dataObj.getFirstName());
		values.put(DBConstants.V_LAST_NAME, dataObj.getLastName());
		values.put(DBConstants.V_CHESS_TITLE, dataObj.getChessTitle());

		return values;
	}

	public static ContentValues putForumCategoryItemToValues(ForumCategoryItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_NAME, dataObj.getCategory());
		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_CREATE_DATE, dataObj.getCreateDate());
		values.put(DBConstants.V_LAST_POST_DATE, dataObj.getLastDate());
		values.put(DBConstants.V_DISPLAY_ORDER, dataObj.getDisplayOrder());
		values.put(DBConstants.V_DESCRIPTION, dataObj.getDescription());
		values.put(DBConstants.V_TOPIC_COUNT, dataObj.getTopicCount());
		values.put(DBConstants.V_POST_COUNT, dataObj.getPostCount());
		values.put(DBConstants.V_MIN_MEMBERSHIP, dataObj.getMinimumMembershipLevel());

		return values;
	}

	public static ContentValues putForumTopicItemToValues(ForumTopicItem.Topic dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_TITLE, dataObj.getSubject());
		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DBConstants.V_CATEGORY, dataObj.getCategoryName());
		values.put(DBConstants.V_URL, dataObj.getUrl());
		values.put(DBConstants.V_USERNAME, dataObj.getTopicUsername());
		values.put(DBConstants.V_LAST_POST_USERNAME, dataObj.getLastPostUsername());
		values.put(DBConstants.V_POST_COUNT, dataObj.getPostCount());
		values.put(DBConstants.V_LAST_POST_DATE, dataObj.getLastPostDate());
		values.put(DBConstants.V_PAGE, dataObj.getPage());

		return values;
	}

	public static ContentValues putForumPostItemToValues(ForumPostItem.Post dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_DESCRIPTION, dataObj.getBody());
		values.put(DBConstants.V_ID, dataObj.getTopicId());
		values.put(DBConstants.V_CREATE_DATE, dataObj.getCreateDate());
		values.put(DBConstants.V_USERNAME, dataObj.getUsername());
		values.put(DBConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DBConstants.V_PREMIUM_STATUS, dataObj.isPremiumStatus());
		values.put(DBConstants.V_PHOTO_URL, dataObj.getAvatarUrl());
		values.put(DBConstants.V_NUMBER, dataObj.getCommentNumber());
		values.put(DBConstants.V_PAGE, dataObj.getPage());

		return values;
	}

	public static ContentValues putLessonsCourseListItemToValues(LessonCourseListItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_NAME, dataObj.getName());
		values.put(DBConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DBConstants.V_COURSE_COMPLETED, dataObj.isCourseCompleted()? 1 : 0);

		return values;
	}

	public static ContentValues putLessonsCourseItemToValues(LessonCourseItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_DESCRIPTION, dataObj.getDescription());
		values.put(DBConstants.V_NAME, dataObj.getCourseName());

		return values;
	}

	public static LessonCourseItem.Data getLessonsCourseItemFromCursor(Cursor cursor) {
		LessonCourseItem.Data dataObj = new LessonCourseItem.Data();
//		dataObj.set(get(cursor, DBConstants.V_));

		dataObj.setId(getLong(cursor, DBConstants.V_ID));
		dataObj.setDescription(getString(cursor, DBConstants.V_DESCRIPTION));
		dataObj.setCourseName(getString(cursor, DBConstants.V_NAME));

		return dataObj;
	}

	public static ContentValues putLessonsListItemToValues(LessonCourseItem.LessonListItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_CATEGORY_ID, dataObj.getCourseId());
		values.put(DBConstants.V_LESSON_COMPLETED, dataObj.isCompleted()? 1: 0);
		values.put(DBConstants.V_NAME, dataObj.getName());

		return values;
	}

	public static LessonCourseItem.LessonListItem getLessonsListItemFromCursor(Cursor cursor) {
		LessonCourseItem.LessonListItem dataObj = new LessonCourseItem.LessonListItem();

		dataObj.setId(getInt(cursor, DBConstants.V_ID));
		dataObj.setCourseId(getLong(cursor, DBConstants.V_CATEGORY_ID));
		dataObj.setCompleted(getInt(cursor, DBConstants.V_LESSON_COMPLETED) > 0);
		dataObj.setName(getString(cursor, DBConstants.V_NAME));

		return dataObj;
	}

	public static ContentValues putLessonsMentorLessonToValues(LessonItem.MentorLesson dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getLessonId());
		values.put(DBConstants.V_NUMBER, dataObj.getLessonNumber());
		values.put(DBConstants.V_GOAL, dataObj.getGoal());
		values.put(DBConstants.V_DIFFICULTY, dataObj.getDifficulty());
		values.put(DBConstants.V_AUTHOR, dataObj.getAuthor());
		values.put(DBConstants.V_NAME, dataObj.getName());
		values.put(DBConstants.V_DESCRIPTION, dataObj.getAbout());
		values.put(DBConstants.V_GOAL_COMMENT, dataObj.getGoalCommentary());
		values.put(DBConstants.V_GOAL_CODE, dataObj.getGoalCode());

		return values;
	}

	public static LessonItem.MentorLesson getLessonsMentorLessonFromCursor(Cursor cursor) {
		LessonItem.MentorLesson dataObj = new LessonItem.MentorLesson();

		dataObj.setLessonId(getLong(cursor, DBConstants.V_ID));
		dataObj.setLessonNumber(getInt(cursor, DBConstants.V_NUMBER));
		dataObj.setGoal(getInt(cursor, DBConstants.V_GOAL));
		dataObj.setDifficulty(getInt(cursor, DBConstants.V_DIFFICULTY));
		dataObj.setAuthor(getString(cursor, DBConstants.V_AUTHOR));
		dataObj.setName(getString(cursor, DBConstants.V_NAME));
		dataObj.setAbout(getString(cursor, DBConstants.V_DESCRIPTION));
		dataObj.setGoalCommentary(getString(cursor, DBConstants.V_GOAL_COMMENT));
		dataObj.setGoalCode(getString(cursor, DBConstants.V_GOAL_CODE));

		return dataObj;
	}

	public static ContentValues putLessonsPositionToValues(LessonItem.MentorPosition dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getLessonId());
		values.put(DBConstants.V_NUMBER, dataObj.getPositionNumber());
		values.put(DBConstants.V_USER_TO_MOVE, dataObj.getUserToMove());
		values.put(DBConstants.V_DIFFICULTY, dataObj.getMoveDifficulty());
		values.put(DBConstants.V_FINAL_POSITION, dataObj.getFinalPosition());
		values.put(DBConstants.V_FEN, dataObj.getFen());
		values.put(DBConstants.V_ADVICE_1, dataObj.getAdvice1());
		values.put(DBConstants.V_ADVICE_2, dataObj.getAdvice2());
		values.put(DBConstants.V_ADVICE_3, dataObj.getAdvice3());
		values.put(DBConstants.V_RESPONSE_MOVE_COMMENT, dataObj.getStandardResponseMoveCommentary());
		values.put(DBConstants.V_WRONG_MOVE_COMMENT, dataObj.getStandardWrongMoveCommentary());
		values.put(DBConstants.V_DESCRIPTION, dataObj.getAbout());

		return values;
	}

	public static LessonItem.MentorPosition getLessonsPositionFromCursor(Cursor cursor) {
		LessonItem.MentorPosition dataObj = new LessonItem.MentorPosition();

		dataObj.setLessonId(getLong(cursor, DBConstants.V_ID));
		dataObj.setPositionNumber(getInt(cursor, DBConstants.V_NUMBER));
		dataObj.setUserToMove(getInt(cursor, DBConstants.V_USER_TO_MOVE));
		dataObj.setMoveDifficulty(getInt(cursor, DBConstants.V_DIFFICULTY));
		dataObj.setFinalPosition(getInt(cursor, DBConstants.V_FINAL_POSITION));
		dataObj.setFen(getString(cursor, DBConstants.V_FEN));
		dataObj.setAdvice1(getString(cursor, DBConstants.V_ADVICE_1));
		dataObj.setAdvice2(getString(cursor, DBConstants.V_ADVICE_2));
		dataObj.setAdvice3(getString(cursor, DBConstants.V_ADVICE_3));
		dataObj.setStandardResponseMoveCommentary(getString(cursor, DBConstants.V_RESPONSE_MOVE_COMMENT));
		dataObj.setStandardWrongMoveCommentary(getString(cursor, DBConstants.V_WRONG_MOVE_COMMENT));
		dataObj.setAbout(getString(cursor, DBConstants.V_DESCRIPTION));

		return dataObj;
	}

	public static ContentValues putLessonsPositionMoveToValues(LessonItem.MentorPosition.PossibleMove dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getLessonId());
		values.put(DBConstants.V_CURRENT_POSITION, dataObj.getPositionNumber());
		values.put(DBConstants.V_NUMBER, dataObj.getMoveNumber());
		values.put(DBConstants.V_MOVE, dataObj.getMove());
		values.put(DBConstants.V_MOVE_COMMENT, dataObj.getMoveCommentary());
		values.put(DBConstants.V_SHORT_RESPONSE_MOVE, dataObj.getShortResponseMove());
		values.put(DBConstants.V_RESPONSE_MOVE_COMMENT, dataObj.getResponseMoveCommentary());
		values.put(DBConstants.V_MOVE_TYPE, dataObj.getMoveType());

		return values;
	}

	public static LessonItem.MentorPosition.PossibleMove getLessonsPositionMoveFromCursor(Cursor cursor) {
		LessonItem.MentorPosition.PossibleMove dataObj = new LessonItem.MentorPosition.PossibleMove();

		dataObj.setLessonId(getLong(cursor, DBConstants.V_ID));
		dataObj.setPositionNumber(getInt(cursor, DBConstants.V_CURRENT_POSITION));
		dataObj.setMoveNumber(getInt(cursor, DBConstants.V_NUMBER));
		dataObj.setMove(getString(cursor, DBConstants.V_MOVE));
		dataObj.setMoveCommentary(getString(cursor, DBConstants.V_MOVE_COMMENT));
		dataObj.setShortResponseMove(getString(cursor, DBConstants.V_SHORT_RESPONSE_MOVE));
		dataObj.setResponseMoveCommentary(getString(cursor, DBConstants.V_RESPONSE_MOVE_COMMENT));
		dataObj.setMoveType(getString(cursor, DBConstants.V_MOVE_TYPE));

		return dataObj;
	}

	public static ContentValues putLessonsUserLessonToValues(LessonItem.UserLesson dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getLessonId());
		values.put(DBConstants.V_CURRENT_POSITION, dataObj.getCurrentPosition());
		values.put(DBConstants.V_CURRENT_POINTS, dataObj.getCurrentPoints());
		values.put(DBConstants.V_CURRENT_POSITION_POINTS, dataObj.getCurrentPositionPoints());
		values.put(DBConstants.V_USER, dataObj.getUsername());
		values.put(DBConstants.V_INITIAL_SCORE, dataObj.getInitialScore());
		values.put(DBConstants.V_LAST_SCORE, dataObj.getLastScore());
		values.put(DBConstants.V_LEGAL_POSITION_CHECK, dataObj.getLegalPositionCheck());
		values.put(DBConstants.V_LEGAL_MOVE_CHECK, dataObj.getLegalMoveCheck());
		values.put(DBConstants.V_LESSON_COMPLETED, dataObj.isLessonCompleted()? 1 : 0);

		return values;
	}

	public static LessonItem.UserLesson getLessonsUserLessonFromCursor(Cursor cursor) {
		LessonItem.UserLesson dataObj = new LessonItem.UserLesson();

		dataObj.setLessonId(getLong(cursor, DBConstants.V_ID));
		dataObj.setCurrentPosition(getInt(cursor, DBConstants.V_CURRENT_POSITION));
		dataObj.setCurrentPoints(getInt(cursor, DBConstants.V_CURRENT_POINTS));
		dataObj.setCurrentPositionPoints(getInt(cursor, DBConstants.V_CURRENT_POSITION_POINTS));
		dataObj.setUsername(getString(cursor, DBConstants.V_USER));
		dataObj.setInitialScore(getString(cursor, DBConstants.V_INITIAL_SCORE));
		dataObj.setLastScore(getString(cursor, DBConstants.V_LAST_SCORE));
		dataObj.setLegalPositionCheck(getString(cursor, DBConstants.V_LEGAL_POSITION_CHECK));
		dataObj.setLegalMoveCheck(getString(cursor, DBConstants.V_LEGAL_MOVE_CHECK));
		dataObj.setLessonCompleted(getInt(cursor, DBConstants.V_LESSON_COMPLETED) > 0);

		return dataObj;
	}

	public static ContentValues putVideoViewedItemToValues(VideoViewedItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, dataObj.getUsername());
		values.put(DBConstants.V_ID, dataObj.getVideoId());
		values.put(DBConstants.V_VIDEO_VIEWED, dataObj.isViewed() ? 1 : 0);

		return values;
	}

	public static ContentValues putCommonFeedCategoryItemToValues(CommonFeedCategoryItem.Data dataObj) {
		ContentValues values = new ContentValues();


		values.put(DBConstants.V_NAME, dataObj.getName());
		values.put(DBConstants.V_CATEGORY_ID, dataObj.getId());
		values.put(DBConstants.V_DISPLAY_ORDER, dataObj.getDisplayOrder());

		return values;
	}

	/* ========================================== Messages ========================================== */
	public static ContentValues putConversationItemToValues(ConversationItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_OTHER_USER_ID, dataObj.getOtherUserId());
		values.put(DBConstants.V_LAST_MESSAGE_ID, dataObj.getLastMessageId());
		values.put(DBConstants.V_LAST_MESSAGE_CREATED_AT, dataObj.getLastMessageCreatedAt());
		values.put(DBConstants.V_OTHER_USER_IS_ONLINE, dataObj.isOtherUserIsOnline() ? 1 : 0);
		values.put(DBConstants.V_NEW_MESSAGES_COUNT, dataObj.getNewMessagesCount());
		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_OTHER_USER_USERNAME, dataObj.getOtherUserUsername());
		values.put(DBConstants.V_OTHER_USER_AVATAR_URL, dataObj.getOtherUserAvatarUrl());
		values.put(DBConstants.V_LAST_MESSAGE_SENDER_USERNAME, dataObj.getLastMessageSenderUsername());
		values.put(DBConstants.V_LAST_MESSAGE_CONTENT, dataObj.getLastMessageContent());

		return values;
	}

	public static ContentValues putMessagesItemToValues(MessagesItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_ID, dataObj.getId());
		values.put(DBConstants.V_CONVERSATION_ID, dataObj.getConversationId());
		values.put(DBConstants.V_OTHER_USER_ID, dataObj.getSenderId());
		values.put(DBConstants.V_CREATE_DATE, dataObj.getCreatedAt());
		values.put(DBConstants.V_OTHER_USER_IS_ONLINE, dataObj.isSenderIsOnline() ? 1 : 0);
		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_OTHER_USER_USERNAME, dataObj.getSenderUsername());
		values.put(DBConstants.V_OTHER_USER_AVATAR_URL, dataObj.getSenderAvatarUrl());
		values.put(DBConstants.V_LAST_MESSAGE_CONTENT, dataObj.getContent());

		return values;
	}


	/* ========================================== Stats ========================================== */

	public static ContentValues putUserStatsLiveItemToValues(UserLiveStatsData.Stats dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, user);
		values.put(DBConstants.V_CURRENT, dataObj.getRating().getCurrent());
		values.put(DBConstants.V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(DBConstants.V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());
		values.put(DBConstants.V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(DBConstants.V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(DBConstants.V_AVERAGE_OPPONENT, dataObj.getRating().getAverageOpponent());

		values.put(DBConstants.V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(DBConstants.V_GAMES_WINS, dataObj.getGames().getWins());
		values.put(DBConstants.V_GAMES_LOSSES, dataObj.getGames().getLosses());
		values.put(DBConstants.V_GAMES_DRAWS, dataObj.getGames().getDraws());

		return values;
	}

	public static ContentValues putUserStatsDailyItemToValues(UserDailyStatsData.ChessStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, user);
		values.put(DBConstants.V_CURRENT, dataObj.getRating().getCurrent());
		values.put(DBConstants.V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(DBConstants.V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());

		values.put(DBConstants.V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(DBConstants.V_BEST_WIN_GAME_ID, dataObj.getRating().getBestWin().getGameId());
		values.put(DBConstants.V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(DBConstants.V_AVERAGE_OPPONENT, dataObj.getRating().getAverageOpponent());
		values.put(DBConstants.V_RANK, dataObj.getRating().getTodaysRank().getRank());
		values.put(DBConstants.V_TOTAL_PLAYER_COUNT, dataObj.getRating().getTodaysRank().getTotalPlayerCount());
		values.put(DBConstants.V_TIMEOUTS, dataObj.getTimeouts());
		values.put(DBConstants.V_TIME_PER_MOVE, dataObj.getTimePerMove());

		values.put(DBConstants.V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(DBConstants.V_GAMES_WINS, dataObj.getGames().getWins());
		values.put(DBConstants.V_GAMES_LOSSES, dataObj.getGames().getLosses());
		values.put(DBConstants.V_GAMES_DRAWS, dataObj.getGames().getDraws());

		return values;
	}

	public static ContentValues putUserStatsTacticsItemToValues(UserTacticsStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, user);
		values.put(DBConstants.V_CURRENT, dataObj.getCurrent());
		values.put(DBConstants.V_HIGHEST_RATING, dataObj.getHighest().getRating());
		values.put(DBConstants.V_HIGHEST_TIMESTAMP, dataObj.getHighest().getTimestamp());
		values.put(DBConstants.V_LOWEST_RATING, dataObj.getLowest().getRating());
		values.put(DBConstants.V_LOWEST_TIMESTAMP, dataObj.getLowest().getTimestamp());

		values.put(DBConstants.V_ATTEMPT_COUNT, dataObj.getAttemptCount());
		values.put(DBConstants.V_PASSED_COUNT, dataObj.getPassedCount());
		values.put(DBConstants.V_FAILED_COUNT, dataObj.getFailedCount());
		values.put(DBConstants.V_TOTAL_SECONDS, dataObj.getTotalSeconds());
		values.put(DBConstants.V_TODAYS_ATTEMPTS, dataObj.getTodaysAttemps());
		values.put(DBConstants.V_TODAYS_AVG_SCORE, dataObj.getTodaysAvgScore());

		return values;
	}

	public static ContentValues putUserStatsChessMentorItemToValues(UserChessMentorStatsData.Rating dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, user);
		values.put(DBConstants.V_CURRENT, dataObj.getCurrent());
		values.put(DBConstants.V_HIGHEST_RATING, dataObj.getHighest().getRating());
		values.put(DBConstants.V_HIGHEST_TIMESTAMP, dataObj.getHighest().getTimestamp());
		values.put(DBConstants.V_LOWEST_RATING, dataObj.getLowest().getRating());
		values.put(DBConstants.V_LOWEST_TIMESTAMP, dataObj.getLowest().getTimestamp());

		values.put(DBConstants.V_LESSONS_TRIED, dataObj.getLessonsTried());
		values.put(DBConstants.V_TOTAL_LESSON_COUNT, dataObj.getTotalLessonCount());
		values.put(DBConstants.V_LESSON_COMPLETE_PERCENTAGE, dataObj.getLessonCompletePercentage());
		values.put(DBConstants.V_TOTAL_TRAINING_SECONDS, dataObj.getTotalTrainingSeconds());

		return values;
	}

	public static ContentValues putGameStatsLiveItemToValues(GameStatsItem.Data dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, user);
		values.put(DBConstants.V_CURRENT, dataObj.getRating().getCurrent());
		values.put(DBConstants.V_RANK, dataObj.getRating().getTodaysRank().getRank());
		values.put(DBConstants.V_TOTAL_PLAYER_COUNT, dataObj.getRating().getTodaysRank().getTotalPlayerCount());
		values.put(DBConstants.V_PERCENTILE, dataObj.getRating().getPercentile());
		values.put(DBConstants.V_GLICKO_RD, dataObj.getRating().getGlickoRd());

		values.put(DBConstants.V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(DBConstants.V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());
		values.put(DBConstants.V_LOWEST_RATING, dataObj.getRating().getLowest().getRating());
		values.put(DBConstants.V_LOWEST_TIMESTAMP, dataObj.getRating().getLowest().getTimestamp());
		values.put(DBConstants.V_AVERAGE_OPPONENT, dataObj.getRating().getAverageOpponent());

		values.put(DBConstants.V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(DBConstants.V_BEST_WIN_GAME_ID, dataObj.getRating().getBestWin().getGameId());
		values.put(DBConstants.V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(DBConstants.V_AVG_OPPONENT_RATING_WIN, dataObj.getRating().getAverageOpponentRating().getWin());
		values.put(DBConstants.V_AVG_OPPONENT_RATING_LOSE, dataObj.getRating().getAverageOpponentRating().getLose());
		values.put(DBConstants.V_AVG_OPPONENT_RATING_DRAW, dataObj.getRating().getAverageOpponentRating().getDraw());
		values.put(DBConstants.V_UNRATED, dataObj.getGames().getUnrated());
		values.put(DBConstants.V_IN_PROGRESS, dataObj.getGames().getInProgress());
		values.put(DBConstants.V_TIMEOUTS, dataObj.getGames().getTimeoutPercent());

		values.put(DBConstants.V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(DBConstants.V_GAMES_BLACK, dataObj.getGames().getBlack());
		values.put(DBConstants.V_GAMES_WHITE, dataObj.getGames().getWhite());

		values.put(DBConstants.V_WINS_TOTAL, dataObj.getGames().getWins().getTotal());
		values.put(DBConstants.V_WINS_WHITE, dataObj.getGames().getWins().getWhite());
		values.put(DBConstants.V_WINS_BLACK, dataObj.getGames().getWins().getBlack());

		values.put(DBConstants.V_LOSSES_TOTAL, dataObj.getGames().getLosses().getTotal());
		values.put(DBConstants.V_LOSSES_WHITE, dataObj.getGames().getLosses().getWhite());
		values.put(DBConstants.V_LOSSES_BLACK, dataObj.getGames().getLosses().getBlack());

		values.put(DBConstants.V_DRAWS_TOTAL, dataObj.getGames().getDraws().getTotal());
		values.put(DBConstants.V_DRAWS_WHITE, dataObj.getGames().getDraws().getWhite());
		values.put(DBConstants.V_DRAWS_BLACK, dataObj.getGames().getDraws().getBlack());

		values.put(DBConstants.V_WINNING_STREAK, dataObj.getGames().getWinningStreak());
		values.put(DBConstants.V_LOSING_STREAK, dataObj.getGames().getLosingStreak());
		if (dataObj.getGames().getMostFrequentOpponent() != null) {
			values.put(DBConstants.V_FREQUENT_OPPONENT_NAME, dataObj.getGames().getMostFrequentOpponent().getUsername());
			values.put(DBConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, dataObj.getGames().getMostFrequentOpponent().getGamesPlayed());
		} else {
			values.put(DBConstants.V_FREQUENT_OPPONENT_NAME, StaticData.SYMBOL_EMPTY);
			values.put(DBConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, 0);
		}

		return values;
	}

	public static ContentValues putGameStatsDailyItemToValues(GameStatsItem.Data dataObj, String user) {
		ContentValues values = new ContentValues();


		values.put(DBConstants.V_USER, user);
		GameStatsItem.GameRating rating = dataObj.getRating();
		if (rating != null) {
			values.put(DBConstants.V_CURRENT, rating.getCurrent());
			values.put(DBConstants.V_RANK, rating.getTodaysRank().getRank());
			values.put(DBConstants.V_TOTAL_PLAYER_COUNT, rating.getTodaysRank().getTotalPlayerCount());
			values.put(DBConstants.V_PERCENTILE, rating.getPercentile());
			values.put(DBConstants.V_GLICKO_RD, rating.getGlickoRd());

			values.put(DBConstants.V_HIGHEST_RATING, rating.getHighest().getRating());
			values.put(DBConstants.V_HIGHEST_TIMESTAMP, rating.getHighest().getTimestamp());
			values.put(DBConstants.V_LOWEST_RATING, rating.getLowest().getRating());
			values.put(DBConstants.V_LOWEST_TIMESTAMP, rating.getLowest().getTimestamp());
			values.put(DBConstants.V_AVERAGE_OPPONENT, rating.getAverageOpponent());

			values.put(DBConstants.V_BEST_WIN_RATING, rating.getBestWin().getRating());
			values.put(DBConstants.V_BEST_WIN_GAME_ID, rating.getBestWin().getGameId());
			values.put(DBConstants.V_BEST_WIN_USERNAME, rating.getBestWin().getUsername());
			values.put(DBConstants.V_AVG_OPPONENT_RATING_WIN, rating.getAverageOpponentRating().getWin());
			values.put(DBConstants.V_AVG_OPPONENT_RATING_LOSE, rating.getAverageOpponentRating().getLose());
			values.put(DBConstants.V_AVG_OPPONENT_RATING_DRAW, rating.getAverageOpponentRating().getDraw());
		}

		GameStatsItem.Games games = dataObj.getGames();
		if (games != null) {
			values.put(DBConstants.V_UNRATED, games.getUnrated());
			values.put(DBConstants.V_IN_PROGRESS, games.getInProgress());
			values.put(DBConstants.V_TIMEOUTS, games.getTimeoutPercent());

			values.put(DBConstants.V_GAMES_TOTAL, games.getTotal());
			values.put(DBConstants.V_GAMES_BLACK, games.getBlack());
			values.put(DBConstants.V_GAMES_WHITE, games.getWhite());

			values.put(DBConstants.V_WINS_TOTAL, games.getWins().getTotal());
			values.put(DBConstants.V_WINS_WHITE, games.getWins().getWhite());
			values.put(DBConstants.V_WINS_BLACK, games.getWins().getBlack());

			values.put(DBConstants.V_LOSSES_TOTAL, games.getLosses().getTotal());
			values.put(DBConstants.V_LOSSES_WHITE, games.getLosses().getWhite());
			values.put(DBConstants.V_LOSSES_BLACK, games.getLosses().getBlack());

			values.put(DBConstants.V_DRAWS_TOTAL, games.getDraws().getTotal());
			values.put(DBConstants.V_DRAWS_WHITE, games.getDraws().getWhite());
			values.put(DBConstants.V_DRAWS_BLACK, games.getDraws().getBlack());

			values.put(DBConstants.V_WINNING_STREAK, games.getWinningStreak());
			values.put(DBConstants.V_LOSING_STREAK, games.getLosingStreak());

			if (games.getMostFrequentOpponent() != null) {
				values.put(DBConstants.V_FREQUENT_OPPONENT_NAME, games.getMostFrequentOpponent().getUsername());
				values.put(DBConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, games.getMostFrequentOpponent().getGamesPlayed());
			} else {
				values.put(DBConstants.V_FREQUENT_OPPONENT_NAME, StaticData.SYMBOL_EMPTY);
				values.put(DBConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, 0);
			}

		}

		Tournaments tournaments = dataObj.getTournaments();
		if (tournaments != null) {
			Tournaments.All tournamentsAll = tournaments.getAll();
			values.put(DBConstants.V_TOURNAMENTS_LEADERBOARD_POINTS, tournamentsAll.getLeaderboardPoints());
			values.put(DBConstants.V_TOURNAMENTS_EVENTS_ENTERED, tournamentsAll.getEventsEntered());
			values.put(DBConstants.V_TOURNAMENTS_FIRST_PLACE_FINISHES, tournamentsAll.getFirstPlaceFinishes());
			values.put(DBConstants.V_TOURNAMENTS_SECOND_PLACE_FINISHES, tournamentsAll.getSecondPlaceFinishes());
			values.put(DBConstants.V_TOURNAMENTS_THIRD_PLACE_FINISHES, tournamentsAll.getThirdPlaceFinishes());
			values.put(DBConstants.V_TOURNAMENTS_WITHDRAWALS, tournamentsAll.getWithdrawals());
			values.put(DBConstants.V_TOURNAMENTS_HOSTED, tournamentsAll.getTournamentsHosted());
			values.put(DBConstants.V_TOTAL_COUNT_PLAYERS_HOSTED, tournamentsAll.getTotalCountPlayersHosted());

			Tournaments.Games tournamentsGames = tournaments.getGames();
			values.put(DBConstants.V_TOURNAMENTS_GAMES_TOTAL, tournamentsGames.getTotal());
			values.put(DBConstants.V_TOURNAMENTS_GAMES_WON, tournamentsGames.getWins());
			values.put(DBConstants.V_TOURNAMENTS_GAMES_LOST, tournamentsGames.getLosses());
			values.put(DBConstants.V_TOURNAMENTS_GAMES_DRAWN, tournamentsGames.getDraws());
			values.put(DBConstants.V_TOURNAMENTS_GAMES_IN_PROGRESS, tournamentsGames.getInProgress());
		}
		return values;
	}

	public static GamesInfoByResult getGameStatsGamesByResultFromCursor(Cursor cursor) {
		GamesInfoByResult tournamentGames = new GamesInfoByResult();
		tournamentGames.setTotal(getInt(cursor, DBConstants.V_GAMES_TOTAL));
		tournamentGames.setWins(getInt(cursor, DBConstants.V_WINS_TOTAL));
		tournamentGames.setLosses(getInt(cursor, DBConstants.V_LOSSES_TOTAL));
		tournamentGames.setDraws(getInt(cursor, DBConstants.V_DRAWS_TOTAL));
		return tournamentGames;
	}


	public static boolean checkIfDrawOffered(ContentResolver resolver, String userName, long gameId) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = userName;
		arguments2[1] = String.valueOf(gameId);

		Cursor cursor = resolver.query(DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()],
				PROJECTION_DAILY_DRAW_OFFERED, SELECTION_OPPONENT_OFFERED_DRAW, arguments2, null);
		return cursor != null && cursor.moveToFirst() && getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0;
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
