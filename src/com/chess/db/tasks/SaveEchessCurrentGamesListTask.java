package com.chess.db.tasks;

import android.content.ContentResolver;
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


//public class SaveEchessCurrentGamesListTask extends AbstractUpdateTask<GameListCurrentItem, Long> {
//public class SaveEchessCurrentGamesListTask extends SaveEchessGamesTask<GameListCurrentItem> {
public class SaveEchessCurrentGamesListTask extends SaveEchessGamesTask<DailyCurrentGameData> {

	public SaveEchessCurrentGamesListTask(TaskUpdateInterface<DailyCurrentGameData> taskFace, List<DailyCurrentGameData> currentItems,
										  ContentResolver resolver) {
        super(taskFace, currentItems, resolver);
	}


	@Override
    protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);
//		String userToken = AppData.getUserToken(context);
		// TODO compare received list of current games with saved db data for current games.
		// if item is not found in received list that means it became finished
		for (DailyCurrentGameData currentItem : itemList) { // if

			arguments[0] = String.valueOf(userName);
			arguments[1] = String.valueOf(currentItem.getGameId());

			// TODO implement beginTransaction logic
			Uri uri = DBConstants.ECHESS_CURRENT_LIST_GAMES_CONTENT_URI;
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments, null);
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName));
			}

			cursor.close();

//			updateOnlineGame(currentItem.getGameId(), userName, userToken);
		}

        result = StaticData.RESULT_OK;

        return result;
    }

}
