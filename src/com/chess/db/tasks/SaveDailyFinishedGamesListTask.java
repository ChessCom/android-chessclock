package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import java.util.List;


public class SaveDailyFinishedGamesListTask extends SaveDailyGamesTask<DailyFinishedGameData> {

	public SaveDailyFinishedGamesListTask(TaskUpdateInterface<DailyFinishedGameData> taskFace,
							List<DailyFinishedGameData> finishedItems, ContentResolver resolver, String username) {
		super(taskFace, finishedItems, resolver, username);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (DailyFinishedGameData finishedItem : itemList) {

				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(username);
				arguments2[1] = String.valueOf(finishedItem.getGameId());

				Uri uri = DbScheme.uriArray[DbScheme.Tables.DAILY_FINISHED_GAMES.ordinal()];
				final Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_GAME_ID,
						DbDataManager.SELECTION_USER_AND_ID, arguments2, null);

				ContentValues values = DbDataManager.putDailyFinishedGameToValues(finishedItem, username);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}
		return StaticData.RESULT_OK;
	}

}
