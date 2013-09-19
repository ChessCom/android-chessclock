package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.stats.UserStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:32
 */
public class SaveUserStatsTask extends AbstractUpdateTask<UserStatsItem.Data, Long> {

	private final String username;
	private ContentResolver resolver;
	protected static String[] arguments = new String[1];

	public SaveUserStatsTask(TaskUpdateInterface<UserStatsItem.Data> taskFace, UserStatsItem.Data item,
							 ContentResolver resolver, String username) {
		super(taskFace);
		this.item = item;
		this.resolver = resolver;
		this.username = username;
	}

	@Override
	protected Integer doTheTask(Long... params) {

		saveLiveStats(username, item, resolver);
		saveDailyStats(username, item, resolver);
		saveTacticsStats(username, item, resolver);
		saveLessonsStats(username, item, resolver);

		return StaticData.RESULT_OK;
	}

	public static void saveLiveStats(String username, UserStatsItem.Data item, ContentResolver contentResolver) {

		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(username);

		// save Live Standard
		if (item.getLiveStandard() != null) {
			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsGameItemToValues(item.getLiveStandard(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		// save Live Lightning
		if (item.getLiveBullet() != null) {
			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsGameItemToValues(item.getLiveBullet(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		// save Live Blitz
		if (item.getLiveBlitz() != null) {
			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsGameItemToValues(item.getLiveBlitz(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

	public static void saveDailyStats(String username, UserStatsItem.Data item, ContentResolver contentResolver) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(username);

		// save Daily Chess
		if (item.getDailyChess() != null) {
			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsGameItemToValues(item.getDailyChess(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		// save  Chess960
		if (item.getChess960() != null) {
			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_DAILY_CHESS960.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsGameItemToValues(item.getChess960(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

	public static void saveTacticsStats(String username, UserStatsItem.Data item, ContentResolver contentResolver) {
		if (item.getTactics() != null && item.getTactics() != null) {
			final String[] userArgument = arguments;
			userArgument[0] = String.valueOf(username);

			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_TACTICS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsTacticsItemToValues(item.getTactics(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

	public static void saveLessonsStats(String username, UserStatsItem.Data item, ContentResolver contentResolver) {
		if (item.getLessons() != null) {
			final String[] userArgument = arguments;
			userArgument[0] = String.valueOf(username);

			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_LESSONS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);
			ContentValues values = DbDataManager.putUserStatsLessonsItemToValues(item.getLessons(), username);
			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}
}
