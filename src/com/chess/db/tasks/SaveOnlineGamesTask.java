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
import com.chess.model.GameOnlineItem;

import java.util.List;


public class SaveOnlineGamesTask extends AbstractUpdateTask<GameOnlineItem, Long> {

    private ContentResolver contentResolver;
    private QueryParams params;
	private List<GameOnlineItem> onlineItems;
	private static String[] arguments = new String[2];

	public SaveOnlineGamesTask(TaskUpdateInterface<GameOnlineItem> taskFace, List<GameOnlineItem> onlineItems) {
        super(taskFace);
		this.onlineItems = onlineItems;
        params = new QueryParams();
		params.setUri(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI);

		contentResolver = taskFace.getMeContext().getContentResolver();
    }

    @Override
    protected Integer doTheTask(Long... ids) {
		String userName = AppData.getUserName(taskFace.getMeContext());
		for (GameOnlineItem onlineItem : onlineItems) {


			arguments[0] = String.valueOf(onlineItem.getGameId());
			arguments[1] = String.valueOf(userName);

			Uri uri = params.getUri();
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments, params.getOrder());
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putGameOnlineItemToValues(onlineItem, userName), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putGameOnlineItemToValues(onlineItem, userName));
			}

			cursor.close();
		}

        result = StaticData.RESULT_OK;

        return result;
    }


}
