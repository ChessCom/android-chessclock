package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.new_api.LessonCourseListItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 16:09
 */
public class SaveLessonsCoursesListTask extends AbstractUpdateTask<LessonCourseListItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[2];
	private String username;

	public SaveLessonsCoursesListTask(TaskUpdateInterface<LessonCourseListItem.Data> taskFace, List<LessonCourseListItem.Data> currentItems,
									  ContentResolver resolver, String username) {
		super(taskFace, new ArrayList<LessonCourseListItem.Data>());
		this.username = username;
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (LessonCourseListItem.Data currentItem : itemList) {
			currentItem.setUser(username);

			DbDataManager.saveCourseListItemToDb(contentResolver, currentItem);

		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
