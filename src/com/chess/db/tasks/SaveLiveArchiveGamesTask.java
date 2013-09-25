package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.LiveArchiveGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.09.13
 * Time: 6:45
 */
public class SaveLiveArchiveGamesTask extends SaveDailyGamesTask<LiveArchiveGameData> {

	public SaveLiveArchiveGamesTask(TaskUpdateInterface<LiveArchiveGameData> taskFace,
										  List<LiveArchiveGameData> finishedItems, ContentResolver resolver, String username) {
		super(taskFace, finishedItems, resolver, username);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (LiveArchiveGameData finishedItem : itemList) {

				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(username);
				arguments2[1] = String.valueOf(finishedItem.getGameId());

				Uri uri = DbScheme.uriArray[DbScheme.Tables.LIVE_ARCHIVE_GAMES.ordinal()];
				final Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_GAME_ID,
						DbDataManager.SELECTION_USER_AND_ID, arguments2, null);

				ContentValues values = DbDataManager.putLiveArchiveGameToValues(finishedItem, username);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}
		return StaticData.RESULT_OK;
	}

}