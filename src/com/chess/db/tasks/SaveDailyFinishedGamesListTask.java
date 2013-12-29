package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.db.DbDataManager;
import com.chess.statics.StaticData;

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
				DbDataManager.saveDailyFinishedGameItemToDb(contentResolver, finishedItem, username);
			}
		}
		return StaticData.RESULT_OK;
	}

}
