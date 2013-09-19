package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;


public class SaveArticlesListTask extends AbstractUpdateTask<ArticleItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveArticlesListTask(TaskUpdateInterface<ArticleItem.Data> taskFace, List<ArticleItem.Data> currentItems,
								ContentResolver resolver) {
        super(taskFace, new ArrayList<ArticleItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
    protected Integer doTheTask(Long... ids) {
		for (ArticleItem.Data currentItem : itemList) {
			DbDataManager.saveArticleItem(contentResolver, currentItem);
		}

        return StaticData.RESULT_OK;
    }


}
