package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.ConversationItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

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
			Uri uri = DbScheme.uriArray[DbScheme.Tables.CONVERSATIONS_INBOX.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
					DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

			ContentValues values = DbDataManager.putConversationItemToValues(currentItem);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);

		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
