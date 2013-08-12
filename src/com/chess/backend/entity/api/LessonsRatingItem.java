package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 17:56
 */
public class LessonsRatingItem extends BaseResponseItem<LessonsRatingItem.Data> {
/*
  "data": {
    "rating": 1484,
    "completed_courses": 0,
    "completed_lessons": 15
  }
*/

	public class Data {
		private int rating;
		private int completed_courses;
		private int completed_lessons;

		public int getRating() {
			return rating;
		}

		public int getCompletedCourses() {
			return completed_courses;
		}

		public int getCompletedLessons() {
			return completed_lessons;
		}
	}
}
