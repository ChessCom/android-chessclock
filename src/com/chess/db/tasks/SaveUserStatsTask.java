package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.stats.UserStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:32
 */
public class SaveUserStatsTask extends AbstractUpdateTask<UserStatsItem.Data, Long> {

	private final String userName;
	private ContentResolver resolver;
	protected static String[] arguments = new String[1];

	public SaveUserStatsTask(TaskUpdateInterface<UserStatsItem.Data> taskFace, UserStatsItem.Data item,
							 ContentResolver resolver) {
		super(taskFace);
		this.item = item;
		this.resolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		userName = appData.getUsername();

	}

	@Override
	protected Integer doTheTask(Long... params) {

		saveLiveStats(userName, item, resolver);
		saveDailyStats(userName, item, resolver);
		saveTacticsStats(userName, item, resolver);
		saveChessMentorStats(userName, item, resolver);

		return StaticData.RESULT_OK;
	}

	public static void saveLiveStats(String userName, UserStatsItem.Data item, ContentResolver resolver) {
		if (item.getLive() == null) {
			return;
		}

		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		{ // Standard
			if (item.getLive().getStandard() == null) {
				return;
			}

			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_LIVE_STANDARD.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsLiveItemToValues(item.getLive().getStandard(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}

		{ // Lightning
			if (item.getLive().getLightning() == null) {
				return;
			}

			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_LIVE_LIGHTNING.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsLiveItemToValues(item.getLive().getLightning(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}

		{ // Blitz
			if (item.getLive().getBlitz() == null) {
				return;
			}

			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_LIVE_BLITZ.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsLiveItemToValues(item.getLive().getBlitz(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}
	}

	public static  void saveDailyStats(String userName, UserStatsItem.Data item, ContentResolver resolver) {
		if (item.getDaily() == null) {
			return;
		}

		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		{ // Classic Chess
			if (item.getDaily().getChess() == null) {
				return;
			}

			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_DAILY_CHESS.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsDailyItemToValues(item.getDaily().getChess(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}

		{ // Chess960
			if (item.getDaily().getChess960() == null) {
				return;
			}

			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_DAILY_CHESS960.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsDailyItemToValues(item.getDaily().getChess960(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}
	}

	public static  void saveTacticsStats(String userName, UserStatsItem.Data item, ContentResolver resolver) {
		if (item.getTactics() == null) {
			return;
		}

		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);
		{ // Standard
			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_TACTICS.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsTacticsItemToValues(item.getTactics(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null); // TODO improve performance by updating only needed fields
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}
	}

	public static  void saveChessMentorStats(String userName, UserStatsItem.Data item, ContentResolver resolver) {
		if (item.getChessMentor() == null) {
			return;
		}

		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		{ // Standard
			Uri uri = DbConstants.uriArray[DbConstants.Tables.USER_STATS_LESSONS.ordinal()];

			Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DbDataManager.putUserStatsChessMentorItemToValues(item.getChessMentor().getRating(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
			cursor.close();
		}
	}




}
