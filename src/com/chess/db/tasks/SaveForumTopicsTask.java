package com.chess.db.tasks;

import android.content.ContentResolver;
import android.util.SparseArray;
import com.chess.backend.entity.new_api.ForumTopicItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.07.13
 * Time: 17:13
 */
public class SaveForumTopicsTask extends AbstractUpdateTask<ForumTopicItem.Topic, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];
	private SparseArray<String> categoriesMap;

	public SaveForumTopicsTask(TaskUpdateInterface<ForumTopicItem.Topic> taskFace, List<ForumTopicItem.Topic> currentItems,
							   ContentResolver resolver, SparseArray<String> categoriesMap) {
		super(taskFace, new ArrayList<ForumTopicItem.Topic>());
		this.categoriesMap = categoriesMap;
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (ForumTopicItem.Topic currentItem : itemList) {
				currentItem.setCategoryName(categoriesMap.get(currentItem.getCategoryId()));
				DBDataManager.updateForumTopicItem(contentResolver, currentItem);
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}

