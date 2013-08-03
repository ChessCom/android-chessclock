package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.ConversationItem;
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
public class SaveConversationsInboxTask extends AbstractUpdateTask<ConversationItem.Data, Long> {

	private final String userName;

	private ContentResolver contentResolver;
	protected static String[] sArguments = new String[2];

	public SaveConversationsInboxTask(TaskUpdateInterface<ConversationItem.Data> taskFace, List<ConversationItem.Data> currentItems,
									  ContentResolver resolver) {
		super(taskFace, new ArrayList<ConversationItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		userName = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (ConversationItem.Data currentItem : itemList) {
			currentItem.setUser(userName);

			final String[] arguments = sArguments;
			arguments[0] = String.valueOf(currentItem.getId());
			arguments[1] = String.valueOf(userName);

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.uriArray[DBConstants.Tables.CONVERSATIONS_INBOX.ordinal()];
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID_AND_USER,
					DBDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

			ContentValues values = DBDataManager.putConversationItemToValues(currentItem);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
