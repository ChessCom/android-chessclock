package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.CommonFeedCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 15:40
 */
public class SaveLessonsCategoriesTask extends AbstractUpdateTask<CommonFeedCategoryItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveLessonsCategoriesTask(TaskUpdateInterface<CommonFeedCategoryItem.Data> taskFace, List<CommonFeedCategoryItem.Data> currentItems,
									 ContentResolver resolver) {
		super(taskFace, new ArrayList<CommonFeedCategoryItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		int i = 0;
		for (CommonFeedCategoryItem.Data currentItem : itemList) {
			currentItem.setDisplay_order(i++);

			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(currentItem.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_CATEGORIES.ordinal()];

			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_V_CATEGORY_ID,
					DbDataManager.SELECTION_CATEGORY_ID, arguments2, null);

			ContentValues values = DbDataManager.putCommonFeedCategoryItemToValues(currentItem);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();

		}


		return StaticData.RESULT_OK;
	}


}
