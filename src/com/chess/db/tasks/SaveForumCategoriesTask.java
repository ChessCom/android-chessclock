package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.ForumCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.07.13
 * Time: 21:44
 */
public class SaveForumCategoriesTask extends AbstractUpdateTask<ForumCategoryItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveForumCategoriesTask(TaskUpdateInterface<ForumCategoryItem.Data> taskFace,
								   List<ForumCategoryItem.Data> currentItems,
							   ContentResolver resolver) {
		super(taskFace, new ArrayList<ForumCategoryItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (ForumCategoryItem.Data currentItem : itemList) {
				DbDataManager.updateForumCategoryItem(contentResolver, currentItem);
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
