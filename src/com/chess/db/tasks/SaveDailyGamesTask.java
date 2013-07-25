package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailyGameBaseData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.AbstractUpdateTask;

import java.util.ArrayList;
import java.util.List;

public abstract class SaveDailyGamesTask<T extends DailyGameBaseData> extends AbstractUpdateTask<T , Long> {

	private final LoadItem loadItem;
	protected String userName;
	protected String userToken;
	protected AppData appData;
	protected ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

	public SaveDailyGamesTask(TaskUpdateInterface<T> taskFace, List<T> currentItems, ContentResolver resolver) {
		super(taskFace, new ArrayList<T>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		loadItem = new LoadItem();

		if (taskFace == null || taskFace.getMeContext() == null){
			cancel(true);
			return;
		}
		appData = new AppData(getTaskFace().getMeContext());
		userToken = appData.getUserToken();
		userName = appData.getUsername();

		loadItem.setLoadPath(RestHelper.CMD_GAMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
	}

}
