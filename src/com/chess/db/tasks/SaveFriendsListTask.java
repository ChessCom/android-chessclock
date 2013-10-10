package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.FriendsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;

import java.util.ArrayList;
import java.util.List;


public class SaveFriendsListTask extends AbstractUpdateTask<FriendsItem.Data, Long> {

	private final String username;

	private ContentResolver contentResolver;

	public SaveFriendsListTask(TaskUpdateInterface<FriendsItem.Data> taskFace, List<FriendsItem.Data> currentItems,
							   ContentResolver resolver) {
		super(taskFace, new ArrayList<FriendsItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		username = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		DbDataManager.checkAndDeleteNonExistFriends(contentResolver, itemList, username);

		for (FriendsItem.Data currentItem : itemList) {
			DbDataManager.saveFriendToDB(username, contentResolver, currentItem);
		}

		return StaticData.RESULT_OK;
	}

}
