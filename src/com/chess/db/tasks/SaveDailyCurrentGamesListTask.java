package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.db.DbScheme;
import com.chess.db.DbDataManager;

import java.util.List;


public class SaveDailyCurrentGamesListTask extends SaveDailyGamesTask<DailyCurrentGameData> {


	public SaveDailyCurrentGamesListTask(TaskUpdateInterface<DailyCurrentGameData> taskFace, List<DailyCurrentGameData> currentItems,
										 ContentResolver resolver) {
		super(taskFace, currentItems, resolver);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		// TODO compare received list of current games with saved db data for current games.
		// if item is not found in received list that means it became finished
		synchronized (itemList) {
			for (DailyCurrentGameData currentItem : itemList) { // if
				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(userName);
				arguments2[1] = String.valueOf(currentItem.getGameId());

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.DAILY_CURRENT_GAMES.ordinal()];
				final Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_GAME_ID,
						DbDataManager.SELECTION_USER_AND_ID, arguments2, null);

				ContentValues values = DbDataManager.putDailyGameCurrentItemToValues(currentItem, userName);

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
