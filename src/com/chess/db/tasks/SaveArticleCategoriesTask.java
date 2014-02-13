package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.statics.StaticData;

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
		CommonFeedCategoryItem.Data allCategory = new CommonFeedCategoryItem.Data();

		allCategory.setId(0);
		allCategory.setName(StaticData.ALL);
		allCategory.setDisplay_order(0);
		allCategory.setCurriculum(false);

		itemList.add(0, allCategory);

		for (CommonFeedCategoryItem.Data currentItem : itemList) {
			DbDataManager.saveArticleCategory(contentResolver, currentItem);
		}

		return StaticData.RESULT_OK;
	}


}
