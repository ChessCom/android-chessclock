package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.CommonConsumeCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.List;


public class SaveVideoCategoriesTask extends AbstractUpdateTask<CommonConsumeCategoryItem.Data, Long> {
	private static final String TAG = "SaveFriendsListTask";

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveVideoCategoriesTask(TaskUpdateInterface<CommonConsumeCategoryItem.Data> taskFace, List<CommonConsumeCategoryItem.Data> currentItems,
								   ContentResolver resolver) {
		super(taskFace);
		this.itemList = currentItems;
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		for (CommonConsumeCategoryItem.Data currentItem : itemList) {
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(currentItem.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.VIDEO_CATEGORIES_CONTENT_URI;

			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_V_CATEGORY_ID,
					DBDataManager.SELECTION_CATEGORY_ID, arguments2, null);
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putCommonConsumeCategoryItemToValues(currentItem), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putCommonConsumeCategoryItemToValues(currentItem));
			}

			cursor.close();

		}

		result = StaticData.RESULT_OK;

		return result;
	}

}
