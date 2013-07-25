package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.LessonCourseItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.07.13
 * Time: 19:23
 */
public class SaveLessonsCourseTask  extends AbstractUpdateTask<LessonCourseItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments1 = new String[1];
	protected static String[] arguments2 = new String[2];

	public SaveLessonsCourseTask(TaskUpdateInterface<LessonCourseItem.Data> taskFace, LessonCourseItem.Data currentItem,
									  ContentResolver resolver) {
		super(taskFace);
		this.item = currentItem;

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		{ // save course id, name, description
			final String[] arguments = arguments1;
			arguments[0] = String.valueOf(item.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.uriArray[DBConstants.Tables.LESSONS_COURSES.ordinal()];
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID,
					DBDataManager.SELECTION_ITEM_ID, arguments, null);

			ContentValues values = DBDataManager.putLessonsCourseItemToValues(item);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();
		}

		for (LessonCourseItem.LessonListItem lesson : item.getLessons()) {
			final String[] arguments = arguments2;
			arguments[0] = String.valueOf(lesson.getId());
			arguments[1] = String.valueOf(lesson.getCourseId());

			lesson.setCourseId(item.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.uriArray[DBConstants.Tables.LESSONS_LESSONS_LIST.ordinal()];
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ITEM_ID_AND_CATEGORY_ID,
					DBDataManager.SELECTION_ITEM_ID_AND_CATEGORY_ID, arguments, null);

			ContentValues values = DBDataManager.putLessonsListItemToValues(lesson);

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
