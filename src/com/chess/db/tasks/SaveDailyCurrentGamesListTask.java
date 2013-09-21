package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.db.DbDataManager;
import com.chess.statics.StaticData;

import java.util.List;


public class SaveDailyCurrentGamesListTask extends SaveDailyGamesTask<DailyCurrentGameData> {


	public SaveDailyCurrentGamesListTask(TaskUpdateInterface<DailyCurrentGameData> taskFace, List<DailyCurrentGameData> currentItems,
										 ContentResolver resolver, String username) {
		super(taskFace, currentItems, resolver, username);
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		// if item is not found in received list that means it became finished
		synchronized (itemList) {
			for (DailyCurrentGameData currentItem : itemList) {
				DbDataManager.saveDailyGame(contentResolver, currentItem, username);
			}
		}

		return StaticData.RESULT_OK;
	}

}
