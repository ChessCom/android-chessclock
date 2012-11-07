package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.QueryParams;
import com.chess.model.GameListCurrentItem;

import java.util.List;


public class SaveEchessCurrentGamesListTask extends AbstractUpdateTask<GameListCurrentItem, Long> {

    private ContentResolver contentResolver;
    private QueryParams params;
	private List<GameListCurrentItem> currentItems;
	private static String[] arguments = new String[2];

	public SaveEchessCurrentGamesListTask(TaskUpdateInterface<GameListCurrentItem> taskFace, List<GameListCurrentItem> currentItems) {
        super(taskFace);
		this.currentItems = currentItems;
        params = new QueryParams();
		params.setUri(DBConstants.ECHESS_FINISHED_LIST_GAMES_CONTENT_URI);

		contentResolver = taskFace.getMeContext().getContentResolver();
    }

    @Override
    protected Integer doTheTask(Long... ids) {
		String userName = AppData.getUserName(taskFace.getMeContext());
		for (GameListCurrentItem currentItem : currentItems) {

			arguments[0] = String.valueOf(currentItem.getGameId());
			arguments[1] = String.valueOf(userName);

			Uri uri = params.getUri();
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments, params.getOrder());
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName));
			}

			cursor.close();
		}

        result = StaticData.RESULT_OK;

        return result;
    }


}
