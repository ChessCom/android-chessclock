package com.chess.backend.entity.api;

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

	public static class Data extends LessonSearchItem.Data{
		private String course_name;
		private String description;

		public String getCourseName() {
			return course_name;
		}

		public String getDescription() {
			return description;
		}

		public void setCourseName(String course_name) {
			this.course_name = course_name;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}
