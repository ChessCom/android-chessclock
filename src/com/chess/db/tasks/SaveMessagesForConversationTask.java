package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.MessagesItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager1;
import com.chess.db.DbScheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 21:56
 */
public class SaveMessagesForConversationTask extends AbstractUpdateTask<MessagesItem.Data, Long> {

	private final String userName;

	private ContentResolver contentResolver;
	protected static String[] sArguments = new String[3];
	private long conversationId;

	public SaveMessagesForConversationTask(TaskUpdateInterface<MessagesItem.Data> taskFace, List<MessagesItem.Data> currentItems,
									  ContentResolver resolver, long conversationId) {
		super(taskFace, new ArrayList<MessagesItem.Data>());
		this.conversationId = conversationId;
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		userName = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (MessagesItem.Data currentItem : itemList) {
			currentItem.setUser(userName);
			currentItem.setConversationId(conversationId);

			final String[] arguments = sArguments;
			arguments[0] = String.valueOf(currentItem.getId());
			arguments[1] = String.valueOf(userName);
			arguments[2] = String.valueOf(conversationId);

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.CONVERSATIONS_MESSAGES.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager1.PROJECTION_ID_USER_CONVERSATION_ID,
					DbDataManager1.SELECTION_ID_USER_CONVERSATION_ID, arguments, null);

			ContentValues values = DbDataManager1.putMessagesItemToValues(currentItem);

			DbDataManager1.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
