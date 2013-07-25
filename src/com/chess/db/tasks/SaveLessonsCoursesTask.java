package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.LessonCourseListItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 16:09
 */
public class SaveLessonsCoursesTask  extends AbstractUpdateTask<LessonCourseListItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[2];
	private String username;

	public SaveLessonsCoursesTask(TaskUpdateInterface<LessonCourseListItem.Data> taskFace, List<LessonCourseListItem.Data> currentItems,
									 ContentResolver resolver, String username) {
		super(taskFace, new ArrayList<LessonCourseListItem.Data>());
		this.username = username;
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (LessonCourseListItem.Data currentItem : itemList) {
				currentItem.setUser(username);
				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(currentItem.getId());
				arguments2[1] = username;

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DBConstants.uriArray[DBConstants.Tables.LESSONS_COURSES.ordinal()];
				Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID_AND_USER,
						DBDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);


				ContentValues values = DBDataManager.putLessonsCourseItemToValues(currentItem);

				if (cursor.moveToFirst()) {
					contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
				} else {
					contentResolver.insert(uri, values);
				}

				cursor.close();

			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
