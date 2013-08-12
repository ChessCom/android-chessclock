package com.chess.backend;

import android.app.IntentService;
import android.content.Intent;
import com.chess.backend.entity.api.stats.UserStatsItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.statics.AppData;
import com.chess.db.tasks.SaveUserStatsTask;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.02.13
 * Time: 8:49
 */
public class GetAndSaveUserStats extends IntentService {

	public GetAndSaveUserStats() {
		super("GetAndSaveUserStats");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AppData appData = new AppData(this);
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USER_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());

		UserStatsItem item = null;
		try {
			item  = RestHelper.requestData(loadItem, UserStatsItem.class, AppUtils.getAppId(getApplicationContext()));
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null) {
			String userName = appData.getUsername();

			SaveUserStatsTask.saveLiveStats(userName, item.getData(), getContentResolver());
			SaveUserStatsTask.saveDailyStats(userName, item.getData(), getContentResolver());
			SaveUserStatsTask.saveTacticsStats(userName, item.getData(), getContentResolver());
			SaveUserStatsTask.saveChessMentorStats(userName, item.getData(), getContentResolver());
		}
	}
}
