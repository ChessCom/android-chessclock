package com.chess.backend;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import com.chess.backend.entity.api.FriendsItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.db.DbDataManager;
import com.chess.statics.AppData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.06.13
 * Time: 22:44
 */
public class GetAndSaveFriends extends IntentService {

	protected static String[] arguments = new String[2];

	public GetAndSaveFriends() {
		super("GetAndSaveFriends");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AppData appData = new AppData(this);

		LoadItem loadItem = LoadHelper.getFriends(appData.getUserToken(), appData.getUsername());

		FriendsItem item = null;
		try {
			item  = RestHelper.getInstance().requestData(loadItem, FriendsItem.class, getApplicationContext());
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null) {
			String username = appData.getUsername();
			ContentResolver contentResolver = getContentResolver();

//			DbDataManager.checkAndDeleteNonExistFriends(contentResolver, item.getData(), username);
			for (FriendsItem.Data currentItem : item.getData()) {
				DbDataManager.saveFriendToDB(username, contentResolver, currentItem);
			}
		}
	}


}
