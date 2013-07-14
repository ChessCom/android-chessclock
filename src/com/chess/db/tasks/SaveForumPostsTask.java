package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.new_api.ForumPostItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 17:42
 */
public class SaveForumPostsTask extends AbstractUpdateTask<ForumPostItem.Post, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];
	private long topicId;

	public SaveForumPostsTask(TaskUpdateInterface<ForumPostItem.Post> taskFace, List<ForumPostItem.Post> currentItems,
							  ContentResolver resolver, long topicId) {
		super(taskFace, new ArrayList<ForumPostItem.Post>());
		this.topicId = topicId;
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (ForumPostItem.Post currentItem : itemList) {
				currentItem.setTopicId(topicId);
				DBDataManager.updateForumPostItem(contentResolver, currentItem);
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
