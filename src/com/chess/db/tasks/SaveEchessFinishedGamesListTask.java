package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.DailyFinishedGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.model.GameListFinishedItem;
import com.chess.model.GameOnlineItem;

import java.util.List;


//public class SaveEchessFinishedGamesListTask extends AbstractUpdateTask<GameListFinishedItem, Long> {
//public class SaveEchessFinishedGamesListTask extends SaveEchessGamesTask<GameListFinishedItem> {
public class SaveEchessFinishedGamesListTask extends SaveEchessGamesTask<DailyFinishedGameData> {

	public SaveEchessFinishedGamesListTask(TaskUpdateInterface<DailyFinishedGameData> taskFace,
										   List<DailyFinishedGameData> finishedItems, ContentResolver resolver) {
        super(taskFace, finishedItems, resolver);
    }

	@Override
    protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);
//		String userToken = AppData.getUserToken(context);

		for (DailyFinishedGameData finishedItem : itemList) {

			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(userName);
			arguments2[1] = String.valueOf(finishedItem.getGameId()); // Test

			Uri uri = DBConstants.ECHESS_FINISHED_LIST_GAMES_CONTENT_URI;
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments2, null);
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putEchessFinishedListGameToValues(finishedItem, userName), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putEchessFinishedListGameToValues(finishedItem, userName));
			}

			cursor.close();

//			updateOnlineGame(finishedItem.getGameId(), userName, userToken);
		}

        result = StaticData.RESULT_OK;

        return result;
    }

}
