package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.DailyFinishedGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.List;


public class SaveDailyFinishedGamesListTask extends SaveDailyGamesTask<DailyFinishedGameData> {

	public SaveDailyFinishedGamesListTask(TaskUpdateInterface<DailyFinishedGameData> taskFace,
										  List<DailyFinishedGameData> finishedItems, ContentResolver resolver) {
		super(taskFace, finishedItems, resolver);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);

		synchronized (itemList) {
//			try {
//				while (saving) {
//					Thread.sleep(100);
//					itemList.wait();
//				}
//
//				saving = true;
				for (DailyFinishedGameData finishedItem : itemList) {

					final String[] arguments2 = arguments;
					arguments2[0] = String.valueOf(userName);
					arguments2[1] = String.valueOf(finishedItem.getGameId()); // Test

					Uri uri = DBConstants.uriArray[DBConstants.DAILY_FINISHED_LIST_GAMES];
//					Log.d("TEST", " save FINISHED , game id = " + finishedItem.getGameId() + " user = " + userName);
					final Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
							DBDataManager.SELECTION_GAME_ID, arguments2, null);

					ContentValues values = DBDataManager.putEchessFinishedListGameToValues(finishedItem, userName);

					if (cursor.moveToFirst()) {
//						Log.d("TEST", " update FINISHED , game id = " + finishedItem.getGameId() + " user = " + userName);
						contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
					} else {
//						Log.d("TEST", " insert FINISHED , game id = " + finishedItem.getGameId() + " user = " + userName);
						contentResolver.insert(uri, values);
					}

					cursor.close();

					updateOnlineGame(finishedItem.getGameId(), userName);
				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			saving = false;
//			itemList.notifyAll();
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
