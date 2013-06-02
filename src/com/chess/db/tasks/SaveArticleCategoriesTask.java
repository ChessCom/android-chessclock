package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.CommonFeedCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.List;


public class SaveArticleCategoriesTask extends AbstractUpdateTask<CommonFeedCategoryItem.Data, Long> {
	private static final String TAG = "SaveFriendsListTask";

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];
	private LoadItem loadItem;

	public SaveArticleCategoriesTask(TaskUpdateInterface<CommonFeedCategoryItem.Data> taskFace, List<CommonFeedCategoryItem.Data> currentItems,
									 ContentResolver resolver) {
		super(taskFace);
		this.itemList = currentItems;
		this.contentResolver = resolver;
		loadItem = new LoadItem();
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		for (CommonFeedCategoryItem.Data currentItem : itemList) {
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(currentItem.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.uriArray[DBConstants.ARTICLE_CATEGORIES];

			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_V_CATEGORY_ID,
					DBDataManager.SELECTION_CATEGORY_ID, arguments2, null);

			ContentValues values = DBDataManager.putCommonFeedCategoryItemToValues(currentItem);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();
		}

		result = StaticData.RESULT_OK;

		return result;
	}


}
