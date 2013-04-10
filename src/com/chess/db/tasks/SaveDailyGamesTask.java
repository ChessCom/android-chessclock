package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailyGameBaseData;
import com.chess.backend.entity.new_api.DailyGameByIdItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;

import java.util.List;

public abstract class SaveDailyGamesTask<T extends DailyGameBaseData> extends AbstractUpdateTask<T , Long> {

	private static final String TAG = "SaveDailyGamesTask";
	private final LoadItem loadItem;
	protected ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

	public SaveDailyGamesTask(TaskUpdateInterface<T> taskFace, List<T> currentItems, ContentResolver resolver) {
		super(taskFace);
		itemList = currentItems;
		this.contentResolver = resolver;
		loadItem = new LoadItem();

		if (taskFace == null || taskFace.getMeContext() == null){
			cancel(true);
			return;
		}

		String userToken = AppData.getUserToken(taskFace.getMeContext());

		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
	}

	protected void updateOnlineGame(long gameId, String userName) {
		loadItem.setLoadPath(RestHelper.CMD_GAME_BY_ID(gameId));

		DailyGameByIdItem.Data currentGame = null;
		try {
			currentGame = RestHelper.requestData(loadItem, DailyGameByIdItem.class).getData();
		} catch (InternalErrorException e) {
			e.logMe();
		}
		if (currentGame != null) {
			result = StaticData.RESULT_OK;
			DBDataManager.updateOnlineGame(contentResolver, currentGame, userName);
		}
	}

}
