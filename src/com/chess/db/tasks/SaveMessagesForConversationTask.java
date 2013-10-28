package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.MessagesItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import java.util.ArrayList;
import java.util.List;

import static com.chess.db.DbScheme.*;
import static com.chess.db.DbScheme.V_LAST_MESSAGE_CONTENT;
import static com.chess.db.DbScheme.V_OTHER_USER_AVATAR_URL;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 21:56
 */
public class SaveMessagesForConversationTask extends AbstractUpdateTask<MessagesItem.Data, Long> {

	private final String username;

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
		username = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (MessagesItem.Data currentItem : itemList) {
			currentItem.setUser(username);
			currentItem.setConversationId(conversationId);

			final String[] arguments = sArguments;
			arguments[0] = String.valueOf(currentItem.getId());
			arguments[1] = String.valueOf(username);
			arguments[2] = String.valueOf(conversationId);

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.CONVERSATIONS_MESSAGES.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ID_USER_CONVERSATION_ID,
					DbDataManager.SELECTION_ID_USER_CONVERSATION_ID, arguments, null);

			ContentValues values = new ContentValues();

			values.put(V_ID, currentItem.getId());
			values.put(V_CONVERSATION_ID, currentItem.getConversationId());
			values.put(V_OTHER_USER_ID, currentItem.getSenderId());
			values.put(V_CREATE_DATE, currentItem.getCreatedAt());
			values.put(V_OTHER_USER_IS_ONLINE, currentItem.isSenderIsOnline() ? 1 : 0);
			values.put(V_USER, currentItem.getUser());
			values.put(V_OTHER_USER_USERNAME, currentItem.getSenderUsername());
			values.put(V_OTHER_USER_AVATAR_URL, currentItem.getSenderAvatarUrl());
			values.put(V_LAST_MESSAGE_CONTENT, currentItem.getContent());

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		return StaticData.RESULT_OK;
	}

}
