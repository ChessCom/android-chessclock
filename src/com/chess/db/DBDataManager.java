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
public class DbDataManager {
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
	public static final String LIMIT_1 = DbConstants._ID + " LIMIT 1";

	public static String[] sArguments1 = new String[1];
	public static String[] sArguments2 = new String[2];
	public static String[] sArguments3 = new String[3];

	// -------------- SELECTION DEFINITIONS ---------------------------

	public static String SELECTION_ITEM_ID_AND_USER = concatArguments(
			DbConstants.V_ID,
			DbConstants.V_USER);

	public static String SELECTION_USER = concatArguments(DbConstants.V_USER);

	public static String SELECTION_ID = concatArguments(DbConstants._ID);

	public static String SELECTION_USER_AND_ID = concatArguments(   // TODO remove duplicate
			DbConstants.V_USER,
			DbConstants.V_ID);

	public static String SELECTION_USER_ID = concatArguments(
			DbConstants.V_USER,
			DbConstants.V_USER_ID);

	public static String SELECTION_OPPONENT_OFFERED_DRAW = concatArguments(
			DbConstants.V_USER,
			DbConstants.V_ID,
			DbConstants.V_OPPONENT_OFFERED_DRAW);

	public static String SELECTION_USER_TURN = concatArguments(
			DbConstants.V_USER,
			DbConstants.V_IS_MY_TURN);

	public static String SELECTION_TITLE = concatArguments(DbConstants.V_TITLE);

	public static String SELECTION_CATEGORY_ID = concatArguments(DbConstants.V_CATEGORY_ID);

	public static String SELECTION_CATEGORY_ID_AND_PAGE = concatArguments(DbConstants.V_CATEGORY_ID, DbConstants.V_PAGE);

	public static String SELECTION_CATEGORY_ID_AND_USER = concatArguments(DbConstants.V_CATEGORY_ID, DbConstants.V_USER);

	public static String SELECTION_CATEGORY = concatArguments(DbConstants.V_CATEGORY);

	public static String SELECTION_ITEM_ID = concatArguments(DbConstants.V_ID);

	public static String SELECTION_ITEM_ID_AND_PAGE = concatArguments(DbConstants.V_ID, DbConstants.V_PAGE);

	public static String SELECTION_ITEM_ID_AND_NUMBER = concatArguments(DbConstants.V_ID, DbConstants.V_NUMBER);

	public static String SELECTION_ITEM_ID_AND_CURRENT_POSITION = concatArguments(
			DbConstants.V_ID,
			DbConstants.V_CURRENT_POSITION);

	public static String SELECTION_ITEM_ID_POSITION_NUMBER = concatArguments(
			DbConstants.V_ID,
			DbConstants.V_CURRENT_POSITION,
			DbConstants.V_NUMBER);

	public static String SELECTION_ID_CATEGORY_ID_USER = concatArguments(
			DbConstants.V_ID,
			DbConstants.V_CATEGORY_ID,
			DbConstants.V_USER
	);

	public static String SELECTION_CREATE_DATE = concatArguments(DbConstants.V_CREATE_DATE);

	public static String SELECTION_ID_USER_CONVERSATION_ID = concatArguments(
			DbConstants.V_ID,
			DbConstants.V_USER,
			DbConstants.V_CONVERSATION_ID
	);

	public static String SELECTION_USER_CONVERSATION_ID = concatArguments(
			DbConstants.V_USER,
			DbConstants.V_CONVERSATION_ID
	);

	public static String SELECTION_USER_AND_LESSON_STATED = concatArguments(
			DbConstants.V_USER,
			DbConstants.V_LESSON_STARTED
	);

	// -------------- PROJECTIONS DEFINITIONS ---------------------------

	public static final String[] PROJECTION_ITEM_ID_AND_USER = new String[]{
			DbConstants._ID,
			DbConstants.V_ID,
			DbConstants.V_USER
	};

	public static final String[] PROJECTION_USER = new String[]{
			DbConstants._ID,
			DbConstants.V_USER
	};

	public static final String[] PROJECTION_GAME_ID = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_ID
	};

	public static final String[] PROJECTION_USER_ID = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_USER_ID
	};

	public static final String[] PROJECTION_CURRENT_GAMES = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_ID,
			DbConstants.V_I_PLAY_AS,
			DbConstants.V_WHITE_USERNAME,
			DbConstants.V_BLACK_USERNAME,
			DbConstants.V_WHITE_AVATAR,
			DbConstants.V_BLACK_AVATAR,
			DbConstants.V_WHITE_PREMIUM_STATUS,
			DbConstants.V_BLACK_PREMIUM_STATUS,
			DbConstants.V_GAME_TYPE,
			DbConstants.V_IS_MY_TURN,
			DbConstants.V_TIMESTAMP,
			DbConstants.V_OPPONENT_OFFERED_DRAW,
			DbConstants.V_HAS_NEW_MESSAGE,
			DbConstants.V_TIME_REMAINING
	};

	public static final String[] PROJECTION_FINISHED_GAMES = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_ID,
			DbConstants.V_I_PLAY_AS,
			DbConstants.V_GAME_TYPE,
			DbConstants.V_GAME_SCORE,
			DbConstants.V_WHITE_USERNAME,
			DbConstants.V_BLACK_USERNAME,
			DbConstants.V_WHITE_AVATAR,
			DbConstants.V_BLACK_AVATAR,
			DbConstants.V_WHITE_RATING,
			DbConstants.V_BLACK_RATING,
			DbConstants.V_WHITE_PREMIUM_STATUS,
			DbConstants.V_BLACK_PREMIUM_STATUS
	};

	public static final String[] PROJECTION_DAILY_PLAYER_NAMES = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_I_PLAY_AS,
			DbConstants.V_WHITE_USERNAME,
			DbConstants.V_BLACK_USERNAME
	};

	public static final String[] PROJECTION_DAILY_DRAW_OFFERED = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_ID,
			DbConstants.V_OPPONENT_OFFERED_DRAW
	};

	public static final String[] PROJECTION_TITLE = new String[]{
			DbConstants._ID,
			DbConstants.V_TITLE
	};

	public static final String[] PROJECTION_USERNAME = new String[]{
			DbConstants._ID,
			DbConstants.V_USERNAME
	};

	public static final String[] PROJECTION_V_CATEGORY_ID = new String[]{
			DbConstants._ID,
			DbConstants.V_CATEGORY_ID
	};

	public static final String[] PROJECTION_ITEM_ID = new String[]{
			DbConstants._ID,
			DbConstants.V_ID
	};

	public static final String[] PROJECTION_CREATE_DATE = new String[]{
			DbConstants._ID,
			DbConstants.V_CREATE_DATE
	};

	public static final String[] PROJECTION_USER_CURRENT_RATING = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_CURRENT
	};

	public static final String[] PROJECTION_VIEWED_VIDEO = new String[]{
			DbConstants._ID,
			DbConstants.V_USER,
			DbConstants.V_ID,
			DbConstants.V_VIDEO_VIEWED
	};

	public static final String[] PROJECTION_ITEM_ID_AND_NUMBER = new String[]{
			DbConstants._ID,
			DbConstants.V_ID,
			DbConstants.V_NUMBER
	};

	public static final String[] PROJECTION_ITEM_ID_POSITION_NUMBER = new String[]{
			DbConstants._ID,
			DbConstants.V_ID,
			DbConstants.V_CURRENT_POSITION,
			DbConstants.V_NUMBER
	};

	public static final String[] PROJECTION_ID_CATEGORY_ID_USER = new String[]{
			DbConstants._ID,
			DbConstants.V_ID,
			DbConstants.V_CATEGORY_ID,
			DbConstants.V_USER
	};

	public static final String[] PROJECTION_ID_CATEGORY_ID_USER_COMPLETED = new String[]{
			DbConstants._ID,
			DbConstants.V_ID,
			DbConstants.V_CATEGORY_ID,
			DbConstants.V_USER,
			DbConstants.V_LESSON_COMPLETED
	};

	public static final String[] PROJECTION_ID_USER_CONVERSATION_ID = new String[]{
			DbConstants._ID,
			DbConstants.V_ID,
			DbConstants.V_USER,
			DbConstants.V_CONVERSATION_ID
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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.DAILY_CURRENT_GAMES.ordinal()],
				PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
		boolean exist = cursor != null && cursor.moveToFirst();

		if (!exist) { // check finished games list
			cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.DAILY_FINISHED_GAMES.ordinal()],
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

		Uri uri = DbConstants.uriArray[DbConstants.Tables.DAILY_CURRENT_GAMES.ordinal()];
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

		Uri uri = DbConstants.uriArray[DbConstants.Tables.DAILY_CURRENT_GAMES.ordinal()];
		long[] gamesIds;
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER, arguments1, null);
		if (cursor != null && cursor.moveToFirst()) {
			gamesIds = new long[cursor.getCount()];
			int i = 0;
			do {
				gamesIds[i++] = getLong(cursor, DbConstants.V_ID);
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

		Uri uri = DbConstants.uriArray[DbConstants.Tables.DAILY_FINISHED_GAMES.ordinal()];
		long[] gamesIds;
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER, arguments1, null);
		if (cursor != null && cursor.moveToFirst()) {
			gamesIds = new long[cursor.getCount()];
			int i = 0;
			do {
				gamesIds[i++] = getLong(cursor, DbConstants.V_ID);
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

		ContentProviderClient client = contentResolver.acquireContentProviderClient(DbConstants.PROVIDER_NAME);
		SQLiteDatabase dbHandle = ((DbDataProvider) client.getLocalContentProvider()).getDbHandle();
		StringBuilder projection = new StringBuilder();
		String selection = DbConstants.V_USER + EQUALS_ARG_ + AND_ + "(" + DbConstants.V_WHITE_USERNAME + NOT_EQUALS_ARG_
				+ AND_ + DbConstants.V_I_PLAY_AS + EQUALS_ARG_ + ")";

		QueryParams params = new QueryParams();
		params.setDbName(DbConstants.Tables.DAILY_FINISHED_GAMES.name());
		params.setProjection(PROJECTION_DAILY_PLAYER_NAMES);
		params.setSelection(selection);
		params.setArguments(arguments3);
		params.setCommands(GROUP_BY + StaticData.SYMBOL_SPACE + DbConstants.V_WHITE_USERNAME + ", " + DbConstants.V_BLACK_USERNAME);

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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.FRIENDS.ordinal()],
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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.TACTICS_BATCH.ordinal()],
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

		Uri uri = DbConstants.uriArray[DbConstants.Tables.TACTICS_BATCH.ordinal()];
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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.TACTICS_BATCH.ordinal()],
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

		Uri uri = DbConstants.uriArray[DbConstants.Tables.TACTICS_RESULTS.ordinal()];
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
		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.TACTICS_RESULTS.ordinal()],
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
		Cursor cursor = contentResolver.query(DbConstants.uriArray[dbUriCode],
				PROJECTION_USER_CURRENT_RATING, SELECTION_USER, arguments1, null);

		if (cursor.moveToFirst()) {
			int rating = getInt(cursor, DbConstants.V_CURRENT);
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

		values.put(DbConstants.V_USER, dataObj.getUser());
		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_FEN, dataObj.getInitialFen());
		values.put(DbConstants.V_MOVE_LIST, dataObj.getCleanMoveString());
		values.put(DbConstants.V_ATTEMPT_CNT, dataObj.getAttemptCnt());
		values.put(DbConstants.V_PASSED_CNT, dataObj.getPassedCnt());
		values.put(DbConstants.V_RATING, dataObj.getRating());
		values.put(DbConstants.V_AVG_SECONDS, dataObj.getAvgSeconds());
		values.put(DbConstants.V_SECONDS_SPENT, dataObj.getSecondsSpent());
		values.put(DbConstants.V_STOP, dataObj.isStop() ? 1 : 0);
		values.put(DbConstants.V_WAS_SHOWED, dataObj.isWasShowed() ? 1 : 0);
		values.put(DbConstants.V_IS_RETRY, dataObj.isRetry() ? 1 : 0);

		return values;
	}


	public static TacticItem.Data getTacticItemFromCursor(Cursor cursor) {
		TacticItem.Data dataObj = new TacticItem.Data();

		dataObj.setUser(getString(cursor, DbConstants.V_USER));
		dataObj.setId(getLong(cursor, DbConstants.V_ID));
		dataObj.setFen(getString(cursor, DbConstants.V_FEN));
		dataObj.setMoveList(getString(cursor, DbConstants.V_MOVE_LIST));
		dataObj.setAttemptCnt(getInt(cursor, DbConstants.V_ATTEMPT_CNT));
		dataObj.setPassedCnt(getInt(cursor, DbConstants.V_PASSED_CNT));
		dataObj.setRating(getInt(cursor, DbConstants.V_RATING));
		dataObj.setAvgSeconds(getInt(cursor, DbConstants.V_AVG_SECONDS));
		dataObj.setSecondsSpent(getInt(cursor, DbConstants.V_SECONDS_SPENT));
		dataObj.setStop(getInt(cursor, DbConstants.V_STOP) > 0);
		dataObj.setWasShowed(getInt(cursor, DbConstants.V_WAS_SHOWED) > 0);
		dataObj.setRetry(getInt(cursor, DbConstants.V_IS_RETRY) > 0);

		return dataObj;
	}

	public static ContentValues putTacticResultItemToValues(TacticRatingData dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, dataObj.getUser());
		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_SCORE, dataObj.getScoreStr());
		values.put(DbConstants.V_USER_RATING_CHANGE, dataObj.getUserRatingChange());
		values.put(DbConstants.V_USER_RATING, dataObj.getUserRating());
		values.put(DbConstants.V_PROBLEM_RATING_CHANGE, dataObj.getProblemRatingChange());
		values.put(DbConstants.V_PROBLEM_RATING, dataObj.getProblemRating());

		return values;
	}


	public static TacticRatingData getTacticResultItemFromCursor(Cursor cursor) {
		TacticRatingData dataObj = new TacticRatingData();

		dataObj.setUser(getString(cursor, DbConstants.V_USER));
		dataObj.setId(getLong(cursor, DbConstants.V_ID));
		dataObj.setScore(getString(cursor, DbConstants.V_SCORE));
		dataObj.setUserRatingChange(getInt(cursor, DbConstants.V_USER_RATING_CHANGE));
		dataObj.setUserRating(getInt(cursor, DbConstants.V_USER_RATING));
		dataObj.setProblemRatingChange(getInt(cursor, DbConstants.V_PROBLEM_RATING_CHANGE));
		dataObj.setProblemRating(getInt(cursor, DbConstants.V_PROBLEM_RATING));

		return dataObj;
	}

	// ----------------------------------- Daily Games -------------------------------------------------------
	public static ContentValues putDailyFinishedGameToValues(DailyFinishedGameData dataObj, String userName) {
		ContentValues values = new ContentValues();

		setValuesFromGameItem(values, dataObj);
		values.put(DbConstants.V_FINISHED, 1);
		values.put(DbConstants.V_USER, userName);
		values.put(DbConstants.V_GAME_SCORE, dataObj.getGameScore());
		values.put(DbConstants.V_RESULT_MESSAGE, dataObj.getResultMessage());
		return values;
	}

	public static DailyFinishedGameData getDailyFinishedGameFromCursor(Cursor cursor) {
		DailyFinishedGameData dataObj = new DailyFinishedGameData();

		setDailyGameFromCursor(dataObj, cursor);
		dataObj.setGameScore(getInt(cursor, DbConstants.V_GAME_SCORE));
		dataObj.setResultMessage(getString(cursor, DbConstants.V_RESULT_MESSAGE));

		return dataObj;
	}

	public static ContentValues putDailyGameCurrentItemToValues(DailyCurrentGameData dataObj, String userName) {
		ContentValues values = new ContentValues();

		setValuesFromGameItem(values, dataObj);
		values.put(DbConstants.V_FINISHED, 0);
		values.put(DbConstants.V_USER, userName);
		values.put(DbConstants.V_OPPONENT_OFFERED_DRAW, dataObj.isDrawOffered() ? 1 : 0);
		values.put(DbConstants.V_IS_MY_TURN, dataObj.isMyTurn() ? 1 : 0);
		return values;
	}

	private static void setValuesFromGameItem(ContentValues values, DailyGameBaseData dataObj) { // TODO remove
		values.put(DbConstants.V_ID, dataObj.getGameId());
		values.put(DbConstants.V_FEN, dataObj.getFen());
		values.put(DbConstants.V_I_PLAY_AS, dataObj.getMyColor());
		values.put(DbConstants.V_GAME_TYPE, dataObj.getGameType());
		values.put(DbConstants.V_WHITE_USERNAME, dataObj.getWhiteUsername());
		values.put(DbConstants.V_BLACK_USERNAME, dataObj.getBlackUsername());
		values.put(DbConstants.V_WHITE_RATING, dataObj.getWhiteRating());
		values.put(DbConstants.V_BLACK_RATING, dataObj.getBlackRating());
		values.put(DbConstants.V_WHITE_PREMIUM_STATUS, dataObj.getWhitePremiumStatus());
		values.put(DbConstants.V_BLACK_PREMIUM_STATUS, dataObj.getBlackPremiumStatus());
		values.put(DbConstants.V_WHITE_AVATAR, dataObj.getWhiteAvatar());
		values.put(DbConstants.V_BLACK_AVATAR, dataObj.getBlackAvatar());
		values.put(DbConstants.V_TIME_REMAINING, dataObj.getTimeRemaining());
		values.put(DbConstants.V_TIMESTAMP, dataObj.getTimestamp());
		values.put(DbConstants.V_LAST_MOVE_FROM_SQUARE, dataObj.getLastMoveFromSquare());
		values.put(DbConstants.V_LAST_MOVE_TO_SQUARE, dataObj.getLastMoveToSquare());
		values.put(DbConstants.V_GAME_NAME, dataObj.getName());
		values.put(DbConstants.V_FEN_START_POSITION, dataObj.getStartingFenPosition());
		values.put(DbConstants.V_MOVE_LIST, dataObj.getMoveList());
		values.put(DbConstants.V_HAS_NEW_MESSAGE, dataObj.hasNewMessage() ? 1 : 0);
		values.put(DbConstants.V_RATED, dataObj.isRated() ? 1 : 0);
		values.put(DbConstants.V_DAYS_PER_MOVE, dataObj.getDaysPerMove());
		values.put(DbConstants.V_IS_OPPONENT_ONLINE, dataObj.isOpponentOnline());
	}

	public static DailyCurrentGameData getDailyCurrentGameFromCursor(Cursor cursor) {
		DailyCurrentGameData dataObj = new DailyCurrentGameData();

		setDailyGameFromCursor(dataObj, cursor);
		dataObj.setDrawOffered(getInt(cursor, DbConstants.V_OPPONENT_OFFERED_DRAW) > 0);
		dataObj.setMyTurn(getInt(cursor, DbConstants.V_IS_MY_TURN) > 0);

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

		dataObj.setGameId(getLong(cursor, DbConstants.V_ID));
		dataObj.setWhiteUsername(getString(cursor, DbConstants.V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, DbConstants.V_BLACK_USERNAME));
		dataObj.setWhiteAvatar(getString(cursor, DbConstants.V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, DbConstants.V_BLACK_AVATAR));
		dataObj.setGameType(getInt(cursor, DbConstants.V_GAME_TYPE));
		dataObj.setMyTurn(getInt(cursor, DbConstants.V_IS_MY_TURN) > 0);
		dataObj.setTimestamp(getLong(cursor, DbConstants.V_TIMESTAMP));
		dataObj.setDrawOffered(getInt(cursor, DbConstants.V_OPPONENT_OFFERED_DRAW) > 0);
		dataObj.setHasNewMessage(getInt(cursor, DbConstants.V_HAS_NEW_MESSAGE) > 0);
		dataObj.setTimeRemaining(getLong(cursor, DbConstants.V_TIME_REMAINING));

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

		dataObj.setGameId(getLong(cursor, DbConstants.V_ID));
		dataObj.setGameType(getInt(cursor, DbConstants.V_GAME_TYPE));
		dataObj.setGameScore(getInt(cursor, DbConstants.V_GAME_SCORE));
		dataObj.setWhiteUsername(getString(cursor, DbConstants.V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, DbConstants.V_BLACK_USERNAME));
		dataObj.setWhiteAvatar(getString(cursor, DbConstants.V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, DbConstants.V_BLACK_AVATAR));
		dataObj.setWhiteRating(getInt(cursor, DbConstants.V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, DbConstants.V_BLACK_RATING));

		return dataObj;
	}

	private static void setDailyGameFromCursor(DailyGameBaseData dataObj, Cursor cursor) {
		dataObj.setGameId(getLong(cursor, DbConstants.V_ID));
		dataObj.setFen(getString(cursor, DbConstants.V_FEN));
		dataObj.setIPlayAs(getInt(cursor, DbConstants.V_I_PLAY_AS));
		dataObj.setLastMoveFromSquare(getString(cursor, DbConstants.V_LAST_MOVE_FROM_SQUARE));
		dataObj.setLastMoveToSquare(getString(cursor, DbConstants.V_LAST_MOVE_TO_SQUARE));
		dataObj.setGameType(getInt(cursor, DbConstants.V_GAME_TYPE));
		dataObj.setTimestamp(getLong(cursor, DbConstants.V_TIMESTAMP));
		dataObj.setName(getString(cursor, DbConstants.V_GAME_NAME));
		dataObj.setWhiteUsername(getString(cursor, DbConstants.V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, DbConstants.V_BLACK_USERNAME));
		dataObj.setWhitePremiumStatus(getInt(cursor, DbConstants.V_WHITE_PREMIUM_STATUS));
		dataObj.setBlackPremiumStatus(getInt(cursor, DbConstants.V_BLACK_PREMIUM_STATUS));
		dataObj.setStartingFenPosition(getString(cursor, DbConstants.V_FEN_START_POSITION));
		dataObj.setMoveList(getString(cursor, DbConstants.V_MOVE_LIST));
		dataObj.setWhiteRating(getInt(cursor, DbConstants.V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, DbConstants.V_BLACK_RATING));
		dataObj.setWhiteAvatar(getString(cursor, DbConstants.V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, DbConstants.V_BLACK_AVATAR));
		dataObj.setHasNewMessage(getInt(cursor, DbConstants.V_HAS_NEW_MESSAGE) > 0);
		dataObj.setTimeRemaining(getLong(cursor, DbConstants.V_TIME_REMAINING));
		dataObj.setRated(getInt(cursor, DbConstants.V_RATED) > 0);
		dataObj.setDaysPerMove(getInt(cursor, DbConstants.V_DAYS_PER_MOVE));
	}

	// ------------------------------------- Friends ----------------------------------------------
	public static ContentValues putFriendItemToValues(FriendsItem.Data dataObj, String userName) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, userName);
		values.put(DbConstants.V_USERNAME, dataObj.getUsername());
		values.put(DbConstants.V_USER_ID, dataObj.getUserId());
		values.put(DbConstants.V_LOCATION, dataObj.getLocation());
		values.put(DbConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DbConstants.V_PHOTO_URL, dataObj.getAvatarUrl());
		values.put(DbConstants.V_IS_OPPONENT_ONLINE, dataObj.isOnline() ? 1 : 0);
		values.put(DbConstants.V_PREMIUM_STATUS, dataObj.getPremiumStatus());
		values.put(DbConstants.V_LAST_LOGIN_DATE, dataObj.getLastLoginDate());

		return values;
	}

	public static ContentValues putArticleItemToValues(ArticleItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_TITLE, dataObj.getTitle());
		values.put(DbConstants.V_CREATE_DATE, dataObj.getCreate_date());
		values.put(DbConstants.V_CATEGORY, dataObj.getCategoryName());
		values.put(DbConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DbConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DbConstants.V_USER_ID, dataObj.getUserId());
		values.put(DbConstants.V_USERNAME, dataObj.getUsername());
		values.put(DbConstants.V_FIRST_NAME, dataObj.getFirstName());
		values.put(DbConstants.V_LAST_NAME, dataObj.getLastName());
		values.put(DbConstants.V_CHESS_TITLE, dataObj.getChessTitle());
		values.put(DbConstants.V_USER_AVATAR, dataObj.getAvatar());
		values.put(DbConstants.V_PHOTO_URL, dataObj.getImageUrl());
		values.put(DbConstants.V_THUMB_CONTENT, dataObj.isIsThumbInContent());

		return values;
	}

	public static void updateVideoItem(ContentResolver contentResolver, VideoItem.Data currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getVideoId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.VIDEOS.ordinal()];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
				DbDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DbDataManager.putVideoItemToValues(currentItem);

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
		Uri uri = DbConstants.uriArray[DbConstants.Tables.FORUM_TOPICS.ordinal()];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
				DbDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DbDataManager.putForumTopicItemToValues(currentItem);

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
		Uri uri = DbConstants.uriArray[DbConstants.Tables.FORUM_CATEGORIES.ordinal()];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
				DbDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DbDataManager.putForumCategoryItemToValues(currentItem);

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
		Uri uri = DbConstants.uriArray[DbConstants.Tables.FORUM_POSTS.ordinal()];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_CREATE_DATE,
				DbDataManager.SELECTION_CREATE_DATE, arguments1, null);

		ContentValues values = DbDataManager.putForumPostItemToValues(currentItem);

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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.VIDEOS.ordinal()],
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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.VIDEOS.ordinal()],
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

		Uri uri = DbConstants.uriArray[DbConstants.Tables.VIDEO_VIEWED.ordinal()];

		Cursor cursor = contentResolver.query(uri, null,
				DbDataManager.SELECTION_USER_AND_ID, arguments2, null);

		ContentValues values = DbDataManager.putVideoViewedItemToValues(currentItem);

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
		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.VIDEO_VIEWED.ordinal()],
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

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.VIDEO_VIEWED.ordinal()],
				null, SELECTION_USER_AND_ID, arguments2, null);

		return cursor != null && cursor.moveToFirst() && getInt(cursor, DbConstants.V_VIDEO_VIEWED) > 0;
	}

	/**
	 * Check if we have saved articles for any user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedArticles(Context context) {
		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(DbConstants.uriArray[DbConstants.Tables.ARTICLES.ordinal()], null, null, null, LIMIT_1);
		boolean exist = cursor.moveToFirst();
		cursor.close();

		return exist;
	}


	public static ContentValues putVideoItemToValues(VideoItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_TITLE, dataObj.getTitle());
		values.put(DbConstants.V_DESCRIPTION, dataObj.getDescription());
		values.put(DbConstants.V_CATEGORY, dataObj.getCategoryName());
		values.put(DbConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DbConstants.V_ID, dataObj.getVideoId());
		values.put(DbConstants.V_SKILL_LEVEL, dataObj.getSkillLevel());
		values.put(DbConstants.V_USER_AVATAR, dataObj.getUserAvatar());
		values.put(DbConstants.V_MINUTES, dataObj.getMinutes());
		values.put(DbConstants.V_VIEW_COUNT, dataObj.getViewCount());
		values.put(DbConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DbConstants.V_COMMENT_COUNT, dataObj.getCommentCount());
		values.put(DbConstants.V_CREATE_DATE, dataObj.getCreateDate());
		values.put(DbConstants.V_URL, dataObj.getUrl());
		values.put(DbConstants.V_KEY_FEN, dataObj.getKeyFen());
		values.put(DbConstants.V_USERNAME, dataObj.getUsername());
		values.put(DbConstants.V_FIRST_NAME, dataObj.getFirstName());
		values.put(DbConstants.V_LAST_NAME, dataObj.getLastName());
		values.put(DbConstants.V_CHESS_TITLE, dataObj.getChessTitle());

		return values;
	}

	public static ContentValues putForumCategoryItemToValues(ForumCategoryItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_NAME, dataObj.getCategory());
		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_CREATE_DATE, dataObj.getCreateDate());
		values.put(DbConstants.V_LAST_POST_DATE, dataObj.getLastDate());
		values.put(DbConstants.V_DISPLAY_ORDER, dataObj.getDisplayOrder());
		values.put(DbConstants.V_DESCRIPTION, dataObj.getDescription());
		values.put(DbConstants.V_TOPIC_COUNT, dataObj.getTopicCount());
		values.put(DbConstants.V_POST_COUNT, dataObj.getPostCount());
		values.put(DbConstants.V_MIN_MEMBERSHIP, dataObj.getMinimumMembershipLevel());

		return values;
	}

	public static ContentValues putForumTopicItemToValues(ForumTopicItem.Topic dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_TITLE, dataObj.getSubject());
		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DbConstants.V_CATEGORY, dataObj.getCategoryName());
		values.put(DbConstants.V_URL, dataObj.getUrl());
		values.put(DbConstants.V_USERNAME, dataObj.getTopicUsername());
		values.put(DbConstants.V_LAST_POST_USERNAME, dataObj.getLastPostUsername());
		values.put(DbConstants.V_POST_COUNT, dataObj.getPostCount());
		values.put(DbConstants.V_LAST_POST_DATE, dataObj.getLastPostDate());
		values.put(DbConstants.V_PAGE, dataObj.getPage());

		return values;
	}

	public static ContentValues putForumPostItemToValues(ForumPostItem.Post dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_DESCRIPTION, dataObj.getBody());
		values.put(DbConstants.V_ID, dataObj.getTopicId());
		values.put(DbConstants.V_CREATE_DATE, dataObj.getCreateDate());
		values.put(DbConstants.V_USERNAME, dataObj.getUsername());
		values.put(DbConstants.V_COUNTRY_ID, dataObj.getCountryId());
		values.put(DbConstants.V_PREMIUM_STATUS, dataObj.isPremiumStatus());
		values.put(DbConstants.V_PHOTO_URL, dataObj.getAvatarUrl());
		values.put(DbConstants.V_NUMBER, dataObj.getCommentNumber());
		values.put(DbConstants.V_PAGE, dataObj.getPage());

		return values;
	}

	public static void saveMentorLessonToDb(ContentResolver contentResolver, LessonItem.MentorLesson mentorLesson,
											long lessonId) {
		mentorLesson.setLessonId(lessonId);
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(lessonId);


		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_MENTOR_LESSONS.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
				DbDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DbDataManager.putLessonsMentorLessonToValues(mentorLesson);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	public static void saveUserLessonToDb(ContentResolver contentResolver, LessonItem.UserLesson userLesson,
										  long lessonId, String username) {
		userLesson.setLessonId(lessonId);
		userLesson.setUsername(username);

		final String[] arguments1 = sArguments2;
		arguments1[0] = String.valueOf(userLesson.getLessonId());
		arguments1[1] = username;


		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_USER_LESSONS.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
				DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments1, null);

		ContentValues values = DbDataManager.putLessonsUserLessonToValues(userLesson);

		if (cursor != null &&cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	public static void saveCourseListItemToDb(ContentResolver contentResolver, LessonCourseListItem.Data currentItem){
		final String[] arguments = sArguments2;
		arguments[0] = String.valueOf(currentItem.getId());
		arguments[1] = currentItem.getUser();

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_COURSE_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
				DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);


		ContentValues values = DbDataManager.putLessonsCourseListItemToValues(currentItem);

		if (cursor != null &&cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	public static ContentValues putLessonsCourseListItemToValues(LessonCourseListItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_USER, dataObj.getUser());
		values.put(DbConstants.V_NAME, dataObj.getName());
		values.put(DbConstants.V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(DbConstants.V_COURSE_COMPLETED, dataObj.isCourseCompleted()? 1 : 0);

		return values;
	}

	public static ContentValues putLessonsCourseItemToValues(LessonCourseItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_DESCRIPTION, dataObj.getDescription());
		values.put(DbConstants.V_NAME, dataObj.getCourseName());

		return values;
	}

	public static LessonCourseItem.Data getLessonsCourseItemFromCursor(Cursor cursor) {
		LessonCourseItem.Data dataObj = new LessonCourseItem.Data();

		dataObj.setId(getLong(cursor, DbConstants.V_ID));
		dataObj.setDescription(getString(cursor, DbConstants.V_DESCRIPTION));
		dataObj.setCourseName(getString(cursor, DbConstants.V_NAME));

		return dataObj;
	}

	public static void saveLessonListItemToDb(ContentResolver contentResolver, LessonListItem lesson){
		final String[] arguments = sArguments3;
		arguments[0] = String.valueOf(lesson.getId());
		arguments[1] = String.valueOf(lesson.getCourseId());
		arguments[2] = lesson.getUser();

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_LESSONS_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ID_CATEGORY_ID_USER,
				DbDataManager.SELECTION_ID_CATEGORY_ID_USER, arguments, null);

		ContentValues values = DbDataManager.putLessonsListItemToValues(lesson);

		if (cursor != null && cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	public static boolean isLessonCompleted(ContentResolver contentResolver, int lessonId, long courseId, String username) {
		final String[] arguments = sArguments3;
		arguments[0] = String.valueOf(lessonId);
		arguments[1] = String.valueOf(courseId);
		arguments[2] = username;

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_LESSONS_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ID_CATEGORY_ID_USER_COMPLETED,
				DbDataManager.SELECTION_ID_CATEGORY_ID_USER, arguments, null);


		if (cursor != null && cursor.moveToFirst()) {
			boolean completed = getInt(cursor, DbConstants.V_LESSON_COMPLETED) > 0;
			cursor.close();

			return completed;
		}
		return false;
	}

	public static List<LessonListItem> getIncompleteLessons(ContentResolver contentResolver, String username) {
		final String[] arguments = sArguments2;
		arguments[0] = username;
		arguments[1] = String.valueOf(1);

		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_LESSONS_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, null, DbDataManager.SELECTION_USER_AND_LESSON_STATED, arguments, null);

		if (cursor != null && cursor.moveToFirst()) {
			List<LessonListItem> incompleteLessons = new ArrayList<LessonListItem>();
			do {
				incompleteLessons.add(getLessonsListItemFromCursor(cursor));
			} while(cursor.moveToNext());
			return incompleteLessons;
		}
		return null;
	}

	public static ContentValues putLessonsListItemToValues(LessonListItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_CATEGORY_ID, dataObj.getCourseId());
		values.put(DbConstants.V_LESSON_COMPLETED, dataObj.isCompleted()? 1: 0);
		values.put(DbConstants.V_LESSON_STARTED, dataObj.isStarted()? 1: 0);
		values.put(DbConstants.V_USER, dataObj.getUser());
		values.put(DbConstants.V_NAME, dataObj.getName());

		return values;
	}

	public static LessonListItem getLessonsListItemFromCursor(Cursor cursor) {
		LessonListItem dataObj = new LessonListItem();

		dataObj.setId(getInt(cursor, DbConstants.V_ID));
		dataObj.setUser(getString(cursor, DbConstants.V_USER));
		dataObj.setCourseId(getLong(cursor, DbConstants.V_CATEGORY_ID));
		dataObj.setCompleted(getInt(cursor, DbConstants.V_LESSON_COMPLETED) > 0);
		dataObj.setStarted(getInt(cursor, DbConstants.V_LESSON_STARTED) > 0);
		dataObj.setName(getString(cursor, DbConstants.V_NAME));

		return dataObj;
	}

	public static ContentValues putLessonsMentorLessonToValues(LessonItem.MentorLesson dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getLessonId());
		values.put(DbConstants.V_NUMBER, dataObj.getLessonNumber());
		values.put(DbConstants.V_GOAL, dataObj.getGoal());
		values.put(DbConstants.V_DIFFICULTY, dataObj.getDifficulty());
		values.put(DbConstants.V_AUTHOR, dataObj.getAuthor());
		values.put(DbConstants.V_NAME, dataObj.getName());
		values.put(DbConstants.V_DESCRIPTION, dataObj.getAbout());
		values.put(DbConstants.V_GOAL_COMMENT, dataObj.getGoalCommentary());
		values.put(DbConstants.V_GOAL_CODE, dataObj.getGoalCode());

		return values;
	}

	public static LessonItem.MentorLesson getLessonsMentorLessonFromCursor(Cursor cursor) {
		LessonItem.MentorLesson dataObj = new LessonItem.MentorLesson();

		dataObj.setLessonId(getLong(cursor, DbConstants.V_ID));
		dataObj.setLessonNumber(getInt(cursor, DbConstants.V_NUMBER));
		dataObj.setGoal(getInt(cursor, DbConstants.V_GOAL));
		dataObj.setDifficulty(getInt(cursor, DbConstants.V_DIFFICULTY));
		dataObj.setAuthor(getString(cursor, DbConstants.V_AUTHOR));
		dataObj.setName(getString(cursor, DbConstants.V_NAME));
		dataObj.setAbout(getString(cursor, DbConstants.V_DESCRIPTION));
		dataObj.setGoalCommentary(getString(cursor, DbConstants.V_GOAL_COMMENT));
		dataObj.setGoalCode(getString(cursor, DbConstants.V_GOAL_CODE));

		return dataObj;
	}

	public static ContentValues putLessonsPositionToValues(LessonItem.MentorPosition dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getLessonId());
		values.put(DbConstants.V_NUMBER, dataObj.getPositionNumber());
		values.put(DbConstants.V_USER_TO_MOVE, dataObj.getUserToMove());
		values.put(DbConstants.V_DIFFICULTY, dataObj.getMoveDifficulty());
		values.put(DbConstants.V_FINAL_POSITION, dataObj.getFinalPosition());
		values.put(DbConstants.V_FEN, dataObj.getFen());
		values.put(DbConstants.V_ADVICE_1, dataObj.getAdvice1());
		values.put(DbConstants.V_ADVICE_2, dataObj.getAdvice2());
		values.put(DbConstants.V_ADVICE_3, dataObj.getAdvice3());
		values.put(DbConstants.V_RESPONSE_MOVE_COMMENT, dataObj.getStandardResponseMoveCommentary());
		values.put(DbConstants.V_WRONG_MOVE_COMMENT, dataObj.getStandardWrongMoveCommentary());
		values.put(DbConstants.V_DESCRIPTION, dataObj.getAbout());

		return values;
	}

	public static LessonItem.MentorPosition getLessonsPositionFromCursor(Cursor cursor) {
		LessonItem.MentorPosition dataObj = new LessonItem.MentorPosition();

		dataObj.setLessonId(getLong(cursor, DbConstants.V_ID));
		dataObj.setPositionNumber(getInt(cursor, DbConstants.V_NUMBER));
		dataObj.setUserToMove(getInt(cursor, DbConstants.V_USER_TO_MOVE));
		dataObj.setMoveDifficulty(getInt(cursor, DbConstants.V_DIFFICULTY));
		dataObj.setFinalPosition(getInt(cursor, DbConstants.V_FINAL_POSITION));
		dataObj.setFen(getString(cursor, DbConstants.V_FEN));
		dataObj.setAdvice1(getString(cursor, DbConstants.V_ADVICE_1));
		dataObj.setAdvice2(getString(cursor, DbConstants.V_ADVICE_2));
		dataObj.setAdvice3(getString(cursor, DbConstants.V_ADVICE_3));
		dataObj.setStandardResponseMoveCommentary(getString(cursor, DbConstants.V_RESPONSE_MOVE_COMMENT));
		dataObj.setStandardWrongMoveCommentary(getString(cursor, DbConstants.V_WRONG_MOVE_COMMENT));
		dataObj.setAbout(getString(cursor, DbConstants.V_DESCRIPTION));

		return dataObj;
	}

	public static ContentValues putLessonsPositionMoveToValues(LessonItem.MentorPosition.PossibleMove dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getLessonId());
		values.put(DbConstants.V_CURRENT_POSITION, dataObj.getPositionNumber());
		values.put(DbConstants.V_NUMBER, dataObj.getMoveNumber());
		values.put(DbConstants.V_MOVE, dataObj.getMove());
		values.put(DbConstants.V_MOVE_COMMENT, dataObj.getMoveCommentary());
		values.put(DbConstants.V_SHORT_RESPONSE_MOVE, dataObj.getShortResponseMove());
		values.put(DbConstants.V_RESPONSE_MOVE_COMMENT, dataObj.getResponseMoveCommentary());
		values.put(DbConstants.V_MOVE_TYPE, dataObj.getMoveType());

		return values;
	}

	public static LessonItem.MentorPosition.PossibleMove getLessonsPositionMoveFromCursor(Cursor cursor) {
		LessonItem.MentorPosition.PossibleMove dataObj = new LessonItem.MentorPosition.PossibleMove();

		dataObj.setLessonId(getLong(cursor, DbConstants.V_ID));
		dataObj.setPositionNumber(getInt(cursor, DbConstants.V_CURRENT_POSITION));
		dataObj.setMoveNumber(getInt(cursor, DbConstants.V_NUMBER));
		dataObj.setMove(getString(cursor, DbConstants.V_MOVE));
		dataObj.setMoveCommentary(getString(cursor, DbConstants.V_MOVE_COMMENT));
		dataObj.setShortResponseMove(getString(cursor, DbConstants.V_SHORT_RESPONSE_MOVE));
		dataObj.setResponseMoveCommentary(getString(cursor, DbConstants.V_RESPONSE_MOVE_COMMENT));
		dataObj.setMoveType(getString(cursor, DbConstants.V_MOVE_TYPE));

		return dataObj;
	}

	public static ContentValues putLessonsUserLessonToValues(LessonItem.UserLesson dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getLessonId());
		values.put(DbConstants.V_CURRENT_POSITION, dataObj.getCurrentPosition());
		values.put(DbConstants.V_CURRENT_POINTS, dataObj.getCurrentPoints());
		values.put(DbConstants.V_CURRENT_POSITION_POINTS, dataObj.getCurrentPositionPoints());
		values.put(DbConstants.V_USER, dataObj.getUsername());
		values.put(DbConstants.V_INITIAL_SCORE, dataObj.getInitialScore());
		values.put(DbConstants.V_LAST_SCORE, dataObj.getLastScore());
		values.put(DbConstants.V_LEGAL_POSITION_CHECK, dataObj.getLegalPositionCheck());
		values.put(DbConstants.V_LEGAL_MOVE_CHECK, dataObj.getLegalMoveCheck());
		values.put(DbConstants.V_LESSON_COMPLETED, dataObj.isLessonCompleted()? 1 : 0);

		return values;
	}

	public static LessonItem.UserLesson getLessonsUserLessonFromCursor(Cursor cursor) {
		LessonItem.UserLesson dataObj = new LessonItem.UserLesson();

		dataObj.setLessonId(getLong(cursor, DbConstants.V_ID));
		dataObj.setCurrentPosition(getInt(cursor, DbConstants.V_CURRENT_POSITION));
		dataObj.setCurrentPoints(getInt(cursor, DbConstants.V_CURRENT_POINTS));
		dataObj.setCurrentPositionPoints(Float.valueOf(getString(cursor, DbConstants.V_CURRENT_POSITION_POINTS)));
		dataObj.setUsername(getString(cursor, DbConstants.V_USER));
		dataObj.setInitialScore(getString(cursor, DbConstants.V_INITIAL_SCORE));
		dataObj.setLastScore(getString(cursor, DbConstants.V_LAST_SCORE));
		dataObj.setLegalPositionCheck(getString(cursor, DbConstants.V_LEGAL_POSITION_CHECK));
		dataObj.setLegalMoveCheck(getString(cursor, DbConstants.V_LEGAL_MOVE_CHECK));
		dataObj.setLessonCompleted(getInt(cursor, DbConstants.V_LESSON_COMPLETED) > 0);

		return dataObj;
	}

	public static ContentValues putVideoViewedItemToValues(VideoViewedItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, dataObj.getUsername());
		values.put(DbConstants.V_ID, dataObj.getVideoId());
		values.put(DbConstants.V_VIDEO_VIEWED, dataObj.isViewed() ? 1 : 0);

		return values;
	}

	public static ContentValues putCommonFeedCategoryItemToValues(CommonFeedCategoryItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_NAME, dataObj.getName());
		values.put(DbConstants.V_CATEGORY_ID, dataObj.getId());
		values.put(DbConstants.V_DISPLAY_ORDER, dataObj.getDisplayOrder());

		return values;
	}

	/* ========================================== Messages ========================================== */
	public static ContentValues putConversationItemToValues(ConversationItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_OTHER_USER_ID, dataObj.getOtherUserId());
		values.put(DbConstants.V_LAST_MESSAGE_ID, dataObj.getLastMessageId());
		values.put(DbConstants.V_LAST_MESSAGE_CREATED_AT, dataObj.getLastMessageCreatedAt());
		values.put(DbConstants.V_OTHER_USER_IS_ONLINE, dataObj.isOtherUserIsOnline() ? 1 : 0);
		values.put(DbConstants.V_NEW_MESSAGES_COUNT, dataObj.getNewMessagesCount());
		values.put(DbConstants.V_USER, dataObj.getUser());
		values.put(DbConstants.V_OTHER_USER_USERNAME, dataObj.getOtherUserUsername());
		values.put(DbConstants.V_OTHER_USER_AVATAR_URL, dataObj.getOtherUserAvatarUrl());
		values.put(DbConstants.V_LAST_MESSAGE_SENDER_USERNAME, dataObj.getLastMessageSenderUsername());
		values.put(DbConstants.V_LAST_MESSAGE_CONTENT, dataObj.getLastMessageContent());

		return values;
	}

	public static ContentValues putMessagesItemToValues(MessagesItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_ID, dataObj.getId());
		values.put(DbConstants.V_CONVERSATION_ID, dataObj.getConversationId());
		values.put(DbConstants.V_OTHER_USER_ID, dataObj.getSenderId());
		values.put(DbConstants.V_CREATE_DATE, dataObj.getCreatedAt());
		values.put(DbConstants.V_OTHER_USER_IS_ONLINE, dataObj.isSenderIsOnline() ? 1 : 0);
		values.put(DbConstants.V_USER, dataObj.getUser());
		values.put(DbConstants.V_OTHER_USER_USERNAME, dataObj.getSenderUsername());
		values.put(DbConstants.V_OTHER_USER_AVATAR_URL, dataObj.getSenderAvatarUrl());
		values.put(DbConstants.V_LAST_MESSAGE_CONTENT, dataObj.getContent());

		return values;
	}


	/* ========================================== Stats ========================================== */

	public static ContentValues putUserStatsLiveItemToValues(UserLiveStatsData.Stats dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, user);
		values.put(DbConstants.V_CURRENT, dataObj.getRating().getCurrent());
		values.put(DbConstants.V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(DbConstants.V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());
		values.put(DbConstants.V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(DbConstants.V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(DbConstants.V_AVERAGE_OPPONENT, dataObj.getRating().getAverageOpponent());

		values.put(DbConstants.V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(DbConstants.V_GAMES_WINS, dataObj.getGames().getWins());
		values.put(DbConstants.V_GAMES_LOSSES, dataObj.getGames().getLosses());
		values.put(DbConstants.V_GAMES_DRAWS, dataObj.getGames().getDraws());

		return values;
	}

	public static ContentValues putUserStatsDailyItemToValues(UserDailyStatsData.ChessStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, user);
		values.put(DbConstants.V_CURRENT, dataObj.getRating().getCurrent());
		values.put(DbConstants.V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(DbConstants.V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());

		values.put(DbConstants.V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(DbConstants.V_BEST_WIN_GAME_ID, dataObj.getRating().getBestWin().getGameId());
		values.put(DbConstants.V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(DbConstants.V_AVERAGE_OPPONENT, dataObj.getRating().getAverageOpponent());
		values.put(DbConstants.V_RANK, dataObj.getRating().getTodaysRank().getRank());
		values.put(DbConstants.V_TOTAL_PLAYER_COUNT, dataObj.getRating().getTodaysRank().getTotalPlayerCount());
		values.put(DbConstants.V_TIMEOUTS, dataObj.getTimeouts());
		values.put(DbConstants.V_TIME_PER_MOVE, dataObj.getTimePerMove());

		values.put(DbConstants.V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(DbConstants.V_GAMES_WINS, dataObj.getGames().getWins());
		values.put(DbConstants.V_GAMES_LOSSES, dataObj.getGames().getLosses());
		values.put(DbConstants.V_GAMES_DRAWS, dataObj.getGames().getDraws());

		return values;
	}

	public static ContentValues putUserStatsTacticsItemToValues(UserTacticsStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, user);
		values.put(DbConstants.V_CURRENT, dataObj.getCurrent());
		values.put(DbConstants.V_HIGHEST_RATING, dataObj.getHighest().getRating());
		values.put(DbConstants.V_HIGHEST_TIMESTAMP, dataObj.getHighest().getTimestamp());
		values.put(DbConstants.V_LOWEST_RATING, dataObj.getLowest().getRating());
		values.put(DbConstants.V_LOWEST_TIMESTAMP, dataObj.getLowest().getTimestamp());

		values.put(DbConstants.V_ATTEMPT_COUNT, dataObj.getAttemptCount());
		values.put(DbConstants.V_PASSED_COUNT, dataObj.getPassedCount());
		values.put(DbConstants.V_FAILED_COUNT, dataObj.getFailedCount());
		values.put(DbConstants.V_TOTAL_SECONDS, dataObj.getTotalSeconds());
		values.put(DbConstants.V_TODAYS_ATTEMPTS, dataObj.getTodaysAttemps());
		values.put(DbConstants.V_TODAYS_AVG_SCORE, dataObj.getTodaysAvgScore());

		return values;
	}

	public static ContentValues putUserStatsChessMentorItemToValues(UserChessMentorStatsData.Rating dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, user);
		values.put(DbConstants.V_CURRENT, dataObj.getCurrent());
		values.put(DbConstants.V_HIGHEST_RATING, dataObj.getHighest().getRating());
		values.put(DbConstants.V_HIGHEST_TIMESTAMP, dataObj.getHighest().getTimestamp());
		values.put(DbConstants.V_LOWEST_RATING, dataObj.getLowest().getRating());
		values.put(DbConstants.V_LOWEST_TIMESTAMP, dataObj.getLowest().getTimestamp());

		values.put(DbConstants.V_LESSONS_TRIED, dataObj.getLessonsTried());
		values.put(DbConstants.V_TOTAL_LESSON_COUNT, dataObj.getTotalLessonCount());
		values.put(DbConstants.V_LESSON_COMPLETE_PERCENTAGE, dataObj.getLessonCompletePercentage());
		values.put(DbConstants.V_TOTAL_TRAINING_SECONDS, dataObj.getTotalTrainingSeconds());

		return values;
	}

	public static ContentValues putGameStatsLiveItemToValues(GameStatsItem.Data dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(DbConstants.V_USER, user);
		values.put(DbConstants.V_CURRENT, dataObj.getRating().getCurrent());
		values.put(DbConstants.V_RANK, dataObj.getRating().getTodaysRank().getRank());
		values.put(DbConstants.V_TOTAL_PLAYER_COUNT, dataObj.getRating().getTodaysRank().getTotalPlayerCount());
		values.put(DbConstants.V_PERCENTILE, dataObj.getRating().getPercentile());
		values.put(DbConstants.V_GLICKO_RD, dataObj.getRating().getGlickoRd());

		values.put(DbConstants.V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(DbConstants.V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());
		values.put(DbConstants.V_LOWEST_RATING, dataObj.getRating().getLowest().getRating());
		values.put(DbConstants.V_LOWEST_TIMESTAMP, dataObj.getRating().getLowest().getTimestamp());
		values.put(DbConstants.V_AVERAGE_OPPONENT, dataObj.getRating().getAverageOpponent());

		values.put(DbConstants.V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(DbConstants.V_BEST_WIN_GAME_ID, dataObj.getRating().getBestWin().getGameId());
		values.put(DbConstants.V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(DbConstants.V_AVG_OPPONENT_RATING_WIN, dataObj.getRating().getAverageOpponentRating().getWin());
		values.put(DbConstants.V_AVG_OPPONENT_RATING_LOSE, dataObj.getRating().getAverageOpponentRating().getLose());
		values.put(DbConstants.V_AVG_OPPONENT_RATING_DRAW, dataObj.getRating().getAverageOpponentRating().getDraw());
		values.put(DbConstants.V_UNRATED, dataObj.getGames().getUnrated());
		values.put(DbConstants.V_IN_PROGRESS, dataObj.getGames().getInProgress());
		values.put(DbConstants.V_TIMEOUTS, dataObj.getGames().getTimeoutPercent());

		values.put(DbConstants.V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(DbConstants.V_GAMES_BLACK, dataObj.getGames().getBlack());
		values.put(DbConstants.V_GAMES_WHITE, dataObj.getGames().getWhite());

		values.put(DbConstants.V_WINS_TOTAL, dataObj.getGames().getWins().getTotal());
		values.put(DbConstants.V_WINS_WHITE, dataObj.getGames().getWins().getWhite());
		values.put(DbConstants.V_WINS_BLACK, dataObj.getGames().getWins().getBlack());

		values.put(DbConstants.V_LOSSES_TOTAL, dataObj.getGames().getLosses().getTotal());
		values.put(DbConstants.V_LOSSES_WHITE, dataObj.getGames().getLosses().getWhite());
		values.put(DbConstants.V_LOSSES_BLACK, dataObj.getGames().getLosses().getBlack());

		values.put(DbConstants.V_DRAWS_TOTAL, dataObj.getGames().getDraws().getTotal());
		values.put(DbConstants.V_DRAWS_WHITE, dataObj.getGames().getDraws().getWhite());
		values.put(DbConstants.V_DRAWS_BLACK, dataObj.getGames().getDraws().getBlack());

		values.put(DbConstants.V_WINNING_STREAK, dataObj.getGames().getWinningStreak());
		values.put(DbConstants.V_LOSING_STREAK, dataObj.getGames().getLosingStreak());
		if (dataObj.getGames().getMostFrequentOpponent() != null) {
			values.put(DbConstants.V_FREQUENT_OPPONENT_NAME, dataObj.getGames().getMostFrequentOpponent().getUsername());
			values.put(DbConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, dataObj.getGames().getMostFrequentOpponent().getGamesPlayed());
		} else {
			values.put(DbConstants.V_FREQUENT_OPPONENT_NAME, StaticData.SYMBOL_EMPTY);
			values.put(DbConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, 0);
		}

		return values;
	}

	public static ContentValues putGameStatsDailyItemToValues(GameStatsItem.Data dataObj, String user) {
		ContentValues values = new ContentValues();


		values.put(DbConstants.V_USER, user);
		GameStatsItem.GameRating rating = dataObj.getRating();
		if (rating != null) {
			values.put(DbConstants.V_CURRENT, rating.getCurrent());
			values.put(DbConstants.V_RANK, rating.getTodaysRank().getRank());
			values.put(DbConstants.V_TOTAL_PLAYER_COUNT, rating.getTodaysRank().getTotalPlayerCount());
			values.put(DbConstants.V_PERCENTILE, rating.getPercentile());
			values.put(DbConstants.V_GLICKO_RD, rating.getGlickoRd());

			values.put(DbConstants.V_HIGHEST_RATING, rating.getHighest().getRating());
			values.put(DbConstants.V_HIGHEST_TIMESTAMP, rating.getHighest().getTimestamp());
			values.put(DbConstants.V_LOWEST_RATING, rating.getLowest().getRating());
			values.put(DbConstants.V_LOWEST_TIMESTAMP, rating.getLowest().getTimestamp());
			values.put(DbConstants.V_AVERAGE_OPPONENT, rating.getAverageOpponent());

			values.put(DbConstants.V_BEST_WIN_RATING, rating.getBestWin().getRating());
			values.put(DbConstants.V_BEST_WIN_GAME_ID, rating.getBestWin().getGameId());
			values.put(DbConstants.V_BEST_WIN_USERNAME, rating.getBestWin().getUsername());
			values.put(DbConstants.V_AVG_OPPONENT_RATING_WIN, rating.getAverageOpponentRating().getWin());
			values.put(DbConstants.V_AVG_OPPONENT_RATING_LOSE, rating.getAverageOpponentRating().getLose());
			values.put(DbConstants.V_AVG_OPPONENT_RATING_DRAW, rating.getAverageOpponentRating().getDraw());
		}

		GameStatsItem.Games games = dataObj.getGames();
		if (games != null) {
			values.put(DbConstants.V_UNRATED, games.getUnrated());
			values.put(DbConstants.V_IN_PROGRESS, games.getInProgress());
			values.put(DbConstants.V_TIMEOUTS, games.getTimeoutPercent());

			values.put(DbConstants.V_GAMES_TOTAL, games.getTotal());
			values.put(DbConstants.V_GAMES_BLACK, games.getBlack());
			values.put(DbConstants.V_GAMES_WHITE, games.getWhite());

			values.put(DbConstants.V_WINS_TOTAL, games.getWins().getTotal());
			values.put(DbConstants.V_WINS_WHITE, games.getWins().getWhite());
			values.put(DbConstants.V_WINS_BLACK, games.getWins().getBlack());

			values.put(DbConstants.V_LOSSES_TOTAL, games.getLosses().getTotal());
			values.put(DbConstants.V_LOSSES_WHITE, games.getLosses().getWhite());
			values.put(DbConstants.V_LOSSES_BLACK, games.getLosses().getBlack());

			values.put(DbConstants.V_DRAWS_TOTAL, games.getDraws().getTotal());
			values.put(DbConstants.V_DRAWS_WHITE, games.getDraws().getWhite());
			values.put(DbConstants.V_DRAWS_BLACK, games.getDraws().getBlack());

			values.put(DbConstants.V_WINNING_STREAK, games.getWinningStreak());
			values.put(DbConstants.V_LOSING_STREAK, games.getLosingStreak());

			if (games.getMostFrequentOpponent() != null) {
				values.put(DbConstants.V_FREQUENT_OPPONENT_NAME, games.getMostFrequentOpponent().getUsername());
				values.put(DbConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, games.getMostFrequentOpponent().getGamesPlayed());
			} else {
				values.put(DbConstants.V_FREQUENT_OPPONENT_NAME, StaticData.SYMBOL_EMPTY);
				values.put(DbConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED, 0);
			}

		}

		Tournaments tournaments = dataObj.getTournaments();
		if (tournaments != null) {
			Tournaments.All tournamentsAll = tournaments.getAll();
			values.put(DbConstants.V_TOURNAMENTS_LEADERBOARD_POINTS, tournamentsAll.getLeaderboardPoints());
			values.put(DbConstants.V_TOURNAMENTS_EVENTS_ENTERED, tournamentsAll.getEventsEntered());
			values.put(DbConstants.V_TOURNAMENTS_FIRST_PLACE_FINISHES, tournamentsAll.getFirstPlaceFinishes());
			values.put(DbConstants.V_TOURNAMENTS_SECOND_PLACE_FINISHES, tournamentsAll.getSecondPlaceFinishes());
			values.put(DbConstants.V_TOURNAMENTS_THIRD_PLACE_FINISHES, tournamentsAll.getThirdPlaceFinishes());
			values.put(DbConstants.V_TOURNAMENTS_WITHDRAWALS, tournamentsAll.getWithdrawals());
			values.put(DbConstants.V_TOURNAMENTS_HOSTED, tournamentsAll.getTournamentsHosted());
			values.put(DbConstants.V_TOTAL_COUNT_PLAYERS_HOSTED, tournamentsAll.getTotalCountPlayersHosted());

			Tournaments.Games tournamentsGames = tournaments.getGames();
			values.put(DbConstants.V_TOURNAMENTS_GAMES_TOTAL, tournamentsGames.getTotal());
			values.put(DbConstants.V_TOURNAMENTS_GAMES_WON, tournamentsGames.getWins());
			values.put(DbConstants.V_TOURNAMENTS_GAMES_LOST, tournamentsGames.getLosses());
			values.put(DbConstants.V_TOURNAMENTS_GAMES_DRAWN, tournamentsGames.getDraws());
			values.put(DbConstants.V_TOURNAMENTS_GAMES_IN_PROGRESS, tournamentsGames.getInProgress());
		}
		return values;
	}

	public static GamesInfoByResult getGameStatsGamesByResultFromCursor(Cursor cursor) {
		GamesInfoByResult tournamentGames = new GamesInfoByResult();
		tournamentGames.setTotal(getInt(cursor, DbConstants.V_GAMES_TOTAL));
		tournamentGames.setWins(getInt(cursor, DbConstants.V_WINS_TOTAL));
		tournamentGames.setLosses(getInt(cursor, DbConstants.V_LOSSES_TOTAL));
		tournamentGames.setDraws(getInt(cursor, DbConstants.V_DRAWS_TOTAL));
		return tournamentGames;
	}


	public static boolean checkIfDrawOffered(ContentResolver resolver, String userName, long gameId) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = userName;
		arguments2[1] = String.valueOf(gameId);

		Cursor cursor = resolver.query(DbConstants.uriArray[DbConstants.Tables.DAILY_CURRENT_GAMES.ordinal()],
				PROJECTION_DAILY_DRAW_OFFERED, SELECTION_OPPONENT_OFFERED_DRAW, arguments2, null);
		return cursor != null && cursor.moveToFirst() && getInt(cursor, DbConstants.V_OPPONENT_OFFERED_DRAW) > 0;
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
		return cursor.getLong(cursor.getColumnIndex(DbConstants._ID));
	}

	public static int getDbVersion() {
		return DbConstants.DATABASE_VERSION;
	}

}
