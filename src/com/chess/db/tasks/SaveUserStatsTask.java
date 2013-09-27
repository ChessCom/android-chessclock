package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.stats.BaseRating;
import com.chess.backend.entity.api.stats.UserLessonsStatsData;
import com.chess.backend.entity.api.stats.UserStatsItem;
import com.chess.backend.entity.api.stats.UserTacticsStatsData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import static com.chess.db.DbScheme.*;

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
		UserTacticsStatsData statsData = item.getTactics();
		if (statsData != null) {
			final String[] userArgument = arguments;
			userArgument[0] = String.valueOf(username);

			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_TACTICS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = new ContentValues();

			values.put(V_USER, username);
			values.put(V_CURRENT, statsData.getCurrent());
			values.put(V_HIGHEST_RATING, statsData.getHighest().getRating());
			values.put(V_HIGHEST_TIMESTAMP, statsData.getHighest().getTimestamp());
			values.put(V_LOWEST_RATING, statsData.getLowest().getRating());
			values.put(V_LOWEST_TIMESTAMP, statsData.getLowest().getTimestamp());

			values.put(V_ATTEMPT_COUNT, statsData.getAttemptCount());
			values.put(V_PASSED_COUNT, statsData.getPassedCount());
			values.put(V_FAILED_COUNT, statsData.getFailedCount());
			values.put(V_TOTAL_SECONDS, statsData.getTotalSeconds());
			values.put(V_TODAYS_ATTEMPTS, statsData.getTodaysAttemps());
			values.put(V_TODAYS_AVG_SCORE, statsData.getTodaysAvgScore());

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

	public static void saveLessonsStats(String username, UserStatsItem.Data item, ContentResolver contentResolver) {
		UserLessonsStatsData statsData = item.getLessons();
		if (statsData != null) {
			final String[] userArgument = arguments;
			userArgument[0] = String.valueOf(username);

			Uri uri = DbScheme.uriArray[DbScheme.Tables.USER_STATS_LESSONS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = new ContentValues();

			values.put(V_USER, username);

			BaseRating highest = statsData.getRatings().getHighest();
			BaseRating lowest = statsData.getRatings().getLowest();
			int current = statsData.getRatings().getCurrent();

			values.put(V_CURRENT, current);
			values.put(V_HIGHEST_RATING, highest.getRating());
			values.put(V_HIGHEST_TIMESTAMP, highest.getTimestamp());
			values.put(V_LOWEST_RATING, lowest.getRating());
			values.put(V_LOWEST_TIMESTAMP, lowest.getTimestamp());

			UserLessonsStatsData.Stats stats = statsData.getStats();
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

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}
}
