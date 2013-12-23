package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import com.chess.backend.entity.api.ConversationItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;

import java.util.ArrayList;
import java.util.List;

import static com.chess.db.DbScheme.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 21:24
 */
public class SaveConversationsInboxTask extends AbstractUpdateTask<ConversationItem.Data, Long> {

	public static final String ORIGINAL_MESSAGE_BY = "Original Message by";
	public static final String MESSAGE_SEPARATOR = "----------------------------------------------------------------------";

	private final String username;

	private ContentResolver contentResolver;
	protected static String[] sArguments = new String[2];

	public SaveConversationsInboxTask(TaskUpdateInterface<ConversationItem.Data> taskFace, List<ConversationItem.Data> currentItems,
									  ContentResolver resolver) {
		super(taskFace, new ArrayList<ConversationItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		username = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (ConversationItem.Data currentItem : itemList) {
			currentItem.setUser(username);

			final String[] arguments = sArguments;
			arguments[0] = String.valueOf(currentItem.getId());
			arguments[1] = String.valueOf(username);

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.CONVERSATIONS_INBOX.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
					DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

			ContentValues values = new ContentValues();

			values.put(V_ID, currentItem.getId());
			values.put(V_OTHER_USER_ID, currentItem.getOtherUserId());
			values.put(V_LAST_MESSAGE_ID, currentItem.getLastMessageId());
			values.put(V_LAST_MESSAGE_CREATED_AT, currentItem.getLastMessageCreatedAt());
			values.put(V_OTHER_USER_IS_ONLINE, currentItem.isOtherUserIsOnline() ? 1 : 0);
			values.put(V_NEW_MESSAGES_COUNT, currentItem.getNewMessagesCount());
			values.put(V_USER, currentItem.getUser());
			values.put(V_OTHER_USER_USERNAME, currentItem.getOtherUserUsername());
			values.put(V_OTHER_USER_AVATAR_URL, currentItem.getOtherUserAvatarUrl());
			values.put(V_LAST_MESSAGE_SENDER_USERNAME, currentItem.getLastMessageSenderUsername());
//			values.put(V_LAST_MESSAGE_CONTENT, currentItem.getLastMessageContent());

			String message = Html.fromHtml(currentItem.getLastMessageContent()).toString();
			if (message.contains(ORIGINAL_MESSAGE_BY)) {
				int quoteStart = message.indexOf(MESSAGE_SEPARATOR);
				message = message.substring(0, quoteStart);
			}
			values.put(V_LAST_MESSAGE_CONTENT, message);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		return StaticData.RESULT_OK;
	}

}
