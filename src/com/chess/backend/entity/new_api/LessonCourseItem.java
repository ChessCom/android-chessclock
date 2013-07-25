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

	public static class Data {
		private String course_name;
		private String description;
		private int lessons_total_count;
		private List<LessonListItem> lessons;
		/* Local additions */
		private long id;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

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

		public void setCourseName(String course_name) {
			this.course_name = course_name;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setLessonsTotalCount(int lessons_total_count) {
			this.lessons_total_count = lessons_total_count;
		}

		public void setLessons(List<LessonListItem> lessons) {
			this.lessons = lessons;
		}
	}

	public static class LessonListItem {
		private int id;
		private String name;
		private boolean completed;
		/* Local addition */
		private long courseId;

		public void setId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isCompleted() {
			return completed;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setCompleted(boolean completed) {
			this.completed = completed;
		}

		public long getCourseId() {
			return courseId;
		}

		public void setCourseId(long courseId) {
			this.courseId = courseId;
		}
	}
}
