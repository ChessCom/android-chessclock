package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.FriendsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 21:24
 */
public class SaveMessagesInboxTask extends AbstractUpdateTask<FriendsItem.Data, Long> {

	private final String userName;

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

	public SaveMessagesInboxTask(TaskUpdateInterface<FriendsItem.Data> taskFace, List<FriendsItem.Data> currentItems,
							   ContentResolver resolver) {
		super(taskFace, new ArrayList<FriendsItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		userName = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		synchronized (itemList) {
			for (FriendsItem.Data currentItem : itemList) { // if
				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(userName);
				arguments2[1] = String.valueOf(currentItem.getUserId());

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DBConstants.uriArray[DBConstants.Tables.FRIENDS.ordinal()];
				Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_USER_ID, DBDataManager.SELECTION_USER_ID, arguments2, null);

				ContentValues values = DBDataManager.putFriendItemToValues(currentItem, userName);

				if (cursor.moveToFirst()) {
					contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
				} else {
					contentResolver.insert(uri, values);
				}

				cursor.close();
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
