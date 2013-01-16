package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.chess.backend.entity.new_api.DailyCurrentGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;


import java.util.List;


//public class SaveDailyCurrentGamesListTask extends AbstractUpdateTask<GameListCurrentItem, Long> {
//public class SaveDailyCurrentGamesListTask extends SaveDailyGamesTask<GameListCurrentItem> {
public class SaveDailyCurrentGamesListTask extends SaveDailyGamesTask<DailyCurrentGameData> {

	public SaveDailyCurrentGamesListTask(TaskUpdateInterface<DailyCurrentGameData> taskFace, List<DailyCurrentGameData> currentItems,
										 ContentResolver resolver) {
        super(taskFace, currentItems, resolver);
	}


	@Override
    protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);
		String userToken = AppData.getUserToken(context);
		// TODO compare received list of current games with saved db data for current games.
		// if item is not found in received list that means it became finished
		for (DailyCurrentGameData currentItem : itemList) { // if
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(userName);
			arguments2[1] = String.valueOf(currentItem.getGameId());

//			Log.d("TEST", "SEARCH game with id = " + currentItem.getGameId() + " user = " + userName);
			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.ECHESS_CURRENT_LIST_GAMES_CONTENT_URI;
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments2, null);
//			Log.d("TEST", "cursor count = " + cursor.getCount());
			if (cursor.moveToFirst()) {
//				Log.d("TEST", "UPDATE game with id = " + currentItem.getGameId() + " user = " + userName);
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName), null, null);
			} else {
//				Log.d("TEST", "INSERT game with id = " + currentItem.getGameId() + " user = " + userName);
				contentResolver.insert(uri, DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName));
			}

			cursor.close();

			updateOnlineGame(currentItem.getGameId(), userName, userToken);
		}

        result = StaticData.RESULT_OK;

        return result;
    }

}
