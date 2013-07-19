package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 22:33
 */
public class LessonCourseItem extends BaseResponseItem<LessonCourseItem.Data> {
/*
  "data": {
    "course_name": "DeepGreene's Learning Bag",
    "description": "I reach into the bag and pull out things to learn you chess...",
    "lessons_total_count": "5",
    "lessons": [
      {
        "id": "5658",
        "name": "Black Moves Last",
        "completed": false
      }
    ]
  }
*/

	public class Data {
		private String course_name;
		private String description;
		private int lessons_total_count;
		private List<LessonListItem> lessons;

		public String getCourseName() {
			return course_name;
		}

		public String getDescription() {
			return description;
		}

		public int getLessonsTotalCount() {
			return lessons_total_count;
		}

		public List<LessonListItem> getLessons() {
			return lessons;
		}
	}

	public class LessonListItem {
		private int id;
		private String name;
		private boolean completed;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isCompleted() {
			return completed;
		}
	}
}
