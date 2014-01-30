package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.ForumPostItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;

import java.util.ArrayList;
import java.util.List;

import static com.chess.db.DbScheme.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 17:42
 */
public class SaveForumPostsTask extends AbstractUpdateTask<ForumPostItem.Post, Long> {

	private ContentResolver contentResolver;
	protected static String[] sArguments = new String[2];
	private final long topicId;
	private final int currentPage;

	public SaveForumPostsTask(TaskUpdateInterface<ForumPostItem.Post> taskFace, List<ForumPostItem.Post> currentItems,
							  ContentResolver resolver, long topicId, int currentPage) {
		super(taskFace, new ArrayList<ForumPostItem.Post>());
		this.topicId = topicId;
		this.currentPage = currentPage;
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (ForumPostItem.Post currentItem : itemList) {
			currentItem.setTopicId(topicId);
			currentItem.setPage(currentPage);

			final String[] arguments = sArguments;
			arguments[0] = String.valueOf(currentItem.getCreateDate());
			arguments[1] = String.valueOf(currentItem.getUsername());

			Uri uri = uriArray[DbScheme.Tables.FORUM_POSTS.ordinal()];

			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_CREATE_DATE_AND_USER,
					DbDataManager.SELECTION_CREATE_DATE_AND_USER, arguments, null);

			ContentValues values = new ContentValues();

			values.put(V_DESCRIPTION, currentItem.getBody());
			values.put(V_ID, currentItem.getTopicId());
			values.put(V_CREATE_DATE, currentItem.getCreateDate());
			values.put(V_USERNAME, currentItem.getUsername());
			values.put(V_COMMENT_ID, currentItem.getCommentId());
			values.put(V_COUNTRY_ID, currentItem.getCountryId());
			values.put(V_PREMIUM_STATUS, currentItem.isPremiumStatus());
			values.put(V_PHOTO_URL, currentItem.getAvatarUrl());
			values.put(V_NUMBER, currentItem.getCommentNumber());
			values.put(V_PAGE, currentItem.getPage());

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		return StaticData.RESULT_OK;
	}

}
