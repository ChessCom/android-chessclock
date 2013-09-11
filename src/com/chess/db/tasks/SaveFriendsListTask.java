package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.FriendsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import java.util.ArrayList;
import java.util.List;


public class SaveFriendsListTask extends AbstractUpdateTask<FriendsItem.Data, Long> {

	private final String username;

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

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
		for (FriendsItem.Data currentItem : itemList) {
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(username);
			arguments2[1] = String.valueOf(currentItem.getUserId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.FRIENDS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER_ID, DbDataManager.SELECTION_USER_ID, arguments2, null);

			ContentValues values = DbDataManager.putFriendItemToValues(currentItem, username);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);

		}

		return StaticData.RESULT_OK;
	}

}
