package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.statics.StaticData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 15:40
 */
public class SaveLessonsCategoriesTask extends AbstractUpdateTask<CommonFeedCategoryItem.Data, Long> {

	private final List<String> curriculumCategoriesList;
	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveLessonsCategoriesTask(TaskUpdateInterface<CommonFeedCategoryItem.Data> taskFace, List<CommonFeedCategoryItem.Data> currentItems,
									 ContentResolver resolver) {
		super(taskFace, new ArrayList<CommonFeedCategoryItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;

		curriculumCategoriesList = new ArrayList<String>();
		curriculumCategoriesList.add("Beginner");
		curriculumCategoriesList.add("Intermediate");
		curriculumCategoriesList.add("Advanced");
		curriculumCategoriesList.add("Expert");
		curriculumCategoriesList.add("Master");

/*
id: 9,name: "Beginner"
id: 10,name: "Intermediate"
id: 11,name: "Advanced"
id: 12,name: "Expert"
id: 13,name: "Master"
id: 6,name: "Rules and Basics"
id: 4,name: "Strategy"
id: 5,name: "Tactics"
id: 3,name: "Attacks"
id: 7,name: "Openings"
id: 2,name: "Endgames"
id: 8,name: "Games"
id: 1,name: "Misc"
*/
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		int i = 0;
		for (CommonFeedCategoryItem.Data currentItem : itemList) {
			currentItem.setDisplay_order(i++);

			if (curriculumCategoriesList.contains(currentItem.getName())) {
				currentItem.setCurriculum(true);
			}

			DbDataManager.saveLessonCategoryToDb(contentResolver, currentItem);
		}

		return StaticData.RESULT_OK;
	}


}
