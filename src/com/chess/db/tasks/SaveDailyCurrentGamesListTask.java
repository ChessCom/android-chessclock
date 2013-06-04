package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.DailyCurrentGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.List;


public class SaveDailyCurrentGamesListTask extends SaveDailyGamesTask<DailyCurrentGameData> {


	public SaveDailyCurrentGamesListTask(TaskUpdateInterface<DailyCurrentGameData> taskFace, List<DailyCurrentGameData> currentItems,
										 ContentResolver resolver) {
		super(taskFace, currentItems, resolver);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);
		// TODO compare received list of current games with saved db data for current games.
		// if item is not found in received list that means it became finished
		synchronized (itemList) {
//			try {
//				while (saving) {
//					Thread.sleep(100);
//					itemList.wait();
//				}

//				saving = true;
				for (DailyCurrentGameData currentItem : itemList) { // if
					final String[] arguments2 = arguments;
					arguments2[0] = String.valueOf(userName);
					arguments2[1] = String.valueOf(currentItem.getGameId());

					// TODO implement beginTransaction logic for performance increase
					Uri uri = DBConstants.uriArray[DBConstants.DAILY_CURRENT_LIST_GAMES];
//					Log.d("TEST", " save DCG , game id = " + currentItem.getGameId() + " user = " + userName);
					final Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
							DBDataManager.SELECTION_GAME_ID, arguments2, null);

					ContentValues values = DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName);

					if (cursor.moveToFirst()) {
//						Log.d("TEST", " update DCG , game id = " + currentItem.getGameId() + " user = " + userName);
						contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
					} else {
//						Log.d("TEST", " insert DCG , game id = " + currentItem.getGameId() + " user = " + userName);
						contentResolver.insert(uri, values);
					}

					cursor.close();

					updateOnlineGame(currentItem.getGameId(), userName);
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
