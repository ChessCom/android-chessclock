package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

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
		synchronized (itemList) {
			for (CommonFeedCategoryItem.Data currentItem : itemList) {
				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(currentItem.getId());

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.ARTICLE_CATEGORIES.ordinal()];

				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_V_CATEGORY_ID,
						DbDataManager.SELECTION_CATEGORY_ID, arguments2, null);

				ContentValues values = DbDataManager.putCommonFeedCategoryItemToValues(currentItem);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);

			}

		}
		result = StaticData.RESULT_OK;

		return result;
	}


}
