package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.daily_games.DailyGameBaseData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.AppData;
import com.chess.backend.tasks.AbstractUpdateTask;

import java.util.ArrayList;
import java.util.List;

public abstract class SaveDailyGamesTask<T extends DailyGameBaseData> extends AbstractUpdateTask<T , Long> {

	protected String username;
	protected String userToken;
	protected AppData appData;
	protected ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

	public SaveDailyGamesTask(TaskUpdateInterface<T> taskFace, List<T> currentItems, ContentResolver resolver, String username) {
		super(taskFace, new ArrayList<T>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;

		if (taskFace == null || taskFace.getMeContext() == null){
			cancel(true);
			return;
		}
		appData = new AppData(getTaskFace().getMeContext());
		userToken = appData.getUserToken();
		this.username = username;
	}

}
