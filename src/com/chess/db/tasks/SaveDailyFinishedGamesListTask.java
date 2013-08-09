package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.DailyFinishedGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;

import java.util.List;


public class SaveDailyFinishedGamesListTask extends SaveDailyGamesTask<DailyFinishedGameData> {

	public SaveDailyFinishedGamesListTask(TaskUpdateInterface<DailyFinishedGameData> taskFace,
										  List<DailyFinishedGameData> finishedItems, ContentResolver resolver) {
		super(taskFace, finishedItems, resolver);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (DailyFinishedGameData finishedItem : itemList) {

				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(userName);
				arguments2[1] = String.valueOf(finishedItem.getGameId()); // Test

				Uri uri = DbConstants.uriArray[DbConstants.Tables.DAILY_FINISHED_GAMES.ordinal()];
				final Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_GAME_ID,
						DbDataManager.SELECTION_USER_AND_ID, arguments2, null);

				ContentValues values = DbDataManager.putDailyFinishedGameToValues(finishedItem, userName);

				if (cursor.moveToFirst()) {
					contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
				} else {
					contentResolver.insert(uri, values);
				}

				cursor.close();
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
