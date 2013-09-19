package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;


public class SaveArticleCategoriesTask extends AbstractUpdateTask<CommonFeedCategoryItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveArticleCategoriesTask(TaskUpdateInterface<CommonFeedCategoryItem.Data> taskFace, List<CommonFeedCategoryItem.Data> currentItems,
									 ContentResolver resolver) {
		super(taskFace, new ArrayList<CommonFeedCategoryItem.Data>());
		this.itemList.addAll(currentItems);
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (CommonFeedCategoryItem.Data currentItem : itemList) {
			DbDataManager.saveArticleCategory(contentResolver, currentItem);
		}

		return StaticData.RESULT_OK;
	}


}
