package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.stats.UserStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:32
 */
public class SaveUserStatsTask extends AbstractUpdateTask<UserStatsItem.Data, Long> {

	private ContentResolver resolver;
	protected static String[] arguments = new String[1];

	public SaveUserStatsTask(TaskUpdateInterface<UserStatsItem.Data> taskFace, UserStatsItem.Data item,
							 ContentResolver resolver) {
		super(taskFace);
		this.item = item;
		this.resolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... params) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);

		saveLiveStats(userName);
		saveDailyStats(userName);
		saveTacticsStats(userName);
		saveChessMentorStats(userName);

		return StaticData.RESULT_OK;
	}

	private void saveLiveStats(String userName) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		{ // Standard
			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_LIVE_STANDARD];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsLiveItemToValues(item.getLive().getStandard(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
		}

		{ // Lightning
			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_LIVE_LIGHTNING];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsLiveItemToValues(item.getLive().getLightning(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
		}

		{ // Blitz
			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_LIVE_BLITZ];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsLiveItemToValues(item.getLive().getBlitz(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
		}
	}

	private void saveDailyStats(String userName) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		{ // Classic Chess

			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_DAILY_CHESS];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsDailyItemToValues(item.getDaily().getChess(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
		}

		{ // Chess960
			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_DAILY_CHESS960];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsDailyItemToValues(item.getDaily().getChess960(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
		}
	}

	private void saveTacticsStats(String userName) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);
		{ // Standard
			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_TACTICS];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsTacticsItemToValues(item.getTactics(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null); // TODO improve performance by updating only needed fields
			} else {
				resolver.insert(uri, values);
			}
		}
	}

	private void saveChessMentorStats(String userName) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		{ // Standard
			Uri uri = DBConstants.uriArray[DBConstants.USER_STATS_CHESS_MENTOR];

			Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

			ContentValues values = DBDataManager.putUserStatsChessMentorItemToValues(item.getChessMentor(), userName);

			if (cursor.moveToFirst()) {
				resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				resolver.insert(uri, values);
			}
		}
	}




}
