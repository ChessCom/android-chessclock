package com.chess.db;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.*;
import com.chess.backend.entity.api.stats.*;
import com.chess.backend.gcm.FriendRequestItem;
import com.chess.backend.gcm.GameOverNotificationItem;
import com.chess.backend.gcm.NewChallengeNotificationItem;
import com.chess.backend.gcm.NewChatNotificationItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.statics.Symbol;

import java.util.ArrayList;
import java.util.List;

import static com.chess.db.DbScheme.*;

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
	public static final String LIKE_ = " LIKE ?";
	public static final String AND_ = " AND ";
	public static final String MORE_ = " > ";
	public static final String EQUALS_ = " = ";
	public static final String EQUALS_ARG_ = "=?";
	public static final String NOT_EQUALS_ARG_ = "!=?";
	public static final String LIMIT_ = " LIMIT ";
	public static final String LIMIT_1 = _ID + " LIMIT 1";

	public static String[] sArguments1 = new String[1];
	public static String[] sArguments2 = new String[2];
	public static String[] sArguments3 = new String[3];
	public static String[] sArguments5 = new String[5];

	// -------------- SELECTION DEFINITIONS ---------------------------

	public static String SELECTION_ITEM_ID_AND_USER = concatArguments(
			V_ID,
			V_USER);

	public static String SELECTION_USER = concatArguments(V_USER);

	public static String SELECTION_ID = concatArguments(_ID);

	public static String SELECTION_USER_AND_ID = concatArguments(   // TODO remove duplicate
			V_USER,
			V_ID);

	public static String SELECTION_USER_ID = concatArguments(
			V_USER,
			V_USER_ID);

	public static String SELECTION_OPPONENT_OFFERED_DRAW = concatArguments(
			V_USER,
			V_ID,
			V_OPPONENT_OFFERED_DRAW);

	public static String SELECTION_USER_TURN = concatArguments(
			V_USER,
			V_IS_MY_TURN);

	public static String SELECTION_TITLE = concatArguments(V_TITLE);

	public static String SELECTION_CATEGORY_ID = concatArguments(V_CATEGORY_ID);

	public static String SELECTION_PARENT_ID = concatArguments(V_PARENT_ID);

	public static String SELECTION_CATEGORY_ID_AND_PAGE = concatArguments(V_CATEGORY_ID, V_PAGE);

	public static String SELECTION_CATEGORY_ID_AND_USER = concatArguments(V_CATEGORY_ID, V_USER);

	public static String SELECTION_CATEGORY = concatArguments(V_CATEGORY);

	public static String SELECTION_ITEM_ID = concatArguments(V_ID);

	public static String SELECTION_ITEM_ID_AND_PAGE = concatArguments(V_ID, V_PAGE);

	public static String SELECTION_ITEM_ID_AND_NUMBER = concatArguments(V_ID, V_NUMBER);

	public static String SELECTION_ITEM_ID_AND_CURRENT_POSITION = concatArguments(
			V_ID,
			V_CURRENT_POSITION);

	public static String SELECTION_ITEM_ID_POSITION_NUMBER = concatArguments(
			V_ID,
			V_CURRENT_POSITION,
			V_NUMBER);

	public static String SELECTION_ID_CATEGORY_ID_USER = concatArguments(
			V_ID,
			V_CATEGORY_ID,
			V_USER
	);

	public static String SELECTION_CREATE_DATE = concatArguments(V_CREATE_DATE);

	public static String SELECTION_ID_USER_CONVERSATION_ID = concatArguments(
			V_ID,
			V_USER,
			V_CONVERSATION_ID
	);

	public static String SELECTION_USER_CONVERSATION_ID = concatArguments(
			V_USER,
			V_CONVERSATION_ID
	);

	public static String SELECTION_USER_AND_LESSON_STATED = concatArguments(V_USER, V_LESSON_STARTED);

	public static String SELECTION_USER_AND_USERNAME = concatArguments(V_USER, V_USERNAME);

	public static String SELECTION_GRAPH_RECORD = concatArguments(
			V_TIMESTAMP,
			V_GAME_TYPE,
			V_USER
	);

	public static String SELECTION_GRAPH_TABLE = concatArguments(
			V_GAME_TYPE,
			V_USER
	);

	public static String SELECTION_PARENT_ID_AND_ITEM_ID = concatArguments(
			V_PARENT_ID,
			V_ID
	);

	public static String SELECTION_USER_AND_SEEN = concatArguments(
			V_USER,
			V_SEEN);

	// -------------- PROJECTIONS DEFINITIONS ---------------------------

	public static final String[] PROJECTION_ITEM_ID_AND_USER = new String[]{
			_ID,
			V_ID,
			V_USER
	};

	public static final String[] PROJECTION_USER = new String[]{
			_ID,
			V_USER
	};

	public static final String[] PROJECTION_GAME_ID = new String[]{
			_ID,
			V_USER,
			V_ID
	};

	public static final String[] PROJECTION_USER_ID = new String[]{
			_ID,
			V_USER,
			V_USER_ID
	};

	public static final String[] PROJECTION_CURRENT_GAMES = new String[]{
			_ID,
			V_USER,
			V_ID,
			V_I_PLAY_AS,
			V_WHITE_USERNAME,
			V_BLACK_USERNAME,
			V_WHITE_AVATAR,
			V_BLACK_AVATAR,
			V_WHITE_PREMIUM_STATUS,
			V_BLACK_PREMIUM_STATUS,
			V_GAME_TYPE,
			V_IS_MY_TURN,
			V_TIMESTAMP,
			V_OPPONENT_OFFERED_DRAW,
			V_IS_OPPONENT_ONLINE,
			V_HAS_NEW_MESSAGE,
			V_TIME_REMAINING
	};

	public static final String[] PROJECTION_FINISHED_GAMES = new String[]{
			_ID,
			V_USER,
			V_ID,
			V_I_PLAY_AS,
			V_GAME_TYPE,
			V_GAME_SCORE,
			V_WHITE_USERNAME,
			V_BLACK_USERNAME,
			V_WHITE_AVATAR,
			V_BLACK_AVATAR,
			V_WHITE_RATING,
			V_BLACK_RATING,
			V_IS_OPPONENT_ONLINE,
			V_WHITE_PREMIUM_STATUS,
			V_BLACK_PREMIUM_STATUS
	};

	public static final String[] PROJECTION_DAILY_PLAYER_NAMES = new String[]{
			_ID,
			V_USER,
			V_I_PLAY_AS,
			V_WHITE_USERNAME,
			V_IS_OPPONENT_ONLINE,
			V_BLACK_USERNAME,
			V_BLACK_AVATAR,
			V_WHITE_AVATAR,
	};

	public static final String[] PROJECTION_DAILY_DRAW_OFFERED = new String[]{
			_ID,
			V_USER,
			V_ID,
			V_OPPONENT_OFFERED_DRAW
	};

	public static final String[] PROJECTION_TITLE = new String[]{_ID, V_TITLE};

	public static final String[] PROJECTION_USERNAME = new String[]{_ID, V_USERNAME};

	public static final String[] PROJECTION_V_CATEGORY_ID = new String[]{_ID, V_CATEGORY_ID};

	public static final String[] PROJECTION_ITEM_ID = new String[]{_ID, V_ID};

	public static final String[] PROJECTION_CREATE_DATE = new String[]{_ID, V_CREATE_DATE};

	public static final String[] PROJECTION_USER_CURRENT_RATING = new String[]{_ID, V_USER, V_CURRENT};

	public static final String[] PROJECTION_ITEM_ID_AND_NUMBER = new String[]{_ID, V_ID, V_NUMBER};

	public static final String[] PROJECTION_ITEM_ID_POSITION_NUMBER = new String[]{
			_ID,
			V_ID,
			V_CURRENT_POSITION,
			V_NUMBER
	};

	public static final String[] PROJECTION_ID_CATEGORY_ID_USER = new String[]{_ID, V_ID, V_CATEGORY_ID, V_USER};

	public static final String[] PROJECTION_ID_USER_CONVERSATION_ID = new String[]{
			_ID,
			V_ID,
			V_USER,
			V_CONVERSATION_ID
	};

	public static final String[] PROJECTION_USER_AND_USERNAME = new String[]{_ID, V_USER, V_USERNAME};

	public static final String[] PROJECTION_GRAPH_RECORD = new String[]{
			_ID,
			V_TIMESTAMP,
			V_GAME_TYPE,
			V_USER,
	};

	public static final String[] PROJECTION_PARENT_ID_AND_ITEM_ID = new String[]{
			_ID,
			V_PARENT_ID,
			V_ID
	};

	public static final String[] PROJECTION_USER_AND_SEEN = new String[]{
			_ID,
			V_USER,
			V_USERNAME,
			V_SEEN
	};

	public static String concatArguments(String... arguments) {
		StringBuilder selection = new StringBuilder();

		String separator = Symbol.EMPTY;
		for (String argument : arguments) {
			selection.append(separator);
			separator = AND_;
			selection.append(argument);
			selection.append(EQUALS_ARG_);
		}
		return selection.toString();
	}

	public static String concatLikeArguments(String... arguments) {
		StringBuilder selection = new StringBuilder();

		String separator = Symbol.EMPTY;
		for (String argument : arguments) {
			selection.append(separator);
			separator = OR_;
			selection.append(argument);
			selection.append(LIKE_);
		}
		return selection.toString();
	}

	public static Cursor query(ContentResolver contentResolver, QueryParams params) {
		return contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(),
				params.getArguments(), params.getOrder());
	}

	/**
	 * Check if we have saved games for current user
	 *
	 * @param context  to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedDailyGame(Context context, String username) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		Cursor cursor = contentResolver.query(uriArray[Tables.DAILY_CURRENT_GAMES.ordinal()],
				PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
		boolean exist = cursor != null && cursor.moveToFirst();
		if (cursor != null) {
			cursor.close();
		}
		if (!exist) { // check finished games list
			cursor = contentResolver.query(uriArray[Tables.DAILY_FINISHED_GAMES.ordinal()],
					PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
			exist = cursor != null && cursor.moveToFirst();
		}
		if (cursor != null) {
			cursor.close();
		}

		return exist;
	}

	public static void updateDailyGame(ContentResolver contentResolver, DailyCurrentGameData currentGame, String username) {

		final String[] arguments2 = sArguments2;
		arguments2[0] = username;
		arguments2[1] = String.valueOf(currentGame.getGameId());

		Uri uri = uriArray[Tables.DAILY_CURRENT_GAMES.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER_AND_ID,
				arguments2, null);
		ContentValues values = putDailyGameCurrentItemToValues(currentGame, username);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	/**
	 * @return true if still have current games
	 */
	public static boolean checkAndDeleteNonExistCurrentGames(Context context, List<DailyCurrentGameData> gamesList, String username) {
		// compare to current list games

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		Uri uri = uriArray[Tables.DAILY_CURRENT_GAMES.ordinal()];
		long[] gamesIds;
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER, arguments1, null);
		if (cursor != null && cursor.moveToFirst()) {
			gamesIds = new long[cursor.getCount()];
			int i = 0;
			do {
				gamesIds[i++] = getLong(cursor, V_ID);
			} while (cursor.moveToNext());
		} else if (gamesList.size() == 0) { // means no current games for that user
			arguments1[0] = username;
			contentResolver.delete(uri, SELECTION_USER, arguments1);

			if (cursor != null) {
				cursor.close();
			}
			return false;
		} else { // current games exist, but not saved yet
			if (cursor != null) {
				cursor.close();
			}
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
				arguments2[0] = username;
				arguments2[1] = String.valueOf(id);
				contentResolver.delete(uri, SELECTION_USER_AND_ID, arguments2);
			}
		}

		return gamesIds.length > idsToRemove.size();
	}

	public static boolean checkAndDeleteNonExistFinishedGames(Context context, List<DailyFinishedGameData> gamesList, String username) {
		// compare to current list games

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		Uri uri = uriArray[Tables.DAILY_FINISHED_GAMES.ordinal()];
		long[] gamesIds;
		Cursor cursor = contentResolver.query(uri, PROJECTION_GAME_ID, SELECTION_USER, arguments1, null);
		if (cursor != null && cursor.moveToFirst()) {
			gamesIds = new long[cursor.getCount()];
			int i = 0;
			do {
				gamesIds[i++] = getLong(cursor, V_ID);
			} while (cursor.moveToNext());
		} else if (gamesList.size() == 0) { // means no finished games for that user
			arguments1[0] = username;
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
				arguments2[0] = username;
				arguments2[1] = String.valueOf(id);
				contentResolver.delete(uri, SELECTION_USER_AND_ID, arguments2);
			}
		}

		return gamesIds.length > idsToRemove.size();
	}

	/**
	 * Search in DAILY_FINISHED_GAMES for match to this query : {@code SELECT user,i_play_as,white_username,is_opponent_online,black_username FROM DAILY_FINISHED_GAMES WHERE user='username' AND ((black_username!='username' AND i_play_as=1) OR (black_username='username' AND i_play_as=2)) GROUP BY white_username, black_username}
	 *
	 * @return Cursor with composition of player names, where current user can be black or white player
	 */
	public static Cursor getRecentOpponentsCursor(Context context, String username) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments = sArguments5;
		arguments[0] = username;
		arguments[1] = username;
		arguments[2] = String.valueOf(RestHelper.P_WHITE);
		arguments[3] = username;
		arguments[4] = String.valueOf(RestHelper.P_BLACK);

		ContentProviderClient client = contentResolver.acquireContentProviderClient(PROVIDER_NAME);
		SQLiteDatabase dbHandle = ((DbDataProvider) client.getLocalContentProvider()).getDbHandle();
		StringBuilder projection = new StringBuilder();
		String selection = V_USER + EQUALS_ARG_ + AND_ + "((" + V_BLACK_USERNAME + NOT_EQUALS_ARG_
				+ AND_ + V_I_PLAY_AS + EQUALS_ARG_ + ") OR (" + V_BLACK_USERNAME + EQUALS_ARG_
				+ AND_ + V_I_PLAY_AS + EQUALS_ARG_ + "))";

		QueryParams params = new QueryParams();
		params.setDbName(Tables.DAILY_FINISHED_GAMES.name());
		params.setProjection(PROJECTION_DAILY_PLAYER_NAMES);
		params.setSelection(selection);
		params.setArguments(arguments);
		params.setCommands(GROUP_BY + Symbol.SPACE + V_WHITE_USERNAME + ", " + V_BLACK_USERNAME);

		for (String projections : params.getProjection()) {
			projection.append(projections).append(Symbol.COMMA);
		}

		Cursor cursor = dbHandle.rawQuery("SELECT " + projection.toString().substring(0, projection.length() - 1)
				+ " FROM " + params.getDbName() + " WHERE " + params.getSelection() +
				Symbol.SPACE + params.getCommands(), params.getArguments());
		client.release();

		return cursor;
	}

	/**
	 * Check if we have saved friends for current user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedFriends(Context context, String username) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		Cursor cursor = contentResolver.query(uriArray[Tables.FRIENDS.ordinal()],
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
	 * @param username auth user
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedTacticGame(Context context, String username) {

		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		Cursor cursor = contentResolver.query(uriArray[Tables.TACTICS_BATCH.ordinal()],
				PROJECTION_USER, SELECTION_USER, arguments1, LIMIT_1);
		boolean exist = cursor != null && cursor.moveToFirst();
		if (cursor != null) {
			cursor.close();
		}

		return exist;
	}

	public static int saveTacticItemToDb(Context context, TacticItem.Data tacticItem, String username) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(tacticItem.getId());
		arguments2[1] = username;

		Uri uri = uriArray[Tables.TACTICS_BATCH.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID_AND_USER,
				SELECTION_ITEM_ID_AND_USER, arguments2, null);

		ContentValues values = putTacticItemToValues(tacticItem);
		updateOrInsertValues(contentResolver, cursor, uri, values);

		if (tacticItem.getResultItem() != null) {
			saveTacticResultItemToDb(context, tacticItem.getResultItem());
		}

		return StaticData.RESULT_OK;
	}

	public static TacticItem.Data getLastTacticItemFromDb(Context context, String username) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		Cursor cursor = contentResolver.query(uriArray[Tables.TACTICS_BATCH.ordinal()],
				null, SELECTION_USER, arguments1, null);

		if (cursor != null) {
			cursor.moveToFirst();
			TacticItem.Data tacticItem = getTacticItemFromCursor(cursor);
			cursor.close();

			// set result item
			TacticRatingData resultItem = getTacticResultItemFromDb(context, tacticItem.getId(), username);
			tacticItem.setResultItem(resultItem);

			return tacticItem;
		} else {
			return null;
		}
	}

	private static void saveTacticResultItemToDb(Context context, TacticRatingData resultItem) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(resultItem.getId());
		arguments2[1] = resultItem.getUser();

		Uri uri = uriArray[Tables.TACTICS_RESULTS.ordinal()];
		Cursor cursor = contentResolver.query(uri, null, SELECTION_ITEM_ID_AND_USER, arguments2, null);

		ContentValues values = putTacticResultItemToValues(resultItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	private static TacticRatingData getTacticResultItemFromDb(Context context, long id, String username) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(id);
		arguments2[1] = username;
		Cursor cursor = contentResolver.query(uriArray[Tables.TACTICS_RESULTS.ordinal()],
				null, SELECTION_ITEM_ID_AND_USER, arguments2, null);

		if (cursor != null && cursor.moveToFirst()) {
			TacticRatingData resultItem = getTacticResultItemFromCursor(cursor);
			cursor.close();

			return resultItem;
		} else {
			if (cursor != null) {
				cursor.close();
			}
			return null;
		}
	}

	public static int getUserRatingFromUsersStats(Context context, int dbUriCode, String username) {
		final int DEFAULT_RATING = 1200;

		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = username;
		Cursor cursor = contentResolver.query(uriArray[dbUriCode],
				PROJECTION_USER_CURRENT_RATING, SELECTION_USER, arguments1, null);

		if (cursor != null && cursor.moveToFirst()) {
			int rating = getInt(cursor, V_CURRENT);
			rating = rating == 0 ? DEFAULT_RATING : rating;
			cursor.close();

			return rating;
		} else {
			if (cursor != null) {
				cursor.close();
			}
			return DEFAULT_RATING;
		}
	}


	// ---------------------- Value Setters ------------------------------------------------------------------------
	public static ContentValues putTacticItemToValues(TacticItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_USER, dataObj.getUser());
		values.put(V_ID, dataObj.getId());
		values.put(V_FEN, dataObj.getInitialFen());
		values.put(V_MOVE_LIST, dataObj.getCleanMoveString());
		values.put(V_ATTEMPT_CNT, dataObj.getAttemptCnt());
		values.put(V_PASSED_CNT, dataObj.getPassedCnt());
		values.put(V_RATING, dataObj.getRating());
		values.put(V_AVG_SECONDS, dataObj.getAvgSeconds());
		values.put(V_SECONDS_SPENT, dataObj.getSecondsSpent());
		values.put(V_STOP, dataObj.isStop() ? 1 : 0);
		values.put(V_WAS_SHOWED, dataObj.isAnswerWasShowed() ? 1 : 0);
		values.put(V_IS_RETRY, dataObj.isRetry() ? 1 : 0);

		return values;
	}


	public static TacticItem.Data getTacticItemFromCursor(Cursor cursor) {
		TacticItem.Data dataObj = new TacticItem.Data();

		dataObj.setUser(getString(cursor, V_USER));
		dataObj.setId(getLong(cursor, V_ID));
		dataObj.setFen(getString(cursor, V_FEN));
		dataObj.setMoveList(getString(cursor, V_MOVE_LIST));
		dataObj.setAttemptCnt(getInt(cursor, V_ATTEMPT_CNT));
		dataObj.setPassedCnt(getInt(cursor, V_PASSED_CNT));
		dataObj.setRating(getInt(cursor, V_RATING));
		dataObj.setAvgSeconds(getInt(cursor, V_AVG_SECONDS));
		dataObj.setSecondsSpent(getInt(cursor, V_SECONDS_SPENT));
		dataObj.setStop(getInt(cursor, V_STOP) > 0);
		dataObj.setAnswerWasShowed(getInt(cursor, V_WAS_SHOWED) > 0);
		dataObj.setRetry(getInt(cursor, V_IS_RETRY) > 0);

		return dataObj;
	}

	public static ContentValues putTacticResultItemToValues(TacticRatingData dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_USER, dataObj.getUser());
		values.put(V_ID, dataObj.getId());
		values.put(V_SCORE, dataObj.getScoreStr());
		values.put(V_USER_RATING_CHANGE, dataObj.getUserRatingChange());
		values.put(V_USER_RATING, dataObj.getUserRating());
		values.put(V_PROBLEM_RATING_CHANGE, dataObj.getProblemRatingChange());
		values.put(V_PROBLEM_RATING, dataObj.getProblemRating());

		return values;
	}


	public static TacticRatingData getTacticResultItemFromCursor(Cursor cursor) {
		TacticRatingData dataObj = new TacticRatingData();

		dataObj.setUser(getString(cursor, V_USER));
		dataObj.setId(getLong(cursor, V_ID));
		dataObj.setScore(getString(cursor, V_SCORE));
		dataObj.setUserRatingChange(getInt(cursor, V_USER_RATING_CHANGE));
		dataObj.setUserRating(getInt(cursor, V_USER_RATING));
		dataObj.setProblemRatingChange(getInt(cursor, V_PROBLEM_RATING_CHANGE));
		dataObj.setProblemRating(getInt(cursor, V_PROBLEM_RATING));

		return dataObj;
	}

	// ----------------------------------- Daily Games -------------------------------------------------------
	public static ContentValues putDailyFinishedGameToValues(DailyFinishedGameData dataObj, String username) {
		ContentValues values = new ContentValues();

		setValuesFromGameItem(values, dataObj);
		values.put(V_FINISHED, 1);
		values.put(V_USER, username);
		values.put(V_GAME_SCORE, dataObj.getGameScore());
		values.put(V_RESULT_MESSAGE, dataObj.getResultMessage());
		return values;
	}

	public static DailyFinishedGameData getDailyFinishedGameFromCursor(Cursor cursor) {
		DailyFinishedGameData dataObj = new DailyFinishedGameData();

		getDailyGameFromCursor(dataObj, cursor);
		dataObj.setGameScore(getInt(cursor, V_GAME_SCORE));
		dataObj.setResultMessage(getString(cursor, V_RESULT_MESSAGE));

		return dataObj;
	}

	public static ContentValues putDailyGameCurrentItemToValues(DailyCurrentGameData dataObj, String username) {
		ContentValues values = new ContentValues();

		setValuesFromGameItem(values, dataObj);
		values.put(V_FINISHED, 0);
		values.put(V_USER, username);
		values.put(V_OPPONENT_OFFERED_DRAW, dataObj.isDrawOffered());
		values.put(V_IS_MY_TURN, dataObj.isMyTurn() ? 1 : 0);
		return values;
	}

	private static void setValuesFromGameItem(ContentValues values, DailyGameBaseData dataObj) { // TODO remove
		values.put(V_ID, dataObj.getGameId());
		values.put(V_FEN, dataObj.getFen());
		values.put(V_I_PLAY_AS, dataObj.getMyColor());
		values.put(V_GAME_TYPE, dataObj.getGameType());
		values.put(V_WHITE_USERNAME, dataObj.getWhiteUsername());
		values.put(V_BLACK_USERNAME, dataObj.getBlackUsername());
		values.put(V_WHITE_RATING, dataObj.getWhiteRating());
		values.put(V_BLACK_RATING, dataObj.getBlackRating());
		values.put(V_WHITE_PREMIUM_STATUS, dataObj.getWhitePremiumStatus());
		values.put(V_BLACK_PREMIUM_STATUS, dataObj.getBlackPremiumStatus());
		values.put(V_WHITE_USER_COUNTRY, dataObj.getWhiteUserCountry());
		values.put(V_BLACK_USER_COUNTRY, dataObj.getBlackUserCountry());
		values.put(V_WHITE_AVATAR, dataObj.getWhiteAvatar());
		values.put(V_BLACK_AVATAR, dataObj.getBlackAvatar());
		values.put(V_TIME_REMAINING, dataObj.getTimeRemaining());
		values.put(V_TIMESTAMP, dataObj.getTimestamp());
		values.put(V_LAST_MOVE_FROM_SQUARE, dataObj.getLastMoveFromSquare());
		values.put(V_LAST_MOVE_TO_SQUARE, dataObj.getLastMoveToSquare());
		values.put(V_GAME_NAME, dataObj.getName());
		values.put(V_FEN_START_POSITION, dataObj.getStartingFenPosition());
		values.put(V_MOVE_LIST, dataObj.getMoveList());
		values.put(V_HAS_NEW_MESSAGE, dataObj.hasNewMessage() ? 1 : 0);
		values.put(V_RATED, dataObj.isRated() ? 1 : 0);
		values.put(V_DAYS_PER_MOVE, dataObj.getDaysPerMove());
		values.put(V_IS_OPPONENT_ONLINE, dataObj.isOpponentOnline());
	}

	public static DailyCurrentGameData getDailyCurrentGameFromCursor(Cursor cursor) {
		DailyCurrentGameData dataObj = new DailyCurrentGameData();

		getDailyGameFromCursor(dataObj, cursor);
		dataObj.setDrawOffered(getInt(cursor, V_OPPONENT_OFFERED_DRAW));
		dataObj.setMyTurn(getInt(cursor, V_IS_MY_TURN) > 0);

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

		dataObj.setGameId(getLong(cursor, V_ID));
		dataObj.setWhiteUsername(getString(cursor, V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, V_BLACK_USERNAME));
		dataObj.setWhiteAvatar(getString(cursor, V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, V_BLACK_AVATAR));
		dataObj.setGameType(getInt(cursor, V_GAME_TYPE));
		dataObj.setMyTurn(getInt(cursor, V_IS_MY_TURN) > 0);
		dataObj.setTimestamp(getLong(cursor, V_TIMESTAMP));
		dataObj.setDrawOffered(getInt(cursor, V_OPPONENT_OFFERED_DRAW));
		dataObj.setHasNewMessage(getInt(cursor, V_HAS_NEW_MESSAGE) > 0);
		dataObj.setTimeRemaining(getLong(cursor, V_TIME_REMAINING));

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

		dataObj.setGameId(getLong(cursor, V_ID));
		dataObj.setGameType(getInt(cursor, V_GAME_TYPE));
		dataObj.setGameScore(getInt(cursor, V_GAME_SCORE));
		dataObj.setWhiteUsername(getString(cursor, V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, V_BLACK_USERNAME));
		dataObj.setWhiteAvatar(getString(cursor, V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, V_BLACK_AVATAR));
		dataObj.setWhiteRating(getInt(cursor, V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, V_BLACK_RATING));

		return dataObj;
	}

	private static void getDailyGameFromCursor(DailyGameBaseData dataObj, Cursor cursor) {
		dataObj.setGameId(getLong(cursor, V_ID));
		dataObj.setFen(getString(cursor, V_FEN));
		dataObj.setIPlayAs(getInt(cursor, V_I_PLAY_AS));
		dataObj.setLastMoveFromSquare(getString(cursor, V_LAST_MOVE_FROM_SQUARE));
		dataObj.setLastMoveToSquare(getString(cursor, V_LAST_MOVE_TO_SQUARE));
		dataObj.setGameType(getInt(cursor, V_GAME_TYPE));
		dataObj.setTimestamp(getLong(cursor, V_TIMESTAMP));
		dataObj.setName(getString(cursor, V_GAME_NAME));
		dataObj.setWhiteUsername(getString(cursor, V_WHITE_USERNAME));
		dataObj.setBlackUsername(getString(cursor, V_BLACK_USERNAME));
		dataObj.setWhitePremiumStatus(getInt(cursor, V_WHITE_PREMIUM_STATUS));
		dataObj.setBlackPremiumStatus(getInt(cursor, V_BLACK_PREMIUM_STATUS));
		dataObj.setWhiteUserCountry(getInt(cursor, V_WHITE_USER_COUNTRY));
		dataObj.setBlackUserCountry(getInt(cursor, V_BLACK_USER_COUNTRY));
		dataObj.setStartingFenPosition(getString(cursor, V_FEN_START_POSITION));
		dataObj.setMoveList(getString(cursor, V_MOVE_LIST));
		dataObj.setWhiteRating(getInt(cursor, V_WHITE_RATING));
		dataObj.setBlackRating(getInt(cursor, V_BLACK_RATING));
		dataObj.setWhiteAvatar(getString(cursor, V_WHITE_AVATAR));
		dataObj.setBlackAvatar(getString(cursor, V_BLACK_AVATAR));
		dataObj.setHasNewMessage(getInt(cursor, V_HAS_NEW_MESSAGE) > 0);
		dataObj.setTimeRemaining(getLong(cursor, V_TIME_REMAINING));
		dataObj.setRated(getInt(cursor, V_RATED) > 0);
		dataObj.setDaysPerMove(getInt(cursor, V_DAYS_PER_MOVE));
	}

	// ------------------------------------- Friends ----------------------------------------------
	public static ContentValues putFriendItemToValues(FriendsItem.Data dataObj, String username) {
		ContentValues values = new ContentValues();

		values.put(V_USER, username);
		values.put(V_USERNAME, dataObj.getUsername());
		values.put(V_USER_ID, dataObj.getUserId());
		values.put(V_LOCATION, dataObj.getLocation());
		values.put(V_COUNTRY_ID, dataObj.getCountryId());
		values.put(V_PHOTO_URL, dataObj.getAvatarUrl());
		values.put(V_IS_OPPONENT_ONLINE, dataObj.isOnline() ? 1 : 0);
		values.put(V_PREMIUM_STATUS, dataObj.getPremiumStatus());
		values.put(V_LAST_LOGIN_DATE, dataObj.getLastLoginDate());

		return values;
	}

	public static ContentValues putArticleItemToValues(ArticleItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_TITLE, dataObj.getTitle());
		values.put(V_CREATE_DATE, dataObj.getCreateDate());
		values.put(V_BODY, dataObj.getBody());
		values.put(V_CATEGORY, dataObj.getCategoryName());
		values.put(V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(V_COUNTRY_ID, dataObj.getCountryId());
		values.put(V_USER_ID, dataObj.getUserId());
		values.put(V_USERNAME, dataObj.getUsername());
		values.put(V_FIRST_NAME, dataObj.getFirstName());
		values.put(V_LAST_NAME, dataObj.getLastName());
		values.put(V_CHESS_TITLE, dataObj.getChessTitle());
		values.put(V_USER_AVATAR, dataObj.getAvatar());
		values.put(V_PHOTO_URL, dataObj.getImageUrl());
		values.put(V_URL, dataObj.getUrl());
		values.put(V_THUMB_CONTENT, dataObj.isIsThumbInContent());

		return values;
	}

	public static ContentValues putCommonCommentItemToValues(CommonCommentItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_PARENT_ID, dataObj.getParentId());
		values.put(V_USER_ID, dataObj.getUserId());
		values.put(V_CREATE_DATE, dataObj.getCreateDate());
		values.put(V_COUNTRY_ID, dataObj.getCountryId());
		values.put(V_USERNAME, dataObj.getUsername());
		values.put(V_FIRST_NAME, dataObj.getFirstName());
		values.put(V_LAST_NAME, dataObj.getLastName());
		values.put(V_USER_AVATAR, dataObj.getAvatar());
		values.put(V_BODY, dataObj.getBody());

		return values;
	}

	public static void saveArticleItem(ContentResolver contentResolver, ArticleItem.Data currentItem) {
		final String[] arguments2 = sArguments1;
		arguments2[0] = String.valueOf(currentItem.getId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbScheme.uriArray[DbScheme.Tables.ARTICLES.ordinal()];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
				DbDataManager.SELECTION_ITEM_ID, arguments2, null);

		ContentValues values = DbDataManager.putArticleItemToValues(currentItem);

		DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveVideoItem(ContentResolver contentResolver, VideoSingleItem.Data currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getVideoId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.VIDEOS.ordinal()];

		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID,
				SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = putVideoItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveForumTopicItem(ContentResolver contentResolver, ForumTopicItem.Topic currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.FORUM_TOPICS.ordinal()];

		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID,
				SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = putForumTopicItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveForumCategoryItem(ContentResolver contentResolver, ForumCategoryItem.Data currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getId());

		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.FORUM_CATEGORIES.ordinal()];

		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID,
				SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = putForumCategoryItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveForumPostItem(ContentResolver contentResolver, ForumPostItem.Post currentItem) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(currentItem.getCreateDate());

		Uri uri = uriArray[Tables.FORUM_POSTS.ordinal()];

		Cursor cursor = contentResolver.query(uri, PROJECTION_CREATE_DATE,
				SELECTION_CREATE_DATE, arguments1, null);

		ContentValues values = putForumPostItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	/**
	 * Check if we have saved videos for any user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedVideos(Context context) {
		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(uriArray[Tables.VIDEOS.ordinal()],
				PROJECTION_ITEM_ID, null, null, LIMIT_1);
		boolean exist = cursor != null && cursor.moveToFirst();
		if (cursor != null) {
			cursor.close();
		}

		return exist;
	}

	/**
	 * @param context to get ContentResolver
	 * @param videoId to search for
	 * @return _id of video if it was saved before
	 */
	public static long haveSavedVideoById(Context context, long videoId) {
		ContentResolver contentResolver = context.getContentResolver();
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(videoId);

		Cursor cursor = contentResolver.query(uriArray[Tables.VIDEOS.ordinal()],
				PROJECTION_ITEM_ID, SELECTION_ITEM_ID, arguments1, LIMIT_1);
		if (cursor != null) {
			boolean exist = cursor.moveToFirst();
			if (exist) {
				return getInt(cursor, DbScheme.V_ID);
			}
			cursor.close();
		}
		return -1;
	}

	public static void saveArticleViewedState(ContentResolver contentResolver, CommonViewedItem currentItem) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(currentItem.getUsername());
		arguments2[1] = String.valueOf(currentItem.getId());

		Uri uri = uriArray[Tables.ARTICLE_VIEWED.ordinal()];

		Cursor cursor = contentResolver.query(uri, null,
				SELECTION_USER_AND_ID, arguments2, null);

		ContentValues values = putCommonViewedItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveVideoViewedState(ContentResolver contentResolver, CommonViewedItem currentItem) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = String.valueOf(currentItem.getUsername());
		arguments2[1] = String.valueOf(currentItem.getId());

		Uri uri = uriArray[Tables.VIDEO_VIEWED.ordinal()];

		Cursor cursor = contentResolver.query(uri, null,
				SELECTION_USER_AND_ID, arguments2, null);

		ContentValues values = putCommonViewedItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static Cursor getVideoViewedCursor(Context context, String username) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = username;
		Cursor cursor = contentResolver.query(uriArray[Tables.VIDEO_VIEWED.ordinal()],
				null, SELECTION_USER, arguments1, null);

		if (cursor != null && cursor.moveToFirst()) {
			return cursor;
		} else {
			return null;
		}
	}

	public static Cursor getArticleViewedCursor(Context context, String username) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments1 = sArguments1;
		arguments1[0] = username;
		Cursor cursor = contentResolver.query(uriArray[Tables.ARTICLE_VIEWED.ordinal()],
				null, SELECTION_USER, arguments1, null);

		if (cursor != null && cursor.moveToFirst()) {
			return cursor;
		} else {
			return null;
		}
	}

	public static boolean isVideoViewed(Context context, String username, long videoId) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = username;
		arguments2[1] = String.valueOf(videoId);

		Cursor cursor = contentResolver.query(uriArray[Tables.VIDEO_VIEWED.ordinal()],
				null, SELECTION_USER_AND_ID, arguments2, null);

		if (cursor != null && cursor.moveToFirst()) {
			boolean isViewed = getInt(cursor, V_DATA_VIEWED) > 0;
			cursor.close();
			return isViewed;
		} else {
			return false;
		}
	}

	public static boolean isArticleViewed(Context context, String username, long articleId) {
		ContentResolver contentResolver = context.getContentResolver();

		final String[] arguments2 = sArguments2;
		arguments2[0] = username;
		arguments2[1] = String.valueOf(articleId);

		Cursor cursor = contentResolver.query(uriArray[Tables.ARTICLE_VIEWED.ordinal()],
				null, SELECTION_USER_AND_ID, arguments2, null);

		if (cursor != null && cursor.moveToFirst()) {
			boolean isViewed = getInt(cursor, V_DATA_VIEWED) > 0;
			cursor.close();
			return isViewed;
		} else {
			return false;
		}
	}

	/**
	 * Check if we have saved articles for any user
	 *
	 * @param context to get resources
	 * @return true if cursor can be positioned to first
	 */
	public static boolean haveSavedArticles(Context context) {
		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(uriArray[Tables.ARTICLES.ordinal()], null, null, null, LIMIT_1);
		boolean exist = cursor != null && cursor.moveToFirst();
		if (cursor != null) {
			cursor.close();
		}

		return exist;
	}


	public static ContentValues putVideoItemToValues(VideoSingleItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_TITLE, dataObj.getTitle());
		values.put(V_DESCRIPTION, dataObj.getDescription());
		values.put(V_CATEGORY, dataObj.getCategoryName());
		values.put(V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(V_ID, dataObj.getVideoId());
		values.put(V_SKILL_LEVEL, dataObj.getSkillLevel());
		values.put(V_USER_AVATAR, dataObj.getUserAvatar());
		values.put(V_MINUTES, dataObj.getMinutes());
		values.put(V_VIEW_COUNT, dataObj.getViewCount());
		values.put(V_COUNTRY_ID, dataObj.getCountryId());
		values.put(V_COMMENT_COUNT, dataObj.getCommentCount());
		values.put(V_CREATE_DATE, dataObj.getCreateDate());
		values.put(V_URL, dataObj.getUrl());
		values.put(V_KEY_FEN, dataObj.getKeyFen());
		values.put(V_USERNAME, dataObj.getUsername());
		values.put(V_FIRST_NAME, dataObj.getFirstName());
		values.put(V_LAST_NAME, dataObj.getLastName());
		values.put(V_CHESS_TITLE, dataObj.getChessTitle());

		return values;
	}

	public static VideoSingleItem.Data fillVideoItemFromCursor(Cursor cursor) {
		VideoSingleItem.Data videoItem = new VideoSingleItem.Data();

		videoItem.setFirstName(getString(cursor, DbScheme.V_FIRST_NAME));
		videoItem.setChessTitle(getString(cursor, DbScheme.V_CHESS_TITLE));
		videoItem.setLastName(getString(cursor, DbScheme.V_LAST_NAME));
		videoItem.setTitle(getString(cursor, DbScheme.V_TITLE));
		videoItem.setCountryId(getInt(cursor, DbScheme.V_COUNTRY_ID));
		videoItem.setMinutes(getInt(cursor, DbScheme.V_MINUTES));
		videoItem.setViewCount(getLong(cursor, DbScheme.V_VIEW_COUNT));
		videoItem.setCreateDate(getLong(cursor, DbScheme.V_CREATE_DATE));
		videoItem.setDescription(getString(cursor, DbScheme.V_DESCRIPTION));
		videoItem.setUrl(getString(cursor, DbScheme.V_URL));
		videoItem.setVideoId(getInt(cursor, DbScheme.V_ID));
		videoItem.setAvatarUrl(getString(cursor, DbScheme.V_USER_AVATAR));
		videoItem.setCommentCount(getInt(cursor, DbScheme.V_COMMENT_COUNT));

		return videoItem;
	}

	public static ContentValues putForumCategoryItemToValues(ForumCategoryItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_NAME, dataObj.getCategory());
		values.put(V_ID, dataObj.getId());
		values.put(V_CREATE_DATE, dataObj.getCreateDate());
		values.put(V_LAST_POST_DATE, dataObj.getLastDate());
		values.put(V_DISPLAY_ORDER, dataObj.getDisplayOrder());
		values.put(V_DESCRIPTION, dataObj.getDescription());
		values.put(V_TOPIC_COUNT, dataObj.getTopicCount());
		values.put(V_POST_COUNT, dataObj.getPostCount());
		values.put(V_MIN_MEMBERSHIP, dataObj.getMinimumMembershipLevel());

		return values;
	}

	public static ContentValues putForumTopicItemToValues(ForumTopicItem.Topic dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_TITLE, dataObj.getSubject());
		values.put(V_ID, dataObj.getId());
		values.put(V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(V_CATEGORY, dataObj.getCategoryName());
		values.put(V_URL, dataObj.getUrl());
		values.put(V_USERNAME, dataObj.getTopicUsername());
		values.put(V_LAST_POST_USERNAME, dataObj.getLastPostUsername());
		values.put(V_POST_COUNT, dataObj.getPostCount());
		values.put(V_LAST_POST_DATE, dataObj.getLastPostDate());
		values.put(V_PAGE, dataObj.getPage());

		return values;
	}

	public static ContentValues putForumPostItemToValues(ForumPostItem.Post dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_DESCRIPTION, dataObj.getBody());
		values.put(V_ID, dataObj.getTopicId());
		values.put(V_CREATE_DATE, dataObj.getCreateDate());
		values.put(V_USERNAME, dataObj.getUsername());
		values.put(V_COUNTRY_ID, dataObj.getCountryId());
		values.put(V_PREMIUM_STATUS, dataObj.isPremiumStatus());
		values.put(V_PHOTO_URL, dataObj.getAvatarUrl());
		values.put(V_NUMBER, dataObj.getCommentNumber());
		values.put(V_PAGE, dataObj.getPage());

		return values;
	}

	public static void updateArticleCommentToDb(ContentResolver contentResolver, CommonCommentItem dataObj, long articleId) {
		String[] arguments = sArguments2;
		for (CommonCommentItem.Data currentItem : dataObj.getData()) {
			currentItem.setParentId(articleId);

			arguments[0] = String.valueOf(articleId);
			arguments[1] = String.valueOf(currentItem.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.ARTICLE_COMMENTS.ordinal()];

			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_PARENT_ID_AND_ITEM_ID,
					DbDataManager.SELECTION_PARENT_ID_AND_ITEM_ID, arguments, null);

			ContentValues values = DbDataManager.putCommonCommentItemToValues(currentItem);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

	public static void updateVideoCommentsToDb(ContentResolver contentResolver, CommonCommentItem dataObj, long videoId) {
		String[] arguments = sArguments2;
		for (CommonCommentItem.Data currentItem : dataObj.getData()) {
			currentItem.setParentId(videoId);

			arguments[0] = String.valueOf(videoId);
			arguments[1] = String.valueOf(currentItem.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[Tables.VIDEO_COMMENTS.ordinal()];

			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_PARENT_ID_AND_ITEM_ID,
					DbDataManager.SELECTION_PARENT_ID_AND_ITEM_ID, arguments, null);

			ContentValues values = DbDataManager.putCommonCommentItemToValues(currentItem);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

	public static void saveMentorLessonToDb(ContentResolver contentResolver, LessonItem.MentorLesson mentorLesson,
											long lessonId) {
		mentorLesson.setLessonId(lessonId);
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(lessonId);


		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.LESSONS_MENTOR_LESSONS.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID,
				SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = putLessonsMentorLessonToValues(mentorLesson);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveUserLessonToDb(ContentResolver contentResolver, LessonItem.UserLesson userLesson,
										  long lessonId, String username) {
		userLesson.setLessonId(lessonId);
		userLesson.setUsername(username);

		final String[] arguments1 = sArguments2;
		arguments1[0] = String.valueOf(userLesson.getLessonId());
		arguments1[1] = username;


		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.LESSONS_USER_LESSONS.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID_AND_USER,
				SELECTION_ITEM_ID_AND_USER, arguments1, null);

		ContentValues values = putLessonsUserLessonToValues(userLesson);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveCourseListItemToDb(ContentResolver contentResolver, LessonCourseListItem.Data currentItem) {
		final String[] arguments = sArguments2;
		arguments[0] = String.valueOf(currentItem.getId());
		arguments[1] = currentItem.getUser();

		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.LESSONS_COURSE_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID_AND_USER,
				SELECTION_ITEM_ID_AND_USER, arguments, null);


		ContentValues values = putLessonsCourseListItemToValues(currentItem);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static ContentValues putLessonsCourseListItemToValues(LessonCourseListItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_USER, dataObj.getUser());
		values.put(V_NAME, dataObj.getName());
		values.put(V_CATEGORY_ID, dataObj.getCategoryId());
		values.put(V_COURSE_COMPLETED, dataObj.isCourseCompleted() ? 1 : 0);

		return values;
	}

	public static ContentValues putLessonsCourseItemToValues(LessonCourseItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_DESCRIPTION, dataObj.getDescription());
		values.put(V_NAME, dataObj.getCourseName());

		return values;
	}

	public static LessonCourseItem.Data getLessonsCourseItemFromCursor(Cursor cursor) {
		LessonCourseItem.Data dataObj = new LessonCourseItem.Data();

		dataObj.setId(getLong(cursor, V_ID));
		dataObj.setDescription(getString(cursor, V_DESCRIPTION));
		dataObj.setCourseName(getString(cursor, V_NAME));

		return dataObj;
	}

	public static void saveLessonListItemToDb(ContentResolver contentResolver, LessonListItem lesson) {
		final String[] arguments = sArguments3;
		arguments[0] = String.valueOf(lesson.getId());
		arguments[1] = String.valueOf(lesson.getCourseId());
		arguments[2] = lesson.getUser();

		// TODO implement beginTransaction logic for performance increase
		Uri uri = uriArray[Tables.LESSONS_LESSONS_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ID_CATEGORY_ID_USER,
				SELECTION_ID_CATEGORY_ID_USER, arguments, null);

		ContentValues values = putLessonsListItemToValues(lesson);

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static List<LessonListItem> getIncompleteLessons(ContentResolver contentResolver, String username) {
		final String[] arguments = sArguments2;
		arguments[0] = username;
		arguments[1] = String.valueOf(1);

		Uri uri = uriArray[Tables.LESSONS_LESSONS_LIST.ordinal()];
		Cursor cursor = contentResolver.query(uri, null, SELECTION_USER_AND_LESSON_STATED, arguments, null);

		if (cursor != null && cursor.moveToFirst()) {
			List<LessonListItem> incompleteLessons = new ArrayList<LessonListItem>();
			do {
				incompleteLessons.add(getLessonsListItemFromCursor(cursor));
			} while (cursor.moveToNext());
			return incompleteLessons;
		}
		return null;
	}

	public static ContentValues putLessonsListItemToValues(LessonListItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_CATEGORY_ID, dataObj.getCourseId());
		values.put(V_LESSON_COMPLETED, dataObj.isCompleted() ? 1 : 0);
		values.put(V_LESSON_STARTED, dataObj.isStarted() ? 1 : 0);
		values.put(V_USER, dataObj.getUser());
		values.put(V_NAME, dataObj.getName());

		return values;
	}

	public static LessonListItem getLessonsListItemFromCursor(Cursor cursor) {
		LessonListItem dataObj = new LessonListItem();

		dataObj.setId(getInt(cursor, V_ID));
		dataObj.setUser(getString(cursor, V_USER));
		dataObj.setCourseId(getLong(cursor, V_CATEGORY_ID));
		dataObj.setCompleted(getInt(cursor, V_LESSON_COMPLETED) > 0);
		dataObj.setStarted(getInt(cursor, V_LESSON_STARTED) > 0);
		dataObj.setName(getString(cursor, V_NAME));

		return dataObj;
	}

	public static ContentValues putLessonsMentorLessonToValues(LessonItem.MentorLesson dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getLessonId());
		values.put(V_NUMBER, dataObj.getLessonNumber());
		values.put(V_GOAL, dataObj.getGoal());
		values.put(V_DIFFICULTY, dataObj.getDifficulty());
		values.put(V_AUTHOR, dataObj.getAuthor());
		values.put(V_NAME, dataObj.getName());
		values.put(V_DESCRIPTION, dataObj.getAbout());
		values.put(V_GOAL_COMMENT, dataObj.getGoalCommentary());
		values.put(V_GOAL_CODE, dataObj.getGoalCode());

		return values;
	}

	public static LessonItem.MentorLesson getLessonsMentorLessonFromCursor(Cursor cursor) {
		LessonItem.MentorLesson dataObj = new LessonItem.MentorLesson();

		dataObj.setLessonId(getLong(cursor, V_ID));
		dataObj.setLessonNumber(getInt(cursor, V_NUMBER));
		dataObj.setGoal(getInt(cursor, V_GOAL));
		dataObj.setDifficulty(getInt(cursor, V_DIFFICULTY));
		dataObj.setAuthor(getString(cursor, V_AUTHOR));
		dataObj.setName(getString(cursor, V_NAME));
		dataObj.setAbout(getString(cursor, V_DESCRIPTION));
		dataObj.setGoalCommentary(getString(cursor, V_GOAL_COMMENT));
		dataObj.setGoalCode(getString(cursor, V_GOAL_CODE));

		return dataObj;
	}

	public static ContentValues putLessonsPositionToValues(LessonItem.MentorPosition dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getLessonId());
		values.put(V_NUMBER, dataObj.getPositionNumber());
		values.put(V_USER_TO_MOVE, dataObj.getUserToMove());
		values.put(V_DIFFICULTY, dataObj.getMoveDifficulty());
		values.put(V_FINAL_POSITION, dataObj.getFinalPosition());
		values.put(V_FEN, dataObj.getFen());
		values.put(V_ADVICE_1, dataObj.getAdvice1());
		values.put(V_ADVICE_2, dataObj.getAdvice2());
		values.put(V_ADVICE_3, dataObj.getAdvice3());
		values.put(V_RESPONSE_MOVE_COMMENT, dataObj.getStandardResponseMoveCommentary());
		values.put(V_WRONG_MOVE_COMMENT, dataObj.getStandardWrongMoveCommentary());
		values.put(V_DESCRIPTION, dataObj.getAbout());

		return values;
	}

	public static LessonItem.MentorPosition getLessonsPositionFromCursor(Cursor cursor) {
		LessonItem.MentorPosition dataObj = new LessonItem.MentorPosition();

		dataObj.setLessonId(getLong(cursor, V_ID));
		dataObj.setPositionNumber(getInt(cursor, V_NUMBER));
		dataObj.setUserToMove(getInt(cursor, V_USER_TO_MOVE));
		dataObj.setMoveDifficulty(getInt(cursor, V_DIFFICULTY));
		dataObj.setFinalPosition(getInt(cursor, V_FINAL_POSITION));
		dataObj.setFen(getString(cursor, V_FEN));
		dataObj.setAdvice1(getString(cursor, V_ADVICE_1));
		dataObj.setAdvice2(getString(cursor, V_ADVICE_2));
		dataObj.setAdvice3(getString(cursor, V_ADVICE_3));
		dataObj.setStandardResponseMoveCommentary(getString(cursor, V_RESPONSE_MOVE_COMMENT));
		dataObj.setStandardWrongMoveCommentary(getString(cursor, V_WRONG_MOVE_COMMENT));
		dataObj.setAbout(getString(cursor, V_DESCRIPTION));

		return dataObj;
	}

	public static ContentValues putLessonsPositionMoveToValues(LessonItem.MentorPosition.PossibleMove dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getLessonId());
		values.put(V_CURRENT_POSITION, dataObj.getPositionNumber());
		values.put(V_NUMBER, dataObj.getMoveNumber());
		values.put(V_MOVE, dataObj.getMove());
		values.put(V_MOVE_COMMENT, dataObj.getMoveCommentary());
		values.put(V_SHORT_RESPONSE_MOVE, dataObj.getShortResponseMove());
		values.put(V_RESPONSE_MOVE_COMMENT, dataObj.getResponseMoveCommentary());
		values.put(V_MOVE_TYPE, dataObj.getMoveType());

		return values;
	}

	public static LessonItem.MentorPosition.PossibleMove getLessonsPositionMoveFromCursor(Cursor cursor) {
		LessonItem.MentorPosition.PossibleMove dataObj = new LessonItem.MentorPosition.PossibleMove();

		dataObj.setLessonId(getLong(cursor, V_ID));
		dataObj.setPositionNumber(getInt(cursor, V_CURRENT_POSITION));
		dataObj.setMoveNumber(getInt(cursor, V_NUMBER));
		dataObj.setMove(getString(cursor, V_MOVE));
		dataObj.setMoveCommentary(getString(cursor, V_MOVE_COMMENT));
		dataObj.setShortResponseMove(getString(cursor, V_SHORT_RESPONSE_MOVE));
		dataObj.setResponseMoveCommentary(getString(cursor, V_RESPONSE_MOVE_COMMENT));
		dataObj.setMoveType(getString(cursor, V_MOVE_TYPE));

		return dataObj;
	}

	public static ContentValues putLessonsUserLessonToValues(LessonItem.UserLesson dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getLessonId());
		values.put(V_CURRENT_POSITION, dataObj.getCurrentPosition());
		values.put(V_CURRENT_POINTS, dataObj.getCurrentPoints());
		values.put(V_CURRENT_POSITION_POINTS, dataObj.getCurrentPositionPoints());
		values.put(V_USER, dataObj.getUsername());
		values.put(V_INITIAL_SCORE, dataObj.getInitialScore());
		values.put(V_LAST_SCORE, dataObj.getLastScore());
		values.put(V_LEGAL_POSITION_CHECK, dataObj.getLegalPositionCheck());
		values.put(V_LEGAL_MOVE_CHECK, dataObj.getLegalMoveCheck());
		values.put(V_LESSON_COMPLETED, dataObj.isLessonCompleted() ? 1 : 0);

		return values;
	}

	public static LessonItem.UserLesson getLessonsUserLessonFromCursor(Cursor cursor) {
		LessonItem.UserLesson dataObj = new LessonItem.UserLesson();

		dataObj.setLessonId(getLong(cursor, V_ID));
		dataObj.setCurrentPosition(getInt(cursor, V_CURRENT_POSITION));
		dataObj.setCurrentPoints(getInt(cursor, V_CURRENT_POINTS));
		dataObj.setCurrentPositionPoints(Float.valueOf(getString(cursor, V_CURRENT_POSITION_POINTS)));
		dataObj.setUsername(getString(cursor, V_USER));
		dataObj.setInitialScore(getString(cursor, V_INITIAL_SCORE));
		dataObj.setLastScore(getString(cursor, V_LAST_SCORE));
		dataObj.setLegalPositionCheck(getString(cursor, V_LEGAL_POSITION_CHECK));
		dataObj.setLegalMoveCheck(getString(cursor, V_LEGAL_MOVE_CHECK));
		dataObj.setLessonCompleted(getInt(cursor, V_LESSON_COMPLETED) > 0);

		return dataObj;
	}

	public static ContentValues putCommonViewedItemToValues(CommonViewedItem dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_USER, dataObj.getUsername());
		values.put(V_ID, dataObj.getId());
		values.put(V_DATA_VIEWED, dataObj.isViewed() ? 1 : 0);

		return values;
	}

	public static ContentValues putCommonFeedCategoryItemToValues(CommonFeedCategoryItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_NAME, dataObj.getName());
		values.put(V_CATEGORY_ID, dataObj.getId());
		values.put(V_DISPLAY_ORDER, dataObj.getDisplayOrder());

		return values;
	}

	/* ========================================== Messages ========================================== */
	public static ContentValues putConversationItemToValues(ConversationItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_OTHER_USER_ID, dataObj.getOtherUserId());
		values.put(V_LAST_MESSAGE_ID, dataObj.getLastMessageId());
		values.put(V_LAST_MESSAGE_CREATED_AT, dataObj.getLastMessageCreatedAt());
		values.put(V_OTHER_USER_IS_ONLINE, dataObj.isOtherUserIsOnline() ? 1 : 0);
		values.put(V_NEW_MESSAGES_COUNT, dataObj.getNewMessagesCount());
		values.put(V_USER, dataObj.getUser());
		values.put(V_OTHER_USER_USERNAME, dataObj.getOtherUserUsername());
		values.put(V_OTHER_USER_AVATAR_URL, dataObj.getOtherUserAvatarUrl());
		values.put(V_LAST_MESSAGE_SENDER_USERNAME, dataObj.getLastMessageSenderUsername());
		values.put(V_LAST_MESSAGE_CONTENT, dataObj.getLastMessageContent());

		return values;
	}

	public static ContentValues putMessagesItemToValues(MessagesItem.Data dataObj) {
		ContentValues values = new ContentValues();

		values.put(V_ID, dataObj.getId());
		values.put(V_CONVERSATION_ID, dataObj.getConversationId());
		values.put(V_OTHER_USER_ID, dataObj.getSenderId());
		values.put(V_CREATE_DATE, dataObj.getCreatedAt());
		values.put(V_OTHER_USER_IS_ONLINE, dataObj.isSenderIsOnline() ? 1 : 0);
		values.put(V_USER, dataObj.getUser());
		values.put(V_OTHER_USER_USERNAME, dataObj.getSenderUsername());
		values.put(V_OTHER_USER_AVATAR_URL, dataObj.getSenderAvatarUrl());
		values.put(V_LAST_MESSAGE_CONTENT, dataObj.getContent());

		return values;
	}


	/* ========================================== Stats ========================================== */

	public static ContentValues putUserStatsGameItemToValues(UserStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(V_USER, user);
		values.put(V_CURRENT, dataObj.getRating());
		values.put(V_HIGHEST_RATING, dataObj.getHighestRating());
		values.put(V_AVERAGE_OPPONENT_RATING, dataObj.getAvgOpponentRating());
		values.put(V_GAMES_TOTAL, dataObj.getTotalGames());
		values.put(V_GAMES_WINS, dataObj.getWins());
		values.put(V_GAMES_LOSSES, dataObj.getLosses());
		values.put(V_GAMES_DRAWS, dataObj.getDraws());
		values.put(V_BEST_WIN_RATING, dataObj.getBestWinRating());
		values.put(V_BEST_WIN_USERNAME, dataObj.getBestWinUsername());

		return values;
	}

	public static ContentValues putUserStatsTacticsItemToValues(UserTacticsStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(V_USER, user);
		values.put(V_CURRENT, dataObj.getCurrent());
		values.put(V_HIGHEST_RATING, dataObj.getHighest().getRating());
		values.put(V_HIGHEST_TIMESTAMP, dataObj.getHighest().getTimestamp());
		values.put(V_LOWEST_RATING, dataObj.getLowest().getRating());
		values.put(V_LOWEST_TIMESTAMP, dataObj.getLowest().getTimestamp());

		values.put(V_ATTEMPT_COUNT, dataObj.getAttemptCount());
		values.put(V_PASSED_COUNT, dataObj.getPassedCount());
		values.put(V_FAILED_COUNT, dataObj.getFailedCount());
		values.put(V_TOTAL_SECONDS, dataObj.getTotalSeconds());
		values.put(V_TODAYS_ATTEMPTS, dataObj.getTodaysAttemps());
		values.put(V_TODAYS_AVG_SCORE, dataObj.getTodaysAvgScore());

		return values;
	}

	public static ContentValues putUserStatsLessonsItemToValues(UserLessonsStatsData dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(V_USER, user);

		BaseRating highest = dataObj.getRatings().getHighest();
		BaseRating lowest = dataObj.getRatings().getLowest();
		int current = dataObj.getRatings().getCurrent();

		values.put(V_CURRENT, current);
		values.put(V_HIGHEST_RATING, highest.getRating());
		values.put(V_HIGHEST_TIMESTAMP, highest.getTimestamp());
		values.put(V_LOWEST_RATING, lowest.getRating());
		values.put(V_LOWEST_TIMESTAMP, lowest.getTimestamp());

		UserLessonsStatsData.Stats stats = dataObj.getLessons().getStats();
		values.put(V_LESSONS_TRIED, stats.getLessonsTried());
		values.put(V_TOTAL_LESSON_COUNT, stats.getTotalLessonCount());
		values.put(V_LESSON_COMPLETE_PERCENTAGE, stats.getLessonCompletePercentage());
		values.put(V_TOTAL_TRAINING_SECONDS, stats.getTotalLessonCount());
		values.put(V_SCORE_90_100, stats.getScore().getP_90_100());
		values.put(V_SCORE_80_89, stats.getScore().getP_80_89());
		values.put(V_SCORE_70_79, stats.getScore().getP_70_79());
		values.put(V_SCORE_60_69, stats.getScore().getP_60_69());
		values.put(V_SCORE_50_59, stats.getScore().getP_50_59());
		values.put(V_SCORE_50, stats.getScore().getP_50());

		return values;
	}

	public static ContentValues putGameStatsLiveItemToValues(GameStatsItem.Data dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(V_USER, user);
		values.put(V_CURRENT, dataObj.getRating().getCurrent());
		values.put(V_RANK, dataObj.getRating().getTodaysRank().getRank());
		values.put(V_TOTAL_PLAYER_COUNT, dataObj.getRating().getTodaysRank().getTotalPlayerCount());
		values.put(V_PERCENTILE, dataObj.getRating().getPercentile());
		values.put(V_GLICKO_RD, dataObj.getRating().getGlickoRd());

		values.put(V_HIGHEST_RATING, dataObj.getRating().getHighest().getRating());
		values.put(V_HIGHEST_TIMESTAMP, dataObj.getRating().getHighest().getTimestamp());
		values.put(V_LOWEST_RATING, dataObj.getRating().getLowest().getRating());
		values.put(V_LOWEST_TIMESTAMP, dataObj.getRating().getLowest().getTimestamp());
		values.put(V_AVERAGE_OPPONENT_RATING, dataObj.getRating().getAverageOpponent());

		values.put(V_BEST_WIN_RATING, dataObj.getRating().getBestWin().getRating());
		values.put(V_BEST_WIN_GAME_ID, dataObj.getRating().getBestWin().getGameId());
		values.put(V_BEST_WIN_USERNAME, dataObj.getRating().getBestWin().getUsername());
		values.put(V_AVG_OPPONENT_RATING_WIN, dataObj.getRating().getAverageOpponentRating().getWin());
		values.put(V_AVG_OPPONENT_RATING_LOSE, dataObj.getRating().getAverageOpponentRating().getLose());
		values.put(V_AVG_OPPONENT_RATING_DRAW, dataObj.getRating().getAverageOpponentRating().getDraw());
		values.put(V_UNRATED, dataObj.getGames().getUnrated());
		values.put(V_IN_PROGRESS, dataObj.getGames().getInProgress());
		values.put(V_TIMEOUTS, dataObj.getGames().getTimeoutPercent());

		values.put(V_GAMES_TOTAL, dataObj.getGames().getTotal());
		values.put(V_GAMES_BLACK, dataObj.getGames().getBlack());
		values.put(V_GAMES_WHITE, dataObj.getGames().getWhite());

		values.put(V_WINS_TOTAL, dataObj.getGames().getWins().getTotal());
		values.put(V_WINS_WHITE, dataObj.getGames().getWins().getWhite());
		values.put(V_WINS_BLACK, dataObj.getGames().getWins().getBlack());

		values.put(V_LOSSES_TOTAL, dataObj.getGames().getLosses().getTotal());
		values.put(V_LOSSES_WHITE, dataObj.getGames().getLosses().getWhite());
		values.put(V_LOSSES_BLACK, dataObj.getGames().getLosses().getBlack());

		values.put(V_DRAWS_TOTAL, dataObj.getGames().getDraws().getTotal());
		values.put(V_DRAWS_WHITE, dataObj.getGames().getDraws().getWhite());
		values.put(V_DRAWS_BLACK, dataObj.getGames().getDraws().getBlack());

		values.put(V_WINNING_STREAK, dataObj.getGames().getWinningStreak());
		values.put(V_LOSING_STREAK, dataObj.getGames().getLosingStreak());
		if (dataObj.getGames().getMostFrequentOpponent() != null) {
			values.put(V_FREQUENT_OPPONENT_NAME, dataObj.getGames().getMostFrequentOpponent().getUsername());
			values.put(V_FREQUENT_OPPONENT_GAMES_PLAYED, dataObj.getGames().getMostFrequentOpponent().getGamesPlayed());
		} else {
			values.put(V_FREQUENT_OPPONENT_NAME, Symbol.EMPTY);
			values.put(V_FREQUENT_OPPONENT_GAMES_PLAYED, 0);
		}

		return values;
	}

	public static ContentValues putGameStatsDailyItemToValues(GameStatsItem.Data dataObj, String user) {
		ContentValues values = new ContentValues();


		values.put(V_USER, user);
		GameStatsItem.GameRating rating = dataObj.getRating();
		if (rating != null) {
			values.put(V_CURRENT, rating.getCurrent());
			values.put(V_RANK, rating.getTodaysRank().getRank());
			values.put(V_TOTAL_PLAYER_COUNT, rating.getTodaysRank().getTotalPlayerCount());
			values.put(V_PERCENTILE, rating.getPercentile());
			values.put(V_GLICKO_RD, rating.getGlickoRd());

			values.put(V_HIGHEST_RATING, rating.getHighest().getRating());
			values.put(V_HIGHEST_TIMESTAMP, rating.getHighest().getTimestamp());
			values.put(V_LOWEST_RATING, rating.getLowest().getRating());
			values.put(V_LOWEST_TIMESTAMP, rating.getLowest().getTimestamp());
			values.put(V_AVERAGE_OPPONENT_RATING, rating.getAverageOpponent());

			values.put(V_BEST_WIN_RATING, rating.getBestWin().getRating());
			values.put(V_BEST_WIN_GAME_ID, rating.getBestWin().getGameId());
			values.put(V_BEST_WIN_USERNAME, rating.getBestWin().getUsername());
			values.put(V_AVG_OPPONENT_RATING_WIN, rating.getAverageOpponentRating().getWin());
			values.put(V_AVG_OPPONENT_RATING_LOSE, rating.getAverageOpponentRating().getLose());
			values.put(V_AVG_OPPONENT_RATING_DRAW, rating.getAverageOpponentRating().getDraw());
		}

		GameStatsItem.Games games = dataObj.getGames();
		if (games != null) {
			values.put(V_UNRATED, games.getUnrated());
			values.put(V_IN_PROGRESS, games.getInProgress());
			values.put(V_TIMEOUTS, games.getTimeoutPercent());

			values.put(V_GAMES_TOTAL, games.getTotal());
			values.put(V_GAMES_BLACK, games.getBlack());
			values.put(V_GAMES_WHITE, games.getWhite());

			values.put(V_WINS_TOTAL, games.getWins().getTotal());
			values.put(V_WINS_WHITE, games.getWins().getWhite());
			values.put(V_WINS_BLACK, games.getWins().getBlack());

			values.put(V_LOSSES_TOTAL, games.getLosses().getTotal());
			values.put(V_LOSSES_WHITE, games.getLosses().getWhite());
			values.put(V_LOSSES_BLACK, games.getLosses().getBlack());

			values.put(V_DRAWS_TOTAL, games.getDraws().getTotal());
			values.put(V_DRAWS_WHITE, games.getDraws().getWhite());
			values.put(V_DRAWS_BLACK, games.getDraws().getBlack());

			values.put(V_WINNING_STREAK, games.getWinningStreak());
			values.put(V_LOSING_STREAK, games.getLosingStreak());

			if (games.getMostFrequentOpponent() != null) {
				values.put(V_FREQUENT_OPPONENT_NAME, games.getMostFrequentOpponent().getUsername());
				values.put(V_FREQUENT_OPPONENT_GAMES_PLAYED, games.getMostFrequentOpponent().getGamesPlayed());
			} else {
				values.put(V_FREQUENT_OPPONENT_NAME, Symbol.EMPTY);
				values.put(V_FREQUENT_OPPONENT_GAMES_PLAYED, 0);
			}

		}

		Tournaments tournaments = dataObj.getTournaments();
		if (tournaments != null) {
			Tournaments.All tournamentsAll = tournaments.getAll();
			values.put(V_TOURNAMENTS_LEADERBOARD_POINTS, tournamentsAll.getLeaderboardPoints());
			values.put(V_TOURNAMENTS_EVENTS_ENTERED, tournamentsAll.getEventsEntered());
			values.put(V_TOURNAMENTS_FIRST_PLACE_FINISHES, tournamentsAll.getFirstPlaceFinishes());
			values.put(V_TOURNAMENTS_SECOND_PLACE_FINISHES, tournamentsAll.getSecondPlaceFinishes());
			values.put(V_TOURNAMENTS_THIRD_PLACE_FINISHES, tournamentsAll.getThirdPlaceFinishes());
			values.put(V_TOURNAMENTS_WITHDRAWALS, tournamentsAll.getWithdrawals());
			values.put(V_TOURNAMENTS_HOSTED, tournamentsAll.getTournamentsHosted());
			values.put(V_TOTAL_COUNT_PLAYERS_HOSTED, tournamentsAll.getTotalCountPlayersHosted());

			Tournaments.Games tournamentsGames = tournaments.getGames();
			values.put(V_TOURNAMENTS_GAMES_TOTAL, tournamentsGames.getTotal());
			values.put(V_TOURNAMENTS_GAMES_WON, tournamentsGames.getWins());
			values.put(V_TOURNAMENTS_GAMES_LOST, tournamentsGames.getLosses());
			values.put(V_TOURNAMENTS_GAMES_DRAWN, tournamentsGames.getDraws());
			values.put(V_TOURNAMENTS_GAMES_IN_PROGRESS, tournamentsGames.getInProgress());
		}
		return values;
	}

	public static ContentValues putGraphDataItemToValues(GraphData.SingleItem dataObj, String user) {
		ContentValues values = new ContentValues();

		values.put(V_TIMESTAMP, dataObj.getTimestamp());
		values.put(V_MIN_Y, dataObj.getMinY());
		values.put(V_MAX_X, dataObj.getMaxX());
		values.put(V_RATING, dataObj.getRating());
		values.put(V_GAME_TYPE, dataObj.getGameType());
		values.put(V_USER, user);

		return values;
	}

	public static GraphData.SingleItem getGraphSingleItemFromCursor(Cursor cursor) {
		GraphData.SingleItem pointData = new GraphData.SingleItem();

		pointData.setTimestamp(getLong(cursor, V_TIMESTAMP));
		pointData.setMinY(getInt(cursor, V_MIN_Y));
		pointData.setMaxX(getInt(cursor, V_MAX_X));
		pointData.setRating(getInt(cursor, V_RATING));

		return pointData;
	}

	public static GamesInfoByResult getGameStatsGamesByResultFromCursor(Cursor cursor) {
		GamesInfoByResult tournamentGames = new GamesInfoByResult();
		tournamentGames.setTotal(getInt(cursor, V_GAMES_TOTAL));
		tournamentGames.setWins(getInt(cursor, V_WINS_TOTAL));
		tournamentGames.setLosses(getInt(cursor, V_LOSSES_TOTAL));
		tournamentGames.setDraws(getInt(cursor, V_DRAWS_TOTAL));
		return tournamentGames;
	}


	public static boolean checkIfDrawOffered(ContentResolver resolver, String username, long gameId) {
		final String[] arguments2 = sArguments2;
		arguments2[0] = username;
		arguments2[1] = String.valueOf(gameId);

		Cursor cursor = resolver.query(uriArray[Tables.DAILY_CURRENT_GAMES.ordinal()],
				PROJECTION_DAILY_DRAW_OFFERED, SELECTION_OPPONENT_OFFERED_DRAW, arguments2, null);
		return cursor != null && cursor.moveToFirst() && getInt(cursor, V_OPPONENT_OFFERED_DRAW) > 0;
	}

	// ============================ GCM notifications ===========================================================

	public static void saveNewFriendRequest(ContentResolver contentResolver, FriendRequestItem item, String username) {
		final String[] arguments1 = sArguments2;
		arguments1[0] = username; // current auth username
		arguments1[1] = item.getUsername();

		Uri uri = uriArray[Tables.NOTIFICATION_FRIEND_REQUEST.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_USER_AND_USERNAME,
				SELECTION_USER_AND_USERNAME, arguments1, null);

		ContentValues values = new ContentValues();

		values.put(V_CREATE_DATE, item.getCreatedAt());
		values.put(V_USER, username);
		values.put(V_SEEN, item.userSawIt() ? 1 : 0);
		values.put(V_MESSAGE, item.getMessage());
		values.put(V_USERNAME, item.getUsername());
		values.put(V_USER_AVATAR, item.getAvatar());

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveNewChatNotification(ContentResolver contentResolver, NewChatNotificationItem item, String username) {
		final String[] arguments1 = sArguments2;
		arguments1[0] = username; // current auth username
		arguments1[1] = item.getUsername();

		Uri uri = uriArray[Tables.NOTIFICATION_NEW_CHAT_MESSAGES.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_USER_AND_USERNAME,
				SELECTION_USER_AND_USERNAME, arguments1, null);

		ContentValues values = new ContentValues();

		values.put(V_ID, item.getGameId());
		values.put(V_CREATE_DATE, item.getCreatedAt());
		values.put(V_USER, username);
		values.put(V_MESSAGE, item.getMessage());
		values.put(V_USERNAME, item.getUsername());
		values.put(V_USER_AVATAR, item.getAvatar());

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveNewChallengeNotification(ContentResolver contentResolver, NewChallengeNotificationItem item, String username) {

		final String[] arguments1 = sArguments2;
		arguments1[0] = String.valueOf(item.getChallengeId());
		arguments1[1] = username; // current auth username

		Uri uri = uriArray[Tables.NOTIFICATION_NEW_CHALLENGES.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID_AND_USER,
				SELECTION_ITEM_ID_AND_USER, arguments1, null);

		ContentValues values = new ContentValues();

		values.put(V_ID, item.getChallengeId());
		values.put(V_USER, username);
		values.put(V_USERNAME, item.getUsername());
		values.put(V_USER_AVATAR, item.getAvatar());

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static void saveGameOverNotification(ContentResolver contentResolver, GameOverNotificationItem item, String username) {

		final String[] arguments1 = sArguments2;
		arguments1[0] = String.valueOf(item.getGameId());
		arguments1[1] = username; // current auth username

		Uri uri = uriArray[Tables.NOTIFICATION_GAMES_OVER.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_ITEM_ID_AND_USER,
				SELECTION_ITEM_ID_AND_USER, arguments1, null);

		ContentValues values = new ContentValues();

		values.put(V_ID, item.getGameId());
		values.put(V_USER, username);
		values.put(V_MESSAGE, item.getMessage());
		values.put(V_USER_AVATAR, item.getAvatar());

		updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	public static int getUnreadNotificationsCnt(ContentResolver contentResolver, String username) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = username;

		int notificationsCnt = 0;
		{
			final String[] arguments = sArguments2;
			arguments[0] = username;
			arguments[1] = String.valueOf(0);

			Cursor cursor = contentResolver.query(uriArray[Tables.NOTIFICATION_FRIEND_REQUEST.ordinal()],
					PROJECTION_USER_AND_SEEN, SELECTION_USER_AND_SEEN, arguments, null);
			if (cursor != null && cursor.moveToFirst()) {
				notificationsCnt += cursor.getCount();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		{
			Cursor cursor = contentResolver.query(uriArray[Tables.NOTIFICATION_NEW_CHAT_MESSAGES.ordinal()],
					PROJECTION_USER, SELECTION_USER, arguments1, null);
			if (cursor != null && cursor.moveToFirst()) {
				notificationsCnt += cursor.getCount();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		{
			Cursor cursor = contentResolver.query(uriArray[Tables.NOTIFICATION_NEW_CHALLENGES.ordinal()],
					PROJECTION_USER, SELECTION_USER, arguments1, null);
			if (cursor != null && cursor.moveToFirst()) {
				notificationsCnt += cursor.getCount();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		{
			Cursor cursor = contentResolver.query(uriArray[Tables.NOTIFICATION_GAMES_OVER.ordinal()],
					PROJECTION_USER, SELECTION_USER, arguments1, null);
			if (cursor != null && cursor.moveToFirst()) {
				notificationsCnt += cursor.getCount();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		return notificationsCnt;
	}

	public static void deleteNewChallengeNotification(ContentResolver contentResolver, String username, Long challengeId) {
		final String[] arguments = sArguments2;
		arguments[0] = String.valueOf(challengeId);
		arguments[1] = username;

		Uri uri = uriArray[Tables.NOTIFICATION_NEW_CHALLENGES.ordinal()];
		int deleteCnt = contentResolver.delete(uri, SELECTION_ITEM_ID_AND_USER, arguments);

		Log.d("TEST", "deleteCnt = " + deleteCnt);
	}

	public static void updateNewFriendRequestNotification(ContentResolver contentResolver, String username,
														  String likelyFriend, boolean userSaw) {
		final String[] arguments1 = sArguments2;
		arguments1[0] = username; // current auth username
		arguments1[1] = likelyFriend;

		Uri uri = uriArray[Tables.NOTIFICATION_FRIEND_REQUEST.ordinal()];
		Cursor cursor = contentResolver.query(uri, PROJECTION_USER_AND_SEEN,
				SELECTION_USER_AND_USERNAME, arguments1, null);

		ContentValues values = new ContentValues();

		values.put(V_SEEN, userSaw ? 1 : 0);

		if (cursor != null && cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, SELECTION_USER_AND_USERNAME, arguments1);
		}
	}

	// ================================= global help methods =======================================
	public static void updateOrInsertValues(ContentResolver contentResolver, Cursor cursor, Uri uri, ContentValues values) {
		if (cursor != null && cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		if (cursor != null) {
			cursor.close();
		}
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
		return cursor.getLong(cursor.getColumnIndex(_ID));
	}

	public static String anyLikeMatch(String query) {
		return "%" + query + "%";
	}

	public static String startLikeMatch(String query) {
		return "%" + query;
	}

	public static String endLikeMatch(String query) {
		return query + "%";
	}


	public static int getDbVersion() {
		return DATABASE_VERSION;
	}

}
