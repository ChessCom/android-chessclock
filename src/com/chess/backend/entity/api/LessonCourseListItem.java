package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 16:05
 */
public class LessonCourseListItem extends BaseResponseItem<List<LessonCourseListItem.Data>> {
/*
  "data": [
    {
      "id": 294,
      "name": "DeepGreene's Learning Bag",
      "category_id": 1,
      "course_completed": false
    },
*/

	public static class Data {
		private int id;
		private String name;
		private int category_id;
		private boolean course_completed;
		private String user;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getCategoryId() {
			return category_id;
		}

		public boolean isCourseCompleted() {
			return course_completed;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setCategoryId(int category_id) {
			this.category_id = category_id;
		}

		public void setCourseCompleted(boolean course_completed) {
			this.course_completed = course_completed;
		}
	}
}
